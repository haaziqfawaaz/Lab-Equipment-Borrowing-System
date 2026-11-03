
package api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import model.Equipment;
import model.User;
import service.EquipmentService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EquipmentController implements HttpHandler {

    private final Gson gson = new Gson();
    private final EquipmentService equipmentService = new EquipmentService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equalsIgnoreCase(method)
                    && path.endsWith("/available")) {

                getAvailableEquipment(exchange);

            } else if ("GET".equalsIgnoreCase(method)) {

                getAllEquipment(exchange);

            } else if ("POST".equalsIgnoreCase(method)) {

                addEquipment(exchange);

            } else if ("PUT".equalsIgnoreCase(method)) {

                updateEquipment(exchange);

            } else {

                sendJson(exchange, 405,
                        Map.of("status", "error",
                               "message", "Method not allowed"));
            }

        } catch (SecurityException e) {

            sendJson(exchange, 403,
                    Map.of("status", "error", "message", e.getMessage()));

        } catch (IllegalArgumentException e) {

            sendJson(exchange, 400,
                    Map.of("status", "error", "message", e.getMessage()));

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(exchange, 500,
                    Map.of("status", "error",
                           "message", "Equipment operation failed"));
        }
    }

    private void getAllEquipment(HttpExchange exchange)
            throws Exception {

        List<Equipment> equipment =
                equipmentService.getAllEquipment();

        sendJson(exchange, 200,
                Map.of("status", "success", "equipment", equipment));
    }

    private void getAvailableEquipment(HttpExchange exchange)
            throws Exception {

        List<Equipment> equipment =
                equipmentService.getAvailableEquipment();

        sendJson(exchange, 200,
                Map.of("status", "success", "equipment", equipment));
    }

    private void addEquipment(HttpExchange exchange)
            throws Exception {

        User admin = getAuthenticatedAdmin(exchange);

        JsonObject data = readRequest(exchange);

        String name = data.get("name").getAsString();
        String category = data.get("category").getAsString();
        int totalQuantity = data.get("totalQuantity").getAsInt();

        String conditionStatus = data.has("conditionStatus")
                && !data.get("conditionStatus").isJsonNull()
                ? data.get("conditionStatus").getAsString()
                : "GOOD";

        Equipment equipment = equipmentService.addEquipment(
                admin.getUserId(),
                name,
                category,
                totalQuantity,
                conditionStatus
        );

        sendJson(exchange, 201,
                Map.of("status", "success",
                       "message", "Equipment added successfully",
                       "equipment", equipment));
    }

    private void updateEquipment(HttpExchange exchange)
            throws Exception {

        User admin = getAuthenticatedAdmin(exchange);

        JsonObject data = readRequest(exchange);

        Equipment equipment = gson.fromJson(data, Equipment.class);

        boolean updated = equipmentService.updateEquipment(
                admin.getUserId(), equipment
        );

        if (updated) {
            sendJson(exchange, 200,
                    Map.of("status", "success",
                           "message", "Equipment updated successfully"));
        } else {
            sendJson(exchange, 404,
                    Map.of("status", "error",
                           "message", "Equipment not found"));
        }
    }

    private User getAuthenticatedAdmin(HttpExchange exchange) {

        String header = exchange.getRequestHeaders()
                .getFirst("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException("Login required");
        }

        String token = header.substring(7).trim();

        User user = SessionManager.getUser(token);

        if (user == null || !SessionManager.isAdmin(token)) {
            throw new SecurityException("Admin access required");
        }

        return user;
    }

    private JsonObject readRequest(HttpExchange exchange)
            throws IOException {

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        return gson.fromJson(body, JsonObject.class);
    }

    private void addCorsHeaders(HttpExchange exchange) {

        String origin = exchange.getRequestHeaders()
                .getFirst("Origin");

        if ("http://localhost:5500".equals(origin)
                || "http://127.0.0.1:5500".equals(origin)) {

            exchange.getResponseHeaders()
                    .set("Access-Control-Allow-Origin", origin);
        }

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
        );
    }

    private void sendJson(
            HttpExchange exchange,
            int statusCode,
            Object response
    ) throws IOException {

        byte[] bytes = gson.toJson(response)
                .getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}

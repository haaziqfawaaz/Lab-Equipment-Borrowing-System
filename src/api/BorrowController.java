
package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import model.BorrowRecord;
import model.User;
import service.BorrowOperations;
import service.BorrowService;
import exception.DuplicateReturnException;
import exception.EquipmentUnavailableException;
import exception.InvalidBorrowRequestException;
import exception.UnauthorizedBorrowingException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

public class BorrowController implements HttpHandler {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(
                    LocalDate.class,
                    (JsonSerializer<LocalDate>) (date, type, context) ->
                            new JsonPrimitive(date.toString())
            )
            .registerTypeAdapter(
                    LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, context) ->
                            LocalDate.parse(json.getAsString())
            )
            .create();

    private final BorrowOperations borrowService = new BorrowService();

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

            User user = getAuthenticatedUser(exchange);

            if ("POST".equalsIgnoreCase(method)
                    && path.endsWith("/return")) {

                returnEquipment(exchange, user);

            } else if ("POST".equalsIgnoreCase(method)) {

                borrowEquipment(exchange, user);

            } else if ("GET".equalsIgnoreCase(method)
                    && path.endsWith("/active")) {

                getActiveRecords(exchange, user);

            } else if ("GET".equalsIgnoreCase(method)
                    && path.endsWith("/all")) {

                getAllHistory(exchange, user);

            } else if ("GET".equalsIgnoreCase(method)) {

                getBorrowingHistory(exchange, user);

            } else {

                sendJson(exchange, 405,
                        Map.of("status", "error",
                               "message", "Method not allowed"));
            }

        } catch (UnauthorizedBorrowingException | SecurityException e) {

            sendJson(exchange, 403,
                    Map.of("status", "error",
                           "message", e.getMessage()));

        } catch (InvalidBorrowRequestException | EquipmentUnavailableException
                | DuplicateReturnException | IllegalArgumentException | IllegalStateException e) {

            sendJson(exchange, 400,
                    Map.of("status", "error",
                           "message", e.getMessage()));

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(exchange, 500,
                    Map.of("status", "error",
                           "message", "Borrow operation failed"));
        }
    }

    private void borrowEquipment(
            HttpExchange exchange,
            User user
    ) throws Exception {

        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException(
                    "Only students can borrow equipment."
            );
        }

        JsonObject data = readRequest(exchange);

        int equipmentId = data.get("equipmentId").getAsInt();

        BorrowRecord record = borrowService.borrowEquipment(
                user.getUserId(),
                equipmentId
        );

        sendJson(exchange, 201,
                Map.of(
                        "status", "success",
                        "message", "Equipment borrowed successfully",
                        "borrowRecord", record
                ));
    }

    private void returnEquipment(
            HttpExchange exchange,
            User user
    ) throws Exception {

        JsonObject data = readRequest(exchange);

        int recordId = data.get("recordId").getAsInt();

        BorrowRecord record = borrowService.returnEquipment(
                recordId,
                user.getUserId()
        );

        sendJson(exchange, 200,
                Map.of(
                        "status", "success",
                        "message", "Equipment returned successfully",
                        "borrowRecord", record
                ));
    }

    private void getBorrowingHistory(
            HttpExchange exchange,
            User user
    ) throws Exception {

        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException(
                    "This endpoint is for students."
            );
        }

        sendJson(exchange, 200,
                Map.of(
                        "status", "success",
                        "history",
                        borrowService.getStudentBorrowingHistory(
                                user.getUserId()
                        )
                ));
    }

    private void getActiveRecords(
            HttpExchange exchange,
            User user
    ) throws Exception {

        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException(
                    "This endpoint is for students."
            );
        }

        sendJson(exchange, 200,
                Map.of(
                        "status", "success",
                        "activeRecords",
                        borrowService.getActiveBorrowRecords(
                                user.getUserId()
                        )
                ));
    }

    private void getAllHistory(
            HttpExchange exchange,
            User user
    ) throws Exception {

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException(
                    "Admin access required."
            );
        }

        sendJson(exchange, 200,
                Map.of(
                        "status", "success",
                        "history",
                        borrowService.getAllBorrowingHistory()
                ));
    }

    private User getAuthenticatedUser(HttpExchange exchange) {

        String header = exchange.getRequestHeaders()
                .getFirst("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException("Please login first.");
        }

        String token = header.substring(7).trim();

        User user = SessionManager.getUser(token);

        if (user == null) {
            throw new SecurityException(
                    "Invalid or expired session."
            );
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

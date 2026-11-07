
package api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import model.User;
import service.UserService;
import exception.DuplicateEmailException;
import exception.InvalidUserDetailsException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthController implements HttpHandler {

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405,
                        Map.of("status", "error",
                               "message", "Method not allowed"));
                return;
            }

            if (path.endsWith("/register/admin")) {
                registerAdmin(exchange);

            } else if (path.endsWith("/register")) {
                registerStudent(exchange);

            } else if (path.endsWith("/login")) {
                login(exchange);

            } else if (path.endsWith("/logout")) {
                logout(exchange);

            } else {
                sendJson(exchange, 404,
                        Map.of("status", "error",
                               "message", "Endpoint not found"));
            }

        } catch (SecurityException e) {

            sendJson(exchange, 403,
                    Map.of("status", "error", "message", e.getMessage()));

        } catch (IllegalArgumentException e) {

            sendJson(exchange, 400,
                    Map.of("status", "error", "message", e.getMessage()));

        } catch (InvalidUserDetailsException | DuplicateEmailException e) {

            sendJson(exchange, 400,
                    Map.of("status", "error", "message", e.getMessage()));

        } catch (Exception e) {

            e.printStackTrace();

            sendJson(exchange, 500,
                    Map.of("status", "error",
                           "message", "Authentication operation failed"));
        }
    }

    // Student registration
    private void registerStudent(HttpExchange exchange)
            throws Exception {

        JsonObject data = readRequest(exchange);

        UserService userService = new UserService();

        User user = userService.registerStudent(
                data.get("name").getAsString(),
                data.get("email").getAsString(),
                data.get("contact").getAsString(),
                data.get("password").getAsString()
        );

        sendJson(exchange, 201,
                Map.of(
                        "status", "success",
                        "message", "Student registered successfully",
                        "user", safeUser(user)
                ));
    }

    // Admin registration
    private void registerAdmin(HttpExchange exchange)
            throws Exception {

        String token = getToken(exchange);

        UserService userService = SessionManager.getService(token);

        if (userService == null || !SessionManager.isAdmin(token)) {
            throw new SecurityException("Admin access required.");
        }

        JsonObject data = readRequest(exchange);

        User admin = userService.registerAdmin(
                data.get("name").getAsString(),
                data.get("email").getAsString(),
                data.get("contact").getAsString(),
                data.get("password").getAsString()
        );

        sendJson(exchange, 201,
                Map.of(
                        "status", "success",
                        "message", "Admin registered successfully",
                        "user", safeUser(admin)
                ));
    }

    // Login
    private void login(HttpExchange exchange) throws Exception {

        JsonObject data = readRequest(exchange);

        UserService userService = new UserService();

        User user = userService.login(
                data.get("email").getAsString(),
                data.get("password").getAsString()
        );

        if (user == null) {
            sendJson(exchange, 401,
                    Map.of("status", "error",
                           "message", "Invalid email or password"));
            return;
        }

        String token = SessionManager.createSession(userService);

        sendJson(exchange, 200,
                Map.of(
                        "status", "success",
                        "message", "Login successful",
                        "token", token,
                        "user", safeUser(user)
                ));
    }

    // Logout
    private void logout(HttpExchange exchange) throws IOException {

        String token = getToken(exchange);

        if (token == null || SessionManager.getUser(token) == null) {
            sendJson(exchange, 401,
                    Map.of("status", "error",
                           "message", "Invalid or expired session"));
            return;
        }

        SessionManager.removeSession(token);

        sendJson(exchange, 200,
                Map.of("status", "success",
                       "message", "Logout successful"));
    }

    private JsonObject readRequest(HttpExchange exchange)
            throws IOException {

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        return gson.fromJson(body, JsonObject.class);
    }

    private String getToken(HttpExchange exchange) {

        String header = exchange.getRequestHeaders()
                .getFirst("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        return header.substring(7).trim();
    }

    private Map<String, Object> safeUser(User user) {

        Map<String, Object> details = new LinkedHashMap<>();

        details.put("userId", user.getUserId());
        details.put("name", user.getName());
        details.put("email", user.getEmail());
        details.put("role", user.getRole());
        details.put("contact", user.getContact());

        return details;
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


package api;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class ApiServer {

    public static void main(String[] args) {

        try {
            HttpServer server = HttpServer.create(
                    new InetSocketAddress(8080), 0
            );

            // Health check
            server.createContext("/api/health", exchange -> {

                String response =
                        "{\"status\":\"success\",\"message\":\"Lab Equipment API is running!\"}";

                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().set(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                exchange.sendResponseHeaders(200, bytes.length);

                try (var output = exchange.getResponseBody()) {
                    output.write(bytes);
                }
            });

            // Authentication API
            server.createContext(
                    "/api/auth",
                    new AuthController()
            );

            // Equipment API
            server.createContext(
                    "/api/equipment",
                    new EquipmentController()
            );

            // Borrowing and Return API
            server.createContext(
                    "/api/borrow",
                    new BorrowController()
            );

            server.setExecutor(null);
            server.start();

            System.out.println("==================================");
            System.out.println("LAB EQUIPMENT API STARTED");
            System.out.println("Server: http://localhost:8080");
            System.out.println("----------------------------------");
            System.out.println("Health:      /api/health");
            System.out.println("Authentication: /api/auth");
            System.out.println("Equipment:   /api/equipment");
            System.out.println("Borrowing:   /api/borrow");
            System.out.println("==================================");

        } catch (IOException e) {

            System.out.println(
                    "API Server Error: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}

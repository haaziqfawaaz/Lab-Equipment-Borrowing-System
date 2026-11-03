
package api;

import model.User;
import service.UserService;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final ConcurrentHashMap<String, UserService> sessions =
            new ConcurrentHashMap<>();

    private static final SecureRandom secureRandom = new SecureRandom();

    // Create an independent session for each authenticated user
    public static String createSession(UserService userService) {

        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);

        sessions.put(token, userService);

        return token;
    }

    // Retrieve the service associated with a session
    public static UserService getService(String token) {

        if (token == null || token.isBlank()) {
            return null;
        }

        return sessions.get(token);
    }

    // Retrieve the authenticated user
    public static User getUser(String token) {

        UserService service = getService(token);

        if (service == null) {
            return null;
        }

        return service.getCurrentUser();
    }

    // Check whether the session belongs to an admin
    public static boolean isAdmin(String token) {

        UserService service = getService(token);

        if (service == null) {
            return false;
        }

        try {
            return service.isAdminLoggedIn();
        } catch (Exception e) {
            return false;
        }
    }

    // Remove the session during logout
    public static void removeSession(String token) {

        if (token != null) {
            sessions.remove(token);
        }
    }
}

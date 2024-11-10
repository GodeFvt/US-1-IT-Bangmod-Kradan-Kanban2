package sit.us1.backend.filters;

public class AuthWhitelistPaths {

    public static final String[] WHITELISTED_PATHS = {
            "/login",
            "/token"
    };

    public static boolean isWhitelisted(String path) {
        for (String allowedPath : WHITELISTED_PATHS) {
            if (path.startsWith(allowedPath)) {
                return true;
            }
        }
        return false;
    }
}

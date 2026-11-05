package backend.academy.common.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class URLValidator {
    public static boolean isValidURL(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() != null
                    && (uri.getScheme().equals("http") || uri.getScheme().equals("https"));
        } catch (URISyntaxException e) {
            return false;
        }
    }
}

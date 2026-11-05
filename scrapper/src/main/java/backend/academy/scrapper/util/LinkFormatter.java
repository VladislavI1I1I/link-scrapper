package backend.academy.scrapper.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkFormatter {
    public static String normalizeLink(String link) {
        return link.startsWith("https://") ? link.replaceFirst("https://", "") : link;
    }
}

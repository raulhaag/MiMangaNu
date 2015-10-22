package ar.rulosoft.mimanganu.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Own implementation of unescaping HTML code.
 *
 * Created by TheSniperFan on 28.05.15.
 */
public class HtmlUnescape {
    private static final HashMap<String, String> definitions = new HashMap<String, String>() {{
        // Definitions start here:

        put("<br\\s*/?>", "\n");
        put("&quot;", "\"");
        put("&amp;", "&");

        // End of definitions
    }};

    public static String Unescape(String input) {
        if (input != null)
        for (Map.Entry<String, String> pair : definitions.entrySet()) {
            input = input.replaceAll(pair.getKey(), pair.getValue());
        }
        return input;
    }
}

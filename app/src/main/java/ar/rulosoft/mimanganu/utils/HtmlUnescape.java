package ar.rulosoft.mimanganu.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
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
        Iterator it = definitions.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, String> pair = (Map.Entry<String, String>)it.next();
            input = input.replaceAll(pair.getKey(), pair.getValue());
        }

        return input;
    }
}

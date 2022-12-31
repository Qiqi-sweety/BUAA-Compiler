package generate;

import java.util.ArrayList;

public class ForMidPrint {
    private static final ArrayList<String> STRINGS = new ArrayList<>();

    public static void add(String s) {
        STRINGS.add(s);
    }

    public static ArrayList<String> get() {
        return STRINGS;
    }
}

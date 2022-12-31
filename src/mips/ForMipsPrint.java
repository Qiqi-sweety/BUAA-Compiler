package mips;

import java.util.ArrayList;

public class ForMipsPrint {
    private static final ArrayList<String> STRINGS = new ArrayList<>();

    public static void add(String s) {
        STRINGS.add(s);
    }

    public static ArrayList<String> get() {
        return STRINGS;
    }
}

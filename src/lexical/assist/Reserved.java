package lexical.assist;

import java.util.LinkedHashMap;

public class Reserved {
    private LinkedHashMap<String, String> reserves = new LinkedHashMap<>();

    public Reserved() {
        make();
    }

    public boolean isReserved(String s) {
        return reserves.containsKey(s);
    }

    public void make() {
        reserves.put("main", "MAINTK");
        reserves.put("const", "CONSTTK");
        reserves.put("int", "INTTK");
        reserves.put("break", "BREAKTK");
        reserves.put("continue", "CONTINUETK");
        reserves.put("if", "IFTK");
        reserves.put("else", "ELSETK");
        reserves.put("while", "WHILETK");
        reserves.put("getint", "GETINTTK");
        reserves.put("printf", "PRINTFTK");
        reserves.put("return", "RETURNTK");
        reserves.put("void", "VOIDTK");
    }

    public String getType(String s) {
        return reserves.get(s);
    }
}

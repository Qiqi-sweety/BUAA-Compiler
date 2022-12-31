package utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SymTable {
    private static ArrayList<LinkedHashMap<String, Sym>> table = new ArrayList<>();
    private static LinkedHashMap<String, Sym> curMap = new LinkedHashMap<>();

    public SymTable() {
        table.add(curMap);
    }

    public static void addLayer() {
        curMap = new LinkedHashMap<>();
        table.add(curMap);
    }

    public static void addItem(String ident, Sym sym) {
        curMap.put(ident, sym);
    }

    public static int getDim(String ident) {
        for (int i = table.size() - 1; i >= 0; i--) {
            if (table.get(i).containsKey(ident)) {
                Sym sym = table.get(i).get(ident);
                return sym.getDim();
            }
        }
        System.err.println("-9999");
        return -1;
    }

    public static boolean isErrB(String ident) {
        if (curMap.containsKey(ident)) {
            return true;
        }
        return false;
    }

    public static boolean isErrH(String ident) {
        for (int i = table.size() - 1; i >= 0; i--) {
            if (table.get(i).containsKey(ident)) {
                return table.get(i).get(ident).isConst();
            }
        }
        return false;
    }

    public static void dropLayer() {
        curMap = table.get(table.size() - 2);
        table.subList(table.size() - 1, table.size()).clear();
    }

    public static boolean isErrC(String ident) {
        for (int i = table.size() - 1; i >= 0; i--) {
            if (table.get(i).containsKey(ident)) {
                return false;
            }
        }
        return true;
    }
}

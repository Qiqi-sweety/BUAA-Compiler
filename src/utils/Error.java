package utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class Error {
    private static final LinkedHashMap<String, String> ERRS = new LinkedHashMap<>();
    private static ArrayList<Pair<String, String>> msgs = new ArrayList<>();
    private static int nowIndex = -1;

    public Error() {
        make();
    }

    public static void make() {
        ERRS.put("IllegalSymbol", "a");
        ERRS.put("DuplicatedDefinition", "b");
        ERRS.put("UndefinedName", "c");
        ERRS.put("NotMatchParaNum", "d");
        ERRS.put("NotMatchParaType", "e");
        ERRS.put("ReturnMore", "f");
        ERRS.put("ReturnLess", "g");
        ERRS.put("ConvertConst", "h");
        ERRS.put("MissSEMICN", "i");
        ERRS.put("MissRPARENT", "j");
        ERRS.put("MissRBRACK", "k");
        ERRS.put("PrintfErr", "l");
        ERRS.put("BreakOrContinue", "m");
    }

    public static void addMsg(String s, String type) {
        msgs.add(new Pair<>(s, type));
    }

    public static ArrayList<String> errs() {
        ArrayList<String> errs = new ArrayList<>();
        msgs.sort(Comparator.comparingInt(a -> Integer.parseInt(a.getHead())));
        for (Pair<String, String> pair : msgs) {
            String res = pair.getHead() + " " + pair.getTail() + "\n";
            errs.add(res);
        }
        LinkedHashSet<String> set = new LinkedHashSet<>(errs);
        errs.clear();
        errs.addAll(set);
        return errs;
    }

    public static void save() {
        nowIndex = msgs.size();
    }

    public static void restore() {
        msgs.subList(nowIndex, msgs.size()).clear();
    }
}

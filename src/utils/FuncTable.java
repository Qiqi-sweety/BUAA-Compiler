package utils;

import syntax.exps.CallExp;
import syntax.exps.LeftExp;
import syntax.nodes.Exp;
import syntax.nodes.FuncFParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FuncTable {
    private static LinkedHashMap<String, Func> TABLE = new LinkedHashMap<>();

    public FuncTable() {
    }

    public static void addItem(String ident, Func func) {
        TABLE.put(ident, func);
    }

    public static boolean isErrB(String ident) {
        return TABLE.containsKey(ident);
    }

    public static boolean isErrD(String ident, int num) {
        Func func = TABLE.get(ident);
        return num != func.paraSize();
    }

    public static int getDim(String ident) {
        Func func = TABLE.get(ident);
        if (func.isVoid()) {
            return -1;
        } else {
            return 0;
        }
    }

    public static boolean isErrC(String ident) {
        return !TABLE.containsKey(ident);
    }

    public static boolean isContain(String ident) {
        return TABLE.containsKey(ident);
    }

    public static boolean isErrE(String ident, ArrayList<Exp> exps) {
        Func func = TABLE.get(ident);
        ArrayList<FuncFParam> funcFParams = func.getParams();
        if (exps.size() == 0) {
            return false;
        }
        for (int i = 0; i < exps.size(); i++) {
            int type1 = 0;
            Exp exp = exps.get(i);
            int type2 = funcFParams.get(i).getType();
            if (exp instanceof LeftExp) {
                type1 = ((LeftExp) exp).getDim();
            } else if (exp instanceof CallExp) {
                type1 = ((CallExp) exp).getDim();
            }
            if (type1 != type2) {
                return true;
            }
        }
        return false;
    }
}

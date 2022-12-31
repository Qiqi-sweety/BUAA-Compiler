package lexical.assist;

import java.util.LinkedHashMap;

public class Signal {
    private LinkedHashMap<String, String> all = new LinkedHashMap<>();
    private LinkedHashMap<String, String> twoChar = new LinkedHashMap<>();
    private LinkedHashMap<String, String> oneChar = new LinkedHashMap<>();

    public void make2Char() {
        twoChar.put("&&", "AND");
        twoChar.put("||", "OR");
        twoChar.put("<=", "LEQ");
        twoChar.put(">=", "GEQ");
        twoChar.put("==", "EQL");
        twoChar.put("!=", "NEQ");
    }

    public void make1Char() {
        oneChar.put("!", "NOT");
        oneChar.put("+", "PLUS");
        oneChar.put("-", "MINU");
        oneChar.put("*", "MULT");
        oneChar.put("/", "DIV");
        oneChar.put("%", "MOD");
        oneChar.put("<", "LSS");
        oneChar.put(">", "GRE");
        oneChar.put("=", "ASSIGN");
        oneChar.put(";", "SEMICN");
        oneChar.put(",", "COMMA");
        oneChar.put("(", "LPARENT");
        oneChar.put(")", "RPARENT");
        oneChar.put("[", "LBRACK");
        oneChar.put("]", "RBRACK");
        oneChar.put("{", "LBRACE");
        oneChar.put("}", "RBRACE");
    }

    public void make() {
        all.put("!", "NOT");
        all.put("&&", "AND");
        all.put("||", "OR");
        all.put("+", "PLUS");
        all.put("-", "MINU");
        all.put("*", "MULT");
        all.put("/", "DIV");
        all.put("%", "MOD");
        all.put("<", "LSS");
        all.put("<=", "LEQ");
        all.put(">", "GRE");
        all.put(">=", "GEQ");
        all.put("==", "EQL");
        all.put("!=", "NEQ");
        all.put("=", "ASSIGN");
        all.put(";", "SEMICN");
        all.put(",", "COMMA");
        all.put("(", "LPARENT");
        all.put(")", "RPARENT");
        all.put("[", "LBRACK");
        all.put("]", "RBRACK");
        all.put("{", "LBRACE");
        all.put("}", "RBRACE");
    }

    public Signal() {
        make1Char();
        make2Char();
        make();
    }

    public boolean is2char(String s) {
        return twoChar.containsKey(s);
    }

    public boolean is1char(String s) {
        return oneChar.containsKey(s);
    }

    public String getType(String s) {
        return all.get(s);
    }
}

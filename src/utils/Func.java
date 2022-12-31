package utils;

import syntax.nodes.FuncFParam;

import java.util.ArrayList;

public class Func {
    private boolean isVoid;
    private String ident;
    private ArrayList<FuncFParam> params;

    public Func(String ident, boolean isVoid, ArrayList<FuncFParam> params) {
        this.ident = ident;
        this.isVoid = isVoid;
        this.params = params;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public ArrayList<FuncFParam> getParams() {
        return params;
    }

    public int paraSize() {
        return params.size();
    }
}

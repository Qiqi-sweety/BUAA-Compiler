package syntax.nodes;

import java.util.ArrayList;

public class Def extends RootNode {
    private final String ident; //a
    private Exp exp1 = null; //a[exp1]
    private Exp exp2 = null; //a[exp2]
    private ArrayList<Exp> exps = new ArrayList<>(); //a[][]=exps
    private final boolean isConst;
    private final int type; // a[exp1][exp2]:2, a[exp1]:1, a:0

    public Def(String ident, Exp exp1, Exp exp2, ArrayList<Exp> exps, boolean isConst) {
        this.ident = ident;
        this.exp1 = exp1;
        this.exp2 = exp2;
        this.exps = exps;
        this.isConst = isConst;
        this.type = (exp1 != null && exp2 != null) ? 2 : (exp1 != null ? 1 : 0);
    }

    public String getIdent() {
        return ident;
    }

    public Exp getExp1() {
        return exp1;
    }

    public Exp getExp2() {
        return exp2;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    public int expSize() {
        if (exps == null) {
            return 0;
        }
        return exps.size();
    }

    public boolean isConst() {
        return isConst;
    }

    public int getType() {
        return type;
    }

}

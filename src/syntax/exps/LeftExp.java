package syntax.exps;

import syntax.nodes.Exp;
import utils.SymTable;

public class LeftExp extends Exp {
    private final String ident;
    private Exp exp1 = null; //ident[exp1]
    private Exp exp2 = null; //ident[exp1][exp2]

    public LeftExp(String ident, Exp exp1, Exp exp2) {
        this.ident = ident;
        this.exp1 = exp1;
        this.exp2 = exp2;
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

    public int getDim() {
        // for err-test
        int init = SymTable.getDim(ident);
        if (exp1 == null && exp2 == null) {
            return init;
        } else if (exp2 == null) {
            return init - 1;
        } else {
            return init - 2;
        }
    }

    public int getOffset() {
        if (exp1 == null && exp2 == null) {
            return 0;
        } else if (exp2 == null) {
            return 1;
        } else {
            return 2;
        }
    }
}

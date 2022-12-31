package syntax.exps;

import syntax.nodes.Exp;

public class UnaryExp extends Exp {
    private final String op;
    private final Exp exp;

    public UnaryExp(String op, Exp exp) {
        this.op = op;
        this.exp = exp;
    }

    public String getOp() {
        return op;
    }

    public Exp getExp() {
        return exp;
    }
}

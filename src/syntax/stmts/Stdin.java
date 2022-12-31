package syntax.stmts;

import syntax.exps.LeftExp;
import syntax.nodes.Stmt;

public class Stdin extends Stmt {
    // exp = getint()
    private final LeftExp exp;

    public Stdin(LeftExp exp) {
        this.exp = exp;
    }

    public LeftExp getLeftExp() {
        return exp;
    }

    public String getLeftIdent() {
        return exp.getIdent();
    }
}

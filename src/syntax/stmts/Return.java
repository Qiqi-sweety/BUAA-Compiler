package syntax.stmts;

import syntax.nodes.Exp;
import syntax.nodes.Stmt;

public class Return extends Stmt {
    private Exp exp = null;
    private final int line;
    private boolean isReturn = true;

    public Return(Exp exp, int line) {
        this.exp = exp;
        this.line = line;
        isReturn = exp != null;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public Exp getExp() {
        return exp;
    }

    public int getLine() {
        return line;
    }
}

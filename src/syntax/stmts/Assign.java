package syntax.stmts;
import syntax.exps.LeftExp;
import syntax.nodes.Exp;
import syntax.nodes.Stmt;

public class Assign extends Stmt {
    private final LeftExp leftExp;
    private final Exp rightExp;

    public Assign(LeftExp leftExp, Exp exp2) {
        this.leftExp = leftExp;
        this.rightExp = exp2;
    }

    public LeftExp getLeftExp() {
        return leftExp;
    }

    public Exp getRightExp() {
        return rightExp;
    }



}

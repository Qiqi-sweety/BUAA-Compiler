package syntax.stmts;

import syntax.nodes.Exp;
import syntax.nodes.Stmt;

public class While extends Stmt {
    private final Exp condition;
    private final Stmt stmt;

    public While(Exp condition, Stmt stmt) {
        this.condition = condition;
        this.stmt = stmt;
    }

    public Exp getCondition() {
        return condition;
    }

    public Stmt getStmt() {
        return stmt;
    }

}

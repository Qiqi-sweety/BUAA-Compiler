package syntax.stmts;

import syntax.nodes.Exp;
import syntax.nodes.Stmt;

public class If extends Stmt {
    private final Exp condition;
    private final Stmt ifBlock;
    private final Stmt elseBlock;

    public If(Exp condition, Stmt ifBlock, Stmt elseBlock) {
        this.condition = condition;
        this.ifBlock = ifBlock;
        this.elseBlock = elseBlock;
    }

    public Exp getCondition() {
        return condition;
    }

    public Stmt getIfBlock() {
        return ifBlock;
    }

    public Stmt getElseBlock() {
        return elseBlock;
    }

}

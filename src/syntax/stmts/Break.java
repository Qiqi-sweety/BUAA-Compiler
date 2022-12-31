package syntax.stmts;

import syntax.nodes.Stmt;

public class Break extends Stmt {
    private final int lineNum;

    public Break(int lineNum) {
        this.lineNum = lineNum;
    }

    public int getLineNum() {
        return lineNum;
    }
}

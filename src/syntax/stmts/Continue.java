package syntax.stmts;

import syntax.nodes.Stmt;

public class Continue extends Stmt {
    private final int line;

    public Continue(int line) {
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}

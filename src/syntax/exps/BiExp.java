package syntax.exps;

import syntax.nodes.Exp;

public class BiExp extends Exp {
    private final String op;
    private final Exp left;
    private final Exp right;

    public BiExp(String op, Exp left, Exp right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public String getOp() {
        return op;
    }

    public Exp getLeft() {
        return left;
    }

    public Exp getRight() {
        return right;
    }
}

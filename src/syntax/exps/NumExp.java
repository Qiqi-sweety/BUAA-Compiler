package syntax.exps;

import syntax.nodes.Exp;

public class NumExp extends Exp {
    private final Long num;

    public NumExp(Long num) {
        this.num = num;
    }

    public Long getNum() {
        return num;
    }
}

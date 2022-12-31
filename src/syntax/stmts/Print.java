package syntax.stmts;

import syntax.nodes.Exp;
import syntax.nodes.Stmt;

import java.util.ArrayList;

public class Print extends Stmt {
    // printf("string",exp1,exp2,..)
    private final String string;
    private final ArrayList<Exp> exps;

    public Print(String string) {
        this.string = string;
        exps = new ArrayList<>();
    }

    public void addExp(Exp exp) {
        exps.add(exp);
    }

    public int expSize() {
        return exps.size();
    }

    public String getString() {
        return string;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

}

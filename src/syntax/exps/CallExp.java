package syntax.exps;

import syntax.nodes.Exp;
import utils.FuncTable;

import java.util.ArrayList;

public class CallExp extends Exp {
    private final String ident;
    private ArrayList<Exp> paras = new ArrayList<>();

    public CallExp(String ident, ArrayList<Exp> paras) {
        this.ident = ident;
        this.paras = paras;
    }

    public String getIdent() {
        return ident;
    }

    public ArrayList<Exp> getParas() {
        if (paras == null) {
            return new ArrayList<>();
        }
        return paras;
    }

    public int getDim() {
        // void -1; int 0 for err-test
        return FuncTable.getDim(ident);
    }
}

package syntax.nodes;

import java.util.ArrayList;

public class Decl extends RootNode {
    private final ArrayList<Def> defs;
    private final boolean isConst;

    public Decl(ArrayList<Def> defs, boolean isConst) {
        this.isConst = isConst;
        this.defs = defs;
    }

    public ArrayList<Def> getDefs() {
        return defs;
    }

    public boolean isConst() {
        return isConst;
    }
}

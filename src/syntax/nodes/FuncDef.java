package syntax.nodes;

import java.util.ArrayList;

public class FuncDef extends RootNode {
    private final String type; //VOIDTK or INTTK
    private final String ident;
    private final ArrayList<FuncFParam> params;
    private final Block block;

    public FuncDef(String type, String ident, ArrayList<FuncFParam> params, Block block) {
        this.type = type;
        this.ident = ident;
        this.params = params;
        this.block = block;
    }

    public String getType() {
        return type;
    }

    public String getIdent() {
        return ident;
    }

    public ArrayList<FuncFParam> getParams() {
        return params;
    }

    public Block getBlock() {
        return block;
    }
}

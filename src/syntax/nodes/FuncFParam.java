package syntax.nodes;

public class FuncFParam extends RootNode {
    private final String ident; // int
    private final int type; //a:0, a[]:1, a[][exp]:2
    private final Exp exp2; // only have exp for 2D

    public FuncFParam(String ident,int type, Exp exp2) {
        this.ident = ident;
        this.type = type;
        this.exp2 = exp2;
    }

    public String getIdent() {
        return ident;
    }

    public int getType() {
        return type;
    }

    public Exp getExp2() {
        return exp2;
    }
}

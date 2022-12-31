package generate.usage.instr.decls;

public class ParaDecl extends Decl {
    private int index = 0;

    public ParaDecl(int index,int type,String name) {
        setName(name);
        setType(type);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return getId() + " = getParameter" + index;
    }
}

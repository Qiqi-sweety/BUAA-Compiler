package generate.usage.instr.decls;

import generate.usage.Value;
import generate.usage.Use;
import static generate.Mediate.link;

public class InlineDecl extends Decl {
    private Use use;
    public InlineDecl(
        Value value,
        ParaDecl paraDecl
    ) {
        super.setSize1(paraDecl.getSize1());
        super.setSize2(paraDecl.getSize2());
        super.setType(paraDecl.getTypeInt());
        super.setName(paraDecl.getName());
        this.use = link(value,this);
    }
    public Value getValue(){
        return use.getVal();
    }
}

package generate.usage.instr.pointers;

import generate.Mediate;
import generate.usage.Instr;
import generate.usage.Use;
import generate.usage.Value;
import generate.usage.instr.decls.Decl;
import generate.usage.instr.decls.InlineDecl;

import static generate.Mediate.link;
public class IdentPointer extends Instr {
    private Decl target = null;
    private String type = "";
    private Use offset = null;

    public IdentPointer(Decl target, String type) {
        this.target = target;
        link(target, this);
        if(target instanceof InlineDecl){
            link(((InlineDecl) target).getValue(),this);
        }
        this.type = type;
    }

    public String getType() {
        return type;
    }


    @Override
    public String toString() {
        String first = getId() + " = addressof " + target.getName();
        if (offset != null) {
            first += "[" + Mediate.idOrConst(getOffset()) + "]";
        }
        return first;
    }


    public Decl getTarget() {
        return target;
    }

    public void setOffset(Value position) {
        this.offset = link(position,this);
    }
    public Value getOffset() {
        if(offset == null){
            return null;
        }
        return offset.getVal();
    }
}

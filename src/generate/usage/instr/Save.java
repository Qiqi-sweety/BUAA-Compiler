package generate.usage.instr;

import generate.Mediate;
import generate.usage.Instr;
import generate.usage.Use;
import generate.usage.Value;
import generate.usage.instr.decls.Decl;
import generate.usage.instr.decls.InlineDecl;
import generate.usage.instr.decls.ParaDecl;

import static generate.Mediate.link;

public class Save extends Instr {
    private Use target = null;
    private Use value = null;
    private Use offset = null;

    public Save(GetAddress target) {
        this.target = link(target,this);
        if(target.getTarget() instanceof InlineDecl){
            link(((InlineDecl) target.getTarget()).getValue(),this);
        }
        else{
            link(target.getTarget(),this);
        }
    }

    public Save() {

    }



    public void setValue(Value value) {
        this.value = link(value,this);
    }
    public void setOffset(Value offset){
        this.offset = link(offset,this);
    }

    public Value getValue() {
        return value.getVal();
    }

    public GetAddress getTarget() {
        return (GetAddress) target.getVal();
    }

    @Override
    public String toString() {
        String result = "";
        if (offset != null) {
            result += ", " + Mediate.idOrConst(offset.getVal());
        }
        if (target.getVal() instanceof ParaDecl) {
            return getId() + " = store i32 " + Mediate.idOrConst(value.getVal()) + ", i32* " +
                    target.getVal().getId() + result;
        }
        return getId() + " = store i32 " + Mediate.idOrConst(value.getVal()) + ", i32* @" +
                target.getVal().getName() + result;
    }

    public Value getOffset() {
        return offset.getVal();
    }
}

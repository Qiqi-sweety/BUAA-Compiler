package generate.usage.instr;

import generate.Mediate;
import generate.usage.Instr;
import generate.usage.Use;
import generate.usage.Value;
import generate.usage.instr.decls.Decl;
import generate.usage.instr.decls.InlineDecl;
import generate.usage.instr.decls.ParaDecl;

import static generate.Mediate.link;

public class Load extends Instr {
    private Use target = null;
    private Use offset = null;

    public Load(GetAddress target) {
        this.target = link(target,this);
        if(target.getTarget() instanceof InlineDecl){
            link(((InlineDecl) target.getTarget()).getValue(),this);
        }
        else{
            link(target.getTarget(),this);
        }
    }

    public GetAddress getTarget() {
        return (GetAddress) target.getVal();
    }


    public void setOffset(Value offset){
        this.offset = link(offset,this);
    }

    @Override
    public String toString() {
        String result = "";
        if (offset != null) {
            result += ", " + Mediate.idOrConst(offset.getVal());
        }
        if (target.getVal() instanceof ParaDecl) {
            return getId() + " = load i32, i32* " + target.getVal().getId() + result;
        }
        return getId() + " = load i32, i32* @" + target.getVal().getId() + result;
    }

    public Value getOffset() {
        return offset.getVal();
    }
}

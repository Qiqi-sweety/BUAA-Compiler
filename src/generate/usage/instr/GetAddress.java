package generate.usage.instr;

import generate.usage.Instr;
import generate.usage.instr.decls.Decl;
import generate.usage.instr.decls.InlineDecl;

import java.util.Objects;

import static generate.Mediate.link;

public class GetAddress extends Instr {
    private Decl target =null;
    public GetAddress(Decl target){
        this.target = target;
        link(target, this);
        if(target instanceof InlineDecl){
            link(((InlineDecl) target).getValue(),this);
        }
    }
    public Decl getTarget(){
        return target;
    }
    public String getType(){
        return target.getType();
    }

    @Override
    public String toString() {
        return getId() + " = getelementptr i32, i32* " + target.getName() + ", i32 0";
    }

}

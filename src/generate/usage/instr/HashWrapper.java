package generate.usage.instr;

import generate.Mediate;
import generate.usage.Value;
import generate.usage.instr.exps.Binary;

import java.util.HashMap;
import java.util.Objects;

public class HashWrapper {
    Value v;
    public HashWrapper(Value value){
        this.v=value;
    }

    @Override
    public int hashCode()
    {
        if(v instanceof Binary){
            Binary value= (Binary) v;
            return Objects.hash(value.getOp(), Mediate.idOrConst(value.getLeft()), Mediate.idOrConst(value.getRight()));
        }
        else if(v instanceof GetAddress){
            GetAddress value=(GetAddress) v;
            return Objects.hash(value.getTarget());
        }else{
            return Objects.hash(v);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if(!(obj instanceof HashWrapper)){
            return false;
        }
        if(v instanceof Binary){
            if (!((((HashWrapper) obj).v) instanceof Binary)) {
                return false;
            }
            Binary value= (Binary) v;
            Binary binary = (Binary) ((HashWrapper) obj).v;
            return Objects.equals(value.getOp(), binary.getOp()) &&
                Objects.equals(Mediate.idOrConst(binary.getLeft()), Mediate.idOrConst(value.getLeft())) &&
                Objects.equals(Mediate.idOrConst(binary.getRight()), Mediate.idOrConst(value.getRight()));
        }
        else if(v instanceof GetAddress) {
            if (!(((HashWrapper) obj).v instanceof GetAddress)) {
                return false;
            }
            GetAddress value=(GetAddress) v;
            GetAddress that = (GetAddress) ((HashWrapper) obj).v;
            return Objects.equals(value.getTarget(), that.getTarget());
        }
        else{
            return Objects.equals(this,obj);
        }
    }
}

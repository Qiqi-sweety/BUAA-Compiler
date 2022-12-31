package generate.usage.instr;

import generate.Mediate;
import generate.usage.Block;
import generate.usage.Instr;
import generate.usage.Use;
import generate.usage.Value;

import static generate.Mediate.link;

public class Branch extends Instr {
    private Use cond = null;
    private Block trueBlock = null;
    private Block falseBlock = null;

    public Branch(Value cond, Block trueBlock, Block falseBlock) {
        this.cond = link(cond,this);
        this.trueBlock = trueBlock;
        this.falseBlock = falseBlock;
    }
    public void setCond(Value value){
        assert value==null;
        cond=null;
    }
    public Value getCond() {
        if(cond == null) return null;
        return cond.getVal();
    }

    public Block getTrueBlock() {
        return trueBlock;
    }

    public Block getFalseBlock() {
        return falseBlock;
    }

    public void setTrueBlock(Block trueBlock) {
        this.trueBlock = trueBlock;
    }

    public void setFalseBlock(Block falseBlock) {
        this.falseBlock = falseBlock;
    }

    public Branch(Block trueBlock) {
        this.trueBlock = trueBlock;
    }


    @Override
    public String toString() {
        if (cond == null) {
            return "br " + trueBlock.getLabel();
        } else {
            return "br " + Mediate.idOrConst(cond.getVal()) + ", " + trueBlock.getLabel() + ", " +
                    falseBlock.getLabel();
        }
    }
}


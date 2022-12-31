package generate.usage;

import generate.Mediate;
import generate.usage.instr.decls.Decl;
import utils.Pair;

import java.util.ArrayList;

import static generate.Mediate.link;
import static generate.Mediate.tryRemoveTrivialPhi;

public class Phi extends Instr {
    private ArrayList<Pair<Use, Block>> operands = new ArrayList<>();
    private final Block belongBlock;

    public Phi(Block belongBlock) {
        this.belongBlock=belongBlock;
        setFatherBlock(belongBlock);
    }

    public Value addPhi(Decl decl) {
        for (Block block : belongBlock.getPres()) {
            operands.add(new Pair<>(link(Mediate.readVar(decl,block),this), block));
        }
        return tryRemoveTrivialPhi(this);
    }

    public ArrayList<Pair<Use, Block>> getOperands() {
        return operands;
    }

    public void addOperand(Value value, Block block) {
        operands.add(new Pair<>(link(value,this), block));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getId());
        sb.append(" = phi i32 ");
        for (int i = 0; i < operands.size(); i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append("[");
            sb.append(Mediate.idOrConst(operands.get(i).getHead().getVal()));
            sb.append(", ");
            sb.append(operands.get(i).getTail().getLabel());
            sb.append("]");
        }
        return sb.toString();
    }

    public Block getBelongBlock() {
        return belongBlock;
    }
}

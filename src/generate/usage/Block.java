package generate.usage;

import generate.ForMidPrint;
import generate.Mediate;
import generate.usage.instr.Branch;
import generate.usage.instr.Ret;
import utils.Pair;

import java.util.ArrayList;
import java.util.LinkedHashSet;

// label:
//     %1 = add i32 %0, 1
//     %2 = icmp slt i32 %1, 10

public class Block extends Value {

    private String label;
    private Block nextBlock = null;
    private Boolean isWhileHead = false;
    BlockType blockType = BlockType.NORMAL;
    private boolean isSealed = true;
    private boolean isRemoved = false;
    private boolean backGenerated = false;
    private ArrayList<Instr> instructions = new ArrayList<>();
    private ArrayList<Phi> phis = new ArrayList<>();
    private ArrayList<Block> pres = new ArrayList<>(); //前驱
    private final ArrayList<Block> follows = new ArrayList<>(); //后继
    private final ArrayList<String> codes;
    private final ArrayList<String> ends = new ArrayList<>(); //结束块
    private final ArrayList<String> exits = new ArrayList<>(); //退出块
    private final ArrayList<Pair<Value, Value>> parallelCopy = new ArrayList<>();

    private LinkedHashSet<Value> liveOut = new LinkedHashSet<>(); //活跃出口
    private final LinkedHashSet<Value> liveIn = new LinkedHashSet<>(); //活跃入口
    private Block next;
    private Block whileEndBlock = null;
    private int start_interval;
    private int end_interval;

    public int getStart_interval() {
        return start_interval;
    }

    public void setStart_interval(int start_interval) {
        this.start_interval = start_interval;
    }

    public int getEnd_interval() {
        return end_interval;
    }

    public void setEnd_interval(int end_interval) {
        this.end_interval = end_interval;
    }

    public ArrayList<String> getEnds() {
        return ends;
    }

    public Block getNext() {
        return next;
    }

    public void setNext(Block next) {
        this.next = next;
    }

    public boolean isBackGenerated() {
        return backGenerated;
    }

    public void setBackGenerated(boolean backGenerated) {
        this.backGenerated = backGenerated;
    }

    public ArrayList<String> getExits() {
        return exits;
    }

    public ArrayList<Phi> getPhis() {
        return phis;
    }

    public boolean isSealed() {
        return isSealed;
    }

    public ArrayList<Block> getFollows() {
        return follows;
    }

    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public Block() {
        label = Mediate.createBlockID();
        pres = new ArrayList<>();
        codes = new ArrayList<>();
    }

    public void setBlockType(BlockType blockType) {
        this.blockType = blockType;
    }

    public ArrayList<Instr> getInstructions() {
        return instructions;
    }

    public void setInstructions(ArrayList<Instr> instructions) {
        this.instructions = instructions;
    }

    private Boolean isEnd(Instr instr) {
        return instr instanceof Branch || instr instanceof Ret;
    }

    public Boolean shouldNotInsertCode() {
        return instructions.size() > 0 && isEnd(instructions.get(instructions.size() - 1));
    }

    public void addInstr(Instr instr) {
        if (shouldNotInsertCode()) {
            return;
        }
        instructions.add(instr);
    }

    public void print() {
        for (Instr instr : instructions) {
            ForMidPrint.add(instr.toString() + "\n");
        }
    }

    public void addPhi(Phi phi) {
        phis.add(phi);
    }

    public void addPre(Block pre) {
        pres.add(pre);
    }

    public void addFollow(Block follow) {
        follows.add(follow);
    }

    public void addCode(String code) {
        codes.add(code);
    }

    public void addCode(ArrayList<String> codes) {
        this.codes.addAll(codes);
    }

    public ArrayList<Block> getPres() {
        return pres;
    }

    public ArrayList<String> getCodes() {
        return codes;
    }

    public String getLabel() {
        if (label.equals("")) {
            label = Mediate.createBlockID();
        }
        return label;
    }

    public void setSealed(boolean sealed) {
        isSealed = sealed;
    }

    public LinkedHashSet<Value> getLiveOut() {
        return liveOut;
    }

    public void setLiveOut(LinkedHashSet<Value> liveOut) {
        this.liveOut = liveOut;
    }

    public LinkedHashSet<Value> getLiveIn() {
        return liveIn;
    }

    public void addLiveIn(LinkedHashSet<Value> liveIn) {
        this.liveIn.addAll(liveIn);
    }

    public Boolean getWhileHead() {
        return isWhileHead;
    }

    public void setWhileHead(Boolean whileHead) {
        isWhileHead = whileHead;
    }

    public Block getWhileEndBlock() {
        return whileEndBlock;
    }

    public void setWhileEndBlock(Block whileEndBlock) {
        this.whileEndBlock = whileEndBlock;
    }

    public void setPhis(ArrayList<Phi> phis) {
        this.phis = phis;
    }

    public void addParallelCopy(Pair<Value, Value> pair) {
        if (pair.getTail() == pair.getHead()) {
            return;
        }
        this.parallelCopy.add(pair);
    }

    public ArrayList<Pair<Value, Value>> getParallelCopy() {
        return parallelCopy;
    }

    public Instr getLastInstr() {
        if (instructions.size() == 0) {
            return null;
        }
        return instructions.get(instructions.size()-1);
    }

}

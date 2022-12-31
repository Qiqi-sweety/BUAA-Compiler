package generate.usage;

import generate.Mediate;
import generate.VarTable;
import generate.usage.instr.decls.Decl;
import mips.LiveInterval;
import utils.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Value {
    boolean reachable;
    private int allocateNumber;


    public int getAllocateNumber() {
        return allocateNumber;
    }

    public void setAllocateNumber(int allocateNumber) {
        this.allocateNumber = allocateNumber;
    }
    private String id = ""; // use for tmp store
    private String globalId = "";

    private String name;
    private ArrayList<Use> uses = new ArrayList<>(); // for what???? // TODO: 2022/11/1
    private Block fatherBlock = null;
    private Stack stack = new Stack();
    private LiveInterval liveInterval = null;

    public Value(String name) {
        this.name = name;
        this.uses = new ArrayList<>();
        this.stack = new Stack();
    }

    public Value() {
        getId();
    }

    public String getGlobalId() {
        if (globalId.equals("")) {
            globalId = Mediate.createGlobalID();
        }
        return globalId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Stack getPlace() {
        return stack;
    }

    public String getId() {
        if (id.equals("")) {
            id = Mediate.createID();
        }
        return id;
    }
    private ArrayList<Use> operands = new ArrayList<>();
    public void addOperand(Use use) {
        operands.add(use);
    }
    public ArrayList<Use> getOperand(){
        return operands;
    }
    public String getName() {
        return name;
    }

    public ArrayList<Use> getUses() {
        return uses;
    }

    public Block getFatherBlock() {
        return fatherBlock;
    }

    public void setFatherBlock(Block fatherBlock) {
        this.fatherBlock = fatherBlock;
    }

    public void addUse(Use use) {
        uses.add(use);
    }

    public void replaceAllUseWith(Value value) {
        assert value != null;
        ArrayList<Pair<Decl,Block>> alluses=new ArrayList<>();
        for(Decl d:Mediate.curDef.keySet()){
            for(Block b:Mediate.curDef.get(d).keySet()){
                if(Mediate.curDef.get(d).get(b).equals(this)){
                    alluses.add(new Pair<>(d,b));
                }
            }
        }
        for(Pair<Decl,Block> p:alluses){
            Mediate.writeVar(p.getHead(),value,p.getTail());
        }
        uses=uses.stream().map(a->a.setVal(value)).collect(Collectors.toCollection(ArrayList::new));
        uses.clear();
    }

    public LiveInterval getLiveInterval() {
        return liveInterval;
    }

    public void setLiveInterval(LiveInterval liveInterval) {
        this.liveInterval = liveInterval;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }
}

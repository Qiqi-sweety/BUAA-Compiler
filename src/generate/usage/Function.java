package generate.usage;

import generate.ForMidPrint;
import generate.usage.instr.decls.ParaDecl;
import syntax.nodes.FuncDef;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class Function extends Value {
    // super name, id
    private final Block entrance;
    private final String returnType;
    private final ArrayList<Block> blocks;
    private ArrayList<Block> linearList;
    private LinkedHashSet<Integer> usedReg=new LinkedHashSet<>();
    private final ArrayList<ParaDecl> paraList;
    private int sp;

    private LinkedHashSet<Function> called=new LinkedHashSet<>();
    private FuncDef origin;
    private Boolean inlined = true;

    public Function(String name, String type, Block entrance,FuncDef origin) {
        super(name);
        this.returnType = type.equals("INTTK") ? "int" : "void";
        this.entrance = entrance;
        this.origin = origin;
        this.paraList = new ArrayList<>();
        this.blocks = new ArrayList<>();
    }

    public ArrayList<Block> getLinearList() {
        return linearList;
    }

    public ArrayList<Block> getBlocks() {
        return blocks;
    }

    public int getSp() {
        return sp;
    }

    public void setSp(int sp) {
        this.sp = sp;
    }

    public void setLinearList(ArrayList<Block> linearList) {
        this.linearList = linearList;
    }

    public ArrayList<ParaDecl> getParaList() {
        return paraList;
    }

    public String getReturnType() {
        return returnType;
    }

    public void addPara(ParaDecl paraDecl) {
        paraList.add(paraDecl);
    }

    public void addBlock(Block block) {
        blocks.add(block);
    }

    public void print() {
        ForMidPrint.add(
                "define " + returnType + " @" + super.getName() + "(" + printParas() + ")" + "\n");
        ForMidPrint.add("{");
        for (Block block : linearList) {
            ForMidPrint.add("\n");
            ForMidPrint.add(block.getLabel() + ":\n");
            for (Phi blockPhi : block.getPhis()) {
                ForMidPrint.add(blockPhi.toString() + "\n");
            }
            for (Instr instr : block.getInstructions()) {
                ForMidPrint.add(instr.toString() + "\n");
            }
        }
        ForMidPrint.add("}\n");
    }

    private String printParas() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < paraList.size(); i++) {
            str.append(String.valueOf(paraList.get(i).getType())).append(" ")
                    .append(String.valueOf(paraList.get(i).getId()));
            if (i != paraList.size() - 1) {
                str.append(", ");
            }
        }
        return str.toString();
    }

    public Block getEntrance() {
        return entrance;
    }

    public Boolean getInlined() {
        return inlined;
    }

    public void setInlined(Boolean inlined) {
        this.inlined = inlined;
    }

    public FuncDef getOrigin() {
        return origin;
    }
    public void addUsedReg(int reg){
        usedReg.add(reg);
    }
    public void addCalled(Function function){
        called.add(function);
    }
    public LinkedHashSet<Integer> getUsedReg() {
        return usedReg;
    }

    public LinkedHashSet<Integer> getUsedRegDfs(LinkedHashSet<Function> visted) {
        if(visted.contains(this)){
            return new LinkedHashSet<>();
        }
        visted.add(this);
        LinkedHashSet<Integer> res = new LinkedHashSet<>(usedReg);
        for (Function function : called) {
            res.addAll(function.getUsedRegDfs(visted));
        }
        return res;
    }
}

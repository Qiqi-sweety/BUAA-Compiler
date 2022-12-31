package mips;

import generate.usage.Block;
import generate.usage.Const;
import generate.usage.Function;
import generate.usage.Instr;
import generate.usage.Phi;
import generate.usage.Program;
import generate.usage.TempValue;
import generate.usage.Use;
import generate.usage.Value;
import generate.usage.instr.Branch;
import generate.usage.instr.GetAddress;
import generate.usage.instr.Load;
import generate.usage.instr.Print;
import generate.usage.instr.Read;
import generate.usage.instr.Ret;
import generate.usage.instr.Save;
import generate.usage.instr.decls.Decl;
import generate.usage.instr.decls.GlobalDecl;
import generate.usage.instr.decls.InlineDecl;
import generate.usage.instr.decls.LocalDecl;
import generate.usage.instr.decls.ParaDecl;
import generate.usage.instr.exps.Binary;
import generate.usage.instr.exps.Call;
import generate.usage.instr.exps.Unary;
import generate.usage.instr.pointers.IdentPointer;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class BackendLinearScan {
    private int strNum;
    private int stackNum;
    private ArrayList<String> globals;
    private ArrayList<String> blocks;
    private Program program;
    private Function workFunc;
    private Block workingBlock;
    private static final boolean isDiv = true;

    public BackendLinearScan(Program program) {
        strNum = 0;
        stackNum = 0;
        globals = new ArrayList<>();
        blocks = new ArrayList<>();
        this.program = program;
    }

    public void forGlobalDecl(Value value) {
        GlobalDecl decl = (GlobalDecl) value;
        StringBuilder str = new StringBuilder("global_" + decl.getGlobalId() + ":.word ");
        switch (decl.getType()) {
            case "i32":
                str.append(decl.getValue());
                break;
            case "i32arr1":
                for (int i = 0; i < decl.getArray().get(0).size(); i++) {
                    if (i != 0) {
                        str.append(",");
                    }
                    str.append(decl.getArray().get(0).get(i));
                }
                break;
            case "i32arr2":
                for (int i = 0; i < decl.getSize1(); i++) {
                    for (int j = 0; j < decl.getSize2(); j++) {
                        if (!(i == 0 && j == 0)) {
                            str.append(",");
                        }
                        str.append(decl.getArray().get(i).get(j));
                    }
                }
                break;
            default:
                System.err.println("Something wrong");
                break;
        }
        globals.add(str.toString());
    }

    public int getValue(Value value, int no) {
        return getValue(value, no, false);
    }

    public int getValue(Value value, int no, boolean inEnd) {
        if (value.getLiveInterval() != null) {
            if (value.getLiveInterval().getAssignedRegister() != -1) {
                if (!inEnd) {
                    workingBlock.getCodes().add("# get " + value.getId() + " to " +
                        value.getLiveInterval().getAssignedRegister());
                } else {
                    workingBlock.getEnds().add("# get " + value.getId() + " to " +
                        value.getLiveInterval().getAssignedRegister());
                }
                return value.getLiveInterval().getAssignedRegister();
            }
        }
        if (value instanceof Const) {
            if (((Const) value).getValue() == 0) {
                return 0;
            }
            if (inEnd) {
                workingBlock.getEnds().add("li $" + no + "," + value);
            } else {
                workingBlock.getCodes().add("li $" + no + "," + value);
            }
            return no;
        }
        alloc(value);
        if (!inEnd) {
            workingBlock.getCodes().add(
                "lw $" + no + "," + value.getPlace().getOffset() + "($sp)" + "# " + value.getId());
        } else {
            workingBlock.getEnds().add(
                "lw $" + no + "," + value.getPlace().getOffset() + "($sp)" + "# " + value.getId());
        }
        return no;
    }

    public void saveValue(Value value, int no) {
        saveValue(value, no, false);
    }

    public void saveValue(Value value, int no, Boolean inEnd) {
        if (value.getLiveInterval().getAssignedRegister() == -1) {
            alloc(value);
            if (inEnd) {
                workingBlock.getEnds().add(
                    "sw $" + no + "," + value.getPlace().getOffset() + "($sp)" + "# " +
                        value.getId());
            } else {
                workingBlock.getCodes().add(
                    "sw $" + no + "," + value.getPlace().getOffset() + "($sp)" + "# " +
                        value.getId());
            }
        } else if (value.getLiveInterval().getAssignedRegister() != no) {
            if (inEnd) {
                workingBlock.getEnds().add(
                    "move $" + value.getLiveInterval().getAssignedRegister() + ",$" + no);
            } else {
                workingBlock.getCodes().add(
                    "move $" + value.getLiveInterval().getAssignedRegister() + ",$" + no);
            }
        }
    }

    public int getAddress(Decl target, int no) {
        if (target instanceof GlobalDecl) {
            workingBlock.getCodes()
                .add("la $" + no + ",global_" + (target).getGlobalId());
            return no;
        } else if (target instanceof LocalDecl) {
            workingBlock.getCodes().add("addi $" + no + ",$sp," + target.getPlace().getOffset());
            return no;
        } else if (target instanceof InlineDecl) {
            return getValue(((InlineDecl) target).getValue(), no);
        } else {
            return getValue(target, no);
        }
    }

    public void alloc(Value value) {
        if (value.getLiveInterval().getAssignedRegister() != -1) {
            return;
        }
        if (value.getPlace().isAssigned()) {
            return;
        }
        value.getPlace().setAssigned(true);
        stackNum -= 4;
        value.getPlace().setOffset(stackNum);
    }

    public void forBlock(Block block) {
        for (Instr instr : block.getInstructions()) {
            if (instr instanceof Binary) {
                Binary binary = (Binary) instr;
                if (binary.getLiveInterval() == null) {
                    continue;
                }
                if (isDiv) {
                    if ((binary.getOp().equals("DIV") ||
                        binary.getOp().equals("MOD")) && binary.getRight() instanceof Const) {
                        int constValue = (((Const) binary.getRight()).getValue()).intValue();
                        if (((constValue & (-constValue)) == constValue) ||
                            ((constValue & (-constValue)) == -constValue)) {
                            int tmp = constValue > 0 ? constValue : -constValue;
                            int cur = 0;
                            while (tmp / 2 > 0) {
                                tmp = tmp / 2;
                                cur++;
                            }
                            int r = getValue(binary.getLeft(), 26);
                            if (r != 26) {
                                workingBlock.getCodes().add("move $26,$" + r);
                                r = 26;
                            }
                            workingBlock.getCodes().add("slt $27,$" + r + ",$0");
                            int temp = constValue > 0 ? constValue - 1 : -constValue - 1;
                            workingBlock.getCodes().add("addiu $28,$" + r + "," + temp);
                            workingBlock.getCodes().add("movn $" + r + ",$28,$27");
                            int dst = binary.getLiveInterval().getAssignedRegister();
                            if (dst == -1) {
                                dst = 27;
                            }
                            workingBlock.getCodes().add("sra $27,$" + r + "," + cur);
                            if (constValue < 0) {
                                workingBlock.getCodes().add("subu $27,$0,$27");
                            }
                            if (binary.getOp().equals("MOD")) {
                                workingBlock.getCodes().add("sll,$27,$27," + cur);
                                int ret = getValue(binary.getLeft(), 26);
                                if (ret != 26) {
                                    workingBlock.getCodes().add("move $26,$" + ret);
                                    ret = 26;
                                }
                                workingBlock.getCodes().add("subu $" + dst + ",$26" + ",$27");
                                if (constValue < 0) {
                                    workingBlock.getCodes().add("subu $" + dst + ",$0,$" + dst);
                                }
                            } else {
                                workingBlock.getCodes().add("move $" + dst + ",$27");
                            }
                            alloc(binary);
                            saveValue(binary, dst);
                            continue;
                        } else {
                            DivInfo divInfo = compute(constValue);
                            int r = getValue(binary.getLeft(), 28);
                            int dst = 27;
                            workingBlock.getCodes().add("li $26," + divInfo.getMul());
                            workingBlock.getCodes().add("mult $" + r + ",$26");
                            workingBlock.getCodes().add("mfhi $27");
                            if (constValue > 0 && divInfo.getMul() < 0) {
                                workingBlock.getCodes().add("addu $" + dst + ",$" + dst + ",$" + r);
                            }
                            if (constValue < 0 && divInfo.getMul() > 0) {
                                workingBlock.getCodes().add("subu $" + dst + ",$" + dst + ",$" + r);
                            }
                            if (divInfo.getShift() > 0) {
                                workingBlock.getCodes()
                                    .add("sra $" + dst + ",$" + dst + "," + divInfo.getShift());
                            }
                            workingBlock.getCodes().add("sra $26,$" + dst + ",31");
                            workingBlock.getCodes().add("sub $" + dst + ",$" + dst + ",$26");
                            if (binary.getOp().equals("MOD")) {
                                int temp = getValue(binary.getRight(), 26);
                                workingBlock.getCodes()
                                    .add("mul $" + dst + ",$" + dst + ",$" + temp);
                                int temp2 = getValue(binary.getLeft(), 28);
                                workingBlock.getCodes()
                                    .add("sub $" + dst + ",$" + temp2 + ",$" + dst);
                            }
                            alloc(binary);
                            saveValue(binary, 27);
                            continue;
                        }
                    }
                }
                int r1 = getValue(binary.getLeft(), 26);
                int r2 = getValue(binary.getRight(), 27);
                int dst = binary.getLiveInterval().getAssignedRegister();
                if (dst == -1) {
                    dst = 27;
                }
                switch (binary.getOp()) {
                    case "PLUS":
                        workingBlock.getCodes().add("addu $" + dst + ",$" + r1 + ",$" + r2);
                        break;
                    case "MINU":
                        workingBlock.getCodes().add("subu $" + dst + ",$" + r1 + ",$" + r2);
                        break;
                    case "MULT":
                        workingBlock.getCodes().add("mul $" + dst + ",$" + r1 + ",$" + r2);
                        break;
                    case "DIV":
                        workingBlock.getCodes().add("div $" + r1 + ",$" + r2);
                        workingBlock.getCodes().add("mflo $" + dst);
                        break;
                    case "MOD":
                        workingBlock.getCodes().add("div $" + r1 + ",$" + r2);
                        workingBlock.getCodes().add("mfhi $" + dst);
                        break;
                    case "GRE":
                        workingBlock.getCodes().add("sgt $" + dst + ",$" + r1 + ",$" + r2);
                        break;
                    case "GEQ":
                        workingBlock.getCodes().add("sge $" + dst + ",$" + r1 + ",$" + r2);
                        break;
                    case "LSS":
                        workingBlock.getCodes().add("slt $" + dst + ",$" + r1 + ",$" + r2);
                        break;
                    case "LEQ":
                        workingBlock.getCodes().add("sle $" + dst + ",$" + r1 + ",$" + r2);
                        break;
                    case "EQL":
                        workingBlock.getCodes().add("seq $" + dst + ",$" + r1 + ",$" + r2);
                        break;
                    case "NEQ":
                        workingBlock.getCodes().add("sne $" + dst + ",$" + r1 + ",$" + r2);
                        break;
                    case "SLL":
                        assert binary.getRight() instanceof Const;
                        workingBlock.getCodes().add("sll $" + dst + ",$" + r1 + "," +
                            ((Const) binary.getRight()).getValue());
                        break;
                    default:
                        System.err.println("Something wrong");
                        break;
                }
                alloc(binary);
                saveValue(binary, dst);
            } else if (instr instanceof Call) {
                // TODO: 2022/11/23
                Call call = (Call) instr;

                LinkedHashSet<Integer> set1 = workFunc.getUsedReg();
                LinkedHashSet<Integer> set2 = call.getTarget().getUsedRegDfs(new LinkedHashSet<>());
                LinkedHashSet<Integer> intercept = new LinkedHashSet<>();
                for (int i : set1) {
                    if (set2.contains(i)) {
                        intercept.add(i);
                    }
                }


                for (int j = 0; j < 4 && j < call.getParamSize(); j++) {
                    int r = getValue(call.getArgs().get(j), 4 + j);
                    if (r != 4 + j) {
                        workingBlock.getCodes().add("move $" + (4 + j) + ",$" + r);
                    }
                }
                int magic=((call.getParamSize() + 1 + intercept.size()) * -4 + stackNum);
                int start = 4;
                for (int j = 4; j < call.getParamSize(); j++) {
                    int r = getValue(call.getArgs().get(j), 26);
                    workingBlock.getCodes().add("sw $" + r + "," + (start+magic) + "($sp)");
                    start += 4;
                }
                workingBlock.getCodes()
                    .add("addi $sp,$sp," +
                        ((call.getParamSize() + 1 + intercept.size()) * -4 + stackNum));
                workingBlock.getCodes().add("sw $ra,0($sp)");
                for (int i : intercept) {
                    workingBlock.getCodes().add("sw $" + i + "," + start + "($sp)");
                    start += 4;
                }
                workingBlock.getCodes().add("jal func_" + call.getTarget().getName());

                workingBlock.getCodes().add("lw $ra,0($sp)");
                start = 4+4*(Math.max(call.getParamSize()-4,0));
                for (int i : intercept) {
                    workingBlock.getCodes().add("lw $" + i + "," + start + "($sp)");
                    start += 4;
                }
                workingBlock.getCodes()
                    .add("addi $sp,$sp," +
                        ((call.getParamSize() + 1 + intercept.size()) * 4 - stackNum));
                if (call.isReturn() && call.getLiveInterval() != null &&
                    call.getLiveInterval().getAssignedRegister() != -1) {
                    alloc(call);
                    saveValue(call, 2);
                }
            } else if (instr instanceof Ret) {
                Ret ret = (Ret) instr;
                if (ret.isReturn()) {
                    int r = getValue(ret.getValue(), 2);
                    if (r != 2) {
                        workingBlock.getCodes().add("move $2,$" + r);
                    }
                }
                workingBlock.getExits().add("j func_" + ret.getName() + "_end");
                return;
            } else if (instr instanceof Read) {
                workingBlock.getCodes().add("li $v0,5");
                workingBlock.getCodes().add("syscall");
                if (instr.getLiveInterval() == null) {
                    continue;
                }
                alloc((Read) instr);
                saveValue(instr, 2);
            } else if (instr instanceof Unary) {
                if (instr.getLiveInterval() == null) {
                    continue;
                }
                Unary unary = (Unary) instr;
                alloc(instr);
                int r = getValue(unary.getValue(), 26);
                int dst = unary.getLiveInterval().getAssignedRegister();
                if (dst == -1) {
                    dst = 27;
                }
                if (unary.getOp().equals("NOT")) {
                    workingBlock.getCodes().add("seq $" + dst + ",$" + r + ",$0");
                } else if (unary.getOp().equals("MINU")) {
                    workingBlock.getCodes().add("subu $" + dst + ",$0,$" + r);
                }
                saveValue(instr, dst);
            } else if (instr instanceof Print) {
                Print print = (Print) instr;
                int paraCount = 0;
                StringBuffer str = new StringBuffer(print.getFormat());
                StringBuilder buffer = new StringBuilder();
                for (int j = 1; j < str.length() - 1; j++) {
                    if (str.charAt(j) == '%' && str.charAt(j + 1) == 'd') {
                        if (buffer.length() != 0) {
                            globals.add("str_const" + strNum + ":.asciiz \"" + buffer + "\"");
                            workingBlock.getCodes().add("li $v0,4");
                            workingBlock.getCodes()
                                .add("la $a0,str_const" + String.valueOf(strNum));
                            strNum++;
                            workingBlock.getCodes().add("syscall");
                        }
                        buffer = new StringBuilder();
                        int r = getValue(print.getValues().get(paraCount), 4);
                        if (r != 4) {
                            workingBlock.getCodes().add("move $4,$" + r);
                        }
                        paraCount++;
                        workingBlock.getCodes().add("li $v0,1");
                        workingBlock.getCodes().add("syscall");
                        j++;
                    } else {
                        buffer.append(print.getFormat().charAt(j));
                    }
                }
                if (buffer.length() != 0) {
                    globals.add("str_const" + strNum + ":.asciiz \"" + buffer + "\"");
                    workingBlock.getCodes().add("li $v0,4");
                    workingBlock.getCodes().add("la $a0,str_const" + String.valueOf(strNum));
                    strNum++;
                    workingBlock.getCodes().add("syscall");
                }
            } else if (instr instanceof Load) {
                if (instr.getLiveInterval() == null) {
                    continue;
                }
                Load load = (Load) instr;
                alloc(load);
                int dst = load.getLiveInterval().getAssignedRegister();
                if (dst == -1) {
                    dst = 27;
                }
                if (load.getTarget().getType().equals("i32")) {
                    int r = getValue(load.getTarget(), 28);
                    workingBlock.getCodes().add("lw $" + dst + ",0($" + r + ")");
                    saveValue(load, dst);
                } else if (load.getTarget().getType().equals("i32arr1")) {
                    int raddr = getValue(load.getTarget(), 26);
                    int r = getValue(load.getOffset(), 27);
                    workingBlock.getCodes().add("add $26,$" + raddr + ",$" + r);
                    workingBlock.getCodes().add("lw $" + dst + ",0($26)");
                    saveValue(load, dst);
                } else {

                    int r = getValue(load.getTarget(), 28);
                    int offset = getValue(load.getOffset(), 26);
                    workingBlock.getCodes().add("addu $26,$" + r + ",$" + offset);
                    workingBlock.getCodes().add("lw $" + dst + ",0($26)");
                    saveValue(load, dst);
                }
            } else if (instr instanceof Save) {
                Save save = (Save) instr;
                int storeValue = getValue(save.getValue(), 26);
                if (save.getTarget().getType().equals("i32")) {
                    int r = getValue(save.getTarget(), 27);
                    workingBlock.getCodes()
                        .add("sw $" + storeValue + ",0($" + r + ")");
                } else if (save.getTarget().getType().equals("i32arr1")) {
                    int raddr = getValue(save.getTarget(), 28);
                    int r = getValue(save.getOffset(), 27);
                    workingBlock.getCodes().add("add $28,$" + raddr + ",$" + r);
                    workingBlock.getCodes().add("sw $" + storeValue + ",0($28)");
                } else {
                    int offset = getValue(save.getOffset(), 27);
                    int r = getValue(save.getTarget(), 28);
                    workingBlock.getCodes().add("addu $28,$" + offset + ",$" + r);
                    workingBlock.getCodes().add("sw $" + storeValue + ",0($28)");
                }
            } else if (instr instanceof ParaDecl) {
                ParaDecl decl = (ParaDecl) instr;
                if (decl.getLiveInterval() == null) {
                    continue;
                }
                alloc(decl);
                if (decl.getIndex() < 4) {
                    saveValue(decl, 4 + decl.getIndex());
                }
                if (decl.getIndex() >= 4) {
                    int dst = decl.getLiveInterval().getAssignedRegister();
                    if (dst == -1) {
                        dst = 27;
                    }
                    workingBlock.getCodes().add("lw $" + dst + "," + (decl.getIndex()-3) * 4 + "($sp)");
                    saveValue(decl, dst);
                }
            } else if (instr instanceof Branch) {
                Branch branch = (Branch) instr;
                // TODO: 2022/11/23  null
                if (branch.getCond() == null) {
                    if (branch.getTrueBlock() != workingBlock.getNext()) {
                        workingBlock.getExits().add("j " + branch.getTrueBlock().getLabel());
                    }
                } else {
                    int r = getValue(branch.getCond(), 26, true);
                    workingBlock.getExits()
                        .add("bnez $" + r + "," + branch.getTrueBlock().getLabel());
                    if (branch.getFalseBlock() != workingBlock.getNext()) {
                        workingBlock.getExits().add("j " + branch.getFalseBlock().getLabel());
                    }
                }
                return;
            } else if (instr instanceof LocalDecl) {
                LocalDecl decl = (LocalDecl) instr;
                if (decl.getType().equals("i32")) {
                    alloc(decl);
                    int dst = decl.getLiveInterval().getAssignedRegister();
                    if (dst == -1) {
                        dst = 26;
                    }
                    int r = getValue(decl.getValue(), dst);
                    saveValue(decl, r);
                } else if (decl.getType().equals("i32arr1")) {
                    decl.getPlace().setAssigned(true);
                    stackNum -= 4 * decl.getSize1();
                    decl.getPlace().setOffset(stackNum);
                    int raddr = getAddress(decl, 26);
                    for (int i = 0; i < decl.getSize1(); i++) {
                        int r = getValue(decl.getArray().get(0).get(i).getVal(), 27);
                        workingBlock.getCodes().add("sw $" + r + "," + i * 4 + "($" + raddr + ")");
                    }
                } else {
                    if (!decl.getPlace().isAssigned()) {
                        decl.getPlace().setAssigned(true);
                        stackNum -= 4 * decl.getSize1() * decl.getSize2();
                        decl.getPlace().setOffset(stackNum);
                    }
                    int raddr = getAddress(decl, 26);
                    for (int i = 0; i < decl.getSize1(); i++) {
                        for (int j = 0; j < decl.getSize2(); j++) {
                            int r = getValue(decl.getArray().get(i).get(j).getVal(), 27);
                            workingBlock.getCodes()
                                .add("sw $" + r + "," + (i * decl.getSize2() + j) * 4 + "($" +
                                    raddr + ")");
                        }
                    }
                }
            } else if (instr instanceof GlobalDecl) {
                if (((GlobalDecl) instr).getType().equals("i32")) {
                    continue;
                }
                forGlobalDecl((GlobalDecl) instr);
            } else if (instr instanceof IdentPointer) {
                if (instr.getLiveInterval() == null) {
                    continue;
                }
                IdentPointer pointer = (IdentPointer) instr;
                int dst = pointer.getLiveInterval().getAssignedRegister();
                if (dst == -1) {
                    dst = 26;
                }
                int r = getAddress(pointer.getTarget(), 27);
                if (pointer.getType().equals("i32arr1") && pointer.getOffset() != null) {
                    int rtemp = getValue(pointer.getOffset(), 28);
                    workingBlock.getCodes().add("addu $" + dst + ",$" + r + ",$" + rtemp);
                }
                else{
                    dst=r;
                }
                alloc(pointer);
                saveValue(pointer, dst);
            } else if (instr instanceof GetAddress) {
                int dst = instr.getLiveInterval().getAssignedRegister();
                if (dst == -1) {
                    dst = 27;
                }
                int r = getAddress(((GetAddress) instr).getTarget(), dst);
                alloc(instr);
                saveValue(instr, r);
            } else {
                System.err.println("Unknown instruction: " + instr);
            }
        }
    }

    public void gen(Function function) {
        for (Block b : function.getLinearList()) {
            workingBlock = b;
            forBlock(b);
        }
    }

    public void resolvePhi(Function function) {
        for (Block b : function.getLinearList()) {
            for (Phi phi : b.getPhis()) {
                alloc(phi);
            }
            for (Block successor : b.getFollows()) {
                for (Phi p : successor.getPhis()) {
                    for (Pair<Use, Block> v : p.getOperands()) {
                        if (v.getTail() == b) {
                            Value value = v.getHead().getVal();
                            b.addParallelCopy(new Pair<>(value, p));
                        }
                    }
                }
            }
        }
        HashMap<Block, Block> addBlocksBefore = new HashMap<>();
        HashMap<Block, Block> addBlocksAfter = new HashMap<>();
        for (Block b : function.getLinearList()) {
            workingBlock = b;
            ArrayList<Pair<Value, Value>> remain = new ArrayList<>();
            HashSet<Block> blocks = new HashSet<>();
            for (Pair<Value, Value> p : b.getParallelCopy()) {
                if (p.getTail().getLiveInterval() != null) {
                    if (p.getTail().getLiveInterval().getAssignedRegister() == -1) {
                        int r = getValue(p.getHead(), 26, true);
                        saveValue(p.getTail(), r, true);
                    }
                }
                remain.add(p);
                blocks.add(p.getTail().getFatherBlock());

            }
            assert blocks.size() <= 2;
            b.getParallelCopy().clear();
            b.getParallelCopy().addAll(remain);
            if (b.getParallelCopy().size() == 0) {
                continue;
            }
            if (b.getFollows().size() == 1) {
                resolvePhi(b);
            } else {
                Block b1 = new Block();
                Block b2 = new Block();
                Instr last = b.getLastInstr();
                assert last instanceof Branch;
                Block originB1 = ((Branch) last).getTrueBlock();
                Block originB2 = ((Branch) last).getFalseBlock();
                Branch branch1 = new Branch(originB1);
                Branch branch2 = new Branch(originB2);
                b1.addInstr(branch1);
                b2.addInstr(branch2);
                for (Pair<Value, Value> p : b.getParallelCopy()) {
                    if (p.getTail().getFatherBlock() == originB1) {
                        b1.addParallelCopy(p);
                    } else {
                        b2.addParallelCopy(p);
                    }
                }
                if (b1.getParallelCopy().size() != 0) {
                    addBlocksBefore.put(originB1, b1);
                    ((Branch) last).setTrueBlock(b1);
                    resolvePhi(b1);
                }
                if (b2.getParallelCopy().size() != 0) {
                    addBlocksAfter.put(b, b2);
                    ((Branch) last).setFalseBlock(b2);
                    resolvePhi(b2);
                }
            }
        }
        ArrayList<Block> blockList = new ArrayList<>();
        for (int i = 0; i < function.getLinearList().size(); i++) {
            if (addBlocksBefore.containsKey(function.getLinearList().get(i))) {
                blockList.add(addBlocksBefore.get(function.getLinearList().get(i)));
            }
            blockList.add(function.getLinearList().get(i));
            if (
                addBlocksAfter.containsKey(function.getLinearList().get(i))) {
                blockList.add(addBlocksAfter.get(function.getLinearList().get(i)));
            }
        }
        function.setLinearList(blockList);
        for (int i = 0; i < blockList.size() - 1; i++) {
            blockList.get(i).setNext(blockList.get(i + 1));
        }
    }

    public void resolvePhi(Block b) {
        int reg = 2;
        workingBlock = b;
        while (!b.getParallelCopy().isEmpty()) {
            HashSet<Integer> liveIn = new HashSet<>();
            for (Pair<Value, Value> p : b.getParallelCopy()) {
                if (p.getHead().getLiveInterval() != null) {
                    if (p.getHead().getLiveInterval().getAssignedRegister() != -1) {
                        assert !liveIn.contains(
                            p.getHead().getLiveInterval().getAssignedRegister());
                        liveIn.add(p.getHead().getLiveInterval().getAssignedRegister());
                    }
                }
            }
            boolean remove = false;
            ArrayList<Pair<Value, Value>> remain = new ArrayList<>();
            for (Pair<Value, Value> p : b.getParallelCopy()) {
                if (liveIn.contains(p.getTail().getLiveInterval().getAssignedRegister())) {
                    remain.add(p);
                } else {
                    remove = true;
                    if (p.getHead().getLiveInterval() != null &&
                        p.getTail().getLiveInterval() != null) {
                        if (p.getHead().getLiveInterval().getAssignedRegister() ==
                            p.getTail().getLiveInterval().getAssignedRegister() &&
                            p.getHead().getLiveInterval().getAssignedRegister() != -1) {
                            continue;
                        }
                    }
                    Instr last = b.getInstructions().get(b.getInstructions().size() - 1);
                    Value use = null;
                    if (last instanceof Branch) {
                        use = ((Branch) last).getCond();
                    }
                    if (last instanceof Ret) {
                        use = ((Ret) last).getValue();
                    }
                    if (use != null && use.getLiveInterval() != null &&
                        use.getLiveInterval().getAssignedRegister() ==
                            p.getTail().getLiveInterval().getAssignedRegister()) {
                        use.getLiveInterval().setAssignedRegister(reg);
                        reg++;
                    }
                    int r = getValue(p.getHead(), 26, true);
                    saveValue(p.getTail(), r, true);
                }
            }
            if (!remove) {
                TempValue v = new TempValue(reg);
                reg += 1;
                Pair<Value, Value> p = b.getParallelCopy().get(0);
                int r = getValue(p.getHead(), 26, true);
                saveValue(v, r, true);
                p.setHead(v);
            } else {
                b.getParallelCopy().clear();
                b.getParallelCopy().addAll(remain);
            }
        }
    }

    public void removePhi(Function function) {
//        removePhi(function.getEntrance());
        resolvePhi(function);
    }

    public void removePhi(Block block) {
        if (block.isRemoved()) {
            return;
        } else {
            block.setRemoved(true);
        }
        for (Phi phi : block.getPhis()) {
            for (Pair<Use, Block> item : phi.getOperands()) {
                workingBlock = item.getTail();
                int r = getValue(item.getHead().getVal(), 26);
                saveValue(phi, r);
            }
        }
        for (Block block1 : block.getFollows()) {
            removePhi(block1);
        }
    }

    public void print(Block block) {
        ForMipsPrint.add("\n" + block.getLabel() + ":\n");
        for (String s : block.getCodes()) {
            ForMipsPrint.add(s + "\n");
        }
        for (String s : block.getEnds()) {
            ForMipsPrint.add(s + "\n");
        }
        for (String s : block.getExits()) {
            ForMipsPrint.add(s + "\n");
        }
    }

    public void get() {
        ForMipsPrint.add(".data\n");
        for (Instr instr : program.getGlobalInit().getInstructions()) {
            forGlobalDecl(instr);
        }
        stackNum = 0;
        workFunc = program.getMain();
        removePhi(program.getMain());
        gen(program.getMain());
        program.getMain().setSp(stackNum);

        for (Function function : program.getFunctions()) {
            if (function.getInlined()) {
                continue;
            }
            stackNum = 0;
            workFunc = function;
            removePhi(function);
            gen(function);
            function.setSp(stackNum);
        }
        for (String s : globals) {
            ForMipsPrint.add(s + "\n");
        }
        ForMipsPrint.add("\n.text\n");
//        if (program.getMain().getSp() != 0) {
//            ForMipsPrint.add("addi $sp,$sp," + program.getMain().getSp() + "\n");
//        }
        for (Block block : program.getMain().getLinearList()) {
            print(block);
        }
        ForMipsPrint.add("\nfunc_main_end:\n");
        ForMipsPrint.add("li $v0,10\n");
        ForMipsPrint.add("syscall\n");
        for (Function function : program.getFunctions()) {
            if (function.getInlined()) {
                continue;
            }
            ForMipsPrint.add("func_" + function.getName() + ":\n");
//            if (function.getSp() != 0) {
//                ForMipsPrint.add("addi $sp,$sp," + function.getSp() + "\n");
//            }

            for (Block block : function.getLinearList()) {
                print(block);
            }
            ForMipsPrint.add("func_" + function.getName() + "_end:\n");
//            if (function.getSp() != 0) {
//                ForMipsPrint.add("addi $sp,$sp," + String.valueOf(-function.getSp()) + "\n");
//            }
            ForMipsPrint.add("jr $ra\n");
        }
    }

    public DivInfo compute(int param) {
        DivInfo res = new DivInfo();
        Unsigned abs = new Unsigned(param);
        //int abs = Math.abs(param);
        Unsigned exp = new Unsigned(31);
        //long exp = (long)31;
        Unsigned maxNum = new Unsigned((long) 1 << 31);
        //long maxNum = (long)1 << 31;
        Unsigned tmp = new Unsigned(maxNum.getValue() + (param < 0 ? 1 : 0));
        //long tmp = maxNum + (param < 0 ? 1 : 0);
        Unsigned testNum = new Unsigned(tmp.getValue() - 1 - tmp.getValue() % abs.getValue());
        //long testNum = tmp - 1 - tmp % abs;
        Unsigned quotient1 = new Unsigned(maxNum.getValue() / testNum.getValue());
        //long quotient1 = maxNum / testNum;
        Unsigned remainder1 = new Unsigned(maxNum.getValue() % testNum.getValue());
        //long remainder1 = maxNum % testNum;
        Unsigned quotient2 = new Unsigned(maxNum.getValue() / abs.getValue());
        //long quotient2 = maxNum / abs;
        Unsigned remainder2 = new Unsigned(maxNum.getValue() % abs.getValue());
        //long remainder2 = maxNum % abs;
        Unsigned delta = new Unsigned(0L);
        //long delta = 0L;

        do {
            exp.setValue(exp.getValue() + 1);
            //exp++;
            quotient1.setValue(quotient1.getValue() * 2);
            //quotient1 *= 2;
            remainder1.setValue(remainder1.getValue() * 2);
            //remainder1 *= 2;
            if (remainder1.getValue() >= testNum.getValue()) {
                //quotient1 += 1;
                quotient1.setValue(quotient1.getValue() + 1);
                //remainder1 -= testNum;
                remainder1.setValue(remainder1.getValue() - testNum.getValue());
            }
            quotient2.setValue(quotient2.getValue() * 2);
            //quotient2 *= 2;
            remainder2.setValue(remainder2.getValue() * 2);
            //remainder2 *= 2;
            if (remainder2.getValue() >= abs.getValue()) {
                quotient2.setValue(quotient2.getValue() + 1);
                //quotient2 += 1;
                remainder2.setValue(remainder2.getValue() - abs.getValue());
                //remainder2 -= abs;
            }
            delta.setValue(abs.getValue() - remainder2.getValue());
        } while (quotient1.getValue() < delta.getValue() ||
            (quotient1.getValue() == delta.getValue() && remainder1.getValue() == 0));

        long k = quotient2.getValue() + 1;
        res.setMul((int) k);
        if (param < 0) {
            res.setMul(-res.getMul());
        }
        res.setShift(exp.getValue() - 32);
        return res;
    }
}

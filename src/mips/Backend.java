package mips;

import generate.Mediate;
import generate.usage.Block;
import generate.usage.Const;
import generate.usage.Function;
import generate.usage.Instr;
import generate.usage.Phi;
import generate.usage.Program;
import generate.usage.Use;
import generate.usage.Value;
import generate.usage.instr.Branch;
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

public class Backend {
    private int strNum;
    private int stackNum;
    private ArrayList<String> globals;
    private ArrayList<String> blocks;
    private Program program;
    private Function workFunc;
    private Block workBlock;

    public Backend(Program program) {
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
        if (value instanceof Const) {
            workBlock.getCodes().add("li $" + no + "," + value);
        } else {
            workBlock.getCodes().add("lw $" + no + "," + value.getPlace().getOffset() + "($fp)"+"# "+value.getId());
        }
        return no;
    }

    public int getValueEnd(Value value, int no) {
        if (value instanceof Const) {
            workBlock.getEnds().add("li $" + no + "," + value);
        } else {
            workBlock.getEnds().add("lw $" + no + "," + value.getPlace().getOffset() + "($fp)");
        }
        return no;
    }

    public void saveValue(Value value, int no) {
        workBlock.getCodes().add("sw $" + no + "," + value.getPlace().getOffset() + "($fp)"+"# "+value.getId());
    }

    public int getAddress(Decl target, int no) {
        if (target instanceof GlobalDecl) {
            workBlock.getCodes()
                    .add("la $" + no + ",global_" + ((GlobalDecl) target).getGlobalId());
            return no;
        } else if (target instanceof LocalDecl) {
            workBlock.getCodes().add("addi $" + no + ",$fp," + target.getPlace().getOffset());
            return no;
        } else if (target instanceof InlineDecl) {
            return getValue(((InlineDecl) target).getValue(), no);
        } else {
            return getValue(target, no);
        }
    }

    public void alloc(Value value) {
        if (value.getPlace().isAssigned()) {
            return;
        }
        value.getPlace().setAssigned(true);
        stackNum -= 4;
        value.getPlace().setOffset(stackNum);
    }

    public void forBlock(Block block) {
//        for (Instr instr : block.getInstructions()) {
//            if (instr instanceof Binary) {
//                Binary binary = (Binary) instr;
//                getValue(binary.getLeft(), 26);
//                getValue(binary.getRight(), 27);
//                switch (binary.getOp()) {
//                    case "PLUS":
//                        workBlock.getCodes().add("addu $28,$26,$27");
//                        break;
//                    case "MINU":
//                        workBlock.getCodes().add("subu $28,$26,$27");
//                        break;
//                    case "MULT":
//                        workBlock.getCodes().add("mul $28,$26,$27");
//                        break;
//                    case "DIV":
//                        workBlock.getCodes().add("div $26,$27");
//                        workBlock.getCodes().add("mflo $28");
//                        break;
//                    case "MOD":
//                        workBlock.getCodes().add("div $26,$27");
//                        workBlock.getCodes().add("mfhi $28");
//                        break;
//                    case "GRE":
//                        workBlock.getCodes().add("sgt $28,$26,$27");
//                        break;
//                    case "GEQ":
//                        workBlock.getCodes().add("sge $28,$26,$27");
//                        break;
//                    case "LSS":
//                        workBlock.getCodes().add("slt $28,$26,$27");
//                        break;
//                    case "LEQ":
//                        workBlock.getCodes().add("sle $28,$26,$27");
//                        break;
//                    case "EQL":
//                        workBlock.getCodes().add("seq $28,$26,$27");
//                        break;
//                    case "NEQ":
//                        workBlock.getCodes().add("sne $28,$26,$27");
//                        break;
//                    default:
//                        System.err.println("Something wrong");
//                        break;
//                }
//                alloc(binary);
//                saveValue(binary, 28);
//            } else if (instr instanceof Call) {
//                // TODO: 2022/11/23
//                Call call = (Call) instr;
//                workBlock.getCodes().add("sw $ra,-4($sp)");
//                workBlock.getCodes().add("sw $fp,-8($sp)");
//                workBlock.getCodes()
//                        .add("addi $sp,$sp," + String.valueOf(-(call.getParamSize() + 2) * 4));
//                for (int j = 0; j < 4 && j < call.getParamSize(); j++) {
//                    getValue(call.getArgs().get(j), 4 + j);
//                }
//                for (int j = 4; j < call.getParamSize(); j++) {
//                    getValue(call.getArgs().get(j), 26);
//                    workBlock.getCodes().add("sw $26," + j * 4 + "($sp)");
//                }
//                workBlock.getCodes().add("jal func_" + call.getTarget().getName());
//                workBlock.getCodes().add("addi $sp,$sp," + ((call.getParamSize() + 2) * 4));
//                workBlock.getCodes().add("lw $ra,-4($sp)");
//                workBlock.getCodes().add("lw $fp,-8($sp)");
//                if (call.isReturn()) {
//                    alloc(call);
//                    saveValue(call, 2);
//                }
//            } else if (instr instanceof Ret) {
//                Ret ret = (Ret) instr;
//                if (ret.isReturn()) {
//                    getValue(ret.getValue(), 2);
//                }
//                workBlock.getExits().add("j func_" + ret.getName() + "_end");
//            } else if (instr instanceof Read) {
//                workBlock.getCodes().add("li $v0,5");
//                workBlock.getCodes().add("syscall");
//                alloc((Read) instr);
//                saveValue(instr, 2);
//            } else if (instr instanceof Unary) {
//                Unary unary = (Unary) instr;
//                alloc(instr);
//                getValue(unary.getValue(), 26);
//                if (unary.getOp().equals("NOT")) {
//                    workBlock.getCodes().add("seq $28,$26,$0");
//                } else if (unary.getOp().equals("MINU")) {
//                    workBlock.getCodes().add("subu $28,$0,$26");
//                }
//                saveValue(instr, 28);
//            } else if (instr instanceof Print) {
//                Print print = (Print) instr;
//                int paraCount = 0;
//                StringBuffer str = new StringBuffer(print.getFormat());
//                StringBuilder buffer = new StringBuilder();
//                for (int j = 1; j < str.length() - 1; j++) {
//                    if (str.charAt(j) == '%' && str.charAt(j + 1) == 'd') {
//                        if (buffer.length() != 0) {
//                            globals.add("str_const" + strNum + ":.asciiz \"" + buffer + "\"");
//                            workBlock.getCodes().add("li $v0,4");
//                            workBlock.getCodes().add("la $a0,str_const" + String.valueOf(strNum));
//                            strNum++;
//                            workBlock.getCodes().add("syscall");
//                        }
//                        buffer = new StringBuilder();
//                        getValue(print.getValues().get(paraCount), 4);
//                        paraCount++;
//                        workBlock.getCodes().add("li $v0,1");
//                        workBlock.getCodes().add("syscall");
//                        j++;
//                    } else {
//                        buffer.append(print.getFormat().charAt(j));
//                    }
//                }
//                if (buffer.length() != 0) {
//                    globals.add("str_const" + strNum + ":.asciiz \"" + buffer + "\"");
//                    workBlock.getCodes().add("li $v0,4");
//                    workBlock.getCodes().add("la $a0,str_const" + String.valueOf(strNum));
//                    strNum++;
//                    workBlock.getCodes().add("syscall");
//                }
//            } else if (instr instanceof Load) {
//                Load load = (Load) instr;
//                alloc(load);
//                if (load.getTarget().getType().equals("i32")) {
//                    workBlock.getCodes().add("lw $27,global_" + load.getTarget().getGlobalId());
//                    saveValue(load, 27);
//                } else if (load.getTarget().getType().equals("i32arr1")) {
//                    int raddr = getAddress(load.getTarget(), 26);
//                    int r = getValue(load.getPosition1(), 27);
//                    workBlock.getCodes().add("sll $27,$27,2");
//                    workBlock.getCodes().add("add $26,$26,$27");
//                    workBlock.getCodes().add("lw $27,0($26)");
//                    saveValue(load, 27);
//                } else {
//                    getValue(load.getPosition1(), 26);
//                    getValue(load.getPosition2(), 27);
//
//                    workBlock.getCodes().add("li $28," + load.getTarget().getSize2());
//                    workBlock.getCodes().add("mul $26,$26,$28");
//
//                    int r = getAddress(load.getTarget(), 28);
//
//                    workBlock.getCodes().add("addu $26,$27,$26");
//                    workBlock.getCodes().add("sll $26,$26,2");
//                    workBlock.getCodes().add("addu $26,$26,$28");
//                    workBlock.getCodes().add("lw $27,0($26)");
//                    saveValue(load, 27);
//                }
//            } else if (instr instanceof Save) {
//                Save save = (Save) instr;
//                int storeValue = getValue(save.getValue(), 26);
//                if (save.getTarget().getType().equals("i32")) {
//                    workBlock.getCodes().add("sw $26,global_" + save.getTarget().getGlobalId());
//                } else if (save.getTarget().getType().equals("i32arr1")) {
//                    int raddr = getAddress(save.getTarget(), 28);
//                    int r = getValue(save.getPosition1(), 27);
//                    workBlock.getCodes().add("sll $27,$27,2");
//                    workBlock.getCodes().add("add $28,$28,$27");
//                    workBlock.getCodes().add("sw $26,0($28)");
//                } else {
//                    int r1 = getValue(save.getPosition1(), 27);
//
//                    workBlock.getCodes().add("li $28," + save.getTarget().getSize2());
//                    workBlock.getCodes().add("mul $27,$27,$28");
//
//                    int r2 = getValue(save.getPosition2(), 28);
//                    workBlock.getCodes().add("addu $27,$27,$28");
//                    workBlock.getCodes().add("sll $27,$27,2");
//                    int r = getAddress(save.getTarget(), 28);
//                    workBlock.getCodes().add("addu $28,$27,$28");
//                    workBlock.getCodes().add("sw $26,0($28)");
//                }
//            } else if (instr instanceof ParaDecl) {
//                ParaDecl decl = (ParaDecl) instr;
//                alloc(decl);
//                if (decl.getIndex() < 4) {
//                    saveValue(decl, 4 + decl.getIndex());
//                }
//                if (decl.getIndex() >= 4) {
//                    workBlock.getCodes().add("lw $26," + decl.getIndex() * 4 + "($fp)");
//                    saveValue(decl, 26);
//                }
//            } else if (instr instanceof Branch) {
//                Branch branch = (Branch) instr;
//                // TODO: 2022/11/23  null
//                if (branch.getCond() == null) {
//                    if (branch.getTrueBlock() != workBlock.getNext()) {
//                        workBlock.getExits().add("j " + branch.getTrueBlock().getLabel());
//                    }
//                } else {
//                    int r = getValueEnd(branch.getCond(), 26);
//                    workBlock.getExits().add("bnez $26," + branch.getTrueBlock().getLabel());
//                    if (branch.getFalseBlock() != workBlock.getNext()) {
//                        workBlock.getExits().add("j " + branch.getFalseBlock().getLabel());
//                    }
//                }
//                return;
//            } else if (instr instanceof LocalDecl) {
//                LocalDecl decl = (LocalDecl) instr;
//                if (decl.getType().equals("i32")) {
//                    alloc(decl);
//                    getValue(decl.getValue(), 26);
//                    saveValue(decl, 26);
//                } else if (decl.getType().equals("i32arr1")) {
//                    decl.getPlace().setAssigned(true);
//                    stackNum -= 4 * decl.getSize1();
//                    decl.getPlace().setOffset(stackNum);
//                    getAddress(decl, 26);
//                    for (int i = 0; i < decl.getSize1(); i++) {
//                        getValue(decl.getArray().get(0).get(i).getVal(), 27);
//                        workBlock.getCodes().add("sw $27," + i * 4 + "($26)");
//                    }
//                } else {
//                    if (!decl.getPlace().isAssigned()) {
//                        decl.getPlace().setAssigned(true);
//                        stackNum -= 4 * decl.getSize1() * decl.getSize2();
//                        decl.getPlace().setOffset(stackNum);
//                    }
//                    getAddress(decl, 26);
//                    for (int i = 0; i < decl.getSize1(); i++) {
//                        for (int j = 0; j < decl.getSize2(); j++) {
//                            getValue(decl.getArray().get(i).get(j).getVal(), 27);
//                            workBlock.getCodes()
//                                    .add("sw $27," + (i * decl.getSize2() + j) * 4 + "($26)");
//                        }
//                    }
//                }
//            } else if (instr instanceof GlobalDecl) {
//                if (((GlobalDecl) instr).getType().equals("i32")) {
//                    continue;
//                }
//                forGlobalDecl((GlobalDecl) instr);
//            } else if (instr instanceof IdentPointer) {
//                IdentPointer pointer = (IdentPointer) instr;
//                getAddress(pointer.getTarget(), 28);
//                if (pointer.getType().equals("i32arr1") && pointer.getPosition() != null) {
//                        getValue(pointer.getPosition(), 27);
//                    workBlock.getCodes()
//                            .add("li $26," + ((IdentPointer) instr).getTarget().getSize2());
//                    workBlock.getCodes().add("mul $26,$27,$26");
//                    workBlock.getCodes().add("sll $26,$26,2");
//                    workBlock.getCodes().add("addu $28,$28,$26");
//                }
//                alloc(pointer);
//                saveValue(pointer, 28);
//            } else {
//                System.err.println("Unknown instruction: " + instr);
//            }
//        }
    }

    public void gen(Block block) {
        if (block.isBackGenerated()) {
            return;
        } else {
            block.setBackGenerated(true);
        }
        for (Phi phi : block.getPhis()) {
            alloc(phi);
        }
        workBlock = block;
        forBlock(block);
        for (Block block1 : block.getFollows()) {
            gen(block1);
        }
    }

    public void removePhi(Function function) {
        removePhi(function.getEntrance());
    }

    public void removePhi(Block block) {
        if (block.isRemoved()) {
            return;
        } else {
            block.setRemoved(true);
        }
        for (Phi phi : block.getPhis()) {
            for (Pair<Use, Block> item : phi.getOperands()) {
                workBlock = item.getTail();
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
        gen(program.getMain().getEntrance());
        removePhi(program.getMain());
        program.getMain().setSp(stackNum);

        for (Function function : program.getFunctions()) {
            stackNum = 0;
            workFunc = function;
            gen(function.getEntrance());
            removePhi(function);
            function.setSp(stackNum);
        }
        for (String s : globals) {
            ForMipsPrint.add(s + "\n");
        }
        ForMipsPrint.add("\n.text\n");
        ForMipsPrint.add("move $fp,$sp\n");
        if(program.getMain().getSp() != 0) {
            ForMipsPrint.add("addi $sp,$sp," + program.getMain().getSp() + "\n");
        }
        for (Block block : program.getMain().getLinearList()) {
            print(block);
        }
        ForMipsPrint.add("\nfunc_main_end:\n");
        ForMipsPrint.add("li $v0,10\n");
        ForMipsPrint.add("syscall\n");
        for (Function function : program.getFunctions()) {
            ForMipsPrint.add("func_" + function.getName() + ":\n");
            ForMipsPrint.add("move $fp,$sp\n");
            if(function.getSp() != 0) {
                ForMipsPrint.add("addi $sp,$sp," + function.getSp() + "\n");
            }
            for (Block block : function.getLinearList()) {
                print(block);
            }
            ForMipsPrint.add("func_" + function.getName() + "_end:\n");
            if(function.getSp() != 0) {
                ForMipsPrint.add("addi $sp,$sp," + String.valueOf(-function.getSp()) + "\n");
            }
            ForMipsPrint.add("jr $ra\n");
        }
    }
}

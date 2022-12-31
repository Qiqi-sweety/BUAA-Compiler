package generate;

import generate.usage.Block;
import generate.usage.BlockType;
import generate.usage.Instr;
import generate.usage.Phi;
import generate.usage.Use;
import generate.usage.instr.Branch;
import generate.usage.instr.GetAddress;
import generate.usage.instr.HashWrapper;
import generate.usage.instr.decls.InlineDecl;
import generate.usage.instr.exps.Binary;
import generate.usage.instr.exps.Call;
import generate.usage.Const;
import generate.usage.instr.decls.Decl;
import generate.usage.Function;
import generate.usage.instr.decls.GlobalDecl;
import generate.usage.instr.exps.Unary;
import generate.usage.instr.pointers.IdentPointer;
import generate.usage.instr.Load;
import generate.usage.instr.decls.LocalDecl;
import generate.usage.instr.decls.ParaDecl;
import generate.usage.instr.Print;
import generate.usage.Program;
import generate.usage.instr.Read;
import generate.usage.instr.Ret;
import generate.usage.instr.Save;
import generate.usage.Value;
import syntax.exps.BiExp;
import syntax.exps.CallExp;
import syntax.exps.LeftExp;
import syntax.exps.NumExp;
import syntax.exps.UnaryExp;
import syntax.nodes.CompUnit;
import syntax.nodes.Def;
import syntax.nodes.Exp;
import syntax.nodes.FuncDef;
import syntax.nodes.FuncFParam;
import syntax.nodes.MainFuncDef;
import syntax.nodes.RootNode;
import syntax.nodes.Stmt;
import syntax.stmts.Break;
import syntax.stmts.Continue;
import syntax.stmts.Return;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Stack;

public class Mediate {
    private final CompUnit compUnit;
    private final LinkedHashMap<String, Function> funcTable;
    private VarTable varTable;
    private ArrayList<Block> linear = new ArrayList<>();

    public static final LinkedHashMap<Decl, LinkedHashMap<Block, Value>> curDef =
        new LinkedHashMap<>();
    private static LinkedHashMap<Block, LinkedHashMap<Decl, Phi>> phis = new LinkedHashMap<>();
    private Function curFunction;
    private Block curBlock;

    private Stack<Block> whileEnd = new Stack<>();
    private Stack<Block> whileHead = new Stack<>();
    private Stack<Block> inlineEnd = new Stack<>();
    private ArrayList<Block> linerBlocks = new ArrayList<>();

    private static int curId = 0;
    private static int curBlockId = 0;
    private static int globalCount = 0;

    public Mediate(CompUnit compUnit) {
        this.compUnit = compUnit;
        this.varTable = new VarTable();
        this.funcTable = new LinkedHashMap<>();
    }

    // just copy
    public long analyse(Exp exp) {
        if (exp instanceof BiExp) {
            BiExp biExp = (BiExp) exp;
            switch (biExp.getOp()) {
                case "PLUS":
                    return analyse(biExp.getLeft()) + analyse(biExp.getRight());
                case "MINU":
                    return analyse(biExp.getLeft()) - analyse(biExp.getRight());
                case "DIV":
                    return analyse(biExp.getLeft()) / analyse(biExp.getRight());
                case "MULT":
                    return analyse(biExp.getLeft()) * analyse(biExp.getRight());
                case "MOD":
                    return analyse(biExp.getLeft()) % analyse(biExp.getRight());
                default:
                    return -99999;
            }
        }
        if (exp instanceof NumExp) {
            return ((NumExp) exp).getNum();
        }
        if (exp instanceof UnaryExp) {
            UnaryExp unaryExp = (UnaryExp) exp;
            switch (unaryExp.getOp()) {
                case "PLUS":
                    return analyse(unaryExp.getExp());
                case "MINU":
                    return -analyse(unaryExp.getExp());
                default:
                    return -99999;
            }
        } else {
            LeftExp leftExp = (LeftExp) exp;
            GlobalDecl decl = (GlobalDecl) varTable.get(leftExp.getIdent());
            switch (decl.getType()) {
                case "i32":
                    return decl.getValue();
                case "i32arr1":
                    int pos = Math.toIntExact(analyse(leftExp.getExp1()));
                    return decl.getArray().get(0).get(pos);
                case "i32arr2":
                    int pos0 = Math.toIntExact(analyse(leftExp.getExp1()));
                    int pos1 = Math.toIntExact(analyse(leftExp.getExp2()));
                    return decl.getArray().get(pos0).get(pos1);
                default:
                    return -99999;
            }
        }
    }

    public static void clearID() {
        curId = 0;
    }

    public static String createID() {
        curId++;
        return "%" + curId;
    }

    public static String createGlobalID() {
        globalCount++;
        return String.valueOf(globalCount);
    }

    public static String createBlockID() {
        return "block_" + curBlockId++;
    }

    // 全局变量需要分配空间，中端记录下变量内部全部的信息
    //ok
    public void generateGlobalVariable(Def def, boolean isConst) {
        GlobalDecl decl = new GlobalDecl(isConst, def.getIdent());
        if (def.getType() == 0) {
            if (def.expSize() != 0) {
                decl.setValue(analyse(def.getExps().get(0)));
            } else {
                decl.setValue(0L);
            }
        } else if (def.getType() == 1) {
            decl.setSize1(analyse(def.getExp1()));
            decl.addLayer();
            if (def.getExps() != null) {
                for (Exp exp : def.getExps()) {
                    decl.getArray().get(0).add(analyse(exp));
                }
                for (long i = decl.getArray().get(0).size(); i < decl.getSize1(); i++) {
                    decl.getArray().get(0).add(0L);
                }
            } else {
                for (long i = 0; i < decl.getSize1(); i++) {
                    decl.getArray().get(0).add(0L);
                }
            }
        } else {
            decl.setSize1(analyse(def.getExp1()));
            decl.setSize2(analyse(def.getExp2()));
            if (def.getExps() == null) {
                for (int i = 0; i < decl.getSize1(); i++) {
                    decl.addLayer();
                    for (int j = 0; j < decl.getSize2(); j++) {
                        decl.getArray().get(i).add(0L);
                    }
                }
            } else {
                int index = 0;
                for (int i = 0; i < decl.getSize1(); i++) {
                    decl.addLayer();
                    for (int j = 0; j < decl.getSize2(); j++) {
                        decl.getArray().get(i).add(analyse(def.getExps().get(index++)));
                    }
                }
            }
        }
        decl.setType(def.getType());
        varTable.add(def.getIdent(), decl);
        curBlock.addInstr(decl);
    }

    //ok
    public void generateLocalVariable(Def def) {
        LocalDecl local = new LocalDecl();
        local.setName(def.getIdent());
        if (def.getType() == 0) {
            Value value = new Const(0L);
            local.setType(0);
            if (def.expSize() != 0) {
                value = generateExp(def.getExps().get(0));
            }
            local.setValue(value);
            // 局部变量放在栈上，不需要分配空间，只需要记录下变量的信息
            writeVar(local, value, curBlock);
            varTable.add(def.getIdent(), local);
        } else if (def.getType() == 1) {
            local.setSize1(analyse(def.getExp1()));
            local.addLayer();
            local.setType(1);
            if (def.getExps() != null) {
                for (Exp exp : def.getExps()) {
                    local.getArray().get(0).add(link(generateExp(exp), local));
                }
                for (long i = local.getArray().get(0).size(); i < local.getSize1(); i++) {
                    local.getArray().get(0).add(link(new Const(0L), local));
                }
            } else {
                for (long i = 0; i < local.getSize1(); i++) {
                    local.getArray().get(0).add(link(new Const(0L), local));
                }
            }
            varTable.add(def.getIdent(), local);
            //对于数组而言，需要在栈上分配空间，不能像变量一样直接进行取值的替换
            curBlock.addInstr(local);
        } else {
            local.setSize1(analyse(def.getExp1()));
            local.setSize2(analyse(def.getExp2()));
            local.setType(2);
            if (def.getExps() == null) {
                for (int i = 0; i < local.getSize1(); i++) {
                    local.addLayer();
                    for (int j = 0; j < local.getSize2(); j++) {
                        local.getArray().get(i).add(link(new Const(0L), local));
                    }
                }
            } else {
                int index = 0;
                for (int i = 0; i < local.getSize1(); i++) {
                    local.addLayer();
                    for (int j = 0; j < local.getSize2(); j++) {
                        local.getArray().get(i)
                            .add(link(generateExp(def.getExps().get(index++)), local));
                    }
                }
            }
            curBlock.addInstr(local);
            varTable.add(def.getIdent(), local);
        }
    }

    //ok
    public Program generate() {
        Program program = new Program();
        // 第一个基本块，用来处理全局变量
        curBlock = new Block();
        // 第一层变量域
        varTable.addLayer();
        for (syntax.nodes.Decl decl : compUnit.getDecls()) {
            for (Def def : decl.getDefs()) {
                // 生成全局变量并添加到变量表和当前基本块
                generateGlobalVariable(def, decl.isConst());
            }
        }
        program.setGlobalInit(curBlock);
        // 第二部分，用来处理函数
        ArrayList<Function> functions = new ArrayList<>();
        for (FuncDef funcDef : compUnit.getFuncDefs()) {
            // 生成函数，添加到函数列表，每个函数重开一个基本块
            functions.add(generateFunction(funcDef));
        }
        program.setFunctions(functions);
        // 第三部分，用来处理main函数
        program.setMain(generateMain(compUnit.getMainFuncDef()));
        // 清空变量表，变量表的一层是{},与基本块的划分无关
        varTable.removeLayer();
        return program;
    }

    //ok
    public Function generateMain(MainFuncDef mainFuncDef) {
        // 重开一个基本块
        curBlock = new Block();
        Function function = new Function("main", "INTTK", curBlock, null);
        // 设置当前函数
        curFunction = function;
        linear = new ArrayList<>();
        linear.add(curBlock);
        // 遇到{，新开一层变量域
        varTable.addLayer();
        // 生成函数体,向当前基本块添加指令
        generateBlock(mainFuncDef.getBlock(), false);
        function.setLinearList(linear);


        optimize(function);

        // 遇到}，清空变量域
        varTable.removeLayer();
        return function;
    }

    private void optimize(Function function) {
        constant_fold(function);
        ADCE(function);
        CommonRemove(function);
//        RemoveTrivialPhi(function);
        //BlockRemove(function);
    }

    private void RemoveTrivialPhi(Function function) {
        for (Block b : function.getLinearList()) {
            for (Phi p : b.getPhis()) {
                ArrayList<Pair<Use, Block>> newop = new ArrayList<>();
                for (Pair<Use, Block> pair : p.getOperands()) {
                    boolean in = false;
                    for (Pair<Use, Block> temp : newop) {
                        if (temp.getHead().getVal() == pair.getHead().getVal() &&
                            temp.getTail() == pair.getTail()) {
                            in = true;
                            break;
                        }
                    }
                    if (!in) {
                        newop.add(pair);
                    }
                }
                p.getOperands().clear();
                p.getOperands().addAll(newop);
            }


        }
    }

    //ok
    public Function generateFunction(FuncDef funcDef) {
        // function结束后，需要还原回原来的block
        Block preBlock = curBlock;
        linear = new ArrayList<>();
        // function内，是一个新开的block
        curBlock = new Block();
        linear.add(curBlock);
        // 遇到{，新开一层变量域
        varTable.addLayer();

        Function function = new Function(funcDef.getIdent(), funcDef.getType(), curBlock, funcDef);

        curFunction = function;
        curFunction.addBlock(curBlock);
        funcTable.put(funcDef.getIdent(), function);

        int count = 0;
        for (FuncFParam param : funcDef.getParams()) {
            ParaDecl paraDecl = new ParaDecl(count++, param.getType(), param.getIdent());
            if (param.getType() == 2) {
                paraDecl.setSize2(analyse(param.getExp2()));
            }
            function.addPara(paraDecl);
            curBlock.addInstr(paraDecl);
            varTable.add(param.getIdent(), paraDecl);
            writeVar(paraDecl, paraDecl, curBlock);
        }

        generateBlock(funcDef.getBlock(), false);
        function.setLinearList(linear);
        for (int i = 0; i < linear.size() - 1; i++) {
            linear.get(i).setNext(linear.get(i + 1));
        }
        optimize(function);
        // function结束后，需要还原回原来的block
        curBlock = preBlock;
        // 遇到}，清空变量域
        varTable.removeLayer();
        return function;
    }

    //ok
    public void generateBlock(syntax.nodes.Block blockNode, boolean isNewLayer) {
        if (isNewLayer) {
            varTable.addLayer();
        }
        for (RootNode node : blockNode.getItems()) {
            if (node instanceof syntax.nodes.Decl) {
                generateDecl((syntax.nodes.Decl) node);
            } else if (node instanceof Stmt) {
                if(curBlock.shouldNotInsertCode()){
                    if (isNewLayer) {
                        varTable.removeLayer();
                    }
                    return;
                }
                generateStmt((Stmt) node);
                if (node instanceof Break || node instanceof Continue || node instanceof Return) {
                    if (isNewLayer) {
                        varTable.removeLayer();
                    }
                    return;
                }
            }
        }
        if (isNewLayer) {
            varTable.removeLayer();
        }
    }

    //ok
    public void generateDecl(syntax.nodes.Decl decl) {
        for (Def def : decl.getDefs()) {
            generateLocalVariable(def);
        }
    }

    public void generateStmt(Stmt stmt) {
        if (stmt == null) {
            return;
        }
        if (stmt instanceof syntax.nodes.Block) {
            generateBlock((syntax.nodes.Block) stmt, true);
        } else if (stmt instanceof syntax.stmts.If) {
            generateIf((syntax.stmts.If) stmt);
        } else if (stmt instanceof syntax.stmts.While) {
            generateWhile((syntax.stmts.While) stmt);
        } else if (stmt instanceof syntax.stmts.Return) {
            generateReturn((syntax.stmts.Return) stmt);
        } else if (stmt instanceof syntax.stmts.Assign) {
            generateAssign((syntax.stmts.Assign) stmt);
        } else if (stmt instanceof syntax.stmts.Print) {
            generatePrint((syntax.stmts.Print) stmt);
        } else if (stmt instanceof Exp) {
            generateExp((Exp) stmt);
        } else if (stmt instanceof syntax.stmts.Stdin) {
            generateStdin((syntax.stmts.Stdin) stmt);
        } else if (stmt instanceof Continue) {
            preAfterLink(curBlock, whileHead.peek());
        } else if (stmt instanceof Break) {
            preAfterLink(curBlock, whileEnd.peek());
        } else {
            System.err.println("generateStmt error");
        }
    }

    public static Value readVar(Decl name, Block block) {
        if (curDef.get(name).containsKey(block)) {
            return curDef.get(name).get(block);
        } else {
            return readRecursive(name, block);
        }
    }

    public static void writeVar(Decl name, Value value, Block block) {
        if (curDef.containsKey(name)) {
            curDef.get(name).put(block, value);
        } else {
            LinkedHashMap<Block, Value> map = new LinkedHashMap<>();
            map.put(block, value);
            curDef.put(name, map);
        }
    }

    public void generateIf(syntax.stmts.If ifStmt) {
        Block thenBlock = new Block();
        Block elseBlock = new Block();
        Block endBlock = new Block();
        Value value = generateExp(ifStmt.getCondition());
        boolean generateThen = true;
        boolean generateElse = true;
        if (ifStmt.getElseBlock() != null) {
            if (ifStmt.getIfBlock() instanceof Break) {
                thenBlock = whileEnd.peek();
                generateThen = false;
            }
            if (ifStmt.getElseBlock() instanceof Break) {
                elseBlock = whileEnd.peek();
                generateElse = false;
            }
            if (ifStmt.getIfBlock() instanceof Continue) {
                thenBlock = whileHead.peek();
                generateThen = false;
            }
            if (ifStmt.getElseBlock() instanceof Continue) {
                elseBlock = whileHead.peek();
                generateElse = false;
            }
            branch(curBlock, value, thenBlock, elseBlock);
            if (generateThen) {
                linear.add(thenBlock);
                curBlock = thenBlock;
                generateStmt(ifStmt.getIfBlock());
                preAfterLink(curBlock, endBlock);
            }
            if (generateElse) {
                linear.add(elseBlock);
                curBlock = elseBlock;
                generateStmt(ifStmt.getElseBlock());
                preAfterLink(curBlock, endBlock);
            }
            if (endBlock.getPres().size() != 0) {
                curBlock = endBlock;
                linear.add(endBlock);
            }
        } else {
            if (ifStmt.getIfBlock() instanceof Break) {
                thenBlock = whileEnd.peek();
                generateThen = false;
            }
            if (ifStmt.getIfBlock() instanceof Continue) {
                thenBlock = whileHead.peek();
                generateThen = false;
            }
            branch(curBlock, value, thenBlock, endBlock);
            if (generateThen) {
                linear.add(thenBlock);
                curBlock = thenBlock;
                generateStmt(ifStmt.getIfBlock());
                preAfterLink(curBlock, endBlock);
            }
            if (endBlock.getPres().size() != 0) {
                curBlock = endBlock;
                linear.add(endBlock);
            }
        }
    }

    public void branch(Block block, Value cond, Block thenBlock, Block elseBlock) {
        if (block.shouldNotInsertCode()) {
            return;
        }
        Branch branch = new Branch(cond, thenBlock, elseBlock);
        curBlock.addInstr(branch);
        block.addFollow(thenBlock);
        block.addFollow(elseBlock);
        thenBlock.addPre(block);
        elseBlock.addPre(block);
    }

    public void preAfterLink(Block pre, Block after) {
        if (pre.shouldNotInsertCode()) {
            return;
        }
        pre.addFollow(after);
        after.addPre(pre);
        Branch branch = new Branch(after);
        pre.addInstr(branch);
    }

    public void generateWhile(syntax.stmts.While whileStmt) {
        Block head = new Block();
        head.setSealed(false);
        head.setBlockType(BlockType.WHILEHEAD);
        Block body = new Block();
        Block end = new Block();
        preAfterLink(curBlock, head);
        linear.add(head);
        curBlock = head;
        head.setWhileHead(true);
        head.setWhileEndBlock(end);
        Value value = generateExp(whileStmt.getCondition());
        branch(curBlock, value, body, end);
        linear.add(body);
        curBlock = body;
        whileHead.add(head);
        whileEnd.add(end);
        generateStmt(whileStmt.getStmt());
        whileEnd.pop();
        whileHead.pop();
        if (curBlock != null) {
            preAfterLink(curBlock, head);
        }
        sealForPhi(head);
        linear.add(end);
        curBlock = end;
    }

    //ok
    public void generateReturn(syntax.stmts.Return returnStmt) {
        Ret ret;
        if (returnStmt.isReturn()) {
            Value value = generateExp(returnStmt.getExp());
            if (!inlineEnd.empty()) {
                inlineEnd.peek().getPhis().get(0).addOperand(value, curBlock);
                preAfterLink(curBlock, inlineEnd.peek());
                return;
            }
            ret = new Ret(value, returnStmt.isReturn());

        } else {
            if (!inlineEnd.empty()) {
                preAfterLink(curBlock, inlineEnd.peek());
                return;
            }
            ret = new Ret(null, returnStmt.isReturn());
        }
        curBlock.addInstr(ret);
        ret.setName(curFunction.getName());
        ret.setFatherBlock(curBlock);
    }

    //ok
    public void generatePrint(syntax.stmts.Print node) {
        Print print = new Print();
        for (Exp exp : node.getExps()) {
            Value value = generateExp(exp);
            print.addValue(value);
        }
        print.setFormat(node.getString());
        curBlock.addInstr(print);
        print.setFatherBlock(curBlock);
    }

    public void generateAssign(syntax.stmts.Assign node) {
        Value value = generateExp(node.getRightExp());
        writeLvalue(value, node.getLeftExp());
    }

    public void writeLvalue(Value value, LeftExp left) {
        Decl tmp = varTable.get(left.getIdent());
        if (tmp.getType().equals("i32")) {
            if (tmp instanceof GlobalDecl) {
                GetAddress getAddress = new GetAddress(tmp);
                Save save = new Save(getAddress);
                curBlock.addInstr(getAddress);
                save.setValue(value);
                save.setFatherBlock(curBlock);
                curBlock.addInstr(save);
            } else {
                writeVar(tmp, value, curBlock);
            }
        } else if (tmp.getType().equals("i32arr1")) {
            Value pos = generateExp(left.getExp1());
            GetAddress getAddress = new GetAddress(tmp);
            Save save = new Save(getAddress);
            curBlock.addInstr(getAddress);
            Instr offset = new Binary("SLL", pos, new Const(2L));
            curBlock.addInstr(offset);
            save.setOffset(offset);
            save.setFatherBlock(curBlock);
            save.setValue(value);
            curBlock.addInstr(save);
        } else {
            Value pos1 = generateExp(left.getExp1());
            Value pos2 = generateExp(left.getExp2());
            GetAddress getAddress = new GetAddress(tmp);
            curBlock.addInstr(getAddress);
            Save save = new Save(getAddress);
            Instr temp1 = new Binary("MULT", pos1, new Const(tmp.getSize2()));
            curBlock.addInstr(temp1);
            Instr temp2 = new Binary("PLUS", temp1, pos2);
            curBlock.addInstr(temp2);
            Instr offset = new Binary("SLL", temp2, new Const(2L));
            curBlock.addInstr(offset);
            save.setOffset(offset);
            save.setValue(value);
            save.setFatherBlock(curBlock);
            curBlock.addInstr(save);
        }
    }

    public void generateStdin(syntax.stmts.Stdin node) {
        Read value = new Read();
        value.setFatherBlock(curBlock);
        curBlock.addInstr(value);
        writeLvalue(value, node.getLeftExp());
    }

    public Binary generateBiExp(BiExp biExp) {
        Value left = generateExp(biExp.getLeft());
        Value right = generateExp(biExp.getRight());
        Binary binary = new Binary(biExp.getOp(), left, right);
        binary.setFatherBlock(curBlock);
        curBlock.addInstr(binary);
        return binary;
    }

    public Value generateUnaryExp(UnaryExp unaryExp) {
        if (unaryExp.getOp().equals("PLUS")) {
            return generateExp(unaryExp.getExp());
        } else {
            Unary unary =
                new Unary(unaryExp.getOp());
            Value value = generateExp(unaryExp.getExp());
            unary.setValue(value);
            curBlock.addInstr(unary);
            return unary;
        }
    }

    public Value generateCallExp(syntax.exps.CallExp callExp) {
        if (funcTable.get(callExp.getIdent()).equals(curFunction)) {
            curFunction.setInlined(false);
        }
        if (funcTable.get(callExp.getIdent()).getInlined()) {
            return generateInlineCallExp(callExp);
        }
        curFunction.addCalled(funcTable.get(callExp.getIdent()));
        Call call = new Call(funcTable.get(callExp.getIdent()));
        for (Exp para : callExp.getParas()) {
            Value value = generateExp(para);
            call.addArg(value);
        }
        call.setFatherBlock(curBlock);
        curBlock.addInstr(call);
        return call;
    }

    private Value generateInlineCallExp(CallExp callExp) {
        Function function = funcTable.get(callExp.getIdent());
        ArrayList<ParaDecl> paras = function.getParaList();
        ArrayList<Exp> args = callExp.getParas();
        ArrayList<Value> values = new ArrayList<>();
        for (int i = 0; i < paras.size(); i++) {
            values.add(generateExp(args.get(i)));
        }
        VarTable backup = varTable.clone();
        varTable.removeToDepth(1);
        varTable.addLayer();
        for (int i = 0; i < paras.size(); i++) {
            InlineDecl decl = new InlineDecl(values.get(i), paras.get(i));
            varTable.add(paras.get(i).getName(), decl);
            writeVar(decl, values.get(i), curBlock);
        }
        Block inlineEndBlock = new Block();
        Phi phi = null;
        if (!function.getReturnType().equals("void")) {
            phi = new Phi(inlineEndBlock);
            inlineEndBlock.getPhis().add(phi);
        }
        inlineEnd.push(inlineEndBlock);
        generateBlock(function.getOrigin().getBlock(), false);
        varTable.removeLayer();
        varTable = backup;
        inlineEnd.pop();
        curBlock = inlineEndBlock;
        curFunction.addBlock(curBlock);
        linear.add(curBlock);
        if (!function.getReturnType().equals("void")) {
            assert phi != null;
            return tryRemoveTrivialPhi(phi);
        } else {
            return null;
        }
    }

    public Value generateLeft(LeftExp leftExp) {
        Decl decl = varTable.get(leftExp.getIdent());
        if (decl.getType().equals("i32")) {
            if (decl instanceof GlobalDecl) {
                if (((GlobalDecl) decl).isConst()) {
                    return new Const(((GlobalDecl) decl).getValue());
                } else {
                    GetAddress getAddress = new GetAddress(decl);
                    Load load = new Load(getAddress);
                    curBlock.addInstr(getAddress);
                    load.setFatherBlock(curBlock);
                    curBlock.addInstr(load);
                    return load;
                }
            } else {
                return readVar(decl, curBlock);
            }
        } else if (decl.getType().equals("i32arr1")) {
            if (leftExp.getOffset() == 0) {
                IdentPointer pointer = new IdentPointer(decl, "i32arr1");
                //decl.addUse(pointer);
                pointer.setFatherBlock(curBlock);
                curBlock.addInstr(pointer);
                return pointer;
            } else {
                Value pos = generateExp(leftExp.getExp1());
                GetAddress getAddress = new GetAddress(decl);
                Load load = new Load(getAddress);
                curBlock.addInstr(getAddress);
                Instr offset = new Binary("SLL", pos, new Const(2L));
                curBlock.addInstr(offset);
                load.setOffset(offset);
                //pos.addUse(load);
                load.setFatherBlock(curBlock);
                curBlock.addInstr(load);
                return load;
            }
        } else {
            if (leftExp.getOffset() == 0) {
                IdentPointer pointer = new IdentPointer(decl, "i32arr2");
                //decl.addUse(pointer);
                pointer.setFatherBlock(curBlock);
                curBlock.addInstr(pointer);
                return pointer;
            } else if (leftExp.getOffset() == 1) {
                Value pos = generateExp(leftExp.getExp1());
                //pos.addUse(load);
                IdentPointer pointer = new IdentPointer(decl, "i32arr1");
                Instr temp = new Binary("MULT", pos, new Const(decl.getSize2()));
                curBlock.addInstr(temp);
                Instr offset = new Binary("SLL", temp, new Const(2L));
                curBlock.addInstr(offset);
                pointer.setOffset(offset);
                //decl.addUse(pointer);
                pointer.setFatherBlock(curBlock);
                curBlock.addInstr(pointer);
                return pointer;
            } else {
                Value value1 = generateExp(leftExp.getExp1());
                Value value2 = generateExp(leftExp.getExp2());
                GetAddress getAddress = new GetAddress(decl);
                curBlock.addInstr(getAddress);
                Load load = new Load(getAddress);
                Instr temp1 = new Binary("MULT", value1, new Const(decl.getSize2()));
                curBlock.addInstr(temp1);
                Instr temp2 = new Binary("PLUS", temp1, value2);
                curBlock.addInstr(temp2);
                Instr offset = new Binary("SLL", temp2, new Const(2L));
                curBlock.addInstr(offset);
                load.setOffset(offset);
                //decl.addUse(load);
                load.setFatherBlock(curBlock);
                curBlock.addInstr(load);
                return load;
            }
        }
    }

    public Value generateCondExp(BiExp exp) {
        Block elseBlock = new Block();
        Block resultBlock = new Block();
        if (exp.getOp().equals("AND")) {
            Value left = generateExp(exp.getLeft());
            Block leftBlock = curBlock;
            branch(curBlock, left, elseBlock, resultBlock);
            linear.add(elseBlock);
            curBlock = elseBlock;
            Value right = generateExp(exp.getRight());
            preAfterLink(curBlock, resultBlock);
            Phi phi = new Phi(resultBlock);
            phi.addOperand(new Const(0L), leftBlock);
            phi.addOperand(right, curBlock);
            resultBlock.addPhi(phi);
            linear.add(resultBlock);
            curBlock = resultBlock;
            return phi;
        } else if (exp.getOp().equals("OR")) {
            Value left = generateExp(exp.getLeft());
            Block leftBlock = curBlock;
            branch(curBlock, left, resultBlock, elseBlock);
            linear.add(elseBlock);
            curBlock = elseBlock;
            Value right = generateExp(exp.getRight());
            preAfterLink(curBlock, resultBlock);
            Phi phi = new Phi(resultBlock);
            phi.addOperand(new Const(1L), leftBlock);
            phi.addOperand(right, curBlock);
            resultBlock.addPhi(phi);
            linear.add(resultBlock);
            curBlock = resultBlock;
            return phi;
        } else {
            System.out.println("error");
            return null;
        }
    }

    public static Value readRecursive(Decl name, Block block) {
        Value value = null;
        if (!block.isSealed()) {
            Phi phi = new Phi(block);
            block.addPhi(phi);
            if (phis.containsKey(block)) {
                phis.get(block).put(name, phi);
            } else {
                LinkedHashMap<Decl, Phi> tmp = new LinkedHashMap<>();
                tmp.put(name, phi);
                phis.put(block, tmp);
            }
            value = phi;
        } else if (block.getPres().size() == 1) {
            value = readVar(name, block.getPres().get(0));
        } else if (block.getPres().size() > 1) {
            Phi phi = new Phi(block);
            block.addPhi(phi);
            writeVar(name, phi, block);
            value = phi.addPhi(name);
        }
        writeVar(name, value, block);
        return value;
    }

    public void sealForPhi(Block block) {
        if (!phis.containsKey(block)) {
            block.setSealed(true);
            return;
        }
        LinkedHashMap<Decl, Phi> map = phis.get(block);
        for (Decl decl : map.keySet()) {
            map.get(decl).addPhi(decl);
        }
        block.setSealed(true);
    }

    //ok
    public Value generateExp(Exp exp) {
        if (exp instanceof BiExp) {
            if (((BiExp) exp).getOp().equals("AND") || ((BiExp) exp).getOp().equals("OR")) {
                return generateCondExp((BiExp) exp);
            } else {
                return generateBiExp((BiExp) exp);
            }
        } else if (exp instanceof UnaryExp) {
            return generateUnaryExp((UnaryExp) exp);
        } else if (exp instanceof NumExp) {
            return new Const(((NumExp) exp).getNum());
        } else if (exp instanceof syntax.exps.CallExp) {
            return generateCallExp((syntax.exps.CallExp) exp);
        } else { // (exp instanceof LeftExp)
            return generateLeft((LeftExp) exp);
        }
    }

    //ok
    public static String idOrConst(Value value) {
        if (value instanceof Const) {
            return String.valueOf(((Const) value).getValue());
        } else {
            return value.getId();
        }
    }

    public static Use link(Value v, Instr u) {

        Use use = new Use(v, u);
        if (v != null) {
            v.addUse(use);
        }
        u.addOperand(use);
        return use;
    }

    public static Value tryRemoveTrivialPhi(Phi phi) {
        Value same = null;
        for (Pair<Use, Block> item : phi.getOperands()) {
            if (item.getHead().getVal() == same || item.getHead().getVal() == phi) {
                continue;
            }
            if (same != null) {
                return phi;
            }
            same = item.getHead().getVal();
        }
        ArrayList<Phi> others = new ArrayList<>();
        for (Use i : phi.getUses()) {
            if (i.getUser() instanceof Phi && i.getUser() != phi) {
                others.add((Phi) i.getUser());
            }
        }
        phi.replaceAllUseWith(same);
        phi.getBelongBlock().getPhis().remove(phi);
        for (Phi p : others) {
            if (p == same) {
                same = tryRemoveTrivialPhi(p);
            } else {
                tryRemoveTrivialPhi(p);
            }
        }
        return same;
    }

    private void constant_fold(Function func) {
        boolean change = true;
        Instr toRemove = null;
        while (change) {
            change = false;
            for (Block BB : func.getLinearList()) {
                for (Instr ir : BB.getInstructions()) {
                    if (ir instanceof Unary) {
                        Unary unary = (Unary) ir;
                        if (unary.getOp().equals("MINU")) {
                            if (unary.getValue() instanceof Const) {
                                Const c = (Const) unary.getValue();
                                unary.replaceAllUseWith(new Const(-c.getValue()));
                                toRemove = ir;
                                change = true;
                                break;
                            }
                        } else if (unary.getOp().equals("NOT")) {
                            if (unary.getValue() instanceof Const) {
                                Const c = (Const) unary.getValue();
                                long r = c.getValue() == 0 ? 1 : 0;
                                unary.replaceAllUseWith(new Const(r));
                                toRemove = ir;
                                change = true;
                            }
                        }
                    } else if (ir instanceof Binary) {
                        Binary p = (Binary) (ir);
                        if (p.getLeft() instanceof Const && p.getRight() instanceof Const) {
                            long r1 = ((Const) p.getLeft()).getValue();
                            long r2 = ((Const) p.getRight()).getValue();
                            toRemove = p;
                            change = true;
                            switch (p.getOp()) {
                                case "PLUS":
                                    p.replaceAllUseWith(new Const(r1 + r2));
                                    break;
                                case "MINU":
                                    p.replaceAllUseWith(new Const(r1 - r2));
                                    break;
                                case "MULT":
                                    p.replaceAllUseWith(new Const(r1 * r2));
                                    break;
                                case "DIV":
                                    p.replaceAllUseWith(new Const(r1 / r2));
                                    break;
                                case "MOD":
                                    p.replaceAllUseWith(new Const(r1 % r2));
                                    break;
                                case "GRE":
                                    long r = r1 > r2 ? 1 : 0;
                                    p.replaceAllUseWith(new Const(r));
                                    break;
                                case "GEQ":
                                    r = r1 >= r2 ? 1 : 0;
                                    p.replaceAllUseWith(new Const(r));
                                    break;
                                case "LSS":
                                    r = r1 < r2 ? 1 : 0;
                                    p.replaceAllUseWith(new Const(r));
                                    break;
                                case "LEQ":
                                    r = r1 <= r2 ? 1 : 0;
                                    p.replaceAllUseWith(new Const(r));
                                    break;
                                case "EQL":
                                    r = r1 == r2 ? 1 : 0;
                                    p.replaceAllUseWith(new Const(r));
                                    break;
                                case "NEQ":
                                    r = r1 != r2 ? 1 : 0;
                                    p.replaceAllUseWith(new Const(r));
                                    break;
                                case "SLL":
                                    p.replaceAllUseWith(new Const(r1 << r2));
                                    break;
                                default:
                                    change = false;
                                    break;
                            }
                            if (change) {
                                break;
                            }
                        }
                    }
                }
                if (toRemove != null) {
                    BB.getInstructions().remove(toRemove);
                    toRemove = null;
                }
                if (change) {
                    break;
                }
            }
        }
    }

    private void markReach(Value use) {
        use.setReachable(true);
        for (Use i : use.getOperand()) {
            if (i.getVal() != null && !i.getVal().isReachable()) {
                markReach(i.getVal());
            }
        }
    }

    private void ADCE(Function func) {
        for (Block block : func.getLinearList()) {
            for (Phi phi : block.getPhis()) {
                phi.setReachable(false);
            }
            for (Instr i : block.getInstructions()) {
                i.setReachable(false);
            }
        }
        for (Block block : func.getLinearList()) {
            for (Instr i : block.getInstructions()) {
                if (i instanceof Print || i instanceof Ret || i instanceof Read ||
                    i instanceof Branch ||
                    i instanceof Save ||
                    i instanceof Call) {
                    markReach(i);
                }
            }
        }
        removeUnreachableInstr(func);
    }

    public void CommonRemove(Function func) {
        for (Block block : func.getLinearList()) {
            LinkedHashMap<HashWrapper, Value> exprs = new LinkedHashMap<>();
            boolean change = true;
            while (change) {
                change = false;
                exprs.clear();
                for (Instr i : block.getInstructions()) {
                    HashWrapper binary = new HashWrapper(i);
                    if (exprs.containsKey(binary) && i.isReachable()) {
                        i.replaceAllUseWith(exprs.get(binary));
                        i.setReachable(false);
                        change = true;
                    } else {
                        exprs.put(binary, i);
                    }
                }
            }
        }
        removeUnreachableInstr(func);
    }

    private void removeUnreachableInstr(Function func) {
        for (Block block : func.getLinearList()) {
            ArrayList<Phi> newPhis = new ArrayList<>();
            for (Phi phi : block.getPhis()) {
                if (phi.isReachable()) {
                    newPhis.add(phi);
                }
            }
            block.setPhis(newPhis);
            ArrayList<Instr> newInstrs = new ArrayList<>();
            for (Instr i : block.getInstructions()) {
                if (i.isReachable()) {
                    newInstrs.add(i);
                }
            }
            block.setInstructions(newInstrs);
        }
    }

    private Block getTrueAfter(Block after) {
        if (after.getInstructions().size() > 1) {
            return after;
        }
        if (after.getInstructions().get(0) instanceof Branch) {
            Branch b = (Branch) after.getInstructions().get(0);
            if (b.getCond() == null) {
                return getTrueAfter(b.getTrueBlock());
            }
            if (b.getCond() instanceof Const) {
                if (((Const) b.getCond()).getValue() != 0) {
                    return getTrueAfter(b.getTrueBlock());
                }
                return b.getFalseBlock();
            }
        }
        return after;
    }

    public void BlockRemove(Block block, HashSet<Block> removed) {
        if (removed.contains(block)) {
            return;
        }
        removed.add(block);
        Instr instr = block.getInstructions().get(block.getInstructions().size() - 1);
        if (instr instanceof Branch) {
            Branch b = (Branch) instr;
            if (b.getCond() instanceof Const) {
                if (((Const) b.getCond()).getValue() != 0) {
                    b.setCond(null);
                    b.setFalseBlock(null);
                }
                b.setCond(null);
//                b.setTrueBlock();
            }
        } else {
            assert instr instanceof Ret;
        }
    }

    private void BlockRemove(Function function) {
        HashSet<Block> set = new HashSet<>();
        BlockRemove(function.getEntrance(), set);
    }
}
package syntax;

import syntax.stmts.Assign;
import syntax.exps.BiExp;
import syntax.exps.CallExp;
import syntax.stmts.Break;
import syntax.stmts.Continue;
import syntax.stmts.If;
import syntax.exps.LeftExp;
import syntax.exps.NumExp;
import syntax.stmts.Print;
import syntax.stmts.Return;
import syntax.nodes.RootNode;
import syntax.stmts.Stdin;
import syntax.nodes.Stmt;
import syntax.stmts.While;
import syntax.nodes.Block;
import syntax.nodes.CompUnit;
import syntax.nodes.Decl;
import syntax.nodes.Def;
import syntax.nodes.Exp;
import syntax.nodes.FuncDef;
import syntax.nodes.FuncFParam;
import syntax.nodes.MainFuncDef;
import syntax.exps.UnaryExp;
import utils.Error;
import utils.FuncTable;
import utils.Func;
import utils.Pair;
import utils.Sym;
import utils.SymTable;

import java.math.BigInteger;
import java.util.ArrayList;

public class SyntaxAnalysis {
    private final ArrayList<Pair<Pair<String, String>, Integer>> items;
    private final ArrayList<Pair<String, String>> input;
    private final ArrayList<Integer> lines;
    private final ArrayList<String> processedData;
    private int nowIndex;
    private boolean globalCond;
    private boolean curIsVoid;
    private int curLoop;
    private int curFuncLine;
    private int curPrint;
    private CompUnit compUnit = null;

    public SyntaxAnalysis(
            ArrayList<Pair<Pair<String, String>, Integer>> items) {
        this.items = items;
        this.input = new ArrayList<>();
        this.lines = new ArrayList<>();
        this.curLoop = 0;
        this.curFuncLine = 0;
        this.curPrint = 0;
        getInput();
        globalCond = false;
        this.nowIndex = 0;
        this.curIsVoid = false;
        this.processedData = new ArrayList<>();
    }

    public ArrayList<String> getProcessedData() {
        return processedData;
    }

    private void getInput() {
        for (Pair<Pair<String, String>, Integer> item : items) {
            input.add(item.getHead());
            lines.add(item.getTail());
        }
    }

    public void start() {
        nowIndex = 0;
        compUnit = getCompUnit();
    }

    public CompUnit ForCompUnit() {
        return compUnit;
    }

    public void nextK(int k) {
        for (int i = 0; i < k; i++) {
            String s = input.get(nowIndex).getHead() + " " + input.get(nowIndex).getTail() + "\n";
            processedData.add(s);
            nowIndex++;
        }
    }

    public String tokenK(int k) {
        return input.get(nowIndex + k).getHead();
    }

    public CompUnit getCompUnit() {
        ArrayList<Decl> decls = new ArrayList<>();
        ArrayList<FuncDef> funcDefs = new ArrayList<>();
        SymTable.addLayer();
        // for decls
        while (!tokenK(2).equals("LPARENT")) {
            decls.add(getDecl());
        }
        // for funcs
        while (!tokenK(1).equals("MAINTK")) {
            funcDefs.add(getFuncDef());
        }
        // for main
        MainFuncDef mainFuncDef = getMainFuncDef();
        processedData.add("<CompUnit>" + "\n");
        return new CompUnit(decls, funcDefs, mainFuncDef);
    }

    public FuncDef getFuncDef() {
        //need to print functype first, since used once, delete type
        final String funcType = tokenK(0);
        nextK(1);
        processedData.add("<FuncType>" + "\n");
        //for paras
        final String ident = input.get(nowIndex).getTail();
        boolean isErrB = false;
        if (FuncTable.isErrB(ident) || SymTable.isErrB(ident)) {
            isErrB = true;
            Error.addMsg(lines.get(nowIndex - 1).toString(), "b");
        }
        nextK(2);
        SymTable.addLayer();
        ArrayList<FuncFParam> funcFParams = new ArrayList<>();
        if (tokenK(0).equals("INTTK")) {
            funcFParams = getFuncFParams();
        }
        if (!tokenK(0).equals("RPARENT")) {
            Error.addMsg(lines.get(nowIndex - 1).toString(), "j");
        } else {
            nextK(1);
        }
        if (!isErrB) {
            FuncTable.addItem(ident, new Func(ident, funcType.equals("VOIDTK"), funcFParams));
        }
        //for block
        curIsVoid = funcType.equals("VOIDTK");
        Block block = getBlock(true, false);
        SymTable.dropLayer();
        if (funcType.equals("INTTK") && block.isErrG()) {
            Error.addMsg(lines.get(nowIndex - 1).toString(), "g");
        }
        if(funcType.equals("VOIDTK")){
            if(block.getItems().size()==0||(!(block.getItems().get(block.getItems().size()-1) instanceof  Return))){
                block.getItems().add(new Return(null,0));
            }
        }
        processedData.add("<FuncDef>" + "\n");
        return new FuncDef(funcType, ident, funcFParams, block);
    }

    public MainFuncDef getMainFuncDef() {
        nextK(3);
        if (!tokenK(0).equals("RPARENT")) {
            Error.addMsg(lines.get(nowIndex - 1).toString(), "j");
        } else {
            nextK(1);
        }
        SymTable.addLayer();
        curIsVoid = false;
        Block block = getBlock(false, false);
        SymTable.dropLayer();
        if (block.isErrG()) {
            Error.addMsg(lines.get(nowIndex - 1).toString(), "g");
        }
        MainFuncDef mainFuncDef = new MainFuncDef(block);
        processedData.add("<MainFuncDef>" + "\n");
        return mainFuncDef;
    }

    public Decl getDecl() { //constDecl & varDecl
        boolean isConst = false;
        //judge whether const
        if (tokenK(0).equals("CONSTTK")) {
            nextK(1);
            isConst = true;
        }
        nextK(1);
        //at least one def
        ArrayList<Def> defs = new ArrayList<>();
        Def def = getDef(isConst);
        defs.add(def);
        //if more
        while (tokenK(0).equals("COMMA")) {
            nextK(1);
            Def tmp = getDef(isConst);
            defs.add(tmp);
        }
        if (!tokenK(0).equals("SEMICN")) {
            Error.addMsg(lines.get(nowIndex - 1).toString(), "i");
        } else {
            nextK(1);
        }
        if (isConst) {
            processedData.add("<ConstDecl>" + "\n");
        } else {
            processedData.add("<VarDecl>" + "\n");
        }
        return new Decl(defs, isConst);
    }

    public Block getBlock(boolean isFunc, boolean isLoop) {
        if (!isFunc) {
            SymTable.addLayer();
        }
        nextK(1);
        ArrayList<RootNode> nodes = new ArrayList<>();
        //judge for stmts and decls
        while (true) {
            if (tokenK(0).equals("RBRACE")) {
                nextK(1);
                if (!isFunc) {
                    SymTable.dropLayer();
                }
                processedData.add("<Block>" + "\n");
                return new Block(nodes);
            } else if (tokenK(0).equals("INTTK") || tokenK(0).equals("CONSTTK")) {
                Decl decl = getDecl();
                nodes.add(decl);
            } else {
                Stmt stmt = getStmt();
                nodes.add(stmt);
            }
        }
    }

    public Def getDef(boolean isConst) { // constDef & varDef
        final String ident = input.get(nowIndex).getTail();
        Exp exp1 = null;
        Exp exp2 = null;
        nextK(1);
        // whether a[k] or a[k1][k2]
        int dim = 0;
        if (tokenK(0).equals("LBRACK")) {
            dim = 1;
            nextK(1);
            exp1 = getExp(true);
            if (!tokenK(0).equals("RBRACK")) {
                Error.addMsg(lines.get(nowIndex - 1).toString(), "k");
            } else {
                nextK(1);
            }
            if (tokenK(0).equals("LBRACK")) {
                dim = 2;
                nextK(1);
                exp2 = getExp(true);
                if (!tokenK(0).equals("RBRACK")) {
                    Error.addMsg(lines.get(nowIndex - 1).toString(), "k");
                } else {
                    nextK(1);
                }
            }
        }
        if (SymTable.isErrB(ident)) {
            Error.addMsg(lines.get(nowIndex - 1).toString(), "b");
        } else {
            SymTable.addItem(ident, new Sym(isConst, dim));
        }
        ArrayList<Exp> exps = null;
        if (tokenK(0).equals("ASSIGN")) {
            nextK(1);
            exps = getInitVal(isConst);
        }
        if (isConst) {
            processedData.add("<ConstDef>" + "\n");
        } else {
            processedData.add("<VarDef>" + "\n");
        }
        return new Def(ident, exp1, exp2, exps, isConst);
    }

    public ArrayList<Exp> getInitVal(boolean isConst) { //constInitVal & varInitVal
        ArrayList<Exp> exps = new ArrayList<>();
        if (tokenK(0).equals("LBRACE")) {
            // 多维数组初值
            nextK(1);
            exps.addAll(getInitVal(isConst));
            while (tokenK(0).equals("COMMA")) {
                nextK(1);
                exps.addAll(getInitVal(isConst));
            }
            nextK(1);
        } else {
            // 表达式初值
            Exp exp = getExp(isConst);
            exps.add(exp);
        }
        if (isConst) {
            processedData.add("<ConstInitVal>" + "\n");
        } else {
            processedData.add("<InitVal>" + "\n");
        }
        return exps;
    }

    public Exp getExp(boolean isConst) { // need to be careful of the isConst
        Exp exp = getAddExp();
        if (isConst) {
            processedData.add("<ConstExp>" + "\n");
        } else {
            processedData.add("<Exp>" + "\n");
        }
        return exp;
    }

    public Exp getCond() {
        globalCond = true;
        Exp exp = getLOrExp();
        globalCond = false;
        processedData.add("<Cond>" + "\n");
        return exp;
    }

    public Exp getPrimaryExp() { // no constExp
        Exp exp = null;
        switch (tokenK(0)) {
            //(exp)
            case "LPARENT":
                nextK(1);
                exp = getExp(false);
                if (!tokenK(0).equals("RPARENT")) {
                    Error.addMsg(lines.get(nowIndex - 1).toString(), "j");
                } else {
                    nextK(1);
                }
                break;
            //LVal
            case "IDENFR":
                exp = getLVal();
                break;
            //Number
            case "INTCON":
                exp = getNumber();
                break;
            default:
                break;
        }
        processedData.add("<PrimaryExp>" + "\n");
        return exp;
    }

    public NumExp getNumber() {
        Long num = new Long(input.get(nowIndex).getTail());
        nextK(1);
        processedData.add("<Number>" + "\n");
        return new NumExp(num);
    }

    public LeftExp getLVal() {
        final String ident = input.get(nowIndex).getTail();
        int line = lines.get(nowIndex);
        if (SymTable.isErrC(ident)) {
            Error.addMsg(lines.get(nowIndex).toString(), "c");
        }
        nextK(1);
        Exp exp1 = null;
        Exp exp2 = null;
        // 一维数组与二维数组
        if (tokenK(0).equals("LBRACK")) {
            nextK(1);
            exp1 = getExp(false);
            if (!tokenK(0).equals("RBRACK")) {
                Error.addMsg(lines.get(nowIndex - 1).toString(), "k");
            } else {
                nextK(1);
            }
            if (tokenK(0).equals("LBRACK")) {
                nextK(1);
                exp2 = getExp(false);
                if (!tokenK(0).equals("RBRACK")) {
                    Error.addMsg(lines.get(nowIndex - 1).toString(), "k");
                } else {
                    nextK(1);
                }
            }
        }
        if (SymTable.isErrH(ident) && tokenK(0).equals("ASSIGN")) {
            Error.addMsg(String.valueOf(line), "h");
        }
        processedData.add("<LVal>" + "\n");
        return new LeftExp(ident, exp1, exp2);
    }

    public Exp getUnaryExp() {
        Exp exp;
        if (tokenK(0).equals("IDENFR") && tokenK(1).equals("LPARENT")) {
            // 函数调用式
            String ident = input.get(nowIndex).getTail();
            curFuncLine = lines.get(nowIndex);
            if (FuncTable.isErrC(ident)) {
                Error.addMsg(lines.get(nowIndex).toString(), "c");
            }
            nextK(2);
            ArrayList<Exp> paras = null;
            if (!tokenK(0).equals("RPARENT") && !(tokenK(0).equals("SEMICN"))) {
                paras = getFuncRParams(ident);
            } else if (FuncTable.isContain(ident)) {
                if (FuncTable.isErrD(ident, 0)) {
                    Error.addMsg(String.valueOf(curFuncLine), "d");
                }
            }
            exp = new CallExp(ident, paras);
            if (!tokenK(0).equals("RPARENT")) {
                Error.addMsg(lines.get(nowIndex - 1).toString(), "j");
            } else {
                nextK(1);
            }
        } else if (tokenK(0).equals("PLUS") || (tokenK(0).equals("NOT") && globalCond) ||
                tokenK(0).equals("MINU")) {
            // UnaryExp
            String op = tokenK(0);
            nextK(1);
            processedData.add("<UnaryOp>" + "\n"); // for UnaryOp type
            Exp tmp = getUnaryExp();
            exp = new UnaryExp(op, tmp);
        } else {
            // PrimaryExp
            exp = getPrimaryExp();
        }
        processedData.add("<UnaryExp>" + "\n");
        return exp;
    }

    public ArrayList<Exp> getFuncRParams(String ident) {
        ArrayList<Exp> exps = new ArrayList<>();
        Exp exp = null;
        if (!tokenK(0).equals(")")) {
            exp = getExp(false);
        }
        if (exp != null) {
            exps.add(exp);
        }
        while (tokenK(0).equals("COMMA")) {
            nextK(1);
            exp = getExp(false);
            exps.add(exp);
        }
        if (FuncTable.isContain(ident)) {
            if (FuncTable.isErrD(ident, exps.size())) {
                Error.addMsg(String.valueOf(curFuncLine), "d");
            } else if (FuncTable.isErrE(ident, exps)) {
                Error.addMsg(String.valueOf(curFuncLine), "e");
            }
        }
        processedData.add("<FuncRParams>" + "\n");
        return exps;
    }

    public Exp getMulExp() {
        Exp left = getUnaryExp();
        processedData.add("<MulExp>" + "\n");
        while (tokenK(0).equals("MULT") || tokenK(0).equals("DIV") || tokenK(0).equals("MOD")) {
            String op = tokenK(0);
            nextK(1);
            Exp right = getUnaryExp();
            left = new BiExp(op, left, right);
            processedData.add("<MulExp>" + "\n");
        }
        return left;
    }

    public Exp getAddExp() {
        Exp left = getMulExp();
        processedData.add("<AddExp>" + "\n");
        while (tokenK(0).equals("PLUS") || tokenK(0).equals("MINU")) {
            String op = tokenK(0);
            nextK(1);
            Exp right = getMulExp();
            left = new BiExp(op, left, right);
            processedData.add("<AddExp>" + "\n");
        }
        return left;
    }

    public Exp getRelExp() {
        Exp left = getAddExp();
        processedData.add("<RelExp>" + "\n");
        while (tokenK(0).equals("LEQ") || tokenK(0).equals("LSS") || tokenK(0).equals("GEQ") ||
                tokenK(0).equals("GRE")) {
            String op = tokenK(0);
            nextK(1);
            Exp right = getAddExp();
            left = new BiExp(op, left, right);
            processedData.add("<RelExp>" + "\n");
        }
        return left;
    }

    public Exp getEqExp() {
        Exp left = getRelExp();
        processedData.add("<EqExp>" + "\n");
        while (tokenK(0).equals("EQL") || tokenK(0).equals("NEQ")) {
            String op = tokenK(0);
            nextK(1);
            Exp right = getRelExp();
            left = new BiExp(op, left, right);
            processedData.add("<EqExp>" + "\n");
        }
        return left;
    }

    public Exp getLAndExp() {
        Exp left = getEqExp();
        processedData.add("<LAndExp>" + "\n");
        while (tokenK(0).equals("AND")) {
            nextK(1);
            Exp right = getEqExp();
            left = new BiExp("AND", left, right);
            processedData.add("<LAndExp>" + "\n");
        }
        return left;
    }

    public Exp getLOrExp() {
        Exp left = getLAndExp();
        processedData.add("<LOrExp>" + "\n");
        while (tokenK(0).equals("OR")) {
            nextK(1);
            Exp right = getLAndExp();
            left = new BiExp("OR", left, right);
            processedData.add("<LOrExp>" + "\n");
        }
        return left;
    }

    public FuncFParam getFuncFParam() {
        nextK(1);
        final String ident = input.get(nowIndex).getTail();
        nextK(1);
        Exp exp = null;
        int dim = 0;
        if (tokenK(0).equals("LBRACK")) {
            nextK(1);
            dim = 1;
            if (!tokenK(0).equals("RBRACK")) {
                Error.addMsg(lines.get(nowIndex - 1).toString(), "k");
            } else {
                nextK(1);
            }
            if (tokenK(0).equals("LBRACK")) {
                dim = 2;
                nextK(1);
                exp = getExp(true);
                if (!tokenK(0).equals("RBRACK")) {
                    Error.addMsg(lines.get(nowIndex - 1).toString(), "k");
                } else {
                    nextK(1);
                }
            }
        }
        if (SymTable.isErrB(ident) || FuncTable.isErrB(ident)) {
            Error.addMsg(lines.get(nowIndex - 1).toString(), "b");
        } else {
            SymTable.addItem(ident, new Sym(false, dim));
        }
        processedData.add("<FuncFParam>" + "\n");
        return new FuncFParam(ident, dim, exp);
    }

    public ArrayList<FuncFParam> getFuncFParams() {
        ArrayList<FuncFParam> funcFParams = new ArrayList<>();
        funcFParams.add(getFuncFParam());
        while (tokenK(0).equals("COMMA")) {
            nextK(1);
            funcFParams.add(getFuncFParam());
        }
        processedData.add("<FuncFParams>" + "\n");
        return funcFParams;
    }

    public Stmt getStmt() {
        Stmt stmt = null;
        switch (tokenK(0)) {
            case "LBRACE":
                stmt = getBlock(false, false);
                break;
            case "IFTK":
                stmt = getIf();
                break;
            case "WHILETK":
                stmt = getWhile();
                break;
            case "CONTINUETK":
                stmt = new Continue(lines.get(nowIndex));
                nextK(1);
                if (curLoop == 0) {
                    Error.addMsg(lines.get(nowIndex - 1).toString(), "m");
                }
                if (!tokenK(0).equals("SEMICN")) {
                    Error.addMsg(lines.get(nowIndex - 1).toString(), "i");
                } else {
                    nextK(1);
                }
                break;
            case "BREAKTK":
                stmt = new Break(lines.get(nowIndex));
                nextK(1);
                if (curLoop == 0) {
                    Error.addMsg(lines.get(nowIndex - 1).toString(), "m");
                }
                if (!tokenK(0).equals("SEMICN")) {
                    Error.addMsg(lines.get(nowIndex - 1).toString(), "i");
                } else {
                    nextK(1);
                }
                break;
            case "RETURNTK":
                stmt = getReturn();
                break;
            case "SEMICN": //stmt:;
                if (!tokenK(0).equals("SEMICN")) {
                    Error.addMsg(lines.get(nowIndex).toString(), "i");
                } else {
                    nextK(1);
                }
                break;
            case "PRINTFTK":
                curPrint = lines.get(nowIndex);
                stmt = getPrint();
                break;
            default: // for special process and the default choice
                stmt = getRoll();
                break;
        }
        processedData.add("<Stmt>" + "\n");
        return stmt;
    }

    public Stmt getRoll() {
        Stmt stmt;
        if (tokenK(0).equals("IDENFR") && !tokenK(1).equals("LPARENT")) {
            int last = nowIndex;
            int processOldSize = processedData.size();
            Error.save();
            LeftExp exp = getLVal();
            if (tokenK(0).equals("ASSIGN")) {
                nextK(1);
                if (tokenK(0).equals("GETINTTK")) {
                    stmt = new Stdin(exp);
                    nextK(2);
                    if (!tokenK(0).equals("RPARENT")) {
                        Error.addMsg(lines.get(nowIndex - 1).toString(), "j");
                    } else {
                        nextK(1);
                    }
                    if (!tokenK(0).equals("SEMICN")) {
                        Error.addMsg(lines.get(nowIndex - 1).toString(), "i");
                    } else {
                        nextK(1);
                    }
                } else {
                    Exp exp2 = getExp(false);
                    stmt = new Assign(exp, exp2);
                    if (!tokenK(0).equals("SEMICN")) {
                        Error.addMsg(lines.get(nowIndex - 1).toString(), "i");
                    } else {
                        nextK(1);
                    }
                }
            } else {
                nowIndex = last;
                Error.restore();
                int size = processedData.size();
                if (size > processOldSize) {
                    processedData.subList(processOldSize, size).clear();
                }
                stmt = getExp(false);
                if (!tokenK(0).equals("SEMICN")) {
                    Error.addMsg(lines.get(nowIndex - 1).toString(), "i");
                } else {
                    nextK(1);
                }
                // TODO: 2022/10/14 这对应的啥情况
            }
        } else {
            // stmt:exp;
            stmt = getExp(false);
            if (!tokenK(0).equals("SEMICN")) {
                Error.addMsg(lines.get(nowIndex - 1).toString(), "i");
            } else {
                nextK(1);
            }
        }
        return stmt;
    }

    public If getIf() {
        nextK(2);
        Exp cond = getCond();
        if (!tokenK(0).equals("RPARENT")) {
            Error.addMsg(lines.get(nowIndex - 1).toString(), "j");
        } else {
            nextK(1);
        }
        Stmt stmt1 = getStmt();
        Stmt stmt2 = null;
        if (tokenK(0).equals("ELSETK")) {
            nextK(1);
            stmt2 = getStmt();
        }
        return new If(cond, stmt1, stmt2);
    }

    public While getWhile() {
        nextK(2);
        Exp cond = getCond();
        if (!tokenK(0).equals("RPARENT")) {
            Error.addMsg(lines.get(nowIndex - 1).toString(), "j");
        } else {
            nextK(1);
        }
        curLoop += 1;
        Stmt stmt = getStmt();
        curLoop -= 1;
        return new While(cond, stmt);
    }

    public Return getReturn() {
        final int line = lines.get(nowIndex);
        nextK(1);
        Exp exp = null;
        if (!tokenK(0).equals("SEMICN")) {
            exp = getExp(false);
            if (curIsVoid && exp != null) {
                Error.addMsg(lines.get(nowIndex).toString(), "f");
            }
        }
        if (!tokenK(0).equals("SEMICN")) {
            Error.addMsg(lines.get(nowIndex - 1).toString(), "i");
        } else {
            nextK(1);
        }
        return new Return(exp, line);
    }

    public Print getPrint() {
        nextK(2);
        String content = input.get(nowIndex).getTail();
        Pair<Boolean, Integer> pair = isErrA(content);
        boolean isErrA = pair.getHead();
        int count = pair.getTail();
        if (isErrA) {
            Error.addMsg(lines.get(nowIndex).toString(), "a");
        }
        nextK(1);
        Print print = new Print(content);
        while (tokenK(0).equals("COMMA")) {
            nextK(1);
            Exp exp = getExp(false);
            print.addExp(exp);
        }
        if (print.expSize() != count) {
            Error.addMsg(String.valueOf(curPrint), "l");
        }
        if (!tokenK(0).equals("RPARENT")) {
            Error.addMsg(lines.get(nowIndex - 1).toString(), "j");
        } else {
            nextK(1);
        }
        if (!tokenK(0).equals("SEMICN")) {
            Error.addMsg(lines.get(nowIndex - 1).toString(), "i");
        } else {
            nextK(1);
        }
        return print;
    }

    public Pair<Boolean, Integer> isErrA(String content) {
        int count = 0;
        boolean isA = false;
        for (int i = 1; i < content.length() - 1; i++) {
            if (content.charAt(i) == '\\') {
                if (!(i + 2 < content.length() && content.charAt(i + 1) == 'n')) {
                    isA = true;
                }
            }
            if (content.charAt(i) == '%') {
                if (!(i + 2 < content.length() && content.charAt(i + 1) == 'd')) {
                    isA = true;
                } else {
                    count += 1;
                }
            } else if (!(content.charAt(i) == 32 || content.charAt(i) == 33 ||
                    (content.charAt(i) >= 40 && content.charAt(i) <= 126))) {
                isA = true;
            }
        }
        return new Pair<>(isA, count);
    }
}

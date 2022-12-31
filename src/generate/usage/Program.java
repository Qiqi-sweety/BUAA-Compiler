package generate.usage;

import generate.Mediate;

import java.util.ArrayList;

public class Program {
    private Block globalInit;
    private Function main;
    private ArrayList<Function> functions;

    public void setGlobalInit(Block globalInit) {
        this.globalInit = globalInit;
    }

    public void setMain(Function main) {
        this.main = main;
    }

    public void setFunctions(ArrayList<Function> functions) {
        this.functions = functions;
    }

    public void print() {
        globalInit.print();
        for (Function f : functions) {
            if(f.getInlined()) continue;
            f.print();
        }
        main.print();
    }

    public Block getGlobalInit() {
        return globalInit;
    }

    public Function getMain() {
        return main;
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }
}

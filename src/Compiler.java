import generate.ForMidPrint;
import generate.Mediate;
import generate.usage.Program;
import lexical.Lexical;
import mips.Backend;
import mips.BackendLinearScan;
import mips.ForMipsPrint;
import mips.MipsAllocator;
import syntax.SyntaxAnalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.io.File;

public class Compiler {
    public static void main(String[] args) {
        String path = "testfile.txt";
        File file = new File(path);
        ArrayList<String> list = readFile(file);
        Lexical lexical = new Lexical(list);
        lexical.analysis();
        SyntaxAnalysis syntaxAnalysis = new SyntaxAnalysis(lexical.getItems());
        syntaxAnalysis.start();
        Mediate mediate = new Mediate(syntaxAnalysis.ForCompUnit());
        Program program = mediate.generate();
        program.print();
        print(ForMidPrint.get());
        BackendLinearScan backend = new BackendLinearScan(program);
//        Backend backend = new Backend(program);
        MipsAllocator.mipsAllocate(program);
        backend.get();
        print2(ForMipsPrint.get());
        //print(syntaxAnalysis.getProcessedData());
        //print(Error.errs());
    }

    private static ArrayList<String> readFile(File file) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            for (String line; (line = reader.readLine()) != null; ) {
                lines.add(line);
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("fail to read file");
            e.printStackTrace();
        }
        return lines;
    }

    private static void print(ArrayList<String> ans) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("llvm.txt"));
            for (String s : ans) {
                out.write(s);
            }
            out.close();
        } catch (IOException ignored) {
            System.err.println("fail to write file");
        }
    }

    private static void print2(ArrayList<String> ans) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("mips.txt"));
            for (String s : ans) {
                out.write(s);
            }
            out.close();
        } catch (IOException ignored) {
            System.err.println("fail to write file");
        }
    }
}

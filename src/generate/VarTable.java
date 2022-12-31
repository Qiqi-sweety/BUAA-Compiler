package generate;

import generate.usage.instr.decls.Decl;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class VarTable implements Cloneable {
    private ArrayList<LinkedHashMap<String, Decl>> table=new ArrayList<>();

    public void add(String ident, Decl decl) {
        table.get(table.size() - 1).put(ident, decl);
    }

    public Decl get(String ident) {
        for (int i = table.size() - 1; i >= 0; i--) {
            LinkedHashMap<String, Decl> pair = table.get(i);
            if (pair.containsKey(ident)) {
                return pair.get(ident);
            }
        }
        return new Decl();
    }

    public ArrayList<LinkedHashMap<String, Decl>> getTable() {
        return table;
    }

    public void addLayer() {
        table.add(new LinkedHashMap<>());
    }

    public void removeLayer() {
        table.remove(table.size() - 1);
    }

    public void removeToDepth(int depth) {
        while (table.size() > depth) {
            removeLayer();
        }

    }

    public void setTable(
        ArrayList<LinkedHashMap<String, Decl>> table) {
        this.table = table;
    }

    @Override
    public VarTable clone() {
        try {
            VarTable clone = (VarTable) super.clone();
            clone.setTable(new ArrayList<>(table));
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}


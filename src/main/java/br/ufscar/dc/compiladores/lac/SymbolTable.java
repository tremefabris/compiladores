package br.ufscar.dc.compiladores.lac;

import java.util.HashMap;
import java.util.Map;


public class SymbolTable {

    /*
     * Types for Linguagem Algoritmica
     */
    public enum LAType {
        LITERAL,
        INTEGER,
        REAL,
        LOGICAL,
        REGISTER,
        TYPE,
        PROCEDURE,
        PTR_INTEGER,
        MEM_ADDR,
        INVALID,
    }

    /*
     * Entry for the symbol table
     */
    class EntrySymbolTable {
        String name;
        LAType type;

        private EntrySymbolTable(String name, LAType type) {
            this.name = name;
            this.type = type;
        }
    }

    /*
     * Declarations for SymbolTable
     */
    private final Map<String, EntrySymbolTable> table;

    public SymbolTable() {
        this.table = new HashMap<>();
    }

    public void add(String name, LAType type) {
        table.put(name, new EntrySymbolTable(name, type));
    }

    public boolean exists(String name) {
        return table.containsKey(name);
    }

    public LAType verify(String name) {
        return table.get(name).type;
    }

    /*
     * GET ALL VARIABLE NAMES AND TYPES THAT START
     * WITH A GIVEN PREFIX
     * 
     * Useful for arrays, registers and custom types,
     * since we store those variables as (in the first
     * case) array.0, array.1, etc. So, to retrieve all
     * elements of 'array', we use this function.
     */
    public Map<String, LAType> getVariablesStartingWith(String prefix) {

        Map<String, LAType> vars = new HashMap<>();
        
        for (Map.Entry<String, EntrySymbolTable> entry: table.entrySet()) {

            if (entry.getKey().startsWith(prefix))
                vars.put(entry.getKey(), entry.getValue().type);

        }

        return vars;
    }
}

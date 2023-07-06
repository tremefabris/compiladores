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
}

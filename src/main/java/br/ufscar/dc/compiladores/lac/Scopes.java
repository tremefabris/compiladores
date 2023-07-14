package br.ufscar.dc.compiladores.lac;

import java.util.LinkedList;
import java.util.List;


public class Scopes {
    
    private LinkedList<SymbolTable> tableStack;

    public Scopes() {
        tableStack = new LinkedList<>();
        createNewScope();
    }

    public void createNewScope() {
        tableStack.push(new SymbolTable());
    }

    public SymbolTable currentScope() {
        return tableStack.peek();
    }

    public List<SymbolTable> browseNestedScopes() {
        return tableStack;
    }

    public void abandonScope() {
        tableStack.pop();
    }

}

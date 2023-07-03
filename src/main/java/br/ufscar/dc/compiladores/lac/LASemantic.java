package br.ufscar.dc.compiladores.lac;

import br.ufscar.dc.compiladores.lac.SymbolTable.LAType;


public class LASemantic extends LABaseVisitor<Void> {

    SymbolTable table;

    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {
        table = new SymbolTable();
        return super.visitPrograma(ctx);
    }

    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        if (ctx.variavel() != null) {

            for (LAParser.IdentificadorContext id: ctx.variavel().identificador()) {

                String ident_name = id.IDENT(0).getText();
                if (table.exists(ident_name)) {

                    LASemanticUtils.addSemanticError(
                        id.IDENT(0).getSymbol(),
                        "Variável " + ident_name + " já existe"
                    );

                } else {
                    
                    table.add(ident_name, LAType.INTEGER);

                }
            }
        }

        return super.visitDeclaracao_local(ctx);
    }
    
}

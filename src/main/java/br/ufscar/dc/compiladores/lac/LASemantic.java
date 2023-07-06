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
    
    @Override
    public Void visitTipo_basico_ident(LAParser.Tipo_basico_identContext ctx) {
        
        /*
         *  CATCHES ERROR: Undeclared type
         * 
         *  Considers every possible type apart from basic types (tipo_basico)
         *  as an undeclared type (good enough for T3)
         */
        if (ctx.IDENT() != null) {

            LASemanticUtils.addSemanticError(
                ctx.IDENT().getSymbol(),
                "tipo " + ctx.IDENT().getText() + " nao declarado"
            );

        }

        return super.visitTipo_basico_ident(ctx);
    }

    /**
     * TODO: Create class visitVariavel that handles variable creation
     * without calling visitIdentificador after
     */

    /**
     * TODO: Create class visitIdentificador that automatically handles
     * unitialized variables
     */

    /**
     * TODO: Both previous TODOs help avoid making a initialized-variable check
     * for every single possible command
     */

    @Override
    public Void visitCmdLeia(LAParser.CmdLeiaContext ctx) {

        for (LAParser.IdentificadorContext ic: ctx.identificador()) {
            
            if (ic.IDENT(0) != null && !table.exists(ic.IDENT(0).getText())) {

                LASemanticUtils.addSemanticError(
                    ic.IDENT(0).getSymbol(),
                    "identificador " + ic.IDENT(0).getText() + " nao declarado"
                );
            }
        }

        return super.visitCmdLeia(ctx);
    }

}

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

    @Override
    public Void visitVariavel(LAParser.VariavelContext ctx) {

        /*
         * HANDLES VARIABLE INITIALIZATION
         * 
         * Adds semantic error if variable already exists. Only adds newly-
         * -created variable to symbol table if its type is basic (tipo_basico).
         * 
         * Skips visitIdentificador()'s execution, since it handles IDENT's
         * validation right here. This means that anything that accesses
         * visitIdenticador() some other way is using that IDENT, not declaring
         * it. 
         */

        for (LAParser.IdentificadorContext ic: ctx.identificador()) {

            if (table.exists(ic.IDENT(0).getText()))
                LASemanticUtils.addSemanticError(
                    ic.IDENT(0).getSymbol(),
                    "identificador " + ic.IDENT(0).getText() + " ja declarado anteriormente" 
                );
            
            else {

                LAType var_type = null;
                switch(ctx.tipo().getText()) {

                    case "literal":
                        var_type = LAType.LITERAL;
                        break;
                    case "inteiro":
                        var_type = LAType.INTEGER;
                        break;
                    case "^inteiro":
                        var_type = LAType.PTR_INTEGER;
                        break;
                    case "real":
                        var_type = LAType.REAL;
                        break;
                    case "logico":
                        var_type = LAType.LOGICAL;
                        break;
                    default:
                        var_type = LAType.INVALID;
                        break;
                
                }

                table.add(ic.IDENT(0).getText(), var_type);
            }
        }
            
        return super.visitTipo(ctx.tipo());  // Skips visitIdentificador
    }

    @Override
    public Void visitIdentificador(LAParser.IdentificadorContext ctx) {

        /*
         * HANDLES UNITIALIZED VARIABLES
         * 
         * Adds semantic error if variable is being used but wasn't initialized.
         */
        
        // Syntatic analyzer won't allow to get here if ctx.IDENT(0) == null
        if (!table.exists(ctx.IDENT(0).getText()))
            LASemanticUtils.addSemanticError(
                ctx.IDENT(0).getSymbol(),
                "identificador " + ctx.IDENT(0).getText() + " nao declarado"
            );

        return super.visitIdentificador(ctx);
    }

    public static boolean areTypesIncompatible(LAType var, LAType exp) {
        boolean ret = (
            var != exp &&
            !(
                (var == LAType.INTEGER && exp == LAType.REAL) ||
                (var == LAType.REAL && exp == LAType.INTEGER)
            ) &&
            !(
                (var == LAType.PTR_INTEGER && exp == LAType.MEM_ADDR)
            )
        );

        return ret;
    }

    @Override
    public Void visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {

        /*
         * HANDLES INCOMPATIBLE ATRIBUTION
         */
        
        LAType exp_type = LASemanticUtils.verifyType(
            table, ctx.expressao()
        );
        String var_name = ctx.identificador().getText();


        if (table.exists(var_name)) {  // non-existance already handled by visitIdentificador

            LAType var_type = table.verify(var_name);
            String ptr_hat = (var_type == LAType.PTR_INTEGER) ? "^" : "";

            /*
             * Errors-out if variable and expression have different types,
             * and they both aren't a combination of INTEGER & REAL (since
             * an INTEGER can be attributed to a REAL variable, etc).
             */
            if (areTypesIncompatible(var_type, exp_type)) {

                LASemanticUtils.addSemanticError(
                    ctx.start,
                    "atribuicao nao compativel para " + ptr_hat + var_name
                );

            }
        }

        return super.visitCmdAtribuicao(ctx);
    }

}

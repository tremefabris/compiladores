package br.ufscar.dc.compiladores.lac;

import br.ufscar.dc.compiladores.lac.SymbolTable.LAType;

import java.util.List;
import java.util.Map;

public class LASemantic extends LABaseVisitor<Void> {

    Scopes scopes;

    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {
        scopes = new Scopes();
        return super.visitPrograma(ctx);
    }
    
    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        if (ctx.tipo() != null) {

            if (scopes.exists(ctx.IDENT().getText())) {
            
                LASemanticUtils.addSemanticError(
                    ctx.IDENT().getSymbol(),
                    "identificador " + ctx.IDENT().getText() + " ja declarado anteriormente" 
                );
            
            } else {

                LAType var_type = LAType.TYPE;
                String var_name = ctx.IDENT().getText();

                scopes.currentScope().add(var_name, var_type);

            }

        }

        return super.visitDeclaracao_local(ctx);
    }

    // TODO: comment thorougly
    @Override
    public Void visitTipo_basico_ident(LAParser.Tipo_basico_identContext ctx) {

        if (ctx.IDENT() != null) {

            String type_text = ctx.IDENT().getText();

            if (scopes.exists(type_text)) {
                
                Map<String, LAType> ct_fields;
                List<String> ct_var_names = LASemanticUtils.getCustomTypeVariableNames(ctx);

                boolean contains_type = false;
                for (String var_name: ct_var_names) {
                    if (var_name.equals(type_text))
                        contains_type = true;
                }
                if (contains_type)
                    ct_var_names.remove(type_text);

                SymbolTable type_table;

                if ((type_table = scopes.getTableFrom(type_text)) != null) {
                    ct_fields = type_table.getVariablesStartingWith(type_text);

                    contains_type = false;
                    for (Map.Entry<String, LAType> ct_field: ct_fields.entrySet()) {
                        if (ct_field.getKey().equals(type_text))
                            contains_type = true;
                    }
                    if (contains_type)
                        ct_fields.remove(type_text);

                    for (String var_name: ct_var_names) {
                        for (Map.Entry<String, LAType> field: ct_fields.entrySet()) {

                            String ct_field_suffix = field.getKey().split("\\.")[1];

                            scopes.currentScope().add(
                                var_name + "." + ct_field_suffix,
                                field.getValue()
                            );
                        }
                    }
                }
            } else {
                LASemanticUtils.addSemanticError(
                    ctx.IDENT().getSymbol(),
                    "tipo " + type_text + " nao declarado"
                );
            }
        }

        return super.visitTipo_basico_ident(ctx);
    }

    // TODO: comment this func more thoroughly
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

            if (scopes.exists(ic.IDENT(0).getText()))
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

                        if (ctx.tipo().getText().startsWith("registro")) {
                            var_type = LAType.REGISTER;
                        } else {
                            var_type = LAType.INVALID;
                        }

                        break;
                
                }

                if (LASemanticUtils.isRegisterAttribute(ic)) {

                    List<String>
                    reg_var_names = LASemanticUtils.getRegisterVariableNames(ic);

                    for (String reg_var: reg_var_names) {

                        String var_name = reg_var + "." + ic.IDENT(0).getText();
                        scopes.currentScope().add(var_name, var_type);
                    }

                } else if (LASemanticUtils.isArray(ic)) {

                    for (
                            int i = 0;
                            i < Integer.parseInt(LASemanticUtils.getArraySize(ic));
                            i++
                    ) {
                        String var_name = ic.IDENT(0).getText();
                        scopes.currentScope().add(
                            var_name + "." + Integer.toString(i),
                            var_type
                        );
                    }

                } else {
                    String var_name = ic.IDENT(0).getText();
                    scopes.currentScope().add(var_name, var_type);
                }
            }
        }

        return super.visitTipo(ctx.tipo());  // Skips visitIdentificador
    }

    // TODO: comment thorougly
    @Override
    public Void visitIdentificador(LAParser.IdentificadorContext ctx) {
        
        String ident_name;

        if (LASemanticUtils.isArray(ctx)) {
            ident_name = LASemanticUtils.formatArrayIdent(ctx, false);
        }
        else {
            ident_name = ctx.getText();
        }

        if (!scopes.exists(ident_name))
            LASemanticUtils.addSemanticError(
                ctx.IDENT(0).getSymbol(),
                "identificador " + ident_name + " nao declarado"
            );

        return super.visitIdentificador(ctx);
    }

    @Override
    public Void visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {

        /*
         * HANDLES INCOMPATIBLE ATRIBUTION
         */
        
        LAType exp_type = LASemanticUtils.verifyType(
            scopes, ctx.expressao()
        );

        String var_name;
        boolean isArray = false;
        if (LASemanticUtils.isArray(ctx.identificador())) {

            var_name = LASemanticUtils.formatArrayIdent(ctx.identificador(), false);
            isArray = true;
        
        }
        else {
            var_name = ctx.identificador().getText();
        }

        if (scopes.exists(var_name)) {  // non-existance already handled by visitIdentificador

            LAType var_type = scopes.verifyType(var_name);
            String ptr_hat = (var_type == LAType.PTR_INTEGER) ? "^" : "";

            /*
             * Errors-out if variable and expression have different types,
             * and they both aren't a combination of INTEGER & REAL (since
             * an INTEGER can be attributed to a REAL variable, etc).
             */
            if (LASemanticUtils.areTypesIncompatible(var_type, exp_type)) {

                if (isArray)
                    var_name = LASemanticUtils.formatArrayIdent(ctx.identificador(), true);

                LASemanticUtils.addSemanticError(
                    ctx.start,
                    "atribuicao nao compativel para " + ptr_hat + var_name
                );

            }
        }

        return super.visitCmdAtribuicao(ctx);
    }

    @Override
    public Void visitCmdPara(LAParser.CmdParaContext ctx) {

        scopes.createNewScope();
        scopes.currentScope().add(
            ctx.IDENT().getText(),
            LASemanticUtils.verifyType(scopes, ctx.exp_aritmetica(0))
        );

        /*
        * Executing the rest of cmdPara manually
        */
        super.visitExp_aritmetica(ctx.exp_aritmetica(0));
        super.visitExp_aritmetica(ctx.exp_aritmetica(1));

        for (LAParser.CmdContext cmd_ctx: ctx.cmd()) {
            super.visitCmd(cmd_ctx);
        }
        /*
        * Now, we are at the "exit" of cmdPara
        */

        scopes.abandonScope();

        return null;
    }

}

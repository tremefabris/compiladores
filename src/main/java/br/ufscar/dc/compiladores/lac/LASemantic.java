package br.ufscar.dc.compiladores.lac;

import br.ufscar.dc.compiladores.lac.SymbolTable.LAType;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
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
    public Void visitCorpo(LAParser.CorpoContext ctx) {

        /*
         * HANDLES RETURN STATEMENT IN MAIN
         * ALGORITHM SCOPE
         */

        for (LAParser.CmdContext cc: ctx.cmd()) {
            if (cc.cmdRetorne() != null) {

                LASemanticUtils.addSemanticError(
                    cc.cmdRetorne().getStart(),
                    "comando retorne nao permitido nesse escopo"
                );

            }
        }
        
        return super.visitCorpo(ctx);
    }

    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        
        /*
         * CUSTOM TYPE AND CONSTANTS DECLARATION HANDLING
         * 
         * In here, custom types* and constants' declarations
         * are handled.
         * 
         * *: The actual instantiation of custom type
         * variables is handled by visitTipo_basico_ident.
         */

        /*
         * If it's a custom type...
         */
        if (ctx.tipo() != null) {

            if (scopes.exists(ctx.IDENT().getText())) {
            
                LASemanticUtils.addSemanticError(
                    ctx.IDENT().getSymbol(),
                    "identificador " + ctx.IDENT().getText() + " ja declarado anteriormente" 
                );
            
            } else {

                /*
                 * Custom type for... custom types.
                 */
                LAType var_type = LAType.TYPE;
                String var_name = ctx.IDENT().getText();

                scopes.currentScope().add(var_name, var_type);

            }

        }

        /*
         * If it's a constant...
         */

        else if (ctx.tipo_basico() != null) {

            String const_name = ctx.IDENT().getText();

            LAType const_type;
            switch (ctx.tipo_basico().getText()) {
                case "literal":
                    const_type = LAType.LITERAL;
                    break;
                case "inteiro":
                    const_type = LAType.INTEGER;
                    break;
                case "real":
                    const_type = LAType.REAL;
                    break;
                case "logico":
                    const_type = LAType.LOGICAL;
                    break;
                default:
                    // unreachable
                    const_type = LAType.INVALID;
                    break;
            }

            scopes.currentScope().add(const_name, const_type);
        }

        return super.visitDeclaracao_local(ctx);
    }

    @Override
    public Void visitDeclaracao_global(LAParser.Declaracao_globalContext ctx) {
        
        /*
         * FUNCTIONS AND PROCEDURES DECLARATION
         */

        /*
         * If we are dealing with a function declaration...
         */
        if (ctx.tipo_estendido() != null) {

            /*
             * Initially, we are concerned about the function's type.
             * Since it's type can be deeply nested in our grammar,
             * the following code is really indented and, consequently,
             * really ugly...
             */

            String fn_name = ctx.IDENT().getText();
            LAType fn_type;

            if (ctx.tipo_estendido().getText().contains("^")) {
                fn_type = LAType.PTR_INTEGER;
            }
            else {

                if (ctx.tipo_estendido().tipo_basico_ident().IDENT() != null) {

                    if (!scopes.exists(
                            ctx.tipo_estendido()
                                .tipo_basico_ident()
                                .IDENT()
                                .getText()
                    )) {
                        Token error_symbol = ctx.tipo_estendido()
                                                .tipo_basico_ident()
                                                .IDENT()
                                                .getSymbol();
                        String type_text   = ctx.tipo_estendido()
                                                .tipo_basico_ident()
                                                .IDENT()
                                                .getText();

                        LASemanticUtils.addSemanticError(
                            error_symbol,
                            "tipo " + type_text + " nao declarado"
                        );
                        fn_type = LAType.INVALID;

                    } else {
                        fn_type = LAType.TYPE;
                    }

                }
                else {
                    switch(ctx.tipo_estendido().tipo_basico_ident().tipo_basico().getText()) {
                        case "literal":
                            fn_type = LAType.LITERAL;
                            break;
                        case "inteiro":
                            fn_type = LAType.INTEGER;
                            break;
                        case "real":
                            fn_type = LAType.REAL;
                            break;
                        case "logico":
                            fn_type = LAType.LOGICAL;
                            break;
                        default:
                            // unreachable
                            fn_type = LAType.INVALID;
                            break;
            }}}
            
            /*
             * Function type discovered!
             * We store it in the current scope.
             */
            scopes.currentScope().add(fn_name, fn_type);

            /*
             * Parameter declaration.
             * 
             * We store every single parameter of
             * the function as a variable to later
             * infer its type, borrowing from the
             * "register variable declaration" school
             * of thought.
             */
            if (ctx.parametros() != null) {

                int param_amount = ctx.parametros().parametro().size();
                LAType[] param_types = new LAType[param_amount];

                for (int i = 0; i < param_amount; i++) {
                    String param_name = fn_name + "." + Integer.toString(i);

                    /*
                     * Discovering parameter types
                     */
                    LAParser.ParametroContext param = ctx.parametros().parametro(i);

                    /*
                     * If it's a pointer...
                     */
                    if (param.tipo_estendido().getText().contains("^")) {
                        param_types[i] = LAType.PTR_INTEGER;
                    }

                    /*
                     * If it's a custom type...
                     */
                    else if (param.tipo_estendido().tipo_basico_ident().IDENT() != null) {
                        if (!scopes.exists(
                                param.tipo_estendido()
                                     .tipo_basico_ident()
                                     .IDENT()
                                     .getText()
                        )) {
                            Token error_symbol = param.tipo_estendido()
                                                     .tipo_basico_ident()
                                                     .IDENT()
                                                     .getSymbol();
                            String type_text   = param.tipo_estendido()
                                                     .tipo_basico_ident()
                                                     .IDENT()
                                                     .getText();

                            LASemanticUtils.addSemanticError(
                                error_symbol,
                                "tipo " + type_text + " nao declarado"
                            );
                            param_types[i] = LAType.INVALID;
                        }
                        else {
                            param_types[i] = LAType.TYPE;
                        }
                    }
                    else {

                        /*
                         * If it's a basic type...
                         */
                        switch(ctx.tipo_estendido().tipo_basico_ident().tipo_basico().getText()) {
                            case "literal":
                                param_types[i] = LAType.LITERAL;
                                break;
                            case "inteiro":
                                param_types[i] = LAType.INTEGER;
                                break;
                            case "real":
                                param_types[i] = LAType.REAL;
                                break;
                            case "logico":
                                param_types[i] = LAType.LOGICAL;
                                break;
                            default:
                                // unreachable
                                param_types[i] = LAType.INVALID;
                                break;
                        }}
                
                    scopes.currentScope().add(param_name, param_types[i]);
                }

                /*
                 * Creating new scope so that
                 * the logic inside a function's
                 * body doesn't throw errors.
                 */
                scopes.createNewScope();

                int param_count = 0;
                for (LAParser.ParametroContext param: ctx.parametros().parametro()) {

                    scopes.currentScope().add(
                        param.identificador(0).getText(),
                        param_types[param_count]
                    );

                    param_count = param_count + 1;
                }
                
                /*
                 * Manually executing the rest of the
                 * visitDeclaracao_global call
                 */
                super.visitParametros(ctx.parametros());
                super.visitTipo_estendido(ctx.tipo_estendido());
                for (LAParser.Declaracao_localContext dlc: ctx.declaracao_local())
                    super.visitDeclaracao_local(dlc);
                for (LAParser.CmdContext cc: ctx.cmd())
                    super.visitCmd(cc);
                /*
                 * "Exiting" visitDeclaracao_global
                 */
                
                scopes.abandonScope();
            }
        }

        /*
         * If not, we are dealing with a procedure...
         */
        else {
            String proc_name = ctx.IDENT().getText();
            
            /*
             * Insert procedure's identifier in 
             * our scopes.
             */
            if (!scopes.exists(proc_name)) {

                LAType proc_type = LAType.PROCEDURE;

                scopes.currentScope().add(proc_name, proc_type);

            }

            for (LAParser.CmdContext cc: ctx.cmd()) {

                /*
                 * If we have a return command amidst
                 * other commands, error out.
                 */
                if (cc.cmdRetorne() != null) {

                    LASemanticUtils.addSemanticError(
                        cc.cmdRetorne().getStart(),
                        "comando retorne nao permitido nesse escopo"
                    );

                }
            }
        }

        return null;
    }

    @Override
    public Void visitTipo_basico_ident(LAParser.Tipo_basico_identContext ctx) {

        /*
         * HANDLES THE PROPER INTIIALIZATION OF
         * CUSTOM TYPE VARIABLES
         * 
         * The actual type is created in 
         * visitDeclaracao_local, but the variables
         * who are assigned the custom type are
         * properly instantiated here (e.g. its
         * attributes)
         */

        /*
         * If it's a custom type variable...
         */
        if (ctx.IDENT() != null) {

            String type_text = ctx.IDENT().getText();

            if (scopes.exists(type_text)) {
                
                /*
                 * Mapping to store all the attributes
                 * that come with the custom type,
                 * consisting of <attribute name,
                 * attribute type>
                 */
                Map<String, LAType> ct_fields;

                /*
                 * List of variable names that need to
                 * receive the custom type proper
                 * initialization
                 */
                List<String> ct_var_names = LASemanticUtils.getCustomTypeVariableNames(ctx);

                /*
                 * Remove the custom type's own name
                 * from var_names
                 */
                boolean contains_type = false;
                for (String var_name: ct_var_names) {
                    if (var_name.equals(type_text))
                        contains_type = true;
                }
                if (contains_type)
                    ct_var_names.remove(type_text);


                /*
                 * Symbol Table in which the custom
                 * type was created.
                 * 
                 * All its attributes are also stored
                 * in the same table, so we need to
                 * retrieve it.
                 */
                SymbolTable type_table;

                if ((type_table = scopes.getTableFrom(type_text)) != null) {
                    ct_fields = type_table.getVariablesStartingWith(type_text);

                    /*
                     * Remove the custom type's own name
                     * from ct_fields
                     */
                    contains_type = false;
                    for (Map.Entry<String, LAType> ct_field: ct_fields.entrySet()) {
                        if (ct_field.getKey().equals(type_text))
                            contains_type = true;
                    }
                    if (contains_type)
                        ct_fields.remove(type_text);

                    /*
                     * For every variable that needs to be
                     * properly instantiated...
                     */
                    for (String var_name: ct_var_names) {

                        /*
                         * For every attribute that needs
                         * to be inserted...
                         */
                        for (Map.Entry<String, LAType> field: ct_fields.entrySet()) {

                            /*
                             * Get only the attribute name...
                             */
                            String ct_field_suffix = field.getKey().split("\\.")[1];

                            /*
                             * ...and add it in the current scope.
                             */
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

    @Override
    public Void visitParcela_unario(LAParser.Parcela_unarioContext ctx) {

        /*
         * HANDLES FUNCTION USAGE
         * 
         * Since functions, unlike procedures, commonly return a value,
         * they are normally considered expressions. Therefore,
         * we find them under the Parcela_unario rule.
         */
        if (ctx.IDENT() != null) {

            if (!scopes.exists(ctx.IDENT().getText())) {
                LASemanticUtils.addSemanticError(
                    ctx.IDENT().getSymbol(),
                    "funcao " + ctx.IDENT().getText() + " desconhecida"
                );
            }
            /*
             * If function exists...
             */
            else {

                String fn_name = ctx.IDENT().getText();
                SymbolTable fn_table = scopes.getTableFrom(fn_name);

                /*
                 * Retrieving only the types of the function's
                 * arguments.
                 * 
                 * When using SymbolTable.getVariablesStartingWith,
                 * the function's own name is retrieved back, so we
                 * use a work around with .remove(fn_name). This
                 * functionality could also be native to SymbolTable's
                 * method.
                 */
                Map<String, LAType> fn_args_map = fn_table.getVariablesStartingWith(fn_name);
                fn_args_map.remove(fn_name);

                List<LAType> fn_args = new ArrayList<>(fn_args_map.values());

                /*
                 * If the amount of arguments expected is different
                 * than the amount of arguments given, then error
                 * out immediately and return.
                 */
                if (fn_args.size() != ctx.expressao().size()) {

                    LASemanticUtils.addSemanticError(
                        ctx.IDENT().getSymbol(),
                        "incompatibilidade de parametros na chamada de " + ctx.IDENT().getText()
                    );
                     
                    return super.visitParcela_unario(ctx);
                }

                /*
                 * If not, if any of the arguments given has a
                 * different type than expected, error out.
                 */
                int exp_count = 0;
                for (LAParser.ExpressaoContext exp: ctx.expressao()) {
                    LAType exp_type = LASemanticUtils.verifyType(scopes, exp);

                    if (fn_args.get(exp_count) != exp_type) {

                        LASemanticUtils.addSemanticError(
                            ctx.IDENT().getSymbol(),
                            "incompatibilidade de parametros na chamada de " + ctx.IDENT().getText()
                        );

                        break;
                    }

                    exp_count = exp_count + 1;
                }
            }
        }

        return super.visitParcela_unario(ctx);
    }

    @Override
    public Void visitVariavel(LAParser.VariavelContext ctx) {

        /*
         * HANDLES VARIABLE INITIALIZATION
         * 
         * This function is used if, and only if, a variable
         * needs to be created.
         * 
         * It handles the creation of these types of variables:
         *     - basic type variables ('real', 'inteiro', etc);
         *     - pointers;
         *     - registers (both register attributes and register
         *       variables);
         *     - arrays.
         * 
         * Any time a variable is USED (as opposed to CREATED),
         * it is actually visitIdentificador which handles that.
         * Since we don't consider CREATION as a form of USE, we
         * skip visitIdentificador inside this function.
         */

        /*
         * For every variable name being created...
         */
        for (LAParser.IdentificadorContext ic: ctx.identificador()) {

            /*
             * If name already exists, error out
             */
            if (scopes.exists(ic.IDENT(0).getText()))
                LASemanticUtils.addSemanticError(
                    ic.IDENT(0).getSymbol(),
                    "identificador " + ic.IDENT(0).getText() + " ja declarado anteriormente" 
                );

            else {
                LAType var_type = null;

                /*
                 * If one of these basic types...
                 */
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
                /*
                 * If it's an array of custom types, it has 
                 * matched the previous 'default' branch and
                 * now is LAType.INVALID.
                 * 
                 * This if prevents this from being the case.
                 */
                if (
                    LASemanticUtils.isArray(ic) &&
                    ctx.tipo().tipo_estendido().tipo_basico_ident().IDENT() != null
                ) {
                    var_type = LAType.TYPE;                    
                }

                /*
                 * If it's a register attribute, recover all
                 * variables that are this register and create
                 * such attribute for them.
                 */
                if (LASemanticUtils.isRegisterAttribute(ic)) {

                    List<String>
                    reg_var_names = LASemanticUtils.getRegisterVariableNames(ic);

                    for (String reg_var: reg_var_names) {

                        String var_name = reg_var + "." + ic.IDENT(0).getText();
                        scopes.currentScope().add(var_name, var_type);
                    }

                }

                /*
                 * If it's an array, create N variables of
                 * same type and same name prefix, where N
                 * is the size given.
                 */
                else if (LASemanticUtils.isArray(ic)) {

                    // the following was done because of test case 6,
                    // where valor[maximoElementos] appears
                    int array_size;
                    String array_size_str = LASemanticUtils.getArraySize(ic);
                    try {
                        array_size = Integer.parseInt(array_size_str);
                    }
                    catch (NumberFormatException e) {
                        array_size = 10;   // arbitrary value...
                    }

                    for (int i = 0; i < array_size; i++) {

                        String var_name = ic.IDENT(0).getText();
                        scopes.currentScope().add(
                            var_name + "." + Integer.toString(i),
                            var_type
                        );
                    
                    }

                }
                
                /*
                 * If none of the above, it's just a simple
                 * variable...
                 */
                else {
                    String var_name = ic.IDENT(0).getText();
                    scopes.currentScope().add(var_name, var_type);
                }
            }
        }

        return super.visitTipo(ctx.tipo());  // Skips visitIdentificador
    }

    @Override
    public Void visitIdentificador(LAParser.IdentificadorContext ctx) {
        
        /*
         * HANDLES VARIABLE USAGE
         * 
         * Every time a variable is USED (as opposed to CREATED),
         * this functions handles it.
         */

        String ident_name;

        /*
         * If the variable in question is an array,
         * retrieve it's formatted name (the one
         * actually stored in Scopes)
         */
        if (LASemanticUtils.isArray(ctx)) {
            ident_name = LASemanticUtils.formatArrayIdent(ctx, false);
        }

        /*
         * Otherwise, just get the context's text...
         */
        else {
            ident_name = ctx.getText();
        }

        /*
         * If variable doesn't exist in scope,
         * error out
         */
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
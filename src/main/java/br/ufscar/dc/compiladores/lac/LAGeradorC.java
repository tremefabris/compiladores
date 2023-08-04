package br.ufscar.dc.compiladores.lac;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.sun.tools.javac.resources.ct;

import br.ufscar.dc.compiladores.lac.LAParser.CmdAtribuicaoContext;
import br.ufscar.dc.compiladores.lac.LAParser.CmdCasoContext;
import br.ufscar.dc.compiladores.lac.LAParser.CmdChamadaContext;
import br.ufscar.dc.compiladores.lac.LAParser.CmdContext;
import br.ufscar.dc.compiladores.lac.LAParser.CmdEnquantoContext;
import br.ufscar.dc.compiladores.lac.LAParser.CmdEscrevaContext;
import br.ufscar.dc.compiladores.lac.LAParser.CmdFacaContext;
import br.ufscar.dc.compiladores.lac.LAParser.CmdLeiaContext;
import br.ufscar.dc.compiladores.lac.LAParser.CmdParaContext;
import br.ufscar.dc.compiladores.lac.LAParser.CmdRetorneContext;
import br.ufscar.dc.compiladores.lac.LAParser.CmdSeContext;
import br.ufscar.dc.compiladores.lac.LAParser.Decl_local_globalContext;
import br.ufscar.dc.compiladores.lac.LAParser.Declaracao_localContext;
import br.ufscar.dc.compiladores.lac.LAParser.Exp_aritmeticaContext;
import br.ufscar.dc.compiladores.lac.LAParser.Exp_relacionalContext;
import br.ufscar.dc.compiladores.lac.LAParser.ExpressaoContext;
import br.ufscar.dc.compiladores.lac.LAParser.Fator_logicoContext;
import br.ufscar.dc.compiladores.lac.LAParser.IdentificadorContext;
import br.ufscar.dc.compiladores.lac.LAParser.Op_relacionalContext;
import br.ufscar.dc.compiladores.lac.LAParser.Parcela_logicaContext;
import br.ufscar.dc.compiladores.lac.LAParser.Termo_logicoContext;
import br.ufscar.dc.compiladores.lac.SymbolTable.LAType;

public class LAGeradorC extends LABaseVisitor<Void> {
    StringBuilder saida;
    SymbolTable table;
    Scopes scopes;

    public LAGeradorC() {
        saida = new StringBuilder();
        this.scopes = new Scopes();
        this.table = scopes.currentScope();

    }

    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {

        // inicio basico do c
        saida.append("#include <stdio.h>\n");
        saida.append("#include <stlib.h>\n");
        saida.append("\n\n");

        for (int i=0; i < ctx.declaracoes().decl_local_global().size(); i++){
            Decl_local_globalContext dec = ctx.declaracoes().decl_local_global(i);
            visitDecl_local_global(dec);

        }        // visita todas as declarações que estão fora do corpo
                                                                                          

        // chamada da função main

        saida.append("\n");
        saida.append("int main() {\n");

        for ( int j=0; j < ctx.corpo().declaracao_local().size(); j++){
            Declaracao_localContext dec = ctx.corpo().declaracao_local(j);
            visitDeclaracao_local(dec);
        }//visita as declarações dentro da main

        

        for (int k=0; k < ctx.corpo().cmd().size(); k++){
            CmdContext cmd = ctx.corpo().cmd(k);
            visitCmd(cmd);
        }
        

        saida.append("return 0;\n");
        saida.append("}\n");

        return null;

    }

    @Override
    public Void visitDecl_local_global(LAParser.Decl_local_globalContext ctx) {
        if (ctx.declaracao_global() != null) {
            saida.append("\n");
            visitDeclaracao_global(ctx.declaracao_global());
            saida.append("\n");

        } else {
            visitDeclaracao_local(ctx.declaracao_local());
            saida.append("\n");

        }
        return null;

    }

    @Override
    public Void visitDeclaracao_global(LAParser.Declaracao_globalContext ctx) {
        // TODO: declarações globais
        if (ctx.tipo_estendido() != null){// é função
            String nomedafuncao = ctx.IDENT().getText();
            String tipofuncao = ctx.tipo_estendido().getText();
            saida.append(tipofuncao + " " + nomedafuncao + "(" );
            visitParametros(ctx.parametros());
            saida.append("){ \n");
            for (int i=0; i < ctx.declaracao_local().size(); i++ ){
                visitDeclaracao_local(ctx.declaracao_local(i));
            }
            for (int j=0; j< ctx.cmd().size(); j++){
                visitCmd(ctx.cmd(j));
            }
            saida.append("}\n\n");


        }else{// é procedimento
            String nomedoprocedimento = ctx.IDENT().getText();
            saida.append("void " + nomedoprocedimento + "(");
            visitParametros(ctx.parametros());
            saida.append(") { \n");
            for (int i=0; i < ctx.declaracao_local().size(); i++ ){
                visitDeclaracao_local(ctx.declaracao_local(i));
            }
            for (int j=0; j< ctx.cmd().size(); j++){
                visitCmd(ctx.cmd(j));
            }
            saida.append("}\n\n");

        }

        return null;
    }

    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        if (ctx.variavel() != null) {// caso seka uma declaração basica
            visitVariavel(ctx.variavel());

        } else if (ctx.tipo_basico() != null) {// caso estja declarando uma constante
            String nome = ctx.IDENT().getText();
            String valor = ctx.valor_constante().getText();
            String strtipo = ctx.tipo_basico().getText();
            LAType tipo = LAType.INVALID;

            switch (strtipo) {
                case "literal":
                    tipo = LAType.LITERAL;
                    break;
                case "inteiro":
                    tipo = LAType.INTEGER;
                    break;
                case "real":
                    tipo = LAType.REAL;
                    break;
                case "logico":
                    tipo = LAType.LOGICAL;
                    break;
            }


            saida.append("#define " + nome + " " + valor);
            saida.append("\n");
            table.add(nome, tipo);

        } else {// caso esteja declarando um registro
            String nome = ctx.IDENT().getText();
            saida.append("typedef struct{ ");
            saida.append("\n");
            visitTipo(ctx.tipo());

            saida.append("}");
            saida.append(nome);
            saida.append("\n");

        }

        return null;
    }

    @Override
    public Void visitVariavel(LAParser.VariavelContext ctx) {
        if (ctx.tipo().tipo_estendido() != null){
            
            String strtipo = ctx.tipo().getText();
            LAType tipo = LAType.INVALID;

            switch (strtipo) {
                case "literal":
                    tipo = LAType.LITERAL;
                    strtipo = "string";
                    break;
                case "inteiro":
                    tipo = LAType.INTEGER;
                    strtipo = "int";
                    break;
                case "real":
                    tipo = LAType.REAL;
                    strtipo = "float";
                    break;
                case "logico":
                    tipo = LAType.LOGICAL;
                    strtipo = "bool";
                    break;

            }
            saida.append(strtipo + " ");    
            
            for (int l = 0; l<ctx.identificador().size(); l++  ){
                if(l != 0){
                    saida.append(", ");
                }
                
                String varname = ctx.identificador(l).getText();
                saida.append(varname);
                table.add(varname, tipo);
                
            }          
                    
                
        }else {
            //TODO: fazer registro

            



        }    
               

        saida.append(";\n");
        return null;
    }

    @Override
    public Void visitTipo(LAParser.TipoContext ctx) {
        // TODO: trabalha registros e ponteiro

        return null;
    }

    @Override
    public Void visitCmd(LAParser.CmdContext ctx) {
        if (ctx.cmdAtribuicao() != null){
            visitCmdAtribuicao(ctx.cmdAtribuicao()); 
        }else if(ctx.cmdLeia() != null) {
            visitCmdLeia(ctx.cmdLeia());
        }else if(ctx.cmdCaso() != null){
            visitCmdCaso(ctx.cmdCaso());
        }else if(ctx.cmdChamada() != null){
            visitCmdCaso(ctx.cmdCaso());
        }else if(ctx.cmdEnquanto() != null){
            visitCmdEnquanto(ctx.cmdEnquanto());
        }else if(ctx.cmdEscreva() != null){
            visitCmdEscreva(ctx.cmdEscreva());
        }else if(ctx.cmdFaca() != null){
            visitCmdFaca(ctx.cmdFaca());
        }else if(ctx.cmdPara() != null){
            visitCmdPara(ctx.cmdPara());
        }else if(ctx.cmdRetorne() != null){
            visitCmdRetorne(ctx.cmdRetorne());
        }else {
            visitCmdSe(ctx.cmdSe());
        }

        saida.append("\n");

        return null;
    }

    @Override
    public Void visitCmdAtribuicao(CmdAtribuicaoContext ctx) {
        saida.append(ctx.identificador().getText() + " = ");
        visitExpressao(ctx.expressao());
        saida.append(" ;\n");
        return null;
    }

    

    @Override
    public Void visitCmdLeia(CmdLeiaContext ctx){
        saida.append("scanf(\"");
        for(int m=0; m<ctx.identificador().size(); m++ ){
            if(m!=0){
                saida.append( ", ");
            }
            String varname = ctx.identificador(m).getText();
            LAType tipo = LASemanticUtils.verifyType(scopes, varname);
            String aux = "";
            switch(tipo){
                case INTEGER:
                    aux = "%d";
                    break;
                case REAL:
                    aux = "%f";
                    break;
                case LOGICAL:
                    aux = "%d";// scanf don't accept booleans
                    break;
                case LITERAL:
                    aux = "%s";
                    break;
                case REGISTER:
                //TODO: register    
                    break;
                case PTR_INTEGER:
                    aux = "%d";
                    break;    
                default:
                saida.append("deu erro no scanf");
                    break;    
            }
            
            saida.append(aux);

        }
        saida.append("\" ");

        for(int n=0; n<ctx.identificador().size(); n++ ){
            if(n != 0){
                saida.append( ", ");
            }
            //the next two lines would no be necessary with the use of a array.
            String varname = ctx.identificador(n).getText();
            LAType tipo = LASemanticUtils.verifyType(scopes, varname);
            if (tipo == LAType.LITERAL || tipo == LAType.PTR_INTEGER || tipo == LAType.MEM_ADDR ){
                saida.append(varname);

            }else{
                saida.append("&" + varname);
            }
            
        }

        saida.append(");\n");

        return null;
    }

    @Override
    public Void visitCmdCaso(CmdCasoContext ctx){
        //TODO: é o switch
        return null;
    }

    @Override
    public Void visitCmdChamada(CmdChamadaContext ctx){
        String msg = ctx.getText();
        saida.append(msg + ";\n");

        return null;
    }

    @Override
    public Void visitCmdEnquanto(CmdEnquantoContext ctx){
        saida.append("while(");
        visitExpressao(ctx.expressao());

        saida.append("){\n");
        
        for (int q=0; q<ctx.cmd().size() ; q++){
            visitCmd(ctx.cmd(q));
        }
        
        saida.append("}\n");


       
        return null;
    }

    @Override
    public Void visitExpressao(ExpressaoContext ctx){
        visitTermo_logico(ctx.termo_logico(0));
        if (ctx.op_logico_1() != null){
            for (int o=0; o < ctx.op_logico_1().size() ; o++){
                saida.append(ctx.op_logico_1(o).getText() + " ");
                visitTermo_logico(ctx.termo_logico(o+1));

            }


        }
        return null;
    }

    @Override
    public Void visitTermo_logico(Termo_logicoContext ctx){
        visitFator_logico(ctx.fator_logico(0));
        if (ctx.op_logico_2() != null){
            for(int p =0; p < ctx.op_logico_2().size(); p++){
                saida.append(ctx.op_logico_2(p) + " ");
                visitFator_logico(ctx.fator_logico(p+1));
            }
        }
        
        return null;

    }

    @Override
    public Void visitFator_logico(Fator_logicoContext ctx){
        if(ctx.getText().startsWith(" nao")){//comparação ruim, para ver se tem o não
            saida.append("!");
            

        }
        visitParcela_logica(ctx.parcela_logica());
        return null;
    }

    @Override
    public Void visitParcela_logica(Parcela_logicaContext ctx){
        if (ctx.getText() == "verdadeiro"){
            saida.append("true");

        }else {
            if(ctx.getText() == "falso"){
            saida.append("false");

            }else{
                visitExp_relacional(ctx.exp_relacional());
            }
        }    
        return null;
    }

    @Override
    public Void visitExp_relacional(Exp_relacionalContext ctx){
        visitExp_aritmetica(ctx.exp_aritmetica(0));
        if (ctx.op_relacional() != null){
            visitOp_relacional(ctx.op_relacional());
            visitExp_aritmetica(ctx.exp_aritmetica(1));
        }
        return null;
    }

    @Override
    public Void visitExp_aritmetica(Exp_aritmeticaContext ctx){
        String conta = ctx.getText();
        saida.append(conta);
        return null;
    }

    @Override
    public Void visitOp_relacional(Op_relacionalContext ctx){
        String sinal = ctx.getText();
        if(sinal == "="){
            saida.append("==");
        }else{
            saida.append(sinal);
        }
        return null;
    }

    @Override
    public Void visitCmdEscreva(CmdEscrevaContext ctx){
        for (int s=0; s<ctx.expressao().size(); s++){
            saida.append("printf(");
            String varname = ctx.expressao(s).getText();
            LAType tipo = LASemanticUtils.verifyType(scopes, varname);
            String aux = "";
            switch(tipo){
                case INTEGER:
                    aux = "%d";
                    break;
                case REAL:
                    aux = "%f";
                    break;
                case LITERAL:
                    aux = "%s";
                    break;
                case PTR_INTEGER:
                    aux = "%d";
                    break;    
                default:
                aux = "%d";
                    break;    
            }
            
            
            saida.append(aux + " , ");
            visitExpressao(ctx.expressao(s));
            saida.append(");\n");
        }


        /*TODO: corrigir o printf        
        for(int s=0; s<ctx.expressao().size(); s++){
            
            if (ctx.expressao(s).termo_logico(0).fator_logico(0).parcela_logica().exp_relacional().exp_aritmetica(0).termo(0).fator(0).parcela(0).parcela_unario().NUM_REAL() !=null){
                saida.append("printf( \"%f\" ,");
                visitExpressao(ctx.expressao(s));
                saida.append(");\n");
            }else{
                if(ctx.expressao(s).termo_logico(0).fator_logico(0).parcela_logica().exp_relacional().exp_aritmetica(0).termo(0).fator(0).parcela(0).parcela_nao_unario().CADEIA() !=null){
                    saida.append("printf( \"%s\" ,");
                    visitExpressao(ctx.expressao(s));
                    saida.append(");\n");
                }else{
                    saida.append("printf( \"%d\" ,");
                    visitExpressao(ctx.expressao(s));
                    saida.append(");\n");
                }
        }        
        saida.append("printf( \"%d\" ,");
        visitExpressao(ctx.expressao(s));
        saida.append(");\n");
            
        }
        */
        return null;

    }

    @Override
    public Void visitCmdFaca(CmdFacaContext ctx){
        saida.append("do(");
        
        for(int r=0; r < ctx.cmd().size(); r++){
            visitCmd(ctx.cmd(r));
        }
        
        saida.append(") while( !");
        visitExpressao(ctx.expressao());
        saida.append(");\n");

        return null;
    }

    
    @Override
    public Void visitCmdPara(CmdParaContext ctx){
        return null;
    }

    @Override
    public Void visitCmdRetorne(CmdRetorneContext ctx){
        String valour = ctx.expressao().getText();

        saida.append("return "+ valour + ";\n");
        return null;
    }

    @Override
    public Void visitCmdSe(CmdSeContext ctx){

        return null;
    }



}
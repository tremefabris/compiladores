package br.ufscar.dc.compiladores.lac;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.sun.tools.javac.resources.ct;

import br.ufscar.dc.compiladores.lac.LAParser.IdentificadorContext;
import br.ufscar.dc.compiladores.lac.SymbolTable.LAType;

public class LAGeradorC extends LABaseVisitor<Void> {
    StringBuilder saida;
    SymbolTable table;

    public LAGeradorC() {
        saida = new StringBuilder();
        this.table = new SymbolTable();

    }

    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {

        // inicio basico do c
        saida.append("#include <stdio.h>\n");
        saida.append("#include <stlib.h>\n");
        saida.append("\n");

        ctx.declaracoes().decl_local_global().forEach(dec -> visitDecl_local_global(dec));// visita todas as declarações
                                                                                          // que estão fora do corpo

        // chamada da função main

        saida.append("\n");
        saida.append("int main() {\n");

        ctx.corpo().cmd().forEach(comando -> visitCmd(comando));// ainda esta incompleto

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
        int a = 0;
        String strtipo = ctx.tipo().getText();
        LAType tipo = LAType.INVALID;
        switch (strtipo) {/*
                           * atualmente só trata tipos basicos
                           * TODO: fazer tratar tipos não basicos
                           */
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

        saida.append(strtipo);

        for(int i=0;i<ctx.identificador().size();i++) {
            IdentificadorContext ident = ctx.identificador(i);
            String nome = ident.getText();
            saida.append(" " + nome);
            if(i != ctx.identificador().size()-1) {
                saida.append(",");

            }
        }
        

        saida.append(";\n");
        return null;
    }

    @Override
    public Void visitTipo(LAParser.TipoContext ctx) {

        return null;
    }

    @Override
    public Void visitCmd(LAParser.CmdContext ctx) {

        return null;
    }

}
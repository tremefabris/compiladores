package br.ufscar.dc.compiladores.lac;

public class LAGeradorC extends LABaseVisitor<Void>{
    StringBuilder saida;
    SymbolTable table;

    public LAGeradorC() {
        saida = new StringBuilder();
        this.table = new SymbolTable();

    }

    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx){

        saida.append("#include <stdio.h>\n");
        saida.append("#include <stlib.h>\n");
        saida.append("\n");

        ctx.declaracoes().decl_local_global().forEach(dec -> visitDecl_local_global(dec));

        saida.append("\n");
        saida.append("int main() {\n");

        ctx.corpo().cmd().forEach(comando -> visitCmd(comando));

        saida.append("}\n");


        return null;

    }

    @Override
    public Void visitDecl_local_global(LAParser.Decl_local_globalContext ctx){
        if (ctx.declaracao_global() != null){
            visitDeclaracao_global(ctx.declaracao_global());

        }else{
            visitDeclaracao_local(ctx.declaracao_local());
        }
        return null;

    }

    @Override
    public Void visitDeclaracao_global(LAParser.Declaracao_globalContext ctx){

        return null;
    }

    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx){

        return null;
    }



    @Override
    public Void visitCmd(){


        
    }

    
    


}
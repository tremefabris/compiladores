package br.ufscar.dc.compiladores.lac;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import br.ufscar.dc.compiladores.lac.LAParser.ProgramaContext;


public class Principal {
    
    /*
     * Guarantees that the file 'filePath' exists.
     * If such file already exists, then nothing is done.
     * Otherwise, it is created. 
     * 
     * Note that if the method 'createNewFile' is unable
     * to create the file, it will throw an IOException,
     * which will lead to termination of the program.
     */
    static void guaranteeExistingFile(String filePath) {
        try {
            File outFile = new File(filePath);
            outFile.createNewFile();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /*
     * Helper function.
     * 
     * Checks if the received token lies under one of the
     * "error rules". If that is the case, this function
     * returns the correct error message associated with it.
     * Otherwise, null is returned, indicating that the token
     * is not an "error".
     */
    static String checkForLexicalError(Token token, String tokenType) {
        String errorMessage = null;

        if (tokenType == "UNIDENTIFIED_SYMBOL")
            errorMessage = "Linha " + token.getLine() + ": " +
                            token.getText() + " - simbolo nao identificado\n";

        else if (tokenType == "UNCLOSED_COMMENT")
            errorMessage = "Linha " + token.getLine() + ": comentario nao fechado\n";

        else if (tokenType == "UNCLOSED_CADEIA")
            errorMessage = "Linha " + token.getLine() + ": cadeia literal nao fechada\n";
        
        return errorMessage;
    }

    /*
     * Main function.
     * 
     * Instantiates a CharStream and an LA (our primary
     * lexer class) to read through a program file. Also opens
     * a PrintWriter to store the program's output.
     * 
     * Receives two command-line arguments:
     * - program file path; and
     * - output file path.
     * 
     * The internal 'while' loop holds most of this program's
     * logic: it receives tokens; extracts their text and type;
     * checks for errors; and correctly outputs the necessary
     * information.
     * 
     * Look inside for more details.
     */
    public static void main(String[] args) {
        try{
            String errorMessage = null;
            Boolean hasLexicalError = false;


            /* Lexer, token stream, and parser configuration */
            CharStream cs = CharStreams.fromFileName(args[0]);
            LALexer lex = new LALexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lex);
            LAParser parser = new LAParser(tokens);

            /* Creating output file to write results */
            String outputFilename = args[1];
            guaranteeExistingFile(outputFilename);
            PrintWriter outputWriter = new PrintWriter(outputFilename, "UTF-8");

            /* Add our custom SyntaxErrorListener */
            parser.removeErrorListeners();
            SyntaxErrorListener sel = new SyntaxErrorListener();
            parser.addErrorListener(sel);

            /**
             * First, we run through the program once to check for lexical errors
             */
            Token t = null;
            while((t = lex.nextToken()).getType() != Token.EOF) {

                String tokenType = LALexer.VOCABULARY.getDisplayName(t.getType());

                if ((errorMessage = checkForLexicalError(t, tokenType)) != null) {
                    hasLexicalError = true;
                    outputWriter.print(errorMessage);
                    break;
                }
            }

            /**
             * If no lexical error has been caught, we check for syntax 
             * and semantic errors
             */
            if (!hasLexicalError) {
                try{
                    lex.reset();

                    ProgramaContext tree = parser.programa();
                    LASemantic sem = new LASemantic();

                    sem.visitPrograma(tree);

                    for (String error: LASemanticUtils.semanticErrors) {
                        outputWriter.println(error);
                        // System.out.println(error);
                    }

                } catch (ParseCancellationException e) {
                    outputWriter.println(e.getMessage());
                }
            }

            outputWriter.println("Fim da compilacao");
            outputWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
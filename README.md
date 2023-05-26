# Trabalho 1 de Compiladores

Professor: [Daniel Lucrédio](https://github.com/dlucredio)

## Grupo

- Bruno Zenatti de Caires Marcelo, RA: 769821
- Guilherme Theodoro, RA: 726531
- Vitor Lopes Fabris, RA: 769822

## Descrição

Este repositório consiste da implementação de um analisador léxico para a Linguagem Algorítmica (LA), desenvolvida pelo prof. Jander Moreira. Dois arquivos são principais para este trabalho:
- `src/main/java/br/ufscar/dc/compiladores/alguma/lexico/Principal.java`: lógica principal para lidar com os tokens léxicos;
- `src/main/antlr4/br/ufscar/dc/compiladores/alguma/lexico/AlgumaLexer.g4`: regras gramaticais para os tokens encontrados no programa a ser analisado.

## Requerimentos

- Java versão 11.0.18
- ANTLR versão 4.11.1
- Apache Maven versão 3.6.0

## Como rodar?

### Direto do `.jar` providenciado

O arquivo `analisador-lexico.jar` (distribuído na raíz) representa o analisador léxico implementado. Para rodá-lo, basta executar o seguinte comando:

```bash
java -jar analisador-lexico.jar /path/to/example/program /path/to/output/file
```

Lembrando que é necessário que o arquivo de código `/path/to/example/program` exista e seja compatível. Se `/path/to/output/file` não existir, será criado pelo programa.

### Buildando o projeto

Para reconstruir o projeto, basta acessar a raíz do repositório e executar o seguinte comando:

```bash
mvn package
```

Desta maneira, o `.jar` a ser executado estará em `target/alguma-lexico-1.0-SNAPSHOT-jar-with-dependencies.jar`. Este obedece as mesmas regras descritas acima.

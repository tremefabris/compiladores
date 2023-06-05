# LAC - Linguagem Algoritmica Compiler

Repositório único para todos os trabalhos da disciplina de **Construção de Compiladores, DC UFSCar, semestre 2023/1**.

Professor: [Daniel Lucrédio](https://github.com/dlucredio)

## Grupo

- Bruno Zenatti de Caires Marcelo, RA: 769821
- Guilherme Theodoro, RA: 726531
- Vitor Lopes Fabris, RA: 769822

## Trabalhos implementados

- [X] T1
- [ ] T2
- [ ] T3
- [ ] T4
- [ ] T5

## Descrição

Este repositório consiste da construção de um compilador para a Linguagem Algorítmica (LA), desenvolvida pelo prof. Jander Moreira. Por se tratar de um esforço prolongado e contínuo durante toda a disciplina, o compilador evoluirá ao longo do tempo: primeiro foi adicionado um tratamento de tokens léxicos, depois regras sintáticas, etc.

Dois arquivos são principais para este trabalho:
- `src/main/java/br/ufscar/dc/compiladores/alguma/lexico/Principal.java`: lógica principal para lidar com os tokens léxicos;
- `src/main/antlr4/br/ufscar/dc/compiladores/alguma/lexico/AlgumaLexer.g4`: regras gramaticais para os tokens encontrados no programa a ser analisado.

## Acessar versões anteriores

Para acessar versões anteriores do presente repositório (por exemplo, quando apenas o T1 estava implementado, etc), os seguintes links são úteis:

- [Finalização do T1](https://github.com/tremefabris/compiladores/tree/28eda28771fb2112ddda98270b558ebae4d1d6f3);

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

Desta maneira, o `.jar` a ser executado estará em `target/lac-1.0-SNAPSHOT-jar-with-dependencies.jar`. Este obedece as mesmas regras descritas acima.

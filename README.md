# LAC - Linguagem Algoritmica Compiler

Repositório único para todos os trabalhos da disciplina de **Construção de Compiladores, DC UFSCar, semestre 2023/1**.

Professor: [Daniel Lucrédio](https://github.com/dlucredio)

## Grupo

- Bruno Zenatti de Caires Marcelo, RA: 769821
- Guilherme Theodoro, RA: 726531
- Vitor Lopes Fabris, RA: 769822

## Trabalhos implementados

- [X] T1
- [X] T2
- [X] T3
- [X] T4
- [ ] T5

## Descrição

Este repositório consiste da construção de um compilador para a Linguagem Algorítmica (LA), desenvolvida pelo prof. Jander Moreira. Por se tratar de um esforço prolongado e contínuo durante toda a disciplina, o compilador evoluirá ao longo do tempo: primeiro foi adicionado um tratamento de tokens léxicos, depois regras sintáticas, semânticas, etc.

### Para o T4

Um novo arquivo, `Scopes.java`, que realiza a lógica de armazenamento de diferentes escopos de código, foi incluído. Os arquivos importantes para o T3 permanecem assim, agora com novas adições:
- `LASemantic.java`: realiza todo o tratamento de erros através dos visitantes, sendo responsável por lidar com criação de variáveis, arrays, registros, funções, procedimentos, atribuções, laços de repetição, etc;
- `LASemanticUtils.java`: implementa funções utilitárias para a análise semântica, mas agora foi expandido para ajudar em situações específicas (como recuperar os atributos que devem pertencer a variáveis de tal registro);
- `SymbolTable.java`: mudou um pouco (uma nova função para recuperar variáveis com um determinado prefixo em comum), mas em geral continua o mesmo.

## Acessar versões anteriores

Para acessar versões anteriores do presente repositório (por exemplo, quando apenas o T1 estava implementado, etc), os seguintes links são úteis:

- [Finalização do T1](https://github.com/tremefabris/compiladores/tree/28eda28771fb2112ddda98270b558ebae4d1d6f3);
- [Finalização do T2](https://github.com/tremefabris/compiladores/tree/a98b56b5c3d3da7617205f4258228c57e748445d);
- [Finalização do T3](https://github.com/tremefabris/compiladores/tree/c8ff1f114965b39c0ab407304df35ec087d4c9f3).

## Requerimentos

- Java versão 11.0.18
- ANTLR versão 4.11.1
- Apache Maven versão 3.6.0

## Como rodar?

### Buildando o projeto

Para reconstruir o projeto, basta acessar a raíz do repositório e executar o seguinte comando:

```bash
mvn clean package
```

Desta maneira, o `.jar` a ser executado estará em `target/lac-1.0-SNAPSHOT-jar-with-dependencies.jar`.

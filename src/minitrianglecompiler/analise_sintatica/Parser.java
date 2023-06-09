package minitrianglecompiler.analise_sintatica;
import java.util.ArrayList;

import minitrianglecompiler.Token;
import minitrianglecompiler.visitor.*;

public class Parser {
  private int currentTokenId;

  private int currentIndex;
  private ArrayList<Token> arrayOfTokens;

  public Parser(ArrayList<Token> arrayList) {
    this.currentIndex = 0;
    this.arrayOfTokens = arrayList;

    this.currentTokenId = this.arrayOfTokens.get(currentIndex).kind;
  }

  private void accept(int tokenId) {
    System.out.println("Current Token on Array: " + arrayOfTokens.get(currentIndex).spelling);
    
    if(tokenId == currentTokenId) {
      currentIndex++;
      if(this.arrayOfTokens.size() > currentIndex) {
        currentTokenId = this.arrayOfTokens.get(currentIndex).kind;
      }
    } else {
      showError("Not accepted\n");
    }
  }

  private void acceptIt() {
    System.out.println("Current Token on Array: " + arrayOfTokens.get(currentIndex).spelling);
    currentIndex++;
    if(this.arrayOfTokens.size() > currentIndex) {
      currentTokenId = this.arrayOfTokens.get(currentIndex).kind;
    }
  }

  private void showError(String message) {
    System.out.println(message);
  }

  private void showError() {
    System.out.println("Erro sintático");
  }

  // Regras da gramática LL1 como métodos
  private nodeComandoAtribuicao parse_atribuicao() {
    nodeComandoAtribuicao comandoAtribuicao = new nodeComandoAtribuicao();

    comandoAtribuicao.variavel = parse_variavel();
    accept(Token.BECOMES);
    comandoAtribuicao.expressao = parse_expressao();

    return comandoAtribuicao;
  }

  private nodeComando parse_comando() {
    nodeComando comando;

    switch(currentTokenId) {
      case Token.IDENTIFIER:
        comando = parse_atribuicao();
        break;
      case Token.IF:
        comando = parse_condicional();
        break;
      case Token.WHILE:
        comando = parse_iterativo();
        break;
      case Token.BEGIN:
        comando = parse_comandoComposto();
        break;
      default:
        showError("parse comando");
        comando = null;
    }
    return comando;
  }

  private nodeComandoComposto parse_comandoComposto() {
    nodeComandoComposto comandoComposto = new nodeComandoComposto();

    accept(Token.BEGIN);
    comandoComposto.comandos = parse_listaDeComandos();
    accept(Token.END);

    return comandoComposto;
  }

  private nodeComandoCondicional parse_condicional() {
    nodeComandoCondicional condicional = new nodeComandoCondicional();

    accept(Token.IF);
    condicional.expressao = parse_expressao();
    accept(Token.THEN);
    condicional.comando1 = parse_comando();

    condicional.comando2 = null;

    if(currentTokenId == Token.ELSE) {
      acceptIt();
      condicional.comando2 = parse_comando();
    }
    return condicional;
  }

  private nodeCorpo parse_corpo() {
    nodeCorpo corpo = new nodeCorpo();

    corpo.declaracoes = parse_declaracoes();
    corpo.comandoComposto = parse_comandoComposto();

    return corpo;
  }

  private nodeDeclaracao parse_declaracao() {
    nodeDeclaracao declaracao = new nodeDeclaracao();
    declaracao.declaracaoDeVariavel = parse_declaracaoDeVariavel();

    return declaracao;
  }

  private nodeDeclaracaoDeVariavel parse_declaracaoDeVariavel() {
    nodeDeclaracaoDeVariavel declaracaoDeVariavel = new nodeDeclaracaoDeVariavel();

    accept(Token.VAR);
    declaracaoDeVariavel.IDs = parse_listaDeIds();
    accept(Token.COLON);
    declaracaoDeVariavel.tipo = parse_tipo();

    return declaracaoDeVariavel;
  }

  private nodeDeclaracoes parse_declaracoes() {
    nodeDeclaracoes declaracoes = new nodeDeclaracoes();
    declaracoes.declaracoes = new ArrayList<>();
    
    while(currentTokenId == Token.VAR) {
      declaracoes.declaracoes.add(parse_declaracao());
      accept(Token.SEMICOLON);
    }

    return declaracoes;
  }

  private nodeExpressao parse_expressao() {
    nodeExpressao expressao = new nodeExpressao();

    expressao.expressaoSimples1 = parse_expressaoSimples();
    expressao.operadorRelacional = null;
    expressao.expressaoSimples2 = null;

    if (currentTokenId == Token.RELATIONALOPERATOR) {
      nodeOperadorRelacional operadorRelacional = new nodeOperadorRelacional();
      operadorRelacional.operador = currentTokenId;
      expressao.operadorRelacional = operadorRelacional;

      acceptIt();

      expressao.expressaoSimples2 = parse_expressaoSimples();
    }
    
    return expressao; 
  }

  private nodeExpressaoSimples parse_expressaoSimples() {
    nodeExpressaoSimples expressaoSimples = new nodeExpressaoSimples();
    expressaoSimples.termo = parse_termo();
    expressaoSimples.operadoresAditivos = new ArrayList<nodeOperadorAditivo>();
    expressaoSimples.termos = new ArrayList<nodeTermo>();
    
    while (currentTokenId == Token.ADITIONALOPERATOR) {
      nodeOperadorAditivo operadorAditivo = new nodeOperadorAditivo();
      operadorAditivo.operador = currentTokenId;
      expressaoSimples.operadoresAditivos.add(operadorAditivo);

      acceptIt();
      
      expressaoSimples.termos.add(parse_termo());
    }
    
    return expressaoSimples;
  }

  private nodeFator parse_fator() {
    nodeFator fator = null;

    switch(currentTokenId) {
      case Token.IDENTIFIER:
        nodeVariavel aux1 = new nodeVariavel();
        aux1.ID = new nodeID();
        fator = aux1;

        acceptIt();
        break;

      case Token.FLOATLITERAL:
      case Token.INTLITERAL:
      case Token.BOOLLITERAL:
        nodeLiteral aux2 = new nodeLiteral();
        fator = aux2;

        acceptIt();
        break;

      case Token.LPAREN:
        acceptIt();

        nodeExpressao expressao = new nodeExpressao();
        expressao = parse_expressao();
        
        accept(Token.RPAREN);
        
        fator = expressao;
        break;

      default:
        showError("parse fator");
    }
    return fator;
  }

  private nodeComandoIterativo parse_iterativo() {
    nodeComandoIterativo comandoIterativo = new nodeComandoIterativo();

    accept(Token.WHILE);
    comandoIterativo.expressao = parse_expressao();
    accept(Token.DO);
    comandoIterativo.comando = parse_comando();

    return comandoIterativo;
  }

  private ArrayList<nodeComando> parse_listaDeComandos() {
    ArrayList<nodeComando> comandos = new ArrayList<>();

    while(currentTokenId == Token.IDENTIFIER) {
      comandos.add(parse_comando());
      accept(Token.SEMICOLON);
    }
    
    return comandos;
  }

  private ArrayList<nodeID> parse_listaDeIds() {
    ArrayList<nodeID> IDs = new ArrayList<>();

    IDs.add(new nodeID());
    accept(Token.IDENTIFIER);

    while(currentTokenId == Token.COMMA) {
      acceptIt();

      IDs.add(new nodeID());
      accept(Token.IDENTIFIER);
    }

    return IDs;
  }

  private void parse_outros() {
    switch(currentTokenId) {
      case Token.EXCLAMATION:
      case Token.ARROBA:
      case Token.HASHTAG:
      case Token.ELLIPSIS:
        acceptIt();
        break;
      default:
        showError("parse outros");
    }
  }

  private nodePrograma parse_programa() {
    nodePrograma programaAST = new nodePrograma();

    accept(Token.PROGRAM);
    programaAST.id = new nodeID();
    accept(Token.IDENTIFIER);

    accept(Token.SEMICOLON);
    programaAST.corpo = parse_corpo();
    accept(Token.PERIOD);

    return programaAST;
  }

  private nodeTermo parse_termo() {
    nodeTermo termo = new nodeTermo();
    termo.fatores = new ArrayList<nodeFator>();
    termo.operadoresMultiplicativos = new ArrayList<nodeOperadorMultiplicativo>();

    termo.fator = parse_fator();
    while(currentTokenId == Token.MULTIPLICATIONALOPERATOR) {
      nodeOperadorMultiplicativo operadorMultiplicativo = new nodeOperadorMultiplicativo();
      operadorMultiplicativo.operador = currentTokenId;
      termo.operadoresMultiplicativos.add(operadorMultiplicativo);

      acceptIt();

      termo.fatores.add(parse_fator());
    }
    return termo;
  }

  private nodeTipo parse_tipo() {
    nodeTipo tipo = new nodeTipo();

    tipo.tipoSimples = new nodeTipoSimples();
    tipo.tipoSimples.tipo = arrayOfTokens.get(currentIndex).spelling;
    accept(Token.TIPOSIMPLES);

    return tipo;
  }

  private nodeVariavel parse_variavel() {
    nodeVariavel variavel = new nodeVariavel();
    
    variavel.ID = new nodeID();
    variavel.ID.valor = arrayOfTokens.get(currentIndex).spelling;
    accept(Token.IDENTIFIER);

    return variavel;
  }

  public nodePrograma parse() {
    return parse_programa();
  }
}

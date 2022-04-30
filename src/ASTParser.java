/* 
 * File: ASTParser.java
 * Date: Spring 2022
 * Auth: Dominic MacIsaac
 * Desc: Type Checks the AST
 *
 *
 */


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ASTParser {
  private Lexer lexer = null;
  private Token currToken = null;
  private Token pastToken = null;
  private boolean currIsArr = false;
  private final boolean DEBUG = false;

  private final static int NO_ARRAY_CHECK = 0;
  private final static int CHECK_ARRAY = 1;

  /** 
   */
  public ASTParser(Lexer lexer) {
    this.lexer = lexer;
  }

  /**
   */
  public Program parse() throws MyPLException
  {
    // <program> ::= (<tdecl> | <fdecl>)*
    Program progNode = new Program();
    advance();
    TypeDecl typeDecl;
    FunDecl funDecl;
    while (!match(TokenType.EOS)) {
      if (match(TokenType.TYPE)) {
        typeDecl = new TypeDecl();
        tdecl(typeDecl);
        progNode.tdecls.add(typeDecl);
      }
      else {
        funDecl = new FunDecl();
        fdecl(funDecl);
        progNode.fdecls.add(funDecl);
      }
    }
    advance(); // eat the EOS token
    return progNode;
  }

  
  //------------------------------------------------------------ 
  // Helper Functions
  //------------------------------------------------------------

  // get next token
  private void advance() throws MyPLException {
    currToken = lexer.nextToken();
  }

  // advance if current token is of given type, otherwise error
  private void eat(TokenType t, String msg) throws MyPLException {
    if (match(t))
      advance();
    else
      error(msg);
  }

  // true if current token is of type t
  private boolean match(TokenType t) {
    return currToken.type() == t;
  }
  
  // throw a formatted parser error
  private void error(String msg) throws MyPLException {
    String s = msg + ", found '" + currToken.lexeme() + "' ";
    s += "at line " + currToken.line();
    s += ", column " + currToken.column();
    throw MyPLException.ParseError(s);
  }

  // output a debug message (if DEBUG is set)
  private void debug(String msg) {
    if (DEBUG)
      System.out.println("[debug]: " + msg);
  }


  // return true if current token is a (non-id) primitive type
  private boolean isPrimitiveType() {
    return match(TokenType.INT_TYPE) || match(TokenType.DOUBLE_TYPE) ||
            match(TokenType.BOOL_TYPE) || match(TokenType.CHAR_TYPE) ||
            match(TokenType.STRING_TYPE);
  }

  // return true if current token is a (non-id) primitive value
  private boolean isPrimitiveValue() {
    return match(TokenType.INT_VAL) || match(TokenType.DOUBLE_VAL) ||
            match(TokenType.BOOL_VAL) || match(TokenType.CHAR_VAL) ||
            match(TokenType.STRING_VAL);
  }

  // return true if current token starts an expression
  private boolean isExpr() {
    return match(TokenType.NOT) || match(TokenType.LPAREN) ||
            match(TokenType.NIL) || match(TokenType.NEW) ||
            match(TokenType.ID) || match(TokenType.NEG) ||
            match(TokenType.INT_VAL) || match(TokenType.DOUBLE_VAL) ||
            match(TokenType.BOOL_VAL) || match(TokenType.CHAR_VAL) ||
            match(TokenType.STRING_VAL);
  }

  private boolean isOperator() {
    return match(TokenType.PLUS) || match(TokenType.MINUS) ||
            match(TokenType.DIVIDE) || match(TokenType.MULTIPLY) ||
            match(TokenType.MODULO) || match(TokenType.AND) ||
            match(TokenType.OR) || match(TokenType.EQUAL) ||
            match(TokenType.LESS_THAN) || match(TokenType.GREATER_THAN) ||
            match(TokenType.LESS_THAN_EQUAL) || match(TokenType.GREATER_THAN_EQUAL) ||
            match(TokenType.NOT_EQUAL);
  }


  //------------------------------------------------------------
  // Recursive Descent Functions
  //------------------------------------------------------------

  private void tdecl(TypeDecl typeDecl) throws MyPLException {

    eat(TokenType.TYPE, "expecting type in tdecl");

    if(match(TokenType.ID)){
      typeDecl.typeName = currToken;
      currIsArr = false;
      advance();
    }
    else{
      error("expecting ID in tdecl");
    }
    eat(TokenType.LBRACE, "expecting LBRACE in tdecl");

    List<VarDeclStmt> vdecls = new ArrayList<>();
    vdecls(vdecls);
    typeDecl.vdecls = vdecls;
    eat(TokenType.RBRACE, "expecting RBRACE in tdecl");

  }

  private void vdecls(List<VarDeclStmt> vdecls) throws MyPLException{
    VarDeclStmt varDeclStmt;
    while(match(TokenType.VAR) || match(TokenType.ARR)){
      varDeclStmt = new VarDeclStmt();
      vdecl_stmt(varDeclStmt);
      vdecls.add(varDeclStmt);
    }
  } //FIN

  private void fdecl(FunDecl funDecl) throws MyPLException{
    eat(TokenType.FUN, "expecting FUN in fdecl");
    if(!match(TokenType.VOID_TYPE)){
      funDecl.returnType = currToken;
      funDecl.returnArray = dtype(CHECK_ARRAY);
    }
    else{
      funDecl.returnType = currToken;
      eat(TokenType.VOID_TYPE, "expecting VOID in fdecl");
    }
    funDecl.funName = currToken;
    eat(TokenType.ID, "expecting ID in fdecl");
    eat(TokenType.LPAREN, "expecting LPAREN in fdecl");
    List<FunParam> funParams = new ArrayList<>();
    params(funParams);
    funDecl.params = funParams;
    eat(TokenType.RPAREN, "expecting RPAREN in fdecl");
    eat(TokenType.LBRACE, "expecting LBRACE in fdecl");
    List<Stmt> stmts = new ArrayList<>();
    stmts(stmts);
    funDecl.stmts = stmts;
    eat(TokenType.RBRACE, "expecting RBRACE in fdecl");
  } //FIN

  private void params(List<FunParam> params) throws MyPLException{ //FIN
    FunParam funParam;
    if(isPrimitiveType() || match(TokenType.ID)) {
      funParam = new FunParam();
      funParam.paramType = currToken;
      funParam.isArray = dtype(CHECK_ARRAY);
      funParam.paramName = currToken;
      eat(TokenType.ID, "expecting ID in params");
      params.add(funParam);
      while (match(TokenType.COMMA)) {
        funParam = new FunParam();
        advance();
        funParam.paramType = currToken;
        funParam.isArray = dtype(CHECK_ARRAY);
        funParam.paramName = currToken;
        eat(TokenType.ID, "expecting ID in params");
        params.add(funParam);
      }
    }
  } //FIN

  private boolean dtype(int mode) throws MyPLException{
    if(isPrimitiveType() || match(TokenType.ID)){
      advance();
    }
    else{
      error("expecting dtype");
    }
    if(mode == CHECK_ARRAY){
      if(match(TokenType.LBRACK)){
        advance();
        eat(TokenType.RBRACK, "expected RBRACK in dtype");
        return true;
      }
    }
    return false;
  }

  private void stmts(List<Stmt> stmts) throws MyPLException{
    while(match(TokenType.VAR) ||match(TokenType.ARR) || match(TokenType.ID) || match(TokenType.IF)
            || match(TokenType.WHILE) || match(TokenType.FOR)
            ||match(TokenType.RETURN) || match(TokenType.DELETE)){
      stmt(stmts);
    }
  } //FIN

  private void stmt(List<Stmt> stmts) throws MyPLException{ //FIN
    if(match(TokenType.VAR) || match(TokenType.ARR)){
      VarDeclStmt stmt = new VarDeclStmt();
      vdecl_stmt(stmt);
      stmts.add(stmt);
    }
    else if(match(TokenType.IF)){
      CondStmt stmt = new CondStmt();
      cond_stmt(stmt);
      stmts.add(stmt);
    }
    else if(match(TokenType.WHILE)){
      WhileStmt stmt = new WhileStmt();
      while_stmt(stmt);
      stmts.add(stmt);
    }
    else if(match(TokenType.FOR)){
      ForStmt stmt = new ForStmt();
      for_stmt(stmt);
      stmts.add(stmt);
    }
    else if(match(TokenType.RETURN)){
      ReturnStmt stmt = new ReturnStmt();
      ret_stmt(stmt);
      stmts.add(stmt);
    }
    else if(match(TokenType.DELETE)){
      DeleteStmt stmt = new DeleteStmt();
      delete_stmt(stmt);
      stmts.add(stmt);
    }
    else if(match(TokenType.ID)){
      pastToken = currToken;
      advance();
      if(match(TokenType.LPAREN)){
        CallExpr stmt = new CallExpr();
        call_expr(stmt);
        stmts.add(stmt);
      }
      else{
        AssignStmt stmt = new AssignStmt();
        assign_stmt(stmt);
        stmts.add(stmt);
      }
    }
    else{
      error("expecting stmt");
    }
  } //FIN

  private void vdecl_stmt(VarDeclStmt varDeclStmt) throws MyPLException{
    varDeclStmt.isArray = match(TokenType.ARR);
    currIsArr = varDeclStmt.isArray;
    List<Expr> exprs = new ArrayList<>();
    if(match(TokenType.VAR) || varDeclStmt.isArray){
      advance();
    }
    else{
      error("incorrect start of variable declarations statement");
    }

    Token explicitDecToken = null;

    if(isPrimitiveType() || match(TokenType.ID)){
      explicitDecToken = currToken;
      dtype(NO_ARRAY_CHECK);
      if(match(TokenType.ID)){
        varDeclStmt.typeName = explicitDecToken;
        varDeclStmt.varName = currToken;
        advance();
      }
      else{
        varDeclStmt.varName = explicitDecToken;
      }
      explicitDecToken = null;
    }

    eat(TokenType.ASSIGN, "expecting ASSIGN in vdecl_stmt");
    if(varDeclStmt.isArray){
      eat(TokenType.LBRACE, "expected lbrace in array vdecl_stmt");
      args(exprs);
      eat(TokenType.RBRACE, "expected rbrace in array vdecl_stmt");
    }
    else{
      Expr expr = new Expr();
      expr(expr);
      exprs.add(expr);
    }
    varDeclStmt.exprs = exprs;
  } //FIN

  private void assign_stmt(AssignStmt assignStmt) throws MyPLException{
    List<PathHolder> lvalue = new ArrayList<>();
    lvalue(lvalue);
    assignStmt.lvalue = lvalue;

    eat(TokenType.ASSIGN, "expecting ASSIGN in assign_stmt");

    Expr expr = new Expr();
    expr(expr);
    assignStmt.expr = expr;
  } //FIM

  private void lvalue(List<PathHolder> lvalue) throws MyPLException{
    PathHolder currentMap = null;
    if(match(TokenType.ID) && pastToken == null){
      currentMap = new PathHolder(currToken, currToken.type());
      lvalue.add(currentMap);
      advance();
    }
    else if(pastToken.type() == TokenType.ID){
      currentMap = new PathHolder(pastToken, pastToken.type());
      lvalue.add(currentMap);
      pastToken = null;
    }
    else{
      error("expecting ID in lvalue");
    }

    boolean switched = false;
    while(match(TokenType.DOT) || match(TokenType.LBRACK)){
      if(match(TokenType.LBRACK)){
        if(switched){
          error("double accessor in lvalue");
        }
        lvalue.get(lvalue.size()-1).replace(lvalue.get(lvalue.size()-1).first, TokenType.ARR);
        advance();
        Expr expr = new Expr();
        expr(expr);
        currentMap = new PathHolder(expr, TokenType.INT_VAL);
        lvalue.add(currentMap);
        eat(TokenType.RBRACK, "expected RBrack in assign stmt for arr");
        switched = true;
      }
      else {
        advance();
        currentMap = new PathHolder(currToken, currToken.type());
        lvalue.add(currentMap);
        eat(TokenType.ID, "expecting ID in lvalue");
        switched = false;
      }
    }
  } //FIN

  private void cond_stmt(CondStmt condStmt) throws MyPLException{

    eat(TokenType.IF, "expecting IF in cond_stmt");
    BasicIf basicIf = new BasicIf();
    Expr cond = new Expr();
    expr(cond);
    basicIf.cond = cond;
    eat(TokenType.LBRACE, "expecting LBRACE in cond_stmt");
    List<Stmt> stmts = new ArrayList<>();
    stmts(stmts);
    basicIf.stmts = stmts;
    condStmt.ifPart = basicIf;
    eat(TokenType.RBRACE, "expecting RBRACE in cond_stmt");

    List<BasicIf> elifs = new ArrayList<>();
    List<Stmt> elseStmts = new ArrayList<>();
    condStmt.elifs = elifs;
    condStmt.elseStmts = elseStmts;
    condt(condStmt);
  } //FIN

  private void condt(CondStmt condStmt) throws MyPLException{
    if(match(TokenType.ELIF)) {
      BasicIf basicIf = new BasicIf();
      advance();

      Expr cond = new Expr();
      expr(cond);
      basicIf.cond = cond;

      eat(TokenType.LBRACE, "expecting LBRACE in condt");

      List<Stmt> stmts = new ArrayList<>();
      stmts(stmts);
      basicIf.stmts = stmts;
      condStmt.elifs.add(basicIf);

      eat(TokenType.RBRACE, "expecting RBRACE in condt");
      condt(condStmt);
    }
    else if (match(TokenType.ELSE)){
      advance();
      eat(TokenType.LBRACE, "expecting LBRACE in condt");

      stmts(condStmt.elseStmts);
      eat(TokenType.RBRACE, "expecting RBRACE in condt");
    }
  } //FIN

  private void while_stmt(WhileStmt whileStmt) throws MyPLException{
    eat(TokenType.WHILE, "expecting WHILE in while_stmt");

    Expr expr = new Expr();
    expr(expr);
    whileStmt.cond = expr;

    eat(TokenType.LBRACE, "expecting LBRACE in while_stmt");

    List<Stmt> stmts = new ArrayList<>();
    stmts(stmts);
    whileStmt.stmts = stmts;

    eat(TokenType.RBRACE, "expecting RBRACE in while_stmt");
  } //FIN

  private void for_stmt(ForStmt forStmt) throws MyPLException{
    eat(TokenType.FOR, "expecting FOR in for_stmt");
    forStmt.varName = currToken;
    eat(TokenType.ID, "expecting ID in for_stmt");
    eat(TokenType.FROM, "expecting FROM in for_stmt");

    Expr start = new Expr();
    expr(start);
    forStmt.start = start;

    if(match(TokenType.UPTO)){
      forStmt.upto = true;
      advance();
    }
    else if(match(TokenType.DOWNTO)){
      forStmt.upto = false;
      advance();
    }
    else{
      error("expecting UPTO or DOWNTO in for_stmt");
    }
    Expr end = new Expr();
    expr(end);
    forStmt.end = end;

    eat(TokenType.LBRACE, "expecting LBRACE in for_stmt");

    List<Stmt> stmts = new ArrayList<>();
    stmts(stmts);
    forStmt.stmts = stmts;

    eat(TokenType.RBRACE, "expecting RBRACE in for_stmt");
  } //FIN

  private void call_expr(CallExpr callExpr) throws MyPLException{
    if(match(TokenType.ID) && pastToken == null){
      callExpr.funName = currToken;
      advance();
    }
    else if(pastToken.type() == TokenType.ID){
      callExpr.funName = pastToken;
      pastToken = null;
    }
    else{
      error("expecting ID in call_expr");
    }
    eat(TokenType.LPAREN, "expecting LPAREN in call_expr");
    List<Expr> args = new ArrayList<>();
    args(args);
    callExpr.args = args;
    eat(TokenType.RPAREN, "expecting RPAREN in call_expr");
  } //FIN

  private void args(List<Expr> args) throws MyPLException{
    Expr expr;
    if(match(TokenType.NOT) || match(TokenType.LPAREN) || isPrimitiveValue()
            || match(TokenType.NIL) | match(TokenType.NEW)|| match(TokenType.NEG)
            || match(TokenType.ID)){
      expr = new Expr();
      expr(expr);
      args.add(expr);
    }
    while(match(TokenType.COMMA)){
      advance();
      expr = new Expr();
      expr(expr);
      args.add(expr);
    }
  } //FIN

  private void ret_stmt(ReturnStmt returnStmt) throws MyPLException{
    Expr expr = new Expr();
    eat(TokenType.RETURN, "expecting RETURN in ret_stmt");
    if(match(TokenType.NOT) || match(TokenType.LPAREN) || isPrimitiveValue()
            || match(TokenType.NIL) | match(TokenType.NEW)|| match(TokenType.NEG)
            || match(TokenType.ID)){
      expr(expr);
      returnStmt.expr = expr;
    }
  } //FIN

  private void delete_stmt(DeleteStmt deleteStmt) throws MyPLException{
    eat(TokenType.DELETE, "expecting DELETE in delete_stmt");

    deleteStmt.varName = currToken;
    eat(TokenType.ID, "expecting ID in delete_stmt");
  } //FIN

  private void expr(Expr expr) throws MyPLException{
    Expr rest;

    if(match(TokenType.NOT)){
      expr.logicallyNegated = true;
      advance();
      ComplexTerm complexTerm = new ComplexTerm();
      complexTerm.expr = new Expr();
      expr(complexTerm.expr);
      expr.first = complexTerm;
    }
    else if(match(TokenType.LPAREN)){
      advance();
      ComplexTerm complexTerm = new ComplexTerm();
      complexTerm.expr = new Expr();
      expr(complexTerm.expr);
      expr.first = complexTerm;
      eat(TokenType.RPAREN, "expecting RPAREN in expr");
    }
    else{
      SimpleTerm simpleTerm = new SimpleTerm();
      simpleTerm.rvalue = rvalue(simpleTerm.rvalue);
      expr.first = simpleTerm;
    }

    if(isOperator()){
      expr.op = currToken;
      operator();
      rest = new Expr();
      expr(rest);
      expr.rest = rest;
    }

  } //FIN

  private void operator() throws MyPLException{
    if(isOperator()){
      advance();
    }
    else{
      error("expecting operator");
    }
  }

  private RValue rvalue(RValue rValue) throws MyPLException{
    if(isPrimitiveValue()){
      SimpleRValue simpleRValue = new SimpleRValue();
      simpleRValue.value = currToken;
      pval();
      return simpleRValue;
    }
    else if(match(TokenType.NIL)){
      SimpleRValue simpleRValue = new SimpleRValue();
      simpleRValue.value = currToken;
      advance();
      return simpleRValue;
    }
    else if(match(TokenType.NEG)){ //NegatedRValue
      advance();
      NegatedRValue negatedRValue = new NegatedRValue();
      Expr expr = new Expr();
      expr(expr);
      negatedRValue.expr = expr;
      return negatedRValue;
    }
    else if(match(TokenType.NEW)){ // NewRValue
      advance();
      NewRValue newRValue = new NewRValue();
      newRValue.typeName = currToken;
      eat(TokenType.ID, "expecting ID in rvalue");
      return newRValue;
    }
    else if(match(TokenType.ID)){
      pastToken = currToken;
      advance();
      if(match(TokenType.LPAREN)){
        CallExpr callExpr = new CallExpr();
        call_expr(callExpr); //CallExpr
        return callExpr;
      }
      else{
        IDRValue idrValue = new IDRValue();
        idrval(idrValue); //IDRValue
        return idrValue;
      }
    }
    else{
      error("expecting rvalue");
    }
    return null;
  } //FIN

  private void pval() throws MyPLException{
    if(isPrimitiveValue()){
      advance();
    }
    else{
      error("expecting primitive value");
    }
  }

  private void idrval(IDRValue idrValue) throws MyPLException{
    PathHolder currentMap = null;
    if(match(TokenType.ID) && pastToken == null){
      currentMap = new PathHolder(currToken, currToken.type());
      idrValue.path.add(currentMap);
      advance();
    }
    else if(pastToken.type() == TokenType.ID){
      currentMap = new PathHolder(pastToken, pastToken.type());
      idrValue.path.add(currentMap);
      pastToken = null;
    }
    else{
      error("expecting ID in lvalue");
    }
    boolean switched = false;
    while(match(TokenType.DOT) || match(TokenType.LBRACK)){
      if(match(TokenType.LBRACK)){
        if(switched){
          error("Double accessor in idrval");
        }
        idrValue.path.get(idrValue.path.size()-1).replace(idrValue.path.get(idrValue.path.size()-1).first, TokenType.ARR);
        advance();
        Expr expr = new Expr();
        expr(expr);
        currentMap = new PathHolder(expr, TokenType.INT_VAL);
        idrValue.path.add(currentMap);
        eat(TokenType.RBRACK, "expected RBrack in assign stmt for arr");
        switched = true;
      }
      else {
        advance();
        currentMap = new PathHolder(currToken, currToken.type());
        idrValue.path.add(currentMap);
        eat(TokenType.ID, "expecting ID in lvalue");
        switched = false;
      }
    }
  } //FIN

}

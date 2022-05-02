/*
 * File: StaticChecker.java
 * Date: Spring 2022
 * Auth: Dominic MacIsaac
 * Desc: Uses a visitor pattern to do static checking on the AST
 *
 * Bazel commands: bazel build //:mypl  || bazel test --test_output=errors //... || bazel test --test_output=errors //:lexer-test
 * bazel-bin/mypl examples/
 */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// NOTE: Some of the following are filled in, some partly filled in,
// and most left for you to fill in. The helper functions are provided
// for you to use as needed. 


public class StaticChecker implements Visitor {

  // the symbol table
  private SymbolTable symbolTable = new SymbolTable();

  // the current expression type
  private String currType = null;
  private boolean currIsArr = false;

  // the program's user-defined (record) types and function signatures
  private TypeInfo typeInfo = null;

  private String currArr = null;

  //--------------------------------------------------------------------
  // helper functions:
  //--------------------------------------------------------------------
  
  // generate an error
  private void error(String msg, Token token) throws MyPLException {
    String s = msg;
    if (token != null)
      s += " near line " + token.line() + ", column " + token.column();
    throw MyPLException.StaticError(s);
  }

  // return all valid types
  // assumes user-defined types already added to symbol table
  private List<String> getValidTypes() {
    List<String> types = new ArrayList<>();
    types.addAll(Arrays.asList("int", "double", "bool", "char", "string",
                               "void"));
    for (String type : typeInfo.types())
      if (symbolTable.get(type).type.equals("type") || symbolTable.get(type).isArray)
        types.add(type);
    return types;
  }

  private List<String> getValidTypesNoArr(){
    List<String> types = new ArrayList<>();
    types.addAll(Arrays.asList("int", "double", "bool", "char", "string",
            "void"));
    for (String type : typeInfo.types())
      if (symbolTable.get(type).type.equals("type"))
        types.add(type);
    return types;
  }

  // return the build in function names
  private List<String> getBuiltinFunctions() {
    return Arrays.asList("print", "read", "length", "get", "stoi",
                         "stod", "itos", "itod", "dtos", "dtoi", "size", "add", "remove");
  }
  
  // check if given token is a valid function signature return type
  private void checkReturnType(Token typeToken) throws MyPLException {
    if (!getValidTypesNoArr().contains(typeToken.lexeme())) {
      String msg = "'" + typeToken.lexeme() + "' is an invalid return type";
      error(msg, typeToken);
    }
  }

  // helper to check if the given token is a valid parameter type
  private void checkParamType(Token typeToken) throws MyPLException {
    if (typeToken.equals("void"))
      error("'void' is an invalid parameter type", typeToken);
    else if (!getValidTypesNoArr().contains(typeToken.lexeme())) {
      String msg = "'" + typeToken.lexeme() + "' is an invalid return type";
      error(msg, typeToken);
    }
  }

  // helpers to get first token from an expression for calls to error
  private Token getFirstToken(Expr expr) {
    return getFirstToken(expr.first);
  }

  private Token getFirstToken(ExprTerm term) {
    if (term instanceof SimpleTerm)
      return getFirstToken(((SimpleTerm)term).rvalue);
    else
      return getFirstToken(((ComplexTerm)term).expr);
  }

  private Token getFirstToken(RValue rvalue) {
    if (rvalue instanceof SimpleRValue)
      return ((SimpleRValue)rvalue).value;
    else if (rvalue instanceof NewRValue)
      return ((NewRValue)rvalue).typeName;
    else if (rvalue instanceof IDRValue) {
        return (Token) ((IDRValue)rvalue).path.get(0).first;
    }
    else if (rvalue instanceof CallExpr)
      return ((CallExpr)rvalue).funName;
    else 
      return getFirstToken(((NegatedRValue)rvalue).expr);
  }

  
  //---------------------------------------------------------------------
  // constructor
  //--------------------------------------------------------------------
  
  public StaticChecker(TypeInfo typeInfo) {
    this.typeInfo = typeInfo;
  }
  

  //--------------------------------------------------------------------
  // top-level nodes
  //--------------------------------------------------------------------
  
  public void visit(Program node) throws MyPLException {
    // push the "global" environment
    symbolTable.pushEnvironment();
    // (1) add each user-defined type name to the symbol table and to
    // the list of rec types, check for duplicate names
    for (TypeDecl tdecl : node.tdecls) {
      String t = tdecl.typeName.lexeme();
      if (symbolTable.nameExists(t)) {
        error("type '" + t + "' already defined", tdecl.typeName);
      }
      // add as a record type to the symbol table
      symbolTable.add(t,  new TypeHolder("type"));
      //system.out.println("added " + t + " to symbol table");
      // add initial type info (rest added by TypeDecl visit function)
      typeInfo.add(t);
    }
    //System.out.println("1" + symbolTable);
    for (FunDecl fdecl : node.fdecls) {
      String funName = fdecl.funName.lexeme();
      // make sure not redefining built-in functions
      if (getBuiltinFunctions().contains(funName)) {
        String m = "cannot redefine built in function " + funName;
        error(m, fdecl.funName);
      }
      // check if function already exists
      if (symbolTable.nameExists(funName)) {
        error("function '" + funName + "' already defined", fdecl.funName);
      }
      // make sure the return type is a valid type
      checkReturnType(fdecl.returnType);

      // add to the symbol table as a function

      symbolTable.add(funName, new TypeHolder("fun"));
      //System.out.println("added " + funName + " to symbol table");
      // add to typeInfo
      typeInfo.add(funName);
      // add each formal parameter as a component type
      for(FunParam param: fdecl.params){
        if(typeInfo.components(funName).contains(param.paramName.lexeme())){
          error("Duplicate param ids",param.paramName);
        }
        checkParamType(param.paramType);
        typeInfo.add(funName,param.paramName.lexeme(),new TypeHolder(param.paramType.lexeme(), param.isArray));
      }
      // add the return type
      typeInfo.add(funName, "return", new TypeHolder(fdecl.returnType.lexeme(), fdecl.returnArray));
    }

    // ensure "void main()" defined and it has correct signature
    if(!symbolTable.nameExists("main")){
      error("main does not exist", null);
    }
    if(!typeInfo.get("main", "return").type.equals("void")){
      error("main not defined with return type 'void'", null);
    }

    if(typeInfo.components("main").size()>1){
      error("main incorrectly defined", null);
    }
    //System.out.println("2" + symbolTable);

    // check each type and function
    for (TypeDecl tdecl : node.tdecls) 
      tdecl.accept(this);
    for (FunDecl fdecl : node.fdecls) 
      fdecl.accept(this);

    // all done, pop the global table
    symbolTable.popEnvironment();
  }

  public void visit(TypeDecl node) throws MyPLException {
    String typeName = node.typeName.lexeme();
    symbolTable.pushEnvironment();
    for(VarDeclStmt varDeclStmt: node.vdecls){
      varDeclStmt.accept(this);
      String nameOfVar = varDeclStmt.varName.lexeme();
      typeInfo.add(typeName, nameOfVar, new TypeHolder(currType, currIsArr));
    }
    symbolTable.popEnvironment();
  }

  public void visit(FunDecl node) throws MyPLException {
    symbolTable.pushEnvironment();
    for(FunParam param: node.params){
      symbolTable.add(param.paramName.lexeme(), new TypeHolder(param.paramType.lexeme(), param.isArray));
      //System.out.println("added " + param.paramName.lexeme() + " to symbol table");
    }
    symbolTable.add("return",new TypeHolder(node.returnType.lexeme(),node.returnArray));
    for(Stmt stmt: node.stmts){
      stmt.accept(this);
    }
    //System.out.println(node.funName.lexeme() + " fun param " + symbolTable);
    symbolTable.popEnvironment();
  }


  //--------------------------------------------------------------------
  // statement nodes
  //--------------------------------------------------------------------
  
  public void visit(VarDeclStmt node) throws MyPLException {
    //check that variable name hasn't been used in the current environment before
    if (symbolTable.nameExistsInCurrEnv(node.varName.lexeme())) {
      error("duplicate variable name", node.varName);
    }
    // var is NOT an array
    if(!node.isArray) {
        //it only has one expr since its not an array
      node.exprs.get(0).accept(this);

      //check that expr was not a type or a function
      if (currType.equals("type") || currType.equals("fun")) {
        error("invalid variable declaration type", getFirstToken(node.exprs.get(0)));
      }

      //Explicit Declaration
      if (node.typeName != null) {
        if (!node.typeName.lexeme().equals(currType) && !currType.equals("void")) {
          error("explicit var type does not match assignment type", node.typeName);
        }
        currType = node.typeName.lexeme();
        symbolTable.add(node.varName.lexeme(), new TypeHolder(currType, false));
      } //Implicit Declaration
      else {
        if (currType.equals("void")) { //implicit can't be void
          error("implicit var declaration cannot be assigned nil", node.varName);
        }
        symbolTable.add(node.varName.lexeme(), new TypeHolder(currType, false));
      }

      if(currIsArr){
          error("can't have array variable in vardeclstmt", node.varName);
      }
      currIsArr = false;
    }
    //var IS an array
    else{
      //No exprs + implicit Declaration check
      if(node.exprs.size() == 0){
        if(node.typeName == null){
          error("implicit var declaration cannot be assigned nil", node.varName);
        }
        symbolTable.add(node.varName.lexeme(), new TypeHolder(node.typeName.lexeme(), true));
        currType = node.typeName.lexeme();
      }
      else{
        String typeOfArr = null;
        if(node.typeName != null){
          //System.out.println("var dec " + node.typeName);
          typeOfArr = node.typeName.lexeme();
        }
        for (Expr expr: node.exprs) {
          expr.accept(this);
          if(currIsArr){
            error("can't have array variable in vardeclstmt", node.varName);
          }
          //check name
          if (currType.equals("type") || currType.equals("fun")) {
            error("invalid variable declaration type in array", getFirstToken(expr));
          }

          //Explict Declaration - check that all exprs are the same type
          if (node.typeName != null) {
            if (!node.typeName.lexeme().equals(currType) && !currType.equals("void")) {
              error("explicit var type does not match assignment type in array", node.typeName);
            }
          }
          //Implicit Declaration - check that all exprs are the same type
          else {
            if (typeOfArr == null) {
                typeOfArr = currType;
            }
            else {
              if (!typeOfArr.equals(currType)) {
                error("variables in array not homogenous", node.typeName);
              }
            }
          }
        }
        currType = typeOfArr;
        symbolTable.add(node.varName.lexeme(), new TypeHolder(typeOfArr, true));

      }
      currIsArr = true;
    }
  }

  public void visit(AssignStmt node) throws MyPLException {
      Token currentToken = (Token) node.lvalue.get(0).first;
      int pathSize = node.lvalue.size();
      //if no path - simple id
      if(pathSize == 1){
          if(symbolTable.nameExists(currentToken.lexeme())) {
              currType = symbolTable.get(currentToken.lexeme()).type;
              currIsArr = symbolTable.get(currentToken.lexeme()).isArray;
          }
          else{
              error("idrvalue error", currentToken);
          }
      }
      else {
        //path
        String currPathName = currentToken.lexeme();
        String currPathType = symbolTable.get(currPathName).type;
        String oldPathType = "";
        boolean checkingArray = node.lvalue.get(0).isArray;
        boolean typeCheck = false;
        Expr currExpr = null;

        if (checkingArray) {
          if (!symbolTable.get(currPathName).isArray) {
            error("not an array in idrval", currentToken);
          }
        }
        else {
          if (!typeInfo.types().contains(currPathType)) {
            error("type doesn't exist", currentToken);
          }
        }

        for (int i = 1; i < pathSize; i++) {
          if (checkingArray) {
            currExpr = (Expr) node.lvalue.get(i).first;
            currExpr.accept(this);
            if (!currType.equals("int") || currIsArr) {
              error("expecting int in array access in idrval", currentToken);
            }
            currType = currPathType;
            checkingArray = node.lvalue.get(i).isArray;

            if (typeCheck) {
              if (!typeInfo.get(oldPathType, currPathName).isArray) {
                error("type incorrect", currentToken);
              }
            }
            currIsArr = false;
          }
          else {
            oldPathType = currPathType;
            currentToken = (Token) node.lvalue.get(i).first;
            currPathName = currentToken.lexeme();
            if (!typeInfo.components(currPathType).contains(currPathName)) {
              error("path doesnt exist in udt in idrval", currentToken);
            }
            currType = typeInfo.get(currPathType, currPathName).type;
            currIsArr = typeInfo.get(currPathType, currPathName).isArray;
            currPathType = currType;
            checkingArray = node.lvalue.get(i).isArray;
            typeCheck = true;
          }
        }
      }
      String lhs = currType;
      boolean lhsarray = currIsArr;
      String rhs = "";
      node.expr.accept(this);
      rhs = currType;
      boolean rhsarray = currIsArr;
      if((!lhs.equals(rhs) && !rhs.equals("void")) || (lhsarray != rhsarray)){
        error("assignment error", getFirstToken(node.expr));
      }
  }

  public void visit(CondStmt node) throws MyPLException {
    symbolTable.pushEnvironment();
    node.ifPart.cond.accept(this);
    if(!currType.equals("bool")|| currIsArr){
      error("non-bool expression in if statement", getFirstToken(node.ifPart.cond));
    }
    for(Stmt stmt: node.ifPart.stmts){
      stmt.accept(this);
    }
    symbolTable.popEnvironment();

    for(BasicIf elifs : node.elifs){
      symbolTable.pushEnvironment();
      elifs.cond.accept(this);
      if(!currType.equals("bool")|| currIsArr){
        error("non-bool expression in if statement", getFirstToken(elifs.cond));
      }
      for(Stmt stmt: elifs.stmts){
        stmt.accept(this);
      }
      symbolTable.popEnvironment();
    }

    symbolTable.pushEnvironment();
    for(Stmt stmt: node.elseStmts){
      stmt.accept(this);
    }
    symbolTable.popEnvironment();

  }

  public void visit(WhileStmt node) throws MyPLException {
    symbolTable.pushEnvironment();
    node.cond.accept(this);
    if(!currType.equals("bool") || currIsArr){
      error("non-bool expression in while loop", getFirstToken(node.cond));
    }
    for(Stmt stmt: node.stmts){
      stmt.accept(this);
    }
    symbolTable.popEnvironment();
  }

  public void visit(ForStmt node) throws MyPLException {
    symbolTable.pushEnvironment();
    symbolTable.add(node.varName.lexeme(), new TypeHolder("int"));
    node.start.accept(this);
    if(!currType.equals("int") || currIsArr){
      error("non int expression in start of for loop", getFirstToken(node.start));
    }
    node.end.accept(this);
    if(!currType.equals("int")|| currIsArr){
      error("non int expression in end of for loop", getFirstToken(node.end));
    }
    for(Stmt stmt: node.stmts){
      stmt.accept(this);
    }
    symbolTable.popEnvironment();
  }

  public void visit(ReturnStmt node) throws MyPLException {
    if(node.expr != null){
      node.expr.accept(this);
    }
    else{
      currType = "void";
    }
    TypeHolder returnType = symbolTable.get("return");
    if(!currType.equals("void") && !currType.equals(returnType.type)){
      error("invalid return type", getFirstToken(node.expr));
    }
    if(currIsArr != returnType.isArray){
        error("invalid return type", getFirstToken(node.expr));
    }

  } //DONE

  public void visit(DeleteStmt node) throws MyPLException {
    if(!symbolTable.nameExists(node.varName.lexeme())){
      error("variable id being deleted does not exist", node.varName);
    }
    if(symbolTable.get(node.varName.lexeme()).type.equals("int") || symbolTable.get(node.varName.lexeme()).type.equals("double")||
            symbolTable.get(node.varName.lexeme()).type.equals("char") || symbolTable.get(node.varName.lexeme()).type.equals("string") ||
            symbolTable.get(node.varName.lexeme()).type.equals("bool") || symbolTable.get(node.varName.lexeme()).type.equals("fun")||
            symbolTable.get(node.varName.lexeme()).type.equals("void")){
      error("variable id being deleted is not a UDT", node.varName);
    }

    if(!typeInfo.types().contains(symbolTable.get(node.varName.lexeme()).type)){
      error("variable id being deleted is not a UDT", node.varName);
    }


  } //DONE


  //----------------------------------------------------------------------
  // statement and rvalue node
  //----------------------------------------------------------------------

  private boolean checkBuiltIn(CallExpr node) throws MyPLException {
    String funName = node.funName.lexeme();
    if (funName.equals("print")) {
      node.args.get(0).accept(this);
      // has to have one argument, any type is allowed
      if (node.args.size() != 1)
        error("print expects one argument", node.funName);
      if(currIsArr){
          error("print doesn't accept arrays", node.funName);
      }
      currType = "void";
      currIsArr = false;
      return true;
    }
    else if (funName.equals("read")) {
      // no arguments allowed
      if (node.args.size() != 0)
        error("read takes no arguments", node.funName);
      currType = "string";
      currIsArr = false;

      return true;
    }
    else if (funName.equals("length")) {
      // one string argument
      if (node.args.size() != 1)
        error("length expects one argument", node.funName);
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("string") || currIsArr)
        error("expecting string in length", getFirstToken(e));
      currType = "int";
      currIsArr = false;
      return true;
    }
    else if (funName.equals("get")) {
      if (node.args.size() != 2)
        error("get expects two argument", node.funName);
      Expr e1 = node.args.get(0);
      e1.accept(this);
      if (!currType.equals("int") || currIsArr)
        error("expecting int in get at first argument", getFirstToken(e1));
      Expr e2 = node.args.get(1);
      e2.accept(this);
      if (!currType.equals("string")|| currIsArr)
        error("expecting string in get at second argument", getFirstToken(e2));
      currType = "char";
      currIsArr = false;
      return true;
    }
    else if (funName.equals("stoi")) {
      if (node.args.size() != 1) {
        error("stoi expects one argument", node.funName);
      }
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("string")|| currIsArr) {
        error("expecting string in stoi", getFirstToken(e));
      }
      currType = "int";
      currIsArr = false;
      return true;
    }
    else if (funName.equals("stod")) {
      if (node.args.size() != 1) {
        error("stod expects one argument", node.funName);
      }
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("string")|| currIsArr) {
        error("expecting string in stod", getFirstToken(e));
      }
      currType = "double";
      currIsArr = false;
      return true;
    }
    else if (funName.equals("itos")) {
      if (node.args.size() != 1) {
        error("itos expects one argument", node.funName);
      }
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("int")|| currIsArr) {
        error("expecting int in itos", getFirstToken(e));
      }
      currType = "string";
      currIsArr = false;
      return true;
    }
    else if (funName.equals("itod")) {
      if(node.args.size() != 1) {
        error("itod expects one argument", node.funName);
      }
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("int")|| currIsArr) {
        error("expecting int in itod", getFirstToken(e));
      }
      currType = "double";
      currIsArr = false;
      return true;
    }
    else if (funName.equals("dtos")) {
      if (node.args.size() != 1) {
        error("dtos expects one argument", node.funName);
      }
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("double")|| currIsArr) {
        error("expecting double in dtos", getFirstToken(e));
      }
      currType = "string";
      currIsArr = false;

      return true;
    }

    else if (funName.equals("dtoi")) {
      if (node.args.size() != 1) {
        error("dtoi expects one argument", node.funName);
      }
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currType.equals("double")|| currIsArr) {
        error("expecting double in dtoi", getFirstToken(e));
      }
      currType = "int";
      currIsArr = false;
      return true;
    }
    else if (funName.equals("size")) {
      if (node.args.size() != 1) {
        error("size expects one argument", node.funName);
      }
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currIsArr) {
        error("expecting double in size", getFirstToken(e));
      }
      currType = "int";
      currIsArr = false;
      return true;
    }

    else if (funName.equals("add")) {
      if (node.args.size() != 2) {
        error("add expects two argument", node.funName);
      }
      Expr e = node.args.get(0);
      e.accept(this);
      if (!currIsArr) {
        error("expecting arr in size", getFirstToken(e));
      }
      String arrTypeForCall = currType;
      Expr e2 = node.args.get(1);
      e2.accept(this);
      if (!arrTypeForCall.equals(currType)) {
        error("expecting value of same type of array", getFirstToken(e));
      }
      currType = "nil";
      currIsArr = false;
      return true;
    }
    else if (funName.equals("remove")) {
      if (node.args.size() != 2) {
        error("add expects two argument", node.funName);
      }
      Expr e = node.args.get(0);
      e.accept(this);
      if(!currIsArr) {
        error("expecting arr in size", getFirstToken(e));
      }
      Expr e2 = node.args.get(1);
      e2.accept(this);
      if (!currType.equals("int") || currIsArr) {
        error("expecting int value in remove", getFirstToken(e));
      }
      currType = "nil";
      currIsArr = false;
      return true;
    }

    return false;
  } //DONE

  
  public void visit(CallExpr node) throws MyPLException {
    boolean found = false;
    String paramName, paramType = "";
    boolean paramIsArr = false;
    int argSize = node.args.size();
    found = checkBuiltIn(node);
    if(!found){
      String nameOfFunct = node.funName.lexeme();
      if(!symbolTable.nameExists(nameOfFunct) || !symbolTable.get(nameOfFunct).type.equals("fun")){
        error("function, "+ nameOfFunct + ", not found", node.funName);
      }

      if(argSize + 1 != typeInfo.components(nameOfFunct).size()){
        //System.out.println(argSize +  "  " + typeInfo.components(nameOfFunct).size());
        error("incorrect amount of argument in call function", getFirstToken(node));
      }
      for(int i = 0; i < argSize; i++){
        //System.out.println("current = " + currType);
        node.args.get(i).accept(this);
        paramName = typeInfo.components(nameOfFunct).toArray()[i].toString();
        paramType = typeInfo.get(nameOfFunct, paramName).type;
        paramIsArr = typeInfo.get(nameOfFunct, paramName).isArray;
        //System.out.println(node.funName.lexeme() + "  " + paramName + " " +paramType + " current: " + currType);

        if(!currType.equals(paramType) && !currType.equals("void") || paramIsArr != currIsArr){
          error("incorrect argument in function " + node.funName.lexeme(), node.funName);
        }
      }
      paramName = typeInfo.components(nameOfFunct).toArray()[argSize].toString();
      currType = typeInfo.get(nameOfFunct, paramName).type;
      currIsArr = typeInfo.get(nameOfFunct, paramName).isArray;
    }

  } //DONE
  

  //----------------------------------------------------------------------
  // rvalue nodes
  //----------------------------------------------------------------------
  
  public void visit(SimpleRValue node) throws MyPLException {
    TokenType tokenType = node.value.type();
    currIsArr = false;
    if (tokenType == TokenType.INT_VAL)
      currType = "int";
    else if (tokenType == TokenType.DOUBLE_VAL)
      currType = "double";
    else if (tokenType == TokenType.BOOL_VAL)
      currType = "bool";
    else if (tokenType == TokenType.CHAR_VAL)    
      currType = "char";
    else if (tokenType == TokenType.STRING_VAL)
      currType = "string";
    else if (tokenType == TokenType.NIL)
      currType = "void";
  }
  
    
  public void visit(NewRValue node) throws MyPLException {
    if(symbolTable.nameExists(node.typeName.lexeme()) && symbolTable.get(node.typeName.lexeme()).type.equals("type")){
      currType = node.typeName.lexeme();
      currIsArr = false;
    }
    else{
      error("new node not a defined type",node.typeName);
    }
  }


  public void visit(IDRValue node) throws MyPLException {
    Token currentToken = (Token) node.path.get(0).first;
    int pathSize = node.path.size();
    //if no path - simple id
    if(pathSize == 1){
      if(symbolTable.nameExists(currentToken.lexeme())) {
        currType = symbolTable.get(currentToken.lexeme()).type;
        currIsArr = symbolTable.get(currentToken.lexeme()).isArray;
      }
      else{
        error("idrvalue error", currentToken);
      }
      return;
    }
    //path
    String currPathName = currentToken.lexeme();
    String currPathType = symbolTable.get(currPathName).type;
    String oldPathType = "";
    boolean checkingArray = node.path.get(0).isArray;
    boolean typeCheck = false;
    Expr currExpr = null;

    if(checkingArray){
      if(!symbolTable.get(currPathName).isArray){
        error("not an array in idrval",currentToken);
      }
    }
    else{
      if(!typeInfo.types().contains(currPathType)){
        error("type doesn't exist", currentToken);
      }
    }

    for(int i = 1; i < pathSize; i++){
      if(checkingArray) {
        currExpr = (Expr) node.path.get(i).first;
        currExpr.accept(this);
        if (!currType.equals("int") || currIsArr) {
          error("expecting int in array access in idrval", currentToken);
        }
        currType = currPathType;
        checkingArray = node.path.get(i).isArray;

        if(typeCheck){
          if (!typeInfo.get(oldPathType, currPathName).isArray) {
            error("type incorrect", currentToken);
          }
        }
        currIsArr = false;
      }
      else{
        oldPathType = currPathType;
        currentToken = (Token) node.path.get(i).first;
        currPathName = currentToken.lexeme();
        if(!typeInfo.components(currPathType).contains(currPathName)){
          error("path doesnt exist in udt in idrval", currentToken);
        }
        //System.out.println("typeInfo " + currPathType + "  " + currPathName + " " + typeInfo.get(currPathType, currPathName).type);
        //System.out.println(typeInfo.components(currPathType).toArray()[0]);
        currType = typeInfo.get(oldPathType, currPathName).type;
        currIsArr = typeInfo.get(currPathType, currPathName).isArray;
        currPathType = currType;
        checkingArray = node.path.get(i).isArray;
        typeCheck = true;
      }
    }
  }
  
      
  public void visit(NegatedRValue node) throws MyPLException {
    node.expr.accept(this);
    if(!(currType.equals("int") || currType.equals("double") || currIsArr)){
      error("negated r value on a non int or double type", node.expr.op);
    }
  }
  

  //----------------------------------------------------------------------
  // expression node
  //----------------------------------------------------------------------
  
  public void visit(Expr node) throws MyPLException {
    String lhsType = "";
    boolean lhsArr = false;
    String rhsType = "";
    boolean rhsArr = false;
    node.first.accept(this);
    lhsType = currType;
    lhsArr = currIsArr;
    if(node.rest == null) {
      if(node.logicallyNegated && !currType.equals("bool")){
        error("logical negation on non bool type", node.op);
      }
      return;
    }
    node.rest.accept(this);
    rhsType = currType;
    rhsArr = currIsArr;
    if((rhsArr && !lhsArr) ||(!rhsArr && lhsArr)){
      error("expr error, one side array one side not", getFirstToken(node));
    }
    if(rhsArr && lhsArr){
      currType = lhsType;
      return;
    }
    currIsArr = false;
    if(node.op.type() == TokenType.MINUS ||node.op.type() == TokenType.MULTIPLY || node.op.type() == TokenType.DIVIDE){
      if(lhsType.equals("int") && rhsType.equals("int")){
        currType = "int";
      }
      else if(lhsType.equals("double") && rhsType.equals("double")){
        currType = "double";
      }
      else{
        error("incorrect types for " + node.op.type().name() + " expression",getFirstToken(node));
      }
    }
    else if(node.op.type() == TokenType.PLUS){
      if((lhsType.equals("string") && rhsType.equals("string")) || (lhsType.equals("char") && rhsType.equals("string")) || (lhsType.equals("string") && rhsType.equals("char"))){
        currType = "string";
      }
      else if(lhsType.equals("int") && rhsType.equals("int")){
        currType = "int";
      }
      else if(lhsType.equals("double") && rhsType.equals("double")){
        currType = "double";
      }
      else{
        error("incorrect types for " + node.op.type().name() + " expression",getFirstToken(node));
      }

    }
    else if(node.op.type() == TokenType.MODULO ){
      if(lhsType.equals("int") && rhsType.equals("int")){
        currType = "int";
      }
      else{
        error("incorrect types for " + node.op.type().name() + " expression",getFirstToken(node));
      }
    }
    else if(node.op.type() == TokenType.EQUAL || node.op.type() == TokenType.NOT_EQUAL){
      if(lhsType.equals(rhsType) || lhsType.equals("void") || rhsType.equals("void")){
        currType = "bool";
      }
      else{
        error("incorrect types for " + node.op.type().name() + " expression",getFirstToken(node));
      }
    }
    else if(node.op.type() == TokenType.LESS_THAN || node.op.type() == TokenType.LESS_THAN_EQUAL || node.op.type() == TokenType.GREATER_THAN || node.op.type() == TokenType.GREATER_THAN_EQUAL){
      if((lhsType.equals("int") && rhsType.equals("int")) || (lhsType.equals("double") && rhsType.equals("double")) ||(lhsType.equals("string") && rhsType.equals("string")) ||(lhsType.equals("char") && rhsType.equals("char"))){
        currType = "bool";
      }
      else{
        error("incorrect types for " + node.op.type().name() + " expression",getFirstToken(node));
      }
    }
    else if(node.op.type() == TokenType.AND ||node.op.type() == TokenType.OR){
      if(!lhsType.equals("bool") || !rhsType.equals("bool")){
        error("AND and OR operators require bools on both sides", getFirstToken(node));
      }
      currType = "bool";
    }
    else{
      error("error", getFirstToken(node));
    }

    if(node.logicallyNegated && !currType.equals("bool")){
      error("logical negation on non bool type", node.op);
    }

  }


  //----------------------------------------------------------------------
  // terms
  //----------------------------------------------------------------------
  
  public void visit(SimpleTerm node) throws MyPLException {
    node.rvalue.accept(this);
  }

  public void visit(ComplexTerm node) throws MyPLException {
    node.expr.accept(this);
  }

}

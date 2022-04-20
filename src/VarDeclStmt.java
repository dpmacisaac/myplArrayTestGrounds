/*
 * File: VarDeclStmt.java
 * Date: Spring 2022
 * Auth: S. Bowers, Dominic MacIsaac
 * Desc: An AST node for representing variable declarations.
 */


import java.util.List;

public class VarDeclStmt implements Stmt {

  public Token typeName = null;
  public Token varName = null;
  public boolean isArray = false;
  public List<Expr> exprs = null;
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

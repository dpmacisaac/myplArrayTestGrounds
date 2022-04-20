/*
 * File: AssignStmt.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node representing variable assignment statements
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignStmt implements Stmt {

  public List<HashMap<Object,TokenType>> lvalue = new ArrayList<>();
  public Expr expr = null;
  
  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

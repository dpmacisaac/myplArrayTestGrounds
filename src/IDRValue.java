/*
 * File: IDRValue.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: An AST node for representing a simple path expression. A path
 *       expression provides access to the components of a
 *       user-defined type object.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IDRValue implements RValue {

  public List<HashMap<Object, TokenType>> path = new ArrayList<>(); //CHANGED

  @Override
  public void accept(Visitor visitor) throws MyPLException {
    visitor.visit(this);
  }

}

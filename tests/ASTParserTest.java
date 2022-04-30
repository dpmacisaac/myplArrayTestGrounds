/*
 * File: ASTParserTest.java
 * Date: Spring 2022
 * Auth: 
 * Desc: Basic unit tests for the MyPL ast-based parser class.
 */

import org.junit.Ignore;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;


public class ASTParserTest {

  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------
  
  private static ASTParser buildParser(String s) throws Exception {
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    ASTParser parser = new ASTParser(new Lexer(in));
    return parser;
  }

  private static String buildString(String... args) {
    String str = "";
    for (String s : args)
      str += s + "\n";
    return str;
  }

  //------------------------------------------------------------
  // TEST CASES
  //------------------------------------------------------------

  @Test
  public void emptyParse() throws Exception {
    ASTParser parser = buildParser("");
    Program p = parser.parse();
    assertEquals(0, p.tdecls.size());
    assertEquals(0, p.fdecls.size());
  }

  @Test
  public void oneTypeDeclInProgram() throws Exception {
    String s = buildString
      ("type Node {",
       "}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(1, p.tdecls.size());
    assertEquals(0, p.fdecls.size());
  }
  
  @Test
  public void oneFunDeclInProgram() throws Exception {
    String s = buildString
      ("fun void main() {",
       "}"
       );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(0, p.tdecls.size());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void multipleTypeAndFunDeclsInProgram() throws Exception {
    String s = buildString
      ("type T1 {}",
       "fun void F1() {}",
       "type T2 {}",
       "fun void F2() {}",
       "fun void main() {}");
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(2, p.tdecls.size());
    assertEquals(3, p.fdecls.size());
  }

  @Test
  public void funDeclManyStmts() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "var x = 0",
                    "var y = 2",
                    "x = y + x",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(3, p.fdecls.get(0).stmts.size());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void forStmtTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "for i from 1 upto 2 {}",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(ForStmt.class, p.fdecls.get(0).stmts.get(0).getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void forStmtUptoTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "for i from 1 upto 2 {}",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();

    ForStmt forStmt = (ForStmt) p.fdecls.get(0).stmts.get(0);
    assertEquals(true, forStmt.upto);
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void forStmtStartTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "for i from 1 upto 2 {}",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();

    ForStmt forStmt = (ForStmt) p.fdecls.get(0).stmts.get(0);
    assertNotEquals(null, forStmt.start.getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void forStmtEndTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "for i from 1 upto 2 {}",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();

    ForStmt forStmt = (ForStmt) p.fdecls.get(0).stmts.get(0);
    assertNotEquals(null, forStmt.end.getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void whileStmtTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "while x > 0 {}",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(WhileStmt.class, p.fdecls.get(0).stmts.get(0).getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void assignStmtTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "x = 1 + 2",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(AssignStmt.class, p.fdecls.get(0).stmts.get(0).getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void returnStmtTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "return x",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(ReturnStmt.class, p.fdecls.get(0).stmts.get(0).getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void DeleteStmtTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "delete x",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(DeleteStmt.class, p.fdecls.get(0).stmts.get(0).getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void CondStmtTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "if x == y {}",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(CondStmt.class, p.fdecls.get(0).stmts.get(0).getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void callExprTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "print(x,y,z)",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(CallExpr.class, p.fdecls.get(0).stmts.get(0).getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void elseIfTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "if x == y {}",
                    "elif x == z {}",
                    "elif x == a {}",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    CondStmt condStmt = (CondStmt) p.fdecls.get(0).stmts.get(0);
    assertEquals(2, condStmt.elifs.size());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void elseTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "if x == y {}",
                    "elif x == z {}",
                    "else{" +
                            "x = z}",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    CondStmt condStmt = (CondStmt) p.fdecls.get(0).stmts.get(0);
    assertEquals(1, condStmt.elseStmts.size());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void basicIfTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "if x == y { " +
                            "x = x + 1 " +
                            "print(x) " +
                            "}",
                    "elif x == z {}",
                    "else{" +
                            "x = z}",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    CondStmt condStmt = (CondStmt) p.fdecls.get(0).stmts.get(0);
    assertNotEquals(null, condStmt.ifPart);
    assertNotEquals(null, condStmt.ifPart.cond);
    assertEquals(2, condStmt.ifPart.stmts.size());
  }


  @Test
  public void funParamTest() throws Exception {
    String s = buildString
            ("fun void main(int x, string str, double z) {",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    assertEquals(3, p.fdecls.get(0).params.size());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void newRValTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "x = new node",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    AssignStmt assignStmt = (AssignStmt) p.fdecls.get(0).stmts.get(0);
    SimpleTerm simpleTerm = (SimpleTerm) assignStmt.expr.first;
    assertEquals(NewRValue.class, simpleTerm.rvalue.getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void callExprTest2() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "x = countNums(x,y)",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    AssignStmt assignStmt = (AssignStmt) p.fdecls.get(0).stmts.get(0);
    SimpleTerm simpleTerm = (SimpleTerm) assignStmt.expr.first;
    assertEquals(CallExpr.class, simpleTerm.rvalue.getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void simpleRValTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "x = 1",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    AssignStmt assignStmt = (AssignStmt) p.fdecls.get(0).stmts.get(0);
    SimpleTerm simpleTerm = (SimpleTerm) assignStmt.expr.first;
    assertEquals(SimpleRValue.class, simpleTerm.rvalue.getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void idrValTest() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "x = node.next.next.val",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    AssignStmt assignStmt = (AssignStmt) p.fdecls.get(0).stmts.get(0);
    SimpleTerm simpleTerm = (SimpleTerm) assignStmt.expr.first;
    assertEquals(IDRValue.class, simpleTerm.rvalue.getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void negatedRVal() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "x = neg x",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    AssignStmt assignStmt = (AssignStmt) p.fdecls.get(0).stmts.get(0);
    SimpleTerm simpleTerm = (SimpleTerm) assignStmt.expr.first;
    assertEquals(NegatedRValue.class, simpleTerm.rvalue.getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void emptyArr() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "arr int x = {}",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    VarDeclStmt varDeclStmt = (VarDeclStmt) p.fdecls.get(0).stmts.get(0);
    assertEquals(VarDeclStmt.class, varDeclStmt.getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void args2Arr() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "arr int x = {1,2}",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    VarDeclStmt varDeclStmt = (VarDeclStmt) p.fdecls.get(0).stmts.get(0);
    assertEquals(VarDeclStmt.class, varDeclStmt.getClass());
    assertEquals(1, p.fdecls.size());
  }

  @Test
  public void accessArr() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "x[1] = 3",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    AssignStmt assignStmt = (AssignStmt) p.fdecls.get(0).stmts.get(0);
    assertEquals(AssignStmt.class, assignStmt.getClass());
    assertEquals(1, p.fdecls.size());
  }
  @Test
  public void accessArrWithTypePath() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "y.x[1] = 3",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    AssignStmt assignStmt = (AssignStmt) p.fdecls.get(0).stmts.get(0);

    assertEquals(AssignStmt.class, assignStmt.getClass());

    assertEquals(3, assignStmt.lvalue.size());
  }

  @Test
  public void accessArrWithTypePath2() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "z[2].y.x[1] = 3",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    AssignStmt assignStmt = (AssignStmt) p.fdecls.get(0).stmts.get(0);

    assertEquals(AssignStmt.class, assignStmt.getClass());

    assertEquals(5, assignStmt.lvalue.size());
  }

  @Test
  public void arrRValWithTypePath() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "x = y.x[1]",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    AssignStmt assignStmt = (AssignStmt) p.fdecls.get(0).stmts.get(0);
    SimpleTerm simpleTerm = (SimpleTerm) assignStmt.expr.first;
    IDRValue idrValue = (IDRValue)  simpleTerm.rvalue;
    assertEquals(IDRValue.class, simpleTerm.rvalue.getClass());


    assertEquals(3,idrValue.path.size());
  }

  @Test
  public void arrRValWithTypePath2() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "x = z.w[1+3+3].y.x[1]",
                    "}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    AssignStmt assignStmt = (AssignStmt) p.fdecls.get(0).stmts.get(0);
    SimpleTerm simpleTerm = (SimpleTerm) assignStmt.expr.first;
    IDRValue idrValue = (IDRValue)  simpleTerm.rvalue;
    assertEquals(IDRValue.class, simpleTerm.rvalue.getClass());


    assertEquals(6,idrValue.path.size());
  }

  @Test
  public void funReturnArr() throws Exception {
    String s = buildString
            ("fun void main() {}",
                    "fun int[] test(){}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    FunDecl fd = (FunDecl) p.fdecls.get(1);
    assertEquals(true, fd.returnArray);
  }

  @Test
  public void funArrParam() throws Exception {
    String s = buildString
            ("fun void main() {}",
                    "fun void test(int[] x, T[] y){}"
            );
    ASTParser parser = buildParser(s);
    Program p = parser.parse();
    FunParam param = (FunParam) p.fdecls.get(1).params.get(0);
    assertEquals(true, param.isArray);
  }
}

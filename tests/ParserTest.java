/*
 * File: ParserTest.java
 * Date: Spring 2022
 * Auth: Dominic MacIsaac
 * Desc: Basic unit tests for the MyPL parser class.
 */


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class ParserTest {

  //------------------------------------------------------------
  // HELPER FUNCTIONS
  //------------------------------------------------------------
  
  private static Parser buildParser(String s) throws Exception {
    InputStream in = new ByteArrayInputStream(s.getBytes("UTF-8"));
    Parser parser = new Parser(new Lexer(in));
    return parser;
  }

  private static String buildString(String... args) {
    String str = "";
    for (String s : args)
      str += s + "\n";
    return str;
  }


  //------------------------------------------------------------
  // POSITIVE TEST CASES
  //------------------------------------------------------------

  @Test
  public void emptyParse() throws Exception {
    Parser parser = buildParser("");
    parser.parse();
  }

  @Test
  public void implicitVariableDecls() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var v1 = 0",
       "  var v2 = 0.0",
       "  var v3 = false",
       "  var v4 = 'a'",
       "  var v5 = \"abc\"",
       "  var v6 = new Node",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void explicitVariableDecls() throws Exception {
    String s = buildString
      ("fun void main() {",
       "  var int v1 = 0",
       "  var double v2 = 0.0",
       "  var bool v3 = false",
       "  var char v4 = 'a'",
       "  var string v5 = \"abc\"",
       "  var Node v5 = new Node",
       "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void cond_stmtWithCondt() throws Exception {
    String s = buildString
            ("fun void main() {",
                    " if NOT (x >= z) { return x} ",
                    " elif (x < z) { return y}",
                    " else { return z}",
                    "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void twoIDsInVarDec() throws Exception {
    String s = buildString
            ("type x {",
                    " var T tempCount = 1 ",
                    "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void rvalDifferences() throws Exception {
    String s = buildString
            ("fun void main() {",
                    " v1 = w () ",
                    " v2 = w.w",
                    " v3 = w",
                    " v3 = w.w.w",
                    "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void rvalDifferences2() throws Exception {
    String s = buildString
            ("fun void main() {",
                    " v4 = 3",
                    " v5 = nil",
                    " v6 = new w",
                    " v7 = neg not (new w) >= w",
                    "}");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void functionAndType() throws Exception {
    String s = buildString
            ("fun void main() {",
                    " v4 = 3",
                    " }",
                    " type x {",
                    " }",
                    " fun void main() {",
                    " v4 = 3",
                    " }",
                    " type x {",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void whileStmt() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "while not x { var x = y } ",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void for_stmt() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "for x from x>0 upto 3 { var x = y } ",
                    "for x from x>0 downto 3 { var x = y } ",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void deleteStmt() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "var x = nil",
                    "delete x",
                    "delete temp",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void returnStmt() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "var x = nil",
                    "return x",
                    "return temp",
                    "return x + y - z + (not nil)",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void arrStmt() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "arr x = {}",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void arrStmtExplicit() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "arr int x = {}",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void arrStmtWithArgs() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "arr x = {1,2,3,4+5}",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void arrStmtWithArgs2() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "arr x = {\"eat\", \"food\", read()}",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void arrAccessLVal() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "arr x = {1}",
                    "x[0] = 2",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void arrAccessLVal2() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "arr x = {1}",
                    "x[0+1+2] = 2",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }
  @Test
  public void arrAccessLVal3() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "arr x = {1}",
                    "x[0+1+2][z] = 2",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void arrAccessRVal3() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "arr x = {1}",
                    "var z = x[0]",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }

  @Test
  public void arrAccessRVal4() throws Exception {
    String s = buildString
            ("fun void main() {",
                    "arr x = {1}",
                    "arr y = {x}",
                    "var z = y[0][0]",
                    " }");
    Parser parser = buildParser(s);
    parser.parse();
  }






  /* TODO: 

       (1). Add your own test cases below as you create your recursive
            descent functions. By the end you should have a full suite
            of positive test cases that "pass" the tests. 

       (2). Ensure your program (bazel-bin/mypl --parse) works for the
            example file (examples/parser.txt). 
 
       (3). For the parser, the "negative" tests below are just as
            important as the "positive" test cases. Like in (1), be
            sure to add negative test cases as you build out your
            parser. By the end you should also have a full set of
            negative cases as well.
  */

  
  //------------------------------------------------------------
  // NEGATIVE TEST CASES
  //------------------------------------------------------------
  
  @Test
  public void statementOutsideOfFunction() throws Exception {
    String s = "var v1 = 0";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }

  @Test
  public void functionWithoutReturnType() throws Exception {
    String s = "fun main() {}";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }

  @Test
  public void functionWithoutClosingBrace() throws Exception {
    String s = "fun void main() {";
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }
  
  /* add additional negative test cases here */
  @Test
  public void typeWithNoName() throws Exception {
    String s = buildString(
            "type {}");
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }

  @Test
  public void ifWithNoExpr() throws Exception {
    String s = buildString(
            "fun void main(){",
            " if { x = y} ",
            "}"
            );
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }

  @Test
  public void ifWrongOrder() throws Exception {
    String s = buildString(
            "fun void main(){",
            " if x {x = y}",
            "else {x = z}",
            "elif z { z = y }",
            "}");
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }

  @Test
  public void doubleOperator() throws Exception {
    String s = buildString(
            "fun void main(){",
            " x = x * / y",
            "}");
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }

  @Test
  public void paramsFail() throws Exception {
    String s = buildString(
            "fun void main(int x, T z, y){",
            "}");
    Parser parser = buildParser(s);
    try {
      parser.parse();
      fail("syntax error not detected");
    } catch(MyPLException e) {
      // can check message here if desired
      // e.g., assertEquals("...", e.getMessage());
    }
  }

}

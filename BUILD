
#======================================================================
# Bare-bones Bazel BUILD file for Final Project
# CPSC 326
# Spring, 2022
#======================================================================

load("@rules_java//java:defs.bzl", "java_test")

java_binary(
  name = "mypl",
  srcs = glob(["src/*.java"]),
  main_class = "MyPL",
)

java_library(
  name = "mypl-lib",
  srcs = glob(["src/*.java"]),
)

#----------------------------------------------------------------------
# TEST SUITES:
#----------------------------------------------------------------------


# TODO: Add your test targets here. See prior homework build files for
#       examples of creating test targets.
java_test(
    name = "parser-test",
    srcs = ["tests/ParserTest.java"],
    test_class = "ParserTest",
    deps = ["lib/junit-4.13.2.jar", "lib/hamcrest-core-1.3.jar","//:mypl-lib"],
)
java_test(
    name = "ast-parser-test",
    srcs = ["tests/ASTParserTest.java"],
    test_class = "ASTParserTest",
    deps = ["lib/junit-4.13.2.jar", "lib/hamcrest-core-1.3.jar","//:mypl-lib"],
)




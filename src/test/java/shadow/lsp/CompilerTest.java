package shadow.lsp;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import shadow.Configuration;

class CompilerTest {

  private final Compiler compiler = new Compiler();

  @BeforeAll
  static void clearConfiguration() throws Exception {
    // Mirrors TypeCheckerTests' setup, in case TypeCollector reads
    // static Configuration state during import resolution.
    Configuration.clearConfiguration();
    Configuration.buildConfiguration("tests/typechecker/Basic.shadow", "tests.json", false);
  }

  @Test
  void cleanFileHasNoErrors() throws Exception {
    System.out.println("\nTesting CleanFileHasNoErrors...");
    Path file = Paths.get("tests/typechecker/Basic.shadow");
    Compiler.CheckResult result = compiler.check(file);

    assertTrue(result.errors.isEmpty(), "Expected no errors for Basic.shadow");
  }

  @Test
  void fileWithImportsHasNoErrors() throws Exception {
    System.out.println("\nTesting FileWithImportsHasNoErrors...");
    Path file = Paths.get("tests/typechecker/Imports.shadow");
    Compiler.CheckResult result = compiler.check(file);

    assertTrue(result.errors.isEmpty(), "Expected no errors for Imports.shadow");
  }

  @Test
  void nonexistentFileReturnsEmptyResult() throws Exception {
    System.out.println("\nTesting NonExistentFIleReturnsEmptyResult...");
    Path file = Paths.get("tests/typechecker/DoesNotExist.shadow");
    Compiler.CheckResult result = compiler.check(file);

    assertTrue(result.errors.isEmpty());
    assertTrue(result.warnings.isEmpty());
  }

  @Test
  void fileWithErrorReportsIt() throws Exception {
    System.out.println("\nTesting FileWithErrorReportsIt...");
    Path file = Paths.get("tests/typechecker/BasicWithError.shadow");
    Compiler.CheckResult result = compiler.check(file);

    assertFalse(result.errors.isEmpty(), "Expected at least one error");

    for (var error : result.errors) {
      System.out.println(error.getMessageText());

      // adding to align column and lines later
      System.out.println("Error line start: " + error.lineStart());
      System.out.println("Error line end: " + error.lineEnd());

      System.out.println("Error column start: " + error.columnStart());
      System.out.println("Error column end: " + error.columnEnd());
    }
  }

  @Test
  void resultListsAreImmutable() throws Exception {
    System.out.println("\nTesting ResultListsAreImmutable...");
    Path file = Paths.get("tests/typechecker/Basic.shadow");
    Compiler.CheckResult result = compiler.check(file);

    assertThrows(UnsupportedOperationException.class, () -> result.errors.add(null));
  }
}

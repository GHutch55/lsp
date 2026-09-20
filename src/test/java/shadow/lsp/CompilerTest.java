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
    Path file = Paths.get("tests/typechecker/Basic.shadow");
    Compiler.CheckResult result = compiler.check(file);

    assertTrue(result.errors.isEmpty(), "Expected no errors for Basic.shadow");
  }

  @Test
  void fileWithImportsHasNoErrors() throws Exception {
    Path file = Paths.get("tests/typechecker/Imports.shadow");
    Compiler.CheckResult result = compiler.check(file);

    assertTrue(result.errors.isEmpty(), "Expected no errors for Imports.shadow");
  }

  @Test
  void nonexistentFileReturnsEmptyResult() throws Exception {
    Path file = Paths.get("tests/typechecker/DoesNotExist.shadow");
    Compiler.CheckResult result = compiler.check(file);

    assertTrue(result.errors.isEmpty());
    assertTrue(result.warnings.isEmpty());
  }

  @Test
  void fileWithErrorReportsIt() throws Exception {
    Path file = Paths.get("tests/typechecker/BasicWithError.shadow");
    Compiler.CheckResult result = compiler.check(file);

    assertFalse(result.errors.isEmpty(), "Expected at least one error");

    for (var error : result.errors) {
      System.out.println(error.getMessageText());
    }
  }

  @Test
  void resultListsAreImmutable() throws Exception {
    Path file = Paths.get("tests/typechecker/Basic.shadow");
    Compiler.CheckResult result = compiler.check(file);

    assertThrows(UnsupportedOperationException.class, () -> result.errors.add(null));
  }
}

package shadow.lsp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import shadow.Configuration;
import shadow.ShadowException;
import shadow.lsp.Compiler.CheckResult;

class DocumentServiceTest {

  private static final Path FIXTURE = Paths.get("tests/typechecker/BasicWithError.shadow").toAbsolutePath().normalize();

  @BeforeAll
  static void setup() throws Exception {
    Configuration.clearConfiguration();
    Configuration.buildConfiguration(FIXTURE.toString(), "tests.json", false);
  }

  @Test
  void didOpenTriggersRealCompile() throws IOException {
    DocumentService service = new DocumentService();

    String uri = FIXTURE.toUri().toString();
    String text = Files.readString(FIXTURE);

    TextDocumentItem item = new TextDocumentItem();
    item.setUri(uri);
    item.setText(text);
    item.setLanguageId("Shadow");
    item.setVersion(1);

    DidOpenTextDocumentParams params = new DidOpenTextDocumentParams();
    params.setTextDocument(item);

    // No assertions here on purpose, this is a manual verification step.
    // Go check the log file after running this and confirm the compiler
    // error message actually appears, coming from DocumentService's
    // logger, not CompilerTest's.
    service.didOpen(params);
  }

  @Test
  void didSaveTriggersRealCompile() {
    DocumentService service = new DocumentService();

    String uri = FIXTURE.toUri().toString();

    TextDocumentIdentifier identifier = new TextDocumentIdentifier(uri);

    DidSaveTextDocumentParams params = new DidSaveTextDocumentParams();
    params.setTextDocument(identifier);

    service.didSave(params);
  }

  // Verifies the ShadowException -> Diagnostic conversion against a real,
  // known compiler error. Adjust method name/params below to match whatever
  // you actually named the conversion method in DocumentService.
  @Test
  void toDiagnosticAppliesLineColumnConversionCorrectly() throws Exception {
    DocumentService service = new DocumentService();
    Compiler compiler = new Compiler();

    CheckResult result = compiler.check(FIXTURE);
    assertEquals(1, result.errors.size(), "Expected exactly one error from the fixture");

    ShadowException exception = result.errors.get(0);

    // Sanity check on the raw compiler values before conversion, so a
    // failure below points clearly at the conversion logic, not a changed
    // fixture file.
    assertEquals(11, exception.lineStart());
    assertEquals(11, exception.lineEnd());
    assertEquals(6, exception.columnStart());
    assertEquals(14, exception.columnEnd());

    Diagnostic diagnostic = service.toDiagnostic(exception, DiagnosticSeverity.Error);

    // Line: compiler is 1-based (11) -> LSP is 0-based (10)
    assertEquals(10, diagnostic.getRange().getStart().getLine());
    assertEquals(10, diagnostic.getRange().getEnd().getLine());

    // Start column: compiler is already 0-based -> unchanged
    assertEquals(6, diagnostic.getRange().getStart().getCharacter());

    // End column: compiler's end is inclusive (14) -> LSP wants exclusive (15)
    assertEquals(15, diagnostic.getRange().getEnd().getCharacter());

    assertEquals(DiagnosticSeverity.Error, diagnostic.getSeverity());
    assertEquals("Shadow", diagnostic.getSource());
    assertEquals(exception.getMessageText(), diagnostic.getMessage().getLeft());
  }
}

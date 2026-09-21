package shadow.lsp;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import shadow.ConfigurationException;
import shadow.ShadowException;
import shadow.lsp.Compiler.CheckResult;

/*
 * Handles things that happen to Shadow source files.
 *
 * The editor will call these methods when a file is opened,
 * changed, saved, or closed.
 *
 * This is also where features like hover and go-to-definition
 * will eventually be implemented.
 */
public class DocumentService implements TextDocumentService {

  /*
   * Represents the editor that is connected to our server.
   *
   * We will use this later to send information back to the editor,
   * such as compiler errors (diagnostics).
   */
  private LanguageClient client;
  private final DocumentManager documentManager;
  private final Compiler compiler;
  private final Logger logger;

  public DocumentService() {
    this.documentManager = new DocumentManager();
    this.compiler = new Compiler();
    this.logger = LogManager.getLogger(DocumentService.class);
  }

  /*
   * Gives this class access to the editor's LanguageClient.
   *
   * ShadowLanguageServer calls this when the server connects
   * to the editor.
   */
  public void connect(LanguageClient client) {
    this.client = client;
  }

  /*
   * Called when the user opens a Shadow file.
   */
  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    String text = params.getTextDocument().getText();

    documentManager.open(uri, text);
    Path path = uriToPath(uri);

    if (path != null) {
      try {
        CheckResult result = compiler.check(path);
        List<Diagnostic> diagnostics = new ArrayList<>();

        for (ShadowException error : result.errors) {
          diagnostics.add(toDiagnostic(error, DiagnosticSeverity.Error));
        }

        for (ShadowException warning : result.warnings) {
          diagnostics.add(toDiagnostic(warning, DiagnosticSeverity.Warning));
        }

        PublishDiagnosticsParams diagParams = new PublishDiagnosticsParams(uri, diagnostics);
        if (client != null) {
          client.publishDiagnostics(diagParams);
        }
      } catch (IOException | ConfigurationException e) {
        logger.error("Compile check failed for {}", uri, e);
      }
    }

    logger.info("Opened: {}", uri);
  }

  /*
   * Called when the user changes a Shadow file.
   *
   * Since we are using full document syncing, the editor will
   * send us the entire file instead of only the changed part.
   */
  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();

    String text = params.getContentChanges().get(0).getText();
    documentManager.update(uri, text);

    logger.info("Changed: {}", uri);
  }

  /*
   * Called when the user closes a Shadow file.
   */
  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();

    documentManager.close(uri);
    PublishDiagnosticsParams diagParams = new PublishDiagnosticsParams(uri, List.of());

    if (client != null) {
      client.publishDiagnostics(diagParams);
    }

    logger.info("Closed: {}", uri);
  }

  /*
   * Called when the user saves a Shadow file
   *
   * Files are only compiled once saved to disk (see Compiler, TypeChecker has
   * no in-memory source entry point). didChange only updates the in-memory
   * buffer.
   */
  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    Path path = uriToPath(uri);

    if (path != null) {
      try {
        CheckResult result = compiler.check(path);
        List<Diagnostic> diagnostics = new ArrayList<>();

        for (ShadowException error : result.errors) {
          diagnostics.add(toDiagnostic(error, DiagnosticSeverity.Error));
        }

        for (ShadowException warning : result.warnings) {
          diagnostics.add(toDiagnostic(warning, DiagnosticSeverity.Warning));
        }

        PublishDiagnosticsParams diagParams = new PublishDiagnosticsParams(uri, diagnostics);
        if (client != null) {
          client.publishDiagnostics(diagParams);
        }
      } catch (IOException | ConfigurationException e) {
        logger.error("Compile check failed for {}", uri, e);
      }
    }

    logger.info("Saved: {}", uri);
  }

  /*
   * Called when the editor asks for hover information.
   *
   * For example, the user could move their mouse over a variable
   * and the server could show information about that variable.
   */
  @Override
  public CompletableFuture<Hover> hover(HoverParams params) {

    /*
     * We don't have hover information implemented yet,
     * so return null for now.
     */
    return CompletableFuture.completedFuture(null);
  }

  private Path uriToPath(String uri) {
    try {
      return Paths.get(URI.create(uri));
    } catch (Exception e) {
      logger.error("Could not convert URI to Path: {}", uri, e);
      return null;
    }
  }

  /*
   * Construct a Diagnostic from a ShadowException, passing the severity in as
   * well (warning or error)
   */
  public Diagnostic toDiagnostic(ShadowException exception, DiagnosticSeverity severity) {
    int lineStart = exception.lineStart();
    int lineEnd = exception.lineEnd();
    int columnStart = exception.columnStart();
    int columnEnd = exception.columnEnd();

    int rangeLineStart, rangeLineEnd, rangeColStart, rangeColEnd;

    if (lineStart == -1) {
      // fallback range
      rangeLineStart = 0;
      rangeLineEnd = 0;
      rangeColStart = 0;
      rangeColEnd = 0;
    } else {
      // note that the lines are 1-indexed
      // but the columns are 0-indexed and inclusive within the range
      rangeLineStart = lineStart - 1;
      rangeLineEnd = lineEnd - 1;
      rangeColStart = columnStart;
      rangeColEnd = columnEnd + 1;
    }

    Position start = new Position(rangeLineStart, rangeColStart);
    Position end = new Position(rangeLineEnd, rangeColEnd);

    Range range = new Range(start, end);

    return new Diagnostic(range, exception.getMessageText(), severity, "Shadow");
  }
}

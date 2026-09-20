package shadow.lsp;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/*
 * This is the main language server for Shadow.
 *
 * LSP4J handles communication with the editor and calls the methods
 * in this class when the editor sends LSP requests.
 */
public class LspServer implements LanguageServer {

  /*
   * Handles things related to individual Shadow files, like opening,
   * changing, and eventually getting hover information.
   */
  private final DocumentService textDocumentService;

  /*
   * Represents the editor connected to our server.
   * We will use this later to send things like compiler errors back
   * to the editor.
   */
  private LanguageClient client;

  /*
   * Create the text document service when the server starts.
   */
  public LspServer() {
    this.textDocumentService = new DocumentService();
  }

  /*
   * Called when an editor first connects to the language server.
   *
   * We use this to tell the editor which features our server supports.
   */
  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {

    ServerCapabilities capabilities = new ServerCapabilities();

    /*
     * Full means the editor will send us the entire file whenever
     * the file changes.
     *
     * This is simpler for the first version than handling only the
     * parts of the file that changed.
     */
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

    /*
     * LSP4J expects this result to be returned as a CompletableFuture.
     */
    return CompletableFuture.completedFuture(new InitializeResult(capabilities));
  }

  /*
   * Called when the editor wants the server to shut down.
   */
  @Override
  public CompletableFuture<Object> shutdown() {
    return CompletableFuture.completedFuture(null);
  }

  /*
   * Actually exits the server program.
   */
  @Override
  public void exit() {
    System.exit(0);
  }

  /*
   * Tells LSP4J which class should handle file/document operations.
   */
  @Override
  public TextDocumentService getTextDocumentService() {
    return textDocumentService;
  }

  /*
   * Handles workspace-level events.
   *
   * We don't need these yet, so these methods are empty for now.
   */
  @Override
  public WorkspaceService getWorkspaceService() {
    return new WorkspaceService() {
      /*
       * Called if the editor's workspace settings change.
       */
      @Override
      public void didChangeConfiguration(DidChangeConfigurationParams params) {

        // TODO: Handle configuration changes later.
      }

      /*
       * Called when a file being watched by the server changes.
       */
      @Override
      public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {

        // TODO: Handle watched files later.
      }
    };
  }

  /*
   * Connects our server to the editor.
   *
   * Main creates the connection and passes the editor's LanguageClient
   * to us here.
   */
  public void connect(LanguageClient client) {
    this.client = client;

    /*
     * The text document service also needs the client so it can
     * eventually send diagnostics and other information back to
     * the editor.
     */
    this.textDocumentService.connect(client);
  }
}

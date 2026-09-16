package shadow.lsp;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

/*
 * Handles things that happen to Shadow source files.
 *
 * The editor will call these methods when a file is opened,
 * changed, saved, or closed.
 *
 * This is also where features like hover and go-to-definition
 * will eventually be implemented.
 */
public class ShadowTextDocumentService implements TextDocumentService {

  /*
   * Represents the editor that is connected to our server.
   *
   * We will use this later to send information back to the editor,
   * such as compiler errors (diagnostics).
   */
  private LanguageClient client;

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
   *
   * We will eventually store the file in memory and run the
   * compiler on it here.
   */
  @Override
  public void didOpen(DidOpenTextDocumentParams params) {

    // TODO: Store the opened document and check it for errors.
  }

  /*
   * Called when the user changes a Shadow file.
   *
   * Since we are using full document syncing, the editor will
   * send us the entire file instead of only the changed part.
   *
   * We will eventually update our stored copy and run the
   * compiler again here.
   */
  @Override
  public void didChange(DidChangeTextDocumentParams params) {

    // TODO: Update the stored document and check it for errors.
  }

  /*
   * Called when the user closes a Shadow file.
   *
   * We will eventually remove the file from our in-memory
   * document storage here.
   */
  @Override
  public void didClose(DidCloseTextDocumentParams params) {

    // TODO: Remove the document from memory.
  }

  /*
   * Called when the user saves a Shadow file.
   *
   * We may use this later if we want to do anything special
   * when a file is saved.
   */
  @Override
  public void didSave(DidSaveTextDocumentParams params) {

    // TODO: Handle saved documents later.
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
}

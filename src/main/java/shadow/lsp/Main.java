package shadow.lsp;

import java.io.InputStream;
import java.io.OutputStream;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;

public class Main {

  public static void main(String[] args) {
    // LSP servers communicate with the editor through stdin/stdout.
    // IMPORTANT: stdout is reserved for LSP protocol messages,
    // so debugging information should be written to stderr.
    System.err.println("Shadow LSP server started.");

    InputStream in = System.in;
    OutputStream out = System.out;

    // Our implementation of the Language Server Protocol.
    ShadowLanguageServer server = new ShadowLanguageServer();

    // LSP4J's Launcher handles the JSON-RPC communication between
    // the editor (LSP client) and our Shadow language server.
    Launcher<LanguageClient> launcher = Launcher.createLauncher(server, LanguageClient.class, in, out);

    // Give the server a proxy representing the LSP client.
    // This will eventually allow us to send diagnostics and other
    // notifications back to the editor.
    server.connect(launcher.getRemoteProxy());

    // Begin waiting for requests from the LSP client.
    // This call continues listening until the connection closes.
    launcher.startListening();
  }
}

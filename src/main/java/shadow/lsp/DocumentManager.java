package shadow.lsp;

import java.util.HashMap;
import java.util.Map;

public class DocumentManager {
  // Storing the URI of the file, then the source code. Opting for a HashMap for
  // O(1) lookup
  private final Map<String, String> documents = new HashMap<>();

  // Storing the file data
  public void open(String uri, String text) {
    documents.put(uri, text);
  }

  // Updating the data of an already opened file
  public void update(String uri, String text) {
    documents.put(uri, text);
  }

  // Closing a file that has been opened
  public void close(String uri) {
    documents.remove(uri);
  }

  // Returning the data of a file
  public String get(String uri) {
    return documents.get(uri);
  }
}

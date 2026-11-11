package es.nachobrito.sdd.example.adapter.out.smtp;

/**
 * Utility for sanitizing dynamic values before inserting into HTML templates.
 */
public final class HtmlEscaper {

  private HtmlEscaper() {
    // Utility class
  }

  /**
   * Escapes special HTML characters in the input string.
   *
   * @param input the string to sanitize
   * @return the escaped HTML string
   */
  public static String escape(String input) {
    if (input == null) {
      return "";
    }

    StringBuilder builder = new StringBuilder(input.length() + 16);
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      switch (c) {
        case '&' -> builder.append("&amp;");
        case '<' -> builder.append("&lt;");
        case '>' -> builder.append("&gt;");
        case '"' -> builder.append("&quot;");
        case '\'' -> builder.append("&#39;");
        default -> builder.append(c);
      }
    }
    return builder.toString();
  }
}


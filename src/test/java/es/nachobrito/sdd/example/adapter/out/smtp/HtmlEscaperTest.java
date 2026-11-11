package es.nachobrito.sdd.example.adapter.out.smtp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HtmlEscaperTest {

  @Test
  void returnsEmptyStringForNullInput() {
    assertEquals("", HtmlEscaper.escape(null));
  }

  @Test
  void returnsOriginalStringWhenNoHtmlCharactersPresent() {
    String input = "Hello World 123";
    assertEquals("Hello World 123", HtmlEscaper.escape(input));
  }

  @Test
  void escapesAmpersand() {
    assertEquals("Tom &amp; Jerry", HtmlEscaper.escape("Tom & Jerry"));
  }

  @Test
  void escapesLessThanAndGreaterThan() {
    assertEquals(
        "&lt;script&gt;alert(1)&lt;/script&gt;",
        HtmlEscaper.escape("<script>alert(1)</script>"));
  }

  @Test
  void escapesQuotes() {
    assertEquals(
        "&quot;Hello&quot; and &#39;World&#39;",
        HtmlEscaper.escape("\"Hello\" and 'World'"));
  }

  @Test
  void escapesCombinedSpecialCharacters() {
    String input = "<b>Items & \"Prices\" 'Sale'</b>";
    String expected = "&lt;b&gt;Items &amp; &quot;Prices&quot; &#39;Sale&#39;&lt;/b&gt;";
    assertEquals(expected, HtmlEscaper.escape(input));
  }
}


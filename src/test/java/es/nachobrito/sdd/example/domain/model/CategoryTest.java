package es.nachobrito.sdd.example.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CategoryTest {

  private static final UUID VALID_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

  @Test
  void createsValidCategoryAndTrimsFields() {
    Category category = new Category(VALID_ID, "  Electronics  ", "  Audio and video  ");

    assertEquals(VALID_ID, category.id());
    assertEquals("Electronics", category.name());
    assertEquals("Audio and video", category.description());
  }

  @Test
  void createsValidCategoryWithNullDescriptionAndBoundaryLengths() {
    String maxName = "N".repeat(Category.MAX_NAME_LENGTH);
    String maxDescription = "D".repeat(Category.MAX_DESCRIPTION_LENGTH);

    Category noDesc = new Category(VALID_ID, maxName, null);
    assertNull(noDesc.description());
    assertEquals(Category.MAX_NAME_LENGTH, noDesc.name().length());

    Category maxDescCat = new Category(VALID_ID, "Books", maxDescription);
    assertEquals(Category.MAX_DESCRIPTION_LENGTH, maxDescCat.description().length());
  }

  @Test
  void rejectsNullIdAndBlankOrOversizedFields() {
    assertThrows(
        InvalidCategoryDataException.class,
        () -> new Category(null, "Electronics", "Desc"));
    assertThrows(
        InvalidCategoryDataException.class,
        () -> new Category(VALID_ID, null, "Desc"));
    assertThrows(
        InvalidCategoryDataException.class,
        () -> new Category(VALID_ID, "   ", "Desc"));
    assertThrows(
        InvalidCategoryDataException.class,
        () -> new Category(VALID_ID, "N".repeat(Category.MAX_NAME_LENGTH + 1), "Desc"));
    assertThrows(
        InvalidCategoryDataException.class,
        () -> new Category(
            VALID_ID, "Valid", "D".repeat(Category.MAX_DESCRIPTION_LENGTH + 1)));
  }
}


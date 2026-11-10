package es.nachobrito.sdd.example.domain.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A version number that follows Semantic Versioning (MAJOR.MINOR.PATCH).
 *
 * <p>This is a walking skeleton value object. It makes sure that all the build checks have code to
 * examine. Delete it when the first real domain object is available.
 *
 * @param major the major version. Increment it for changes that are not backward compatible.
 * @param minor the minor version. Increment it for backward compatible features.
 * @param patch the patch version. Increment it for backward compatible fixes.
 */
public record SemanticVersion(int major, int minor, int patch) {

  private static final Pattern FORMAT =
      Pattern.compile("(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)");

  /**
   * Creates a version.
   *
   * @throws IllegalArgumentException if a part of the version is negative
   */
  public SemanticVersion {
    if (major < 0 || minor < 0 || patch < 0) {
      throw new IllegalArgumentException("Version parts must not be negative");
    }
  }

  /**
   * Reads a version from text in the MAJOR.MINOR.PATCH format.
   *
   * @param text the version text, for example {@code 1.4.2}
   * @return the version
   * @throws IllegalArgumentException if the text does not have the MAJOR.MINOR.PATCH format
   */
  public static SemanticVersion parse(String text) {
    Matcher matcher = FORMAT.matcher(text);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Version must have the MAJOR.MINOR.PATCH format");
    }
    return new SemanticVersion(
        Integer.parseInt(matcher.group(1)),
        Integer.parseInt(matcher.group(2)),
        Integer.parseInt(matcher.group(3)));
  }

  /**
   * Tells if this version can replace an other version without a breaking change.
   *
   * @param other the version that is replaced
   * @return {@code true} if the major versions are equal and this version is not older
   */
  public boolean isBackwardCompatibleWith(SemanticVersion other) {
    if (major != other.major) {
      return false;
    }
    if (minor != other.minor) {
      return minor > other.minor;
    }
    return patch >= other.patch;
  }

  @Override
  public String toString() {
    return major + "." + minor + "." + patch;
  }
}


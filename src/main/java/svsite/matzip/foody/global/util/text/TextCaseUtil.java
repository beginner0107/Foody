package svsite.matzip.foody.global.util.text;

import java.util.concurrent.ConcurrentHashMap;

public final class TextCaseUtil {

  private static final ConcurrentHashMap<String, String> cachedCapitalizationMap = new ConcurrentHashMap<>();

  public static String capitalizeAndSaveUpperSnakeCase(String upperSnakeCase) {
    return cachedCapitalizationMap.computeIfAbsent(upperSnakeCase, TextCaseUtil::capitalizeUpperSnakeCase);
  }

  public static String capitalizeUpperSnakeCase(String upperSnakeCase) {
    char[] chars = upperSnakeCase.toCharArray();
    boolean willBeLower = false;

    for (int i = 0; i < chars.length; i++) {
      char ch = chars[i];
      if (willBeLower && 'A' <= ch && ch <= 'Z') {
        ch |= ' '; // ch = ch | ' ';
      } else if (ch == '_') {
        ch = ' ';
        willBeLower = false;
      } else {
        willBeLower = true;
      }
      chars[i] = ch;
    }

    return new String(chars);
  }
}

/*
 * =================================LICENSE_START=================================
 * MYDATA Control Technologies
 *
 * Copyright (C) 2016 - present Fraunhofer-Gesellschaft zur Foerderung der
 * angewandten Forschung e.V. acting on behalf of its Fraunhofer Institute
 * for Experimental Software Engineering (IESE)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =================================LICENSE_END===================================
 */

package de.fraunhofer.iese.mydata.pep.common;

import java.util.Optional;

public class SubstringUtil {
  static final String BOTH_ARE_SET = "0";

  static final String END_NOT_SET = "1";

  static final String START_NOT_SET = "2";

  static final String BOTH_NOT_SET = "3";

  private SubstringUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * STRING := "0123456789" | substring(s, 1, null/0) => 123456789 |
   * substring(s, 2, ") => 2345 | substring(s, null/0, 3) => 012 | substring(s,
   * -2, null) => 89 | substring(s, -3, -2) => 7 | substring(s, -2,2) => 0189 |
   * substring(s, 2, -4) => 2345 | substring(s, null, -5) => 01234 |
   * substring(s, 6,2) => | substring(s, -3, -4) => | substring(s, null, null)
   * => |
   *
   * @param s
   * @param start
   * @param end
   * @param fillString
   * @param autoFill
   * @return
   */
  public static String substring(String s, int start, int end, String fillString, boolean autoFill) {

    final String valueCheck = getValueCheck(s, start, end);
    final int fixedEnd = fixEnd(s, end);
    final String fixedFill = fixFill(fillString);

    switch (valueCheck) {
      case BOTH_ARE_SET:
        return substringChecked(s, start, fixedEnd, fixedFill, autoFill);

      case END_NOT_SET:
        return substringFrom(s, start, fixedFill, autoFill);

      case START_NOT_SET:
        return substringTo(s, fixedEnd, fixedFill, autoFill);

      case BOTH_NOT_SET:
        return s;

      default:
        return "";
    }
  }

  private static int fixEnd(String s, int end) {
    return Math.min(end, s.length());
  }

  private static String fixFill(String fillString) {
    if (null == fillString) {
      return "";
    }
    return fillString;
  }

  private static String substringTo(String s, int signedEnd, String fillString, boolean autoFill) {
    if (!isNegative(signedEnd)) {
      return fillSuffix(s.substring(0, signedEnd), fillString, autoFill, s.length());
    } else if (!isNegative(s.length() + signedEnd)) {
      return fillSuffix(s.substring(0, s.length() + signedEnd), fillString, autoFill, s.length());
    }
    return fillSuffix("", fillString, autoFill, s.length());
  }

  private static String substringFrom(String s, int signedStart, String fillString, boolean autoFill) {
    if (!isNegative(signedStart)) {
      return fillPrefix(s.substring(signedStart), fillString, autoFill, s.length());
    } else if (!isNegative(s.length() + signedStart)) {
      return fillPrefix(s.substring(s.length() + signedStart), fillString, autoFill, s.length());
    }
    return "";

  }

  private static String getValueCheck(String s, int start, int end) {
    if (Optional.ofNullable(s).isPresent()) {
      if (0 != start && 0 != end) {
        return BOTH_ARE_SET;
      }
      if (0 != start && 0 == end) {
        return END_NOT_SET;
      }
      if (0 == start && 0 != end) {
        return START_NOT_SET;
      }
      if (0 == start && 0 == end) {
        return BOTH_NOT_SET;
      }
    }

    return "";

  }

  private static String substringChecked(String s, int signedStart, int signedEnd, String fillString, boolean autoFill) {
    String resultString = "";
    if (!isNegative(signedStart) && !isNegative(signedEnd) && signedStart < signedEnd) {
      final String newString = s.substring(signedStart, signedEnd);
      resultString = fillString(newString, fillString, autoFill, s.length(), signedStart, s.length() - signedEnd);
    }

    if (!isNegative(signedStart) && isNegative(signedEnd) && signedStart < getIndexfromNegative(s, signedEnd)) {
      final String newString = s.substring(signedStart, getIndexfromNegative(s, signedEnd));
      resultString = fillString(newString, fillString, autoFill, s.length(), signedStart, Math.abs(signedEnd));
    }
    if (isNegative(signedStart) && !isNegative(signedEnd) && getIndexfromNegative(s, signedStart) > signedEnd) {
      resultString = cutBetween(s, getIndexfromNegative(s, signedStart), signedEnd, fillString, autoFill);
    }
    if (isNegative(signedStart) && isNegative(signedEnd) && getIndexfromNegative(s, signedStart) < getIndexfromNegative(s, signedEnd)) {
      final String newString = s.substring(getIndexfromNegative(s, signedStart), getIndexfromNegative(s, signedEnd));
      resultString = fillString(newString, fillString, autoFill, s.length(), getIndexfromNegative(s, signedStart), Math.abs(signedEnd));
    }
    return resultString;
  }

  private static String cutBetween(String s, int startIndex, int endIndex, String fillString, boolean autoFill) {
    if (autoFill) {
      return new StringBuilder(s.substring(0, endIndex)).append(buildFillString(s.length(), s.length() - (startIndex - endIndex), fillString)).append(s.substring(startIndex)).toString();
    }
    return new StringBuilder(s.substring(0, endIndex)).append(fillString).append(s.substring(startIndex)).toString();
  }

  private static boolean isNegative(int i) {
    return i < 0;
  }

  private static int getIndexfromNegative(String s, int negativeInt) {
    int resultInt = s.length() + negativeInt;
    if (isNegative(resultInt)) {
      resultInt = 0;
    }
    return resultInt;
  }

  private static String fillSuffix(String newString, String fillString, boolean autoFill, int orgLength) {
    if (autoFill) {
      return new StringBuilder(newString).append(buildFillString(orgLength, newString.length(), fillString)).toString();
    } else {
      return new StringBuilder(newString).append(fillString).toString();
    }
  }

  private static String fillPrefix(String newString, String fillString, boolean autoFill, int orgLength) {
    if (autoFill) {
      return new StringBuilder(buildFillString(orgLength, newString.length(), fillString)).append(newString).toString();
    } else {
      return new StringBuilder(fillString).append(newString).toString();
    }
  }

  private static String fillString(String newString, String fillString, boolean autoFill, int orgLength, int frontCut, int backCut) {
    if (autoFill) {
      return new StringBuilder(buildFillString(frontCut, 0, fillString)).append(newString).append(buildFillString(backCut, 0, fillString)).toString();
    } else {
      return new StringBuilder(fillString).append(newString).append(fillString).toString();
    }
  }

  private static String buildFillString(int orgLength, int newLength, String fillString) {
    final int fillLength = orgLength - newLength;
    if (fillLength < fillString.length()) {
      return fillString.substring(0, fillLength);
    } else if (fillLength > fillString.length()) {
      final StringBuilder stringBuilder = new StringBuilder(fillString);
      while (fillLength > stringBuilder.length()) {
        stringBuilder.append(fillString);
      }
      return buildFillString(orgLength, newLength, stringBuilder.toString());
    }

    return fillString;
  }

}

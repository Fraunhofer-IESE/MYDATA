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

package de.fraunhofer.iese.mydata.util;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generate a random string
 */
public class RandomStringUtil {

  /**
   * Upper case letters
   */
  public static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /**
   * Lower case letters
   */
  public static final String LOWER = UPPER.toLowerCase(Locale.ROOT);

  /**
   * Digits
   */
  public static final String DIGITS = "0123456789";

  /**
   * Special characters that you can also use in URL parameters without coding
   */
  public static final String SPECIAL = "-_.~";

  /**
   * Alphanumeric characters
   */
  public static final String ALPHANUM = UPPER + LOWER + DIGITS;

  /**
   * Alphanumeric characters and special chars
   */
  public static final String ALPHANUMSPECIAL = ALPHANUM + SPECIAL;

  private final Random random;

  private final char[] symbols;

  private final char[] buf;

  /**
   * @param length  Length of the random string to be generated
   * @param random  Random instance to be used to generate the random string
   * @param symbols Symbols to be used to generate the random string
   */
  public RandomStringUtil(int length, Random random, String symbols) {
    if (length < 1) {
      throw new IllegalArgumentException();
    }
    if (symbols.length() < 2) {
      throw new IllegalArgumentException();
    }
    this.random = Objects.requireNonNull(random);
    this.symbols = symbols.toCharArray();
    this.buf = new char[length];
  }

  /**
   * @param length  Length of the random string to be generated
   * @param symbols Symbols to be used to generate the random string
   */
  public RandomStringUtil(int length, String symbols) {
    this(length, new SecureRandom(), symbols);
  }

  /**
   * Create an alphanumeric string generator.
   * 
   * @param length Length of the random string to be generated
   * @param random Random instance to be used to generate the random string
   */
  public RandomStringUtil(int length, Random random) {
    this(length, random, ALPHANUM);
  }

  /**
   * Create an alphanumeric strings from a secure generator.
   * 
   * @param length Length of the random string to be generated
   */
  public RandomStringUtil(int length) {
    this(length, new SecureRandom());
  }

  /**
   * Create session identifiers.
   */
  public RandomStringUtil() {
    this(21);
  }

  /**
   * Generate a random string.
   * 
   * @return a random string
   */
  public String generateString() {
    for (int idx = 0; idx < this.buf.length; ++idx) {
      this.buf[idx] = this.symbols[this.random.nextInt(this.symbols.length)];
    }
    return new String(this.buf);
  }
}

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

package de.fraunhofer.iese.mydata.pep.modifiers.string;

import de.fraunhofer.iese.mydata.pep.common.PrimitiveModifierMethod;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.registry.ActionDescription;
import de.fraunhofer.iese.mydata.registry.ActionParameterDescription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

/****
 * NOTE: This method does not anagram the string as primitive or json . It just jumbles up the
 * letters of the word so string does not make any sense. Implementation is subject to change for
 * later needs. It produces anagram of a given string.
 */
public class AnagramModifierMethod extends PrimitiveModifierMethod {

  private static final Logger LOG = LoggerFactory.getLogger(AnagramModifierMethod.class);

  /**
   * @return Display Name
   */
  @Override
  public String getDisplayName() {
    return "anagram";
  }

  @Override
  public Object doModification(Object currentObject, ParameterList modifierMethodParameterList) {
    final Object percentage = modifierMethodParameterList.getParameterValueForName("percentage");
    final int percentageParam = percentage != null
        ? (int) Math.round(Double.parseDouble(String.valueOf(percentage)))
        : 0;

    return this.anagram(currentObject.toString(), percentageParam);
  }

  /**
   * @param  inputString string to scramble
   * @param  percentage  percentage of String to be modified (value between 0 and 100)
   * @return             Scrambled string
   */
  @ActionDescription(description = "Jumbles up the letters of the word so string does not make any sense", pepSupportedType = String.class)
  public String anagram(String inputString,
      @ActionParameterDescription(name = "percentage", description = "percentage of String to be modified (value between 0 and 100)", mandatory = true) int percentage) {

    if (inputString == null || inputString.length() <= 2) {
      return inputString;
    }

    boolean canBeScrambled = false;
    for (int i = 1; i < inputString.length(); i++) {
      final char c = inputString.charAt(i);
      if (c != inputString.charAt(0)) {
        canBeScrambled = true;
        break;
      }
    }

    if (!canBeScrambled) {
      return inputString;
    }

    final char[] a = inputString.toCharArray();

    final int limit = Math.round(a.length * (Math.min(percentage, 100) / 100f));

    for (int i = 0; i < Math.min(a.length - 1, limit); i++) {
      // TODO check whether we have to use SecureRandom here
      final int j = ThreadLocalRandom.current().nextInt(Math.min(a.length - 1, limit));
      final char temp = a[i];
      a[i] = a[j];
      a[j] = temp;
    }
    String result = new String(a);
    if (result.equals(inputString)) {
      result = this.anagram(inputString, percentage);
    }
    LOG.trace("Built anagram {}% of {} --> {}", percentage, inputString, result);
    return result;
  }

}

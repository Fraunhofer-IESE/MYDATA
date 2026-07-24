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

package de.fraunhofer.iese.mydata.pdp.language.model.function.bool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class GreaterEqualFunction.
 */
public class GreaterEqualFunction extends ArithmeticFunction {
  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(GreaterEqualFunction.class);

  /**
   * Instantiates a new greater equal function.
   */
  public GreaterEqualFunction() {
    super();
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.function.
   * ArithmeticFunction#compare(double, double)
   */
  @Override
  protected boolean compare(double a, double b) {
    final boolean res = a >= b;
    LOG.debug("Compare(): {} >= {} = {}", a, b, res);
    return res;
  }
}

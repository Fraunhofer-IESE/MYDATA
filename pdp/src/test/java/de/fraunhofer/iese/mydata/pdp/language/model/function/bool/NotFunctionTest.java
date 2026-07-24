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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.pdp.Evaluation;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

/**
 * The Class NotOperatorTest.
 */
public class NotFunctionTest {
  public Evaluation evaluation;

  /**
   * And operator basic tests.
   *
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  @MethodSource("data")
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @ParameterizedTest(name = "not {0}")
  public void andOperatorBasicTests(Evaluation evaluation) throws EvaluationUndecidableException {

    this.initNotFunctionTest(evaluation);

    if (evaluation.expectedException != null) {
      assertThrows(evaluation.expectedException, () -> {

        final NotFunction operator = new NotFunction();
        if (evaluation.parameters != null && evaluation.parameters.length >= 1) {
          operator.addParameter(new PolicyConstant(evaluation.parameters[0]));
        }

        final DataObject<Boolean> result = operator.evaluate(null);
        assertEquals(evaluation.expectedResult, result.getValue());
      });
      return;
    }

    final NotFunction operator = new NotFunction();
    if (evaluation.parameters != null && evaluation.parameters.length >= 1) {
      operator.addParameter(new PolicyConstant(evaluation.parameters[0]));
    }

    final DataObject<Boolean> result = operator.evaluate(null);
    assertEquals(evaluation.expectedResult, result.getValue());
  }

  /**
   * Data.
   *
   * @return the collection
   */
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {
            new Evaluation(true, false)
        }, {
            new Evaluation(false, true)
        }, {
            new Evaluation(IllegalArgumentException.class, 3)
        }, {
            new Evaluation(EvaluationUndecidableException.class)
        }
    });
  }

  public void initNotFunctionTest(Evaluation evaluation) {
    this.evaluation = evaluation;
  }

}

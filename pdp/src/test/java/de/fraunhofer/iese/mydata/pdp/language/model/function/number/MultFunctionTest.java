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

package de.fraunhofer.iese.mydata.pdp.language.model.function.number;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iese.mydata.pdp.Evaluation;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The Class AndOperatorTest.
 */
public class MultFunctionTest {
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
  @ParameterizedTest(name = "mult {0}")
  public void multOperatorBasicTests(Evaluation evaluation) throws EvaluationUndecidableException {

    this.initMultFunctionTest(evaluation);

    if (evaluation.expectedException != null) {
      assertThrows(evaluation.expectedException, () -> {

        final MultFunction operator = new MultFunction();
        final List<IFunction> params = new ArrayList<>();
        for (final Object o : evaluation.parameters) {
          params.add(new PolicyConstant(o));
        }

        operator.setParameters(params);

        final DataObject<Number> result = operator.evaluate(null);
        assertEquals(evaluation.expectedResult, result.getValue());
      });
      return;
    }

    final MultFunction operator = new MultFunction();
    final List<IFunction> params = new ArrayList<>();
    for (final Object o : evaluation.parameters) {
      params.add(new PolicyConstant(o));
    }

    operator.setParameters(params);

    final DataObject<Number> result = operator.evaluate(null);
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
            new Evaluation(1, 1, 1)
        }, {
            new Evaluation(18, 6, 3)
        }, {
            new Evaluation(0, 0, 0)
        }, {
            new Evaluation(2, 1, 2)
        }, {
            new Evaluation(0, 700, 200, 0)
        }, {
            new Evaluation(96, 12, 2, 2, 2)
        }, {
            new Evaluation(-2.25, -1.5, 1.5)
        }, {
            new Evaluation(EvaluationUndecidableException.class, -1.5, "test")
        }, {
            new Evaluation(4611686014132420609l, Integer.MAX_VALUE, Integer.MAX_VALUE)
        }, {
            new Evaluation(2147483648l, (Integer.MAX_VALUE + 1l) / 2, 2)

        }, {
            new Evaluation(EvaluationUndecidableException.class)
        }
    });
  }

  public void initMultFunctionTest(Evaluation evaluation) {
    this.evaluation = evaluation;
  }

}

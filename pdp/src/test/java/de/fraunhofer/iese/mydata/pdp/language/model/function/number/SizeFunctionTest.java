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
 * The Class SizeFunctionTest.
 */
public class SizeFunctionTest {
  public Evaluation evaluation;

  /**
   * Less function basic tests.
   *
   * @throws EvaluationUndecidableException the evaluation undecidable exception
   */
  @MethodSource("data")
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @ParameterizedTest(name = "size {0}")
  public void lessFunctionBasicTests(Evaluation evaluation) throws EvaluationUndecidableException {

    this.initSizeFunctionTest(evaluation);

    if (this.evaluation.expectedException != null) {
      assertThrows(this.evaluation.expectedException, () -> {

        final SizeFunction function = new SizeFunction();
        final List<IFunction> params = new ArrayList<>();
        for (final Object o : this.evaluation.parameters) {
          params.add(new PolicyConstant(o, Object[].class));
        }

        function.setParameters(params);

        final DataObject<Integer> result = function.evaluate(null);
        assertEquals(this.evaluation.expectedResult, result.getValue());
      });
      return;
    }

    final SizeFunction function = new SizeFunction();
    final List<IFunction> params = new ArrayList<>();
    for (final Object o : this.evaluation.parameters) {
      params.add(new PolicyConstant(o, Object[].class));
    }

    function.setParameters(params);

    final DataObject<Integer> result = function.evaluate(null);
    assertEquals(this.evaluation.expectedResult, result.getValue());
  }

  /**
   * Data.
   *
   * @return the collection
   */
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {
            new Evaluation(3, new Object[] {
                1, 2, 3
            }, null)
        }, {
            new Evaluation(0, new Object[] {

            }, null)
        }, {
            new Evaluation(3, Arrays.asList(new Object[] {
                1, 2, 3
            }), null)
        }
    });
  }

  public void initSizeFunctionTest(Evaluation evaluation) {
    this.evaluation = evaluation;
  }

}

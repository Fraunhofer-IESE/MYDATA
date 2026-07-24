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

import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.function.EMultiFunctionMode;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class ContainsFunction checks if the list provided as first parameter contains one or all of
 * the subsequently provided elements.
 */
public class ContainsFunction extends BooleanFunction {

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ContainsFunction.class);

  /**
   * The function mode.
   */
  private EMultiFunctionMode functionMode;

  /**
   * Instantiates a new contains function.
   */
  public ContainsFunction() {
    this(EMultiFunctionMode.ALL);
  }

  /**
   * Instantiates a new contains function.
   *
   * @param mode the mode (match all or one)
   */
  public ContainsFunction(EMultiFunctionMode mode) {
    super();
    this.functionMode = mode;
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.function.BooleanFunction
   * #evaluate(de.fraunhofer.iese.mydata.api.policy.Event)
   */
  @Override
  public DataObject<Boolean> evaluate(Event e) throws EvaluationUndecidableException {
    LOG.debug("Entering evaluate(name={}, parameters={}, event={})", this.getName(),
        this.getParameters(), e);

    if (this.getParameters().isEmpty() || this.getParameters().size() < 2) {
      LOG.warn("Insufficient parameters for size function");
      throw new IllegalArgumentException("Insufficent parameters");
    }

    final List<?> list = this.getList(this.getParameters().get(0).evaluate(e).getValue());

    LOG.debug("Mode is {}", this.functionMode);
    int numberOfMatches = 0;
    for (int i = 1; i < this.getParameters().size(); i++) {
      final IFunction param = this.getParameters().get(i);
      final Object paramValue = param.evaluate(e).getValue();

      boolean contained = false;

      if (paramValue.getClass() == String.class) {
        final JsonElement a = new Gson().fromJson(paramValue.toString(), JsonElement.class);
        for (final Object listElement : list) {
          if (listElement.getClass() != String.class) {
            continue;
          }
          final JsonElement b = new Gson().fromJson(listElement.toString(), JsonElement.class);
          if (a.equals(b)) {
            contained = true;
            break;
          }
        }
      } else {
        contained = list.contains(paramValue);
      }

      if (contained && this.functionMode == EMultiFunctionMode.AT_LEAST_ONE) {
        LOG.debug("Leaving evaluate(): true. Parameter {} found and mode is AT_LEAST_ONE",
            paramValue);
        return PolicyConstant.TRUE;

      } else if (!contained && this.functionMode == EMultiFunctionMode.ALL) {
        LOG.debug(
            "Leaving evaluate(): false. Parameter {} does not exist in the list, but mode is ALL",
            paramValue);
        return PolicyConstant.FALSE;

      } else if (contained && this.functionMode == EMultiFunctionMode.NONE) {
        LOG.debug("Leaving evaluate(): false. Parameter {} contained in the list, but mode is NONE",
            paramValue);
        return PolicyConstant.FALSE;

      } else if (contained && this.functionMode == EMultiFunctionMode.EXACTLY_ONE) {
        if (numberOfMatches > 0) {
          LOG.debug(
              "Leaving evaluate(): false. More than one parameter contained in the list, but mode is EXACTLY_ONE");
          return PolicyConstant.FALSE;
        }
        numberOfMatches++;
      }
    }

    if (this.functionMode == EMultiFunctionMode.EXACTLY_ONE) {
      final PolicyConstant<Boolean> result = numberOfMatches == 1 ? PolicyConstant.TRUE
          : PolicyConstant.FALSE;

      LOG.debug("Leaving evaluate(): {}. {} parameters matched in EXACTLY_ONE mode",
          result.getValue(), numberOfMatches);
      return result;
    }

    LOG.debug("Leaving evaluate(): {}", this.functionMode != EMultiFunctionMode.AT_LEAST_ONE);
    return this.functionMode == EMultiFunctionMode.AT_LEAST_ONE ? PolicyConstant.FALSE
        : PolicyConstant.TRUE;

  }

  /**
   * Creates a List from the given object.
   *
   * @param  o an array or list
   * @return   a List from the given object.
   */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  private List getList(final Object o) {
    List list;

    if (o.getClass().isArray()) {
      list = new ArrayList<>();
      final int length = Array.getLength(o);
      for (int i = 0; i < length; i++) {
        final Object arrayElement = Array.get(o, i);
        list.add(arrayElement);
      }
    } else if (o instanceof List) {
      list = (List<?>) o;

    } else {
      LOG.warn("Parameter must be an Array or List, but is of type {}", o.getClass());
      throw new IllegalArgumentException(
          "Parameter must be an Array or List, but is of type " + o.getClass());
    }
    return list;
  }

  /**
   * Gets the function mode.
   *
   * @return the function mode
   */
  @XmlAttribute(name = "mode")
  public EMultiFunctionMode getFunctionMode() {
    return this.functionMode;
  }

  public void setFunctionMode(EMultiFunctionMode functionMode) {
    this.functionMode = functionMode;
  }
}

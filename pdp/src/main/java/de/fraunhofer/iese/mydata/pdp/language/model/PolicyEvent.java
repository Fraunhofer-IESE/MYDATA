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

package de.fraunhofer.iese.mydata.pdp.language.model;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.pdp.language.parser.PolicyVisitor;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.parameter.DataType;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Class Parameter.
 *
 * @param <T> the generic type
 */
@XmlRootElement(namespace = "http://www.mydata-control.de/4.0/event")
public class PolicyEvent<T> extends DataObject<T> implements IFunction {

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PolicyEvent.class);

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 4955861931044219944L;

  protected DataObject<T> defaultValue;

  /**
   * The name.
   */
  private String name;

  private String jsonPathQuery;

  /**
   * The {@link DataType} of the value.
   */
  private String type;

  /**
   * The modify engine.
   */
  private transient List<PolicyModify> modifyEngine;

  public PolicyEvent() {
    // Used for JAXB
  }

  /**
   * Instantiates a new pe.
   *
   * @param clazz the clazz
   */
  public PolicyEvent(Class<T> clazz) {
    super(clazz);
    this.setModifyEngine(new ArrayList<>());
    this.type = clazz.getCanonicalName();
  }

  /**
   * Instantiates a new constant.
   *
   * @param val the val
   */
  public PolicyEvent(T val) {
    super(val);
    this.setModifyEngine(new ArrayList<>());
    this.type = val.getClass().getCanonicalName();
  }

  /**
   * Instantiates a new constant.
   *
   * @param val   the val
   * @param clazz the clazz
   */
  public PolicyEvent(T val, Class<T> clazz) {
    super(clazz);
    if (val instanceof String) {
      this.setName((String) val);
    } else {
      super.setValue(val);
    }

    this.setModifyEngine(new ArrayList<>());
    this.type = clazz.getCanonicalName();
  }

  /**
   * Instantiates a new parameter.
   *
   * @param name       the name
   * @param clazz      the clazz
   * @param expression the expression
   */
  public PolicyEvent(String name, Class<T> clazz, String expression) {
    this.setName(name);
    this.setModifyEngine(new ArrayList<>());
    this.jsonPathQuery = expression;
    this.type = clazz.getCanonicalName();
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.Constant#evaluate(de.
   * fraunhofer.iese.mydata.internal.policy.Event)
   */
  @Override
  public DataObject<?> evaluate(Event evt) throws EvaluationUndecidableException {
    LOG.debug("Entering evaluate(name={}, expression={}", this.getName(), this.jsonPathQuery);
    LOG.trace("event={}", evt);

    if (evt == null) {
      return this.defaultValue;
    }
    final de.fraunhofer.iese.mydata.policy.parameter.Parameter<?> eventParam = evt
        .getParameterForName(this.getName());

    if (eventParam == null) {
      if (this.defaultValue != null) {
        return this.defaultValue;
      } else {
        throw new EvaluationUndecidableException(
            "Neither parameter nor event contain value " + this.getName());
      }
    }

    if (this.jsonPathQuery == null) {
      return this.evaluateEvent(evt, eventParam);
    }

    // TODO check whether .getValue().toString() is appropriate
    return this.evaluateExpression(this.evaluateEvent(evt, eventParam).getValue().toString());
  }

  @Override
  public void accept(PolicyVisitor policyVisitor) {
    policyVisitor.visit(this);
  }

  /**
   * @param  result
   * @return
   * @throws EvaluationUndecidableException
   */
  private DataObject<T> evaluateExpression(String result) throws EvaluationUndecidableException {
    DataObject<T> resultAfterJsonPath = null;

    try {

      final Object expressionResult = JsonPath.parse(result).read(this.jsonPathQuery);

      if (expressionResult.getClass() == this.getType() || (expressionResult instanceof Number
          && Number.class.isAssignableFrom(this.getType()))) {
        resultAfterJsonPath = new DataObject<>((T) expressionResult);
        return resultAfterJsonPath;

      } else if (expressionResult instanceof JSONArray) {
        final Type collectionType = new TypeToken<List<?>>() {
        }.getType();
        final String expressionResultAsJSON = MyDataEntity.getGson().toJson(expressionResult);
        final List<?> genericType = MyDataEntity.getGson().fromJson(expressionResultAsJSON,
            collectionType);
        // TODO isComplex?
        resultAfterJsonPath = new DataObject(genericType); // TODO optimize

      } else if (expressionResult instanceof LinkedHashMap) {
        resultAfterJsonPath = new DataObject(MyDataEntity.getGson()
            .fromJson(MyDataEntity.getGson().toJson(expressionResult), this.getType())); // TODO
                                                                                                                                                   // optimize

      } else {
        throw new EvaluationUndecidableException(
            "JSON Path expression resulted in incompatible return type. Expected "
                + this.type
                + ", but was "
                + expressionResult.getClass());
      }
    } catch (final JsonSyntaxException | InvalidJsonException | PathNotFoundException e) {
      if (result != null && result.startsWith("[")) {
        throw new EvaluationUndecidableException(
            "JSON Path expression resulted in incompatible return value. Did you send a list for the decision? List are not supported yet, please use a wrapper.",
            e);
      } else {
        if (e.getMessage().contains("Missing property in path")) {
          LOG.info(
              "JSON Path expression did not match any value. The default value will be rendered.",
              e);
          return this.defaultValue;
        } else {
          throw new EvaluationUndecidableException(
              "JSON Path expression resulted in incompatible return value.", e);
        }
      }
    }

    if (this.getType().isAssignableFrom(resultAfterJsonPath.getType()))

    {
      return resultAfterJsonPath;
    }
    throw new EvaluationUndecidableException(
        "JSON Path expression resulted in incompatible return type. Expected "
            + this.type
            + ", but was "
            + resultAfterJsonPath.getType());
  }

  /**
   * Retrieves the parameter value from an event.
   *
   * @param  evt                            the event
   * @return                                the value of the parameter with the given name
   * @throws EvaluationUndecidableException if the event does not contain such a parameter
   */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  private DataObject<T> evaluateEvent(Event evt, Parameter eventParam)
      throws EvaluationUndecidableException {
    LOG.debug("Parameter does not have value, querying event");

    if (this.isEventParamCompartible(eventParam) || this.equalTypes(eventParam)
        || this.getType() == Object.class || this.jsonPathQuery != null) {
      LOG.debug("Leaving evaluateEvent(): {}", eventParam.getValue());

      final PolicyConstant policyConstant;
      final Object o = eventParam.getValue();
      if (ClassUtils.isPrimitiveOrWrapper(o.getClass()) || o instanceof String
          || o instanceof List) {
        // TODO why can a List be used here? does it provide a toString for evaluateExpression?
        policyConstant = new PolicyConstant(o);
      } else {
        // TODO type information will be lost...
        policyConstant = new PolicyConstant(MyDataEntity.getGson().toJson(o));
      }

      policyConstant.setComplex(this.getType() == Object.class);
      return policyConstant;
    }
    LOG.warn("Incompatible return types. Expected {}, but is {}", this.type,
        eventParam.getTypeName());
    throw new EvaluationUndecidableException("Event does not contain parameter " + this.getName());
  }

  private boolean equalTypes(Parameter<?> eventParam) {
    return this.getType().getCanonicalName().equals(eventParam.getTypeName());
  }

  private boolean isEventParamCompartible(Parameter<?> eventParam) {
    return eventParam.getType() != null && this.getType().isAssignableFrom(eventParam.getType());
  }

  /**
   * Gets the modify engine.
   *
   * @return the modifyEngine
   */
  public List<PolicyModify> getModifyEngine() {
    return this.modifyEngine;
  }

  /**
   * Sets the modify engine.
   *
   * @param modifyEngine the modifyEngine to set
   */
  public void setModifyEngine(List<PolicyModify> modifyEngine) {
    this.modifyEngine = modifyEngine;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Override
  @XmlAttribute(name = "eventParameter")
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((this.modifyEngine == null) ? 0 : this.modifyEngine.hashCode());
    result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    @SuppressWarnings("rawtypes")
    final PolicyEvent other = (PolicyEvent) obj;
    if (this.modifyEngine == null) {
      if (other.modifyEngine != null) {
        return false;
      }
    } else if (!this.modifyEngine.equals(other.modifyEngine)) {
      return false;
    }
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  /**
   * Gets the {@link DataType} of the value.
   *
   * @return the {@link DataType} of the value
   */
  @Override
  public Class<?> getType() {
    try {
      return Class.forName(this.type);
    } catch (final ClassNotFoundException e) {
      LOG.trace("Class not found for {}", this.type, e);
      return null;
    }
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the jsonPathQuery Expression
   */
  @XmlAttribute()
  public String getJsonPathQuery() {
    return this.jsonPathQuery;
  }

  /**
   * @param expression the jsonPathQuery expression to set
   */
  public void setJsonPathQuery(String expression) {
    this.jsonPathQuery = expression;
  }

}

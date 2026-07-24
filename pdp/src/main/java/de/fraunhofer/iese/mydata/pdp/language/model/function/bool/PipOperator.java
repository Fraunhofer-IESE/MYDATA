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

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.information.method.InputParameterDescription;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.pdp.PolicyDecisionPoint;
import de.fraunhofer.iese.mydata.pdp.language.model.BooleanParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.ListParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.NumberParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.ObjectParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.StringParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.pdp.language.parser.PolicyVisitor;
import de.fraunhofer.iese.mydata.policy.PipRequest;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.InfoId;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.parameter.ParameterList;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The Class PipOperator.
 *
 * @param <T>
 */
@XmlRootElement
@Getter

@SuppressWarnings("javadoc")
public class PipOperator<T> implements IFunction {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PipOperator.class);

  /**
   * The default return value.
   */
  private DataObject<T> defaultReturnValue;

  /**
   * ttl (time to live) string, might be null
   */
  private String ttl;

  /**
   * ttl (time to live) in milliseconds, based on the ttl string
   */
  private long timeToLive;

  /**
   * The method name.
   */
  private String methodName;

  /**
   * The sub operators.
   */
  @XmlTransient
  private List<IFunction> subOperators;

  /**
   * The return type.
   */
  protected Class<?> returnType;

  private T defaultValue;

  /**
   * default constructor
   */
  public PipOperator() {
    super();
    this.returnType = Object.class;
  }

  /**
   * Instantiates a new PIP operator.
   *
   * @param  resourceIdentifierURN  the resource identifier URN
   * @param  ttl                    the ttl in the format XyXwXdXhXmXs
   * @param  defaultValue           the default value
   * @param  retType                the ret type
   * @throws InvalidEntityException
   */
  public PipOperator(final String resourceIdentifierURN, final String ttl,
      final DataObject<T> defaultValue, final Class<?> retType) throws InvalidEntityException {
    this.defaultReturnValue = defaultValue;
    this.ttl = ttl;
    this.timeToLive = this.getMillis(ttl);
    this.methodName = resourceIdentifierURN;
    final InfoId iid = new InfoId(this.methodName);
    MyDataEntity.validate(iid);
    this.returnType = retType;
  }

  /**
   * Gets the millis.
   *
   * @param  ttl the ttl in the format XyXwXdXhXmXs
   * @return     the millis
   */
  private long getMillis(final String ttl) {
    // Sanity check
    if (ttl == null || ttl.length() < 2) {
      return 0;
    }

    // Init
    final BigInteger maxValue = BigInteger.valueOf(Long.MAX_VALUE);
    BigInteger millisecondsForTTL = BigInteger.ZERO;

    // Tokenize TTL String and add up the values
    final StringTokenizer ttlTokenizer = new StringTokenizer(ttl, "ywdhms", true);
    while (ttlTokenizer.hasMoreTokens()) {
      final String valueRaw = ttlTokenizer.nextToken();
      String unit = "";

      // Sanity check
      if (ttlTokenizer.hasMoreTokens()) {
        unit = ttlTokenizer.nextToken();
      } else { // No more tokens? Unit is missing -> Wrong format
        LOG.error("Wrong TTL format supplied. Ignoring TTL (TTL = 0).");
        return 0;
      }

      BigInteger valueAsBigInteger;
      try {
        valueAsBigInteger = BigInteger.valueOf(Long.valueOf(valueRaw));
      } catch (final NumberFormatException e) {
        LOG.error("Number of PIP TTL Value is to big or has a wrong format (unit: {}; value: {})!",
            unit, valueRaw);
        continue;
      }

      BigInteger multiplyFactor;
      switch (unit) {
        case "y":
          multiplyFactor = BigInteger.valueOf((long) 1000 * 60 * 60 * 24 * 365);
          break;

        case "w":
          multiplyFactor = BigInteger.valueOf((long) 1000 * 60 * 60 * 24 * 7);
          break;

        case "d":
          multiplyFactor = BigInteger.valueOf((long) 1000 * 60 * 60 * 24);
          break;

        case "h":
          multiplyFactor = BigInteger.valueOf((long) 1000 * 60 * 60);
          break;

        case "m":
          multiplyFactor = BigInteger.valueOf((long) 1000 * 60);
          break;

        case "s":
          multiplyFactor = BigInteger.valueOf(1000);
          break;

        default: // no valid unit -> wrong format
          LOG.error("Wrong TTL format supplied. Ignoring TTL (TTL = 0).");
          return 0;
      }

      millisecondsForTTL = millisecondsForTTL.add(valueAsBigInteger.multiply(multiplyFactor));

      // Sanity check - ensure that value does not exceed Long.MAX_VALUE ->
      // prevent long overflow
      if (millisecondsForTTL.compareTo(maxValue) > 0) {
        LOG.error(
            "Number of PIP TTL Value is to big. Using Long.MAX_VALUE as this is the maximum waiting time!");
        return Long.MAX_VALUE;
      }
    }

    return millisecondsForTTL.longValue();
  }

  /**
   * Gets the default return value.
   *
   * @return the default return value
   */
  public DataObject<T> getDefaultReturnValue() {
    return this.defaultReturnValue;
  }

  public T getDefaultValue() {
    return this.defaultValue;
  }

  public void setDefaultValue(T defaultValue) {
    this.defaultValue = defaultValue;
  }

  protected void setDefaultReturnValue(DataObject<T> defaultReturnValue) {
    this.defaultReturnValue = defaultReturnValue;
  }

  /**
   * Gets the method name.
   *
   * @return the method name
   */
  @XmlAttribute(name = "method")
  public String getMethodName() {
    return this.methodName;
  }

  public void setMethodName(String methodName) throws InvalidEntityException {
    this.methodName = methodName;
    final InfoId iid = new InfoId(methodName);
    MyDataEntity.validate(iid);
  }

  /**
   * Get the ttl (time to live) string
   *
   * @return the ttl string
   */
  @XmlAttribute(name = "ttl")
  public String getTtl() {
    return this.ttl;
  }

  public void setTtl(String ttl) {
    this.ttl = ttl;
    this.timeToLive = this.getMillis(ttl);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.IOperator#evaluate(de.
   * fraunhofer.iese.mydata.internal.policy.Event)
   */
  @Override
  public DataObject<?> evaluate(final Event evt) throws EvaluationUndecidableException {
    LOG.debug("Evaluating PIP request: {}", this.methodName);

    final MethodInterfaceDescription pipQuery = new MethodInterfaceDescription(this.methodName,
        this.returnType, "");
    final ParameterList paramList = new ParameterList();
    if (this.subOperators != null) {
      for (final IFunction operator : this.subOperators) {
        if (operator instanceof PolicyParameter) {
          final PolicyParameter<?> param = (PolicyParameter<?>) operator;
          final DataObject<?> evalResult = param.evaluate(evt);
          pipQuery.addParameter(
              new InputParameterDescription(param.getName(), "", evalResult.getType()));
          paramList.add(new de.fraunhofer.iese.mydata.policy.parameter.Parameter<>(param.getName(),
              evalResult.getValue()));
        }
      }
    }

    PipRequest pipRequestObject;
    try {
      pipRequestObject = new PipRequest(new InfoId(this.methodName), paramList);
      return PolicyDecisionPoint.getInstance().getPipOperatorCache().getEvalResultFromCache(
          this.timeToLive, pipQuery, pipRequestObject, SolutionId.fromActionId(evt.getActionId()));

    } catch (final Exception e) {
      LOG.warn("Error during PIP evaluation, returning default value: {}", this.defaultReturnValue,
          e);
      return this.defaultReturnValue;
    }

  }

  @Override
  public void accept(PolicyVisitor policyVisitor) {
    policyVisitor.visit(this);
  }

  /*
   * (non-Javadoc)
   * @see de.fraunhofer.iese.mydata.pdp.java.language.model.IOperator#getType()
   */
  @Override
  public Class<?> getType() {
    return this.returnType;
  }

  @XmlElements({
      @XmlElement(name = "string", namespace = "http://www.mydata-control.de/4.0/parameter", type = StringParameter.class),
      @XmlElement(name = "object", namespace = "http://www.mydata-control.de/4.0/parameter", type = ObjectParameter.class),
      @XmlElement(name = "list", namespace = "http://www.mydata-control.de/4.0/parameter", type = ListParameter.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/parameter", type = BooleanParameter.class),
      @XmlElement(name = "number", namespace = "http://www.mydata-control.de/4.0/parameter", type = NumberParameter.class)
  })
  public List<IFunction> getSubOperators() {
    return this.subOperators;
  }

  public void setSubOperators(List<IFunction> subOperators) {
    this.subOperators = subOperators;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

}

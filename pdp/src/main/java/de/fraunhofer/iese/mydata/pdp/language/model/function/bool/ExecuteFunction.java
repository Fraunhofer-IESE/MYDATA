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
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.pdp.PxpExecutor;
import de.fraunhofer.iese.mydata.pdp.language.model.BooleanParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.ListParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.NumberParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.ObjectParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.StringParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.pdp.language.parser.PolicyVisitor;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class ExecuteAction.
 */
@XmlRootElement(name = "execute", namespace = "http://www.mydata-control.de/4.0/mydataLanguage")
public class ExecuteFunction implements IFunction {

  private static final Logger LOG = LoggerFactory.getLogger(ExecuteFunction.class);

  /**
   * The function and parameters.
   */
  private List<PolicyParameter<?>> parameters;

  /**
   * The name.
   */
  private String name;

  /**
   * Instantiates a new execute action.
   *
   * @throws InvalidEntityException
   */
  public ExecuteFunction() throws InvalidEntityException {
    this.setParameters(new ArrayList<PolicyParameter<?>>());
  }

  /**
   * Instantiates a new execute action.
   *
   * @param numFunctionsAndParameters the num functions and parameters
   */
  public ExecuteFunction(int numFunctionsAndParameters) {
    this.setParameters(new ArrayList<PolicyParameter<?>>(numFunctionsAndParameters));
  }

  /**
   * Gets the function and parameters.
   *
   * @return the functionAndParameters
   */
  @XmlElements({
      @XmlElement(name = "string", namespace = "http://www.mydata-control.de/4.0/parameter", type = StringParameter.class),
      @XmlElement(name = "object", namespace = "http://www.mydata-control.de/4.0/parameter", type = ObjectParameter.class),
      @XmlElement(name = "list", namespace = "http://www.mydata-control.de/4.0/parameter", type = ListParameter.class),
      @XmlElement(name = "boolean", namespace = "http://www.mydata-control.de/4.0/parameter", type = BooleanParameter.class),
      @XmlElement(name = "number", namespace = "http://www.mydata-control.de/4.0/parameter", type = NumberParameter.class)
  })
  public List<PolicyParameter<?>> getParameters() {
    return this.parameters;
  }

  /**
   * Sets the function and parameters.
   *
   * @param parameters the functionAndParameters to set
   */
  public void setParameters(List<PolicyParameter<?>> parameters) {
    this.parameters = parameters;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Override
  @XmlAttribute(name = "action")
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name.
   *
   * @param  name                   the name to set
   * @throws InvalidEntityException
   */
  public void setName(String name) throws InvalidEntityException {
    this.name = name;
    final ActionId aid = new ActionId(name);
    MyDataEntity.validate(aid);
  }

  @Override
  public Class<?> getType() {
    return null;
  }

  @Override
  public DataObject<?> evaluate(Event evt) throws EvaluationUndecidableException {
    try {
      return new DataObject<>(new PxpExecutor(this, evt).call());
    } catch (final Exception e) {
      LOG.info("Exception catched at evaluate", e);
      return new DataObject<>(false);
    }
  }

  @Override
  public void accept(PolicyVisitor policyVisitor) {
    policyVisitor.visit(this);
  }
}

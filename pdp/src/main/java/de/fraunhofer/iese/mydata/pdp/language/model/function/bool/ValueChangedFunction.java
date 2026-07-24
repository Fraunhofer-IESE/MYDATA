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
import de.fraunhofer.iese.mydata.pdp.PolicyDecisionPoint;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import com.google.gson.Gson;
import jakarta.xml.bind.annotation.XmlAttribute;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("javadoc")
public class ValueChangedFunction extends BooleanFunction {

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ValueChangedFunction.class);

  protected final transient Gson gson = MyDataEntity.getGson();

  String changedTo;

  boolean hasChanged = false;

  private String id;

  public ValueChangedFunction() {
    super();
  }

  public ValueChangedFunction(String id, String changedTo) {
    super();
    this.id = id;
    this.changedTo = changedTo;
  }

  /**
   * The identifier for the block, unique and mandatory for this policy
   *
   * @return
   */
  @XmlAttribute(name = "component_id")
  public String getId() {
    return this.id;
  }

  public void setId(String str) {
    this.id = str;
  }

  /**
   * @return the minOccurrences
   */
  @XmlAttribute(name = "to")
  public String getChangedTo() {
    return this.changedTo;
  }

  public void setChangedTo(String changedTo) {
    this.changedTo = changedTo;
  }

  /**
   * Gets the data from the database for the current valueChange block based on its identifier and
   * the policyID Compares the value in the db with the current evaluation Compares if needed to the
   * changedTo attribute Returns false if: nothing in the db OR dbValue!=policyValue but changedTo
   * attribute condition not fullfilled Returns true component_id: dbValue!=policyValue AND
   * changedTo attribute fullfilled (if present) Update the value in the DB if the
   * dbValue!=policyValue
   */
  @Override
  public DataObject<Boolean> evaluate(Event evt) throws EvaluationUndecidableException {
    LOG.debug("Entering evaluate valueChanged(name={}, parameters={}, event={})", this.getName(),
        this.getParameters(), evt.getActionId());
    if (this.getParameters().isEmpty()) {
      LOG.warn("Insufficient parameters for value changed function");
      throw new IllegalArgumentException("Insufficent parameters");
    }

    boolean conditionFullfiled = false;
    PolicyId policyId = null;
    Object valueInPolicy = null;
    String hashedValueInDB = null;
    String toStringValue = "";
    String hashedPolicyValue = "";
    final Policy p = new Policy();
    try {
      policyId = evt.getPolicyId();
      p.setPolicyId(policyId);
      for (final IFunction op : this.getParameters()) {
        final DataObject<?> evaluation = op.evaluate(evt);
        valueInPolicy = evaluation.getValue();
      }
      final Optional<IEventRepository> eventRepositoryOptional = PolicyDecisionPoint.getInstance()
          .getEventRepository();
      if (!eventRepositoryOptional.isPresent()) {
        return PolicyConstant.FALSE;
      }
      hashedValueInDB = eventRepositoryOptional.get().getValueChanged(this.id, p);

      if (valueInPolicy instanceof Boolean) {
        toStringValue = String.valueOf(valueInPolicy);
      } else if (valueInPolicy instanceof String) {
        toStringValue = (String) valueInPolicy;
      } else if (valueInPolicy instanceof Number) {
        toStringValue = String.valueOf(new BigDecimal((String) valueInPolicy));
      } else if (valueInPolicy instanceof List) {
        toStringValue = this.gson.toJson(valueInPolicy);
      } else if (valueInPolicy instanceof Object) {
        toStringValue = this.gson.toJson(valueInPolicy);
      }
      hashedPolicyValue = DigestUtils.sha256Hex(toStringValue);

      // if nothing in the db, then it returns false (cf. definition DB_VALUE !=
      // CHILD_VALUE ^ (TO_VALUE == CHILD_VALUE v TO_VALUE == null))
      // if value in DB is null from start, then the first save is considered as
      // a change
      if (hashedValueInDB == null && hashedPolicyValue != null) {
        this.hasChanged = true;
      }
      conditionFullfiled = this.compareStrings(hashedValueInDB, hashedPolicyValue);
      if (conditionFullfiled) {
        return PolicyConstant.TRUE;
      } else {
        return PolicyConstant.FALSE;
      }
    } finally {
      try {

        if (valueInPolicy != null && policyId != null && this.hasChanged) {
          final Optional<IEventRepository> eventRepositoryOptional = PolicyDecisionPoint
              .getInstance().getEventRepository();
          if (eventRepositoryOptional.isPresent()) {
            eventRepositoryOptional.get().setValueChanged(p, this.id, hashedPolicyValue);
          }
        }
      } catch (final Exception e) {
        LOG.info("Could not update the value in the DB: {}", e.getMessage(), e);
      }
    }

  }

  private boolean compareStrings(String valueInDB, String valueInPolicy) {

    if (!valueInPolicy.equalsIgnoreCase(valueInDB)) {
      this.hasChanged = true;
    }
    if (valueInDB != null && valueInDB.equalsIgnoreCase(valueInPolicy)
        || (valueInDB == null && valueInPolicy == null)) {
      return false;
    }
    if (this.changedTo != null) {
      final String hashChangedTo = DigestUtils.sha256Hex(this.changedTo);
      if (valueInPolicy.equalsIgnoreCase(hashChangedTo)) {
        return true;
      }
      return false;
    }
    if (valueInPolicy != null && valueInDB == null) {
      return true;
    }
    if (!valueInDB.equalsIgnoreCase(valueInPolicy)) {
      return true;
    }
    return false;
  }

}

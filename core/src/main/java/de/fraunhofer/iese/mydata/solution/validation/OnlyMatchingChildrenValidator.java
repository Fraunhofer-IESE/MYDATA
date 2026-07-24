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

package de.fraunhofer.iese.mydata.solution.validation;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEvent;
import de.fraunhofer.iese.mydata.solution.Solution;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.timer.Timer;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * The Class PolicyValidator.
 */
public class OnlyMatchingChildrenValidator
    implements ConstraintValidator<OnlyMatchingChildren, Solution> {

  @Override
  public void initialize(OnlyMatchingChildren constraintAnnotation) {
    // usually blank
  }

  /**
   * Use this method to test the constraint.
   *
   * @see jakarta.validation.ConstraintValidator#isValid(java.lang.Object,
   *      jakarta.validation.ConstraintValidatorContext)
   */
  @Override
  public boolean isValid(Solution solution, ConstraintValidatorContext context) {

    final SolutionId sId = solution.getSolutionId();

    if (sId == null) {
      return false;
    }

    if (!this.validatePeps(sId, solution.getPeps())) {
      return false;
    }

    if (!this.validatePips(sId, solution.getPips())) {
      return false;
    }

    if (!this.validatePxps(sId, solution.getPxps())) {
      return false;
    }

    if (!this.validatePolicies(sId, solution.getPolicies())) {
      return false;
    }

    if (!this.validateTimers(sId, solution.getTimers())) {
      return false;
    }

    //    if (!validateHistoricEvents(sId, solution.getHistoricEvents())) { //TODO in action id?
    //      return false;
    //    }

    return true;
  }

  private boolean validateHistoricEvents(SolutionId sId, Set<HistoricEvent> historicEvents) {
    if (null == historicEvents) {
      return true;
    }
    for (final HistoricEvent h : historicEvents) {
      if (h == null) {
        return false;
      }
      try {
        final ActionId actionId = h.getActionId();
        MyDataEntity.validateAndNullCheck(actionId);
        if (!sId.equals(SolutionId.fromActionId(h.getActionId()))) {
          return false;
        }
      } catch (final InvalidEntityException e) {
        return false;
      }
    }
    return true;

  }

  private boolean validateTimers(SolutionId sId, Set<Timer> timerSet) {
    if (timerSet == null) {
      return true;
    }
    for (final Timer p : timerSet) {
      if (p == null) {
        return false;
      }
      try {
        MyDataEntity.validateAndNullCheck(p);
        if (!sId.equals(SolutionId.fromTimerId(p.getTimerId()))) {
          return false;
        }
      } catch (final InvalidEntityException e) {
        return false;
      }
    }
    return true;
  }

  private boolean validatePxps(SolutionId sId, Set<PxpComponentInformation> pxpSet) {
    if (pxpSet == null) {
      return true;
    }
    for (final PxpComponentInformation p : pxpSet) {
      if (p == null) {
        return false;
      }
      try {
        MyDataEntity.validateAndNullCheck(p);
        if (!sId.equals(SolutionId.fromComponentId(p.getComponentId()))) {
          return false;
        }
      } catch (final InvalidEntityException e) {
        return false;
      }
    }
    return true;
  }

  private boolean validatePips(SolutionId sId, Set<PipComponentInformation> pipSet) {
    if (pipSet == null) {
      return true;
    }
    for (final PipComponentInformation p : pipSet) {
      if (p == null) {
        return false;
      }
      try {
        MyDataEntity.validateAndNullCheck(p);
        final SolutionId solutionId = SolutionId.fromComponentId(p.getComponentId());
        if (!sId.equals(solutionId)) {
          return false;
        }
      } catch (final InvalidEntityException e) {
        return false;
      }
    }
    return true;
  }

  private boolean validatePeps(SolutionId sId, Set<PepComponentInformation> set) {
    if (set == null) {
      return true;
    }
    for (final PepComponentInformation p : set) {
      if (p == null) {
        return false;
      }
      try {
        MyDataEntity.validateAndNullCheck(p);
        if (!sId.equals(SolutionId.fromComponentId(p.getComponentId()))) {
          return false;
        }
      } catch (final InvalidEntityException e) {
        return false;
      }
    }
    return true;
  }

  private boolean validatePolicies(SolutionId sId, Set<Policy> list) {
    if (list == null) {
      return true;
    }
    for (final Policy p : list) {
      if (p == null) {
        return false;
      }
      try {
        MyDataEntity.validateAndNullCheck(p);
        if (!sId.equals(SolutionId.fromPolicyId(p.getPolicyId()))) {
          return false;
        }
      } catch (final InvalidEntityException e) {
        return false;
      }
    }
    return true;
  }

}

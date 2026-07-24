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

package de.fraunhofer.iese.mydata.internal;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.decision.ExecuteAction;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @see MyDataComponentWrapper
 */
public class PxpWrapper extends MyDataComponentWrapper<IPolicyExecutionPoint> implements IPolicyExecutionPoint {
  private static final Logger LOG = LoggerFactory.getLogger(PxpWrapper.class);

  public PxpWrapper(ComponentId componentId, Object instanceToWrap) throws InvalidEntityException {
    super(componentId, instanceToWrap, ComponentType.PXP);
  }


  @Override
  public boolean execute(ExecuteAction action) throws IOException {
    if (null != this.alreadyRightTypedReference) {
      return this.alreadyRightTypedReference.execute(action);
    } else {
      try {
        return (boolean) callServiceMethod(action.getId().getUrn(), action.getParameters());
      } catch (InformationUndeterminableException e) {
        LOG.error("PXP call failed with reason: {}", e.getMessage(), e);
        return false;
      }
    }
  }
}

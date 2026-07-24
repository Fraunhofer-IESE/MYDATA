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

package de.fraunhofer.iese.mydata.component.connector;

import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.health.Status;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.policy.PipRequest;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import java.io.IOException;

/**
 * @author Fraunhofer IESE
 */
//@XmlRootElement
@Connector(protocol = {
    "test"
}, type = ComponentType.PIP)
public class CorruptedTestPipTestConnector implements IPolicyInformationPoint {

  // Should have url as parameter. For test purposes.
  public CorruptedTestPipTestConnector() {

  }

  @Override
  public DataObject<?> evaluate(final PipRequest pipRequest) throws IOException, InformationUndeterminableException {
    return null;
  }

  @Override
  public boolean reset() throws IOException {
    return false;
  }

  @Override
  public ComponentId getId() throws IOException {
    return null;
  }

  @Override
  public HealthStatus getHealth() throws IOException {
    return HealthStatus.of(Status.UP);
  }
}

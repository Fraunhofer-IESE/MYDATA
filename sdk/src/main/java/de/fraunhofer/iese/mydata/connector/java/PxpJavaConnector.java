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

package de.fraunhofer.iese.mydata.connector.java;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.MyDataEnvironmentManager;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.connector.Connector;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.policy.decision.ExecuteAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@Connector(protocol = {
    "java"
}, type = ComponentType.PXP)
public class PxpJavaConnector implements IPolicyExecutionPoint {
  private static final Logger LOG = LoggerFactory.getLogger(PxpJavaConnector.class);

  private final IPolicyExecutionPoint pxp;

  public PxpJavaConnector(URI uri) {
    // java://de.fraunhofer.iese.mydata.MyDataEnvironmentManager/{envId}/local-component/{componentId}
    final String environmentType = uri.getAuthority();
    if (!MyDataEnvironmentManager.class.getCanonicalName().equals(environmentType)) {
      throw new RuntimeException("Unsupported EnvironmentType " + environmentType);
    }
    final String[] pathSplit = uri.getPath().split("/");
    if (pathSplit.length != 4) {
      throw new IllegalArgumentException("Malformed URI");
    }
    final String environmentId = pathSplit[1];
    final String solution = pathSplit[2];
    final ComponentId componentId = new ComponentId(pathSplit[3]);
    final Optional<IMyDataEnvironment> myDataEnvironmentOptional = MyDataEnvironmentManager
        .getEnvironment(environmentId);
    if (!myDataEnvironmentOptional.isPresent()) {
      throw new RuntimeException("IMyDataEnvironment with id " + environmentId + " not available.");
    }
    final IMyDataEnvironment myDataEnvironment = myDataEnvironmentOptional.get();
    final Optional<IPolicyExecutionPoint> pxpOptional = myDataEnvironment
        .getManagedPxp(componentId);
    if (!pxpOptional.isPresent()) {
      throw new RuntimeException("Component " + componentId
          + " not registered in IMyDataEnvironment with id " + environmentId + ".");
    }
    this.pxp = pxpOptional.get();
  }

  @Override
  public ComponentId getId() throws IOException {
    return this.pxp.getId();
  }

  @Override
  public boolean reset() throws IOException, NoSuchEntityException {
    return this.pxp.reset();
  }

  @Override
  public HealthStatus getHealth() throws IOException {
    return this.pxp.getHealth();
  }

  @Override
  public boolean execute(ExecuteAction action) throws IOException {
    return this.pxp.execute(action);
  }

}

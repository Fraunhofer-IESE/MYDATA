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
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.policy.PipRequest;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@Connector(protocol = {
    "java"
}, type = ComponentType.PIP)
public class PipJavaConnector implements IPolicyInformationPoint {

  private static final Logger LOG = LoggerFactory.getLogger(PipJavaConnector.class);

  private final IPolicyInformationPoint pip;

  public PipJavaConnector(URI uri) {
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
    final Optional<IPolicyInformationPoint> pipOptional = myDataEnvironment
        .getManagedPip(componentId);
    if (!pipOptional.isPresent()) {
      throw new RuntimeException("Component " + componentId
          + " not registered in IMyDataEnvironment with id " + environmentId + ".");
    }
    this.pip = pipOptional.get();
  }

  @Override
  public ComponentId getId() throws IOException {
    return this.pip.getId();
  }

  @Override
  public boolean reset() throws IOException, NoSuchEntityException {
    return this.pip.reset();
  }

  @Override
  public HealthStatus getHealth() throws IOException {
    return this.pip.getHealth();
  }

  @Override
  public DataObject<?> evaluate(PipRequest request)
      throws IOException, InformationUndeterminableException {
    return this.pip.evaluate(request);
  }

}

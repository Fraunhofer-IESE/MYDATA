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

package de.fraunhofer.iese.mydata.pxp;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.health.Status;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyExecutionPoint;
import de.fraunhofer.iese.mydata.policy.decision.ExecuteAction;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;
import de.fraunhofer.iese.mydata.registry.RestExposeHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

/**
 * Default Controller for the channel from PDP to PolicyExecutionPoints. Calls the relevant method
 * of one @PxpService.
 */
@RestController
@ConditionalOnProperty(prefix = "mydata", name = "external-server-url")
public class PolicyExecutionPointController {
  private static final Logger LOG = LoggerFactory.getLogger(PolicyExecutionPointController.class);

  private final IMyDataEnvironment myDataEnvironment;

  private final RestExposeHelper restExposeHelper;

  public PolicyExecutionPointController(IMyDataEnvironment myDataEnvironment,
      RestExposeHelper restExposeHelper) {
    this.myDataEnvironment = myDataEnvironment;
    this.restExposeHelper = restExposeHelper;
  }

  /**
   * Executes a PXP Action that is defined in a {@link PxpService}Annnotated Bean.
   *
   * @param  requestString Action that should be executed.
   * @param  path
   * @return               A ResponseEntity containing the result or exception.
   */
  @PostMapping(value = "/#{beanFactory.getBean(T(de.fraunhofer.iese.mydata.autoconfiguration.MyDataConfigurationProperties)).component.path}/pxp/{path}/execute", produces = {
      APPLICATION_JSON_VALUE
  })
  public ResponseEntity<?> execute(@RequestBody String requestString,
      @PathVariable(name = "path") String path) {
    try {
      final Optional<ComponentId> componentIdOptional = this.restExposeHelper
          .getPxpComponentIdForPath(path);
      final Optional<IPolicyExecutionPoint> policyExecutionPointOptional = componentIdOptional
          .flatMap(this.myDataEnvironment::getManagedPxp);
      if (!policyExecutionPointOptional.isPresent()) {
        throw new InformationUndeterminableException(
            "Component for path '" + path + "' unavailable.");
      }
      final IPolicyExecutionPoint policyExecutionPoint = policyExecutionPointOptional.get();
      final ExecuteAction request = MyDataEntity.fromJson(requestString, ExecuteAction.class);
      MyDataEntity.validateAndNullCheck(request);
      final Boolean b = policyExecutionPoint.execute(request);
      return new ResponseEntity<>(b, OK);
    } catch (final InformationUndeterminableException e) {
      LOG.warn("Error evaluating {}", requestString, e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_CONTENT);
    } catch (final Exception e) {
      LOG.warn("Error evaluating {}", requestString, e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Alive Check for a PxpService.
   *
   * @param  name Name of the PxpService to check.
   * @return      The component_id of the PxpService
   */
  @GetMapping(value = "/#{beanFactory.getBean(T(de.fraunhofer.iese.mydata.autoconfiguration.MyDataConfigurationProperties)).component.path}/pxp/{name}/component-id", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ComponentId> aliveCheck(@PathVariable("name") String name) {
    return ResponseEntity.of(this.restExposeHelper.getPxpComponentIdForPath(name));
  }

  /**
   * URL request mapping to check the component health status. Checks if a Method HealthStatus
   * health() is provided by the PXP. In case this method is not found, SERVICE_UNAVAILABLE is
   * returned.
   *
   * @param  name
   * @return      process result wrapped in ResponseEntity with appropriate response code.
   */
  @GetMapping(value = "/#{beanFactory.getBean(T(de.fraunhofer.iese.mydata.autoconfiguration.MyDataConfigurationProperties)).component.path}/pxp/{name}/health", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HealthStatus> getHealth(@PathVariable("name") String name) {
    final Optional<ComponentId> componentIdOptional = this.restExposeHelper
        .getPxpComponentIdForPath(name);
    if (!componentIdOptional.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    final Optional<IPolicyExecutionPoint> pxpOptional = this.restExposeHelper
        .getPxpComponentIdForPath(name).flatMap(this.myDataEnvironment::getManagedPxp);
    if (!pxpOptional.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    try {
      final HealthStatus healthStatus = pxpOptional.get().getHealth();
      return ResponseEntity.ok(healthStatus);
    } catch (final IOException ioException) {
      LOG.warn("IOException in getHealth method of PolicyExecutionPointController: {}",
          ioException.getMessage(), ioException);
      return ResponseEntity.ok(HealthStatus.of(Status.UNKNOWN)); // TODO check default
    }
  }
}

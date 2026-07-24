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

package de.fraunhofer.iese.mydata.pip;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.health.Status;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyInformationPoint;
import de.fraunhofer.iese.mydata.policy.PipRequest;
import de.fraunhofer.iese.mydata.policy.exception.InformationUndeterminableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.registry.RestExposeHelper;

import jakarta.servlet.http.HttpServletResponse;
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
 * Default Controller for the channel from PDP to PolicyInformationPoints. Calls the relevant method
 * of one @PipService.
 */
@RestController
@ConditionalOnProperty(prefix = "mydata", name = "external-server-url")
public class PolicyInformationPointController {
  private static final Logger LOG = LoggerFactory.getLogger(PolicyInformationPointController.class);

  private final IMyDataEnvironment myDataEnvironment;

  private final RestExposeHelper restExposeHelper;

  public PolicyInformationPointController(IMyDataEnvironment myDataEnvironment,
      RestExposeHelper restExposeHelper) {
    this.myDataEnvironment = myDataEnvironment;
    this.restExposeHelper = restExposeHelper;
  }

  /***
   * Url Request mapping for all POST PIP request. and will be dispatched to particular function
   * annotated with @{@link PipService}
   *
   * @param  requestString PIP request as JsonString
   * @param  path
   * @param  response
   * @return               process result wrapped in ResponseEntity with appropriate response code.
   */
  @PostMapping(value = "/#{beanFactory.getBean(T(de.fraunhofer.iese.mydata.autoconfiguration.MyDataConfigurationProperties)).component.path}/pip/{path}/execute", produces = {
      APPLICATION_JSON_VALUE
  })
  public Object execute(@RequestBody String requestString, @PathVariable(name = "path") String path,
      HttpServletResponse response) {
    try {
      final Optional<ComponentId> componentIdOptional = this.restExposeHelper
          .getPipComponentIdForPath(path);
      final Optional<IPolicyInformationPoint> policyInformationPointOptional = componentIdOptional
          .flatMap(this.myDataEnvironment::getManagedPip);
      if (!policyInformationPointOptional.isPresent()) {
        throw new InformationUndeterminableException(
            "Component for path '" + path + "' unavailable.");
      }
      final IPolicyInformationPoint policyInformationPoint = policyInformationPointOptional.get();
      final PipRequest request = MyDataEntity.fromJson(requestString, PipRequest.class);
      MyDataEntity.validateAndNullCheck(request);
      final DataObject<?> obj = policyInformationPoint.evaluate(request);
      response.setStatus(OK.value());
      response.addHeader("Content-Type", APPLICATION_JSON_VALUE);
      response.getOutputStream().write(obj.toJson(false).getBytes());
      return null;
    } catch (final InformationUndeterminableException e) {
      LOG.warn("Error evaluating {}", requestString, e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_CONTENT);
    } catch (final Exception e) {
      LOG.warn("Error evaluating {}", requestString, e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * * Url request mapping for GET PIP component Id.
   *
   * @param  name the name
   * @return      the string
   */
  @GetMapping(value = "/#{beanFactory.getBean(T(de.fraunhofer.iese.mydata.autoconfiguration.MyDataConfigurationProperties)).component.path}/pip/{name}/component-id", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ComponentId> aliveCheck(@PathVariable("name") String name) {
    return ResponseEntity.of(this.restExposeHelper.getPipComponentIdForPath(name));
  }

  /**
   * URL request mapping to check the component health status. Checks if a Method HealthStatus
   * health() is provided by the PIP. In case this method is not found, SERVICE_UNAVAILABLE is
   * returned.
   *
   * @param  name
   * @return      process result wrapped in ResponseEntity with appropriate response code.
   */
  @GetMapping(value = "/#{beanFactory.getBean(T(de.fraunhofer.iese.mydata.autoconfiguration.MyDataConfigurationProperties)).component.path}/pip/{name}/health", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HealthStatus> getHealth(@PathVariable("name") String name) {
    final Optional<IPolicyInformationPoint> pipOptional = this.restExposeHelper
        .getPipComponentIdForPath(name).flatMap(this.myDataEnvironment::getManagedPip);
    if (!pipOptional.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    try {
      final HealthStatus healthStatus = pipOptional.get().getHealth();
      return ResponseEntity.ok(healthStatus);
    } catch (final IOException ioException) {
      LOG.warn("IOException in getHealth method of PolicyInformationPointController: {}",
          ioException.getMessage(), ioException);
      return ResponseEntity.ok(HealthStatus.of(Status.UNKNOWN)); // TODO check default
    }

  }

}

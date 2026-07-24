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

package de.fraunhofer.iese.mydata.pmp.synchronizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.client.SyncNotification;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.IPolicyService;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.timer.ITimerService;
import de.fraunhofer.iese.mydata.timer.Timer;
import de.fraunhofer.iese.mydata.timer.TimerId;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class CloudSynchronizerTest {

  private static final String POLICY_CACHE_PATH = "mydata-ptest-cache.json";

  private static final String TIMER_CACHE_PATH = "mydata-ttest-cache.json";

  private static PipComponentInformation PIP_COMPONENT_INFORMATION;

  private static PxpComponentInformation PXP_COMPONENT_INFORMATION;

  private static PepComponentInformation PEP_COMPONENT_INFORMATION;

  private static String POLICY_SET_JSON;

  private static String TIMER_SET_JSON;

  private String POLICY_CACHE_FILE_CONTENT;

  private String TIMER_CACHE_FILE_CONTENT;

  private CloudSynchronizer testSubject;

  @Mock
  private IManagementService managementService;

  @Mock
  private IPolicyService policyService;

  @Mock
  private ITimerService timerService;

  @Mock
  private IPolicyDecisionPoint policyDecisionPoint;

  @Mock
  private Scheduler scheduler;

  @Mock
  private Supplier<Instant> instantSupplier;

  @BeforeEach
  void prepare() throws Exception {

    Files.deleteIfExists(Paths.get(POLICY_CACHE_PATH));
    Files.deleteIfExists(Paths.get(TIMER_CACHE_PATH));
    PIP_COMPONENT_INFORMATION = new PipComponentInformation(
        new ComponentId("urn:component:test:pip:my-pip"),
        Collections.singletonList(URI.create("http://localhost")), Collections.singletonList(
            new MethodInterfaceDescription("urn:info:test:bla", String.class, "bla")));
    PXP_COMPONENT_INFORMATION = new PxpComponentInformation(
        new ComponentId("urn:component:test:pxp:my-pxp"),
        Collections.singletonList(URI.create("http://localhost")), Collections.singletonList(
            new MethodInterfaceDescription("urn:action:test:bla", String.class, "bla")));
    PEP_COMPONENT_INFORMATION = new PepComponentInformation(
        new ComponentId("urn:component:test:pep:my-pep"));

    POLICY_SET_JSON = "[{\"policyId\":{\"urn\":\"urn:policy:test:bla\"},\"xmlValid\":true,\"languageValid\":true,\"solutionValid\":true,\"deployed\":true,\"description\":\"bla123\",\"policy\":\"\\u003cpolicy id\\u003d\\u0027urn:policy:test:access-data-ABC-123-connection-CONN-123-bis\\u0027\\n        description\\u003d\\u0027This is the generated access policy for connection CONN-123 to dataset ABC-123. Time of generation: 2019-03-28T15:52:24.322Z\\u0027\\n        xmlns\\u003d\\u0027http://www.mydata-control.de/4.0/mydataLanguage\\u0027\\n        xmlns:parameter\\u003d\\u0027http://www.mydata-control.de/4.0/parameter\\u0027\\n        xmlns:event\\u003d\\u0027http://www.mydata-control.de/4.0/event\\u0027\\n        xmlns:constant\\u003d\\u0027http://www.mydata-control.de/4.0/constant\\u0027\\n\\u003e\\n    \\u003cmechanism event\\u003d\\u0027urn:action:test:read\\u0027\\u003e\\n        \\u003cif\\u003e\\n           \\u003clessEqual\\u003e\\n                    \\u003ccount\\u003e\\n                        \\u003ceventOccurrence event\\u003d\\u0027urn:action:test:read\\u0027\\u003e\\n                            \\u003cparameter:object name\\u003d\\u0027User\\u0027 jsonPathQuery\\u003d\\\"$.name\\\"\\u003e\\n\\t\\t\\t                \\u003cevent:string eventParameter\\u003d\\u0027User\\u0027 default\\u003d\\u0027\\u0027 jsonPathQuery\\u003d\\u0027$.name\\u0027/\\u003e\\n\\t\\t\\t              \\u003c/parameter:object\\u003e\\n                        \\u003c/eventOccurrence\\u003e\\n                        \\u003cwhen fixedTime\\u003d\\u0027thisHour\\u0027/\\u003e\\n                    \\u003c/count\\u003e\\n                    \\u003cconstant:number value\\u003d\\u00275\\u0027/\\u003e\\n                \\u003c/lessEqual\\u003e\\n            \\u003cthen\\u003e\\n                \\u003callow/\\u003e\\n            \\u003c/then\\u003e\\n        \\u003c/if\\u003e\\n        \\u003celse\\u003e\\n            \\u003cinhibit/\\u003e\\n        \\u003c/else\\u003e\\n    \\u003c/mechanism\\u003e\\n\\u003c/policy\\u003e\",\"modificationTime\":42}]";
    TIMER_SET_JSON = "[{\"timerId\":{\"urn\":\"urn:timer:test:bla\"},\"xmlValid\":true,\"languageValid\":true,\"solutionValid\":true,\"deployed\":true,\"description\":\"bla123\",\"cron\":\"0/55 * * * * ?\",\"timer\":\"\\u003ctimer id\\u003d\\u0027urn:timer:test:access-data-ABC-123-connection-CONN-123-bis\\u0027\\n        description\\u003d\\u0027This is the generated access timer for connection CONN-123 to dataset ABC-123. Time of generation: 2019-03-28T15:52:24.322Z\\u0027\\n        xmlns\\u003d\\u0027http://www.mydata-control.de/4.0/mydataLanguage\\u0027\\n        xmlns:parameter\\u003d\\u0027http://www.mydata-control.de/4.0/parameter\\u0027\\n        xmlns:event\\u003d\\u0027http://www.mydata-control.de/4.0/event\\u0027\\n        xmlns:constant\\u003d\\u0027http://www.mydata-control.de/4.0/constant\\u0027\\n\\u003e\\n    \\u003cmechanism event\\u003d\\u0027urn:action:test:read\\u0027\\u003e\\n        \\u003cif\\u003e\\n           \\u003clessEqual\\u003e\\n                    \\u003ccount\\u003e\\n                        \\u003ceventOccurrence event\\u003d\\u0027urn:action:test:read\\u0027\\u003e\\n                            \\u003cparameter:object name\\u003d\\u0027User\\u0027 jsonPathQuery\\u003d\\\"$.name\\\"\\u003e\\n\\t\\t\\t                \\u003cevent:string eventParameter\\u003d\\u0027User\\u0027 default\\u003d\\u0027\\u0027 jsonPathQuery\\u003d\\u0027$.name\\u0027/\\u003e\\n\\t\\t\\t              \\u003c/parameter:object\\u003e\\n                        \\u003c/eventOccurrence\\u003e\\n                        \\u003cwhen fixedTime\\u003d\\u0027thisHour\\u0027/\\u003e\\n                    \\u003c/count\\u003e\\n                    \\u003cconstant:number value\\u003d\\u00275\\u0027/\\u003e\\n                \\u003c/lessEqual\\u003e\\n            \\u003cthen\\u003e\\n                \\u003callow/\\u003e\\n            \\u003c/then\\u003e\\n        \\u003c/if\\u003e\\n        \\u003celse\\u003e\\n            \\u003cinhibit/\\u003e\\n        \\u003c/else\\u003e\\n    \\u003c/mechanism\\u003e\\n\\u003c/timer\\u003e\",\"modificationTime\":42}]";

    this.POLICY_CACHE_FILE_CONTENT = "{\n"
        + "  \"lastUpdate\": "
        + Instant.now().toEpochMilli()
        + ",\n"
        + "  \"deployedPolicyCache\": [\n"
        + "    {\n"
        + "      \"policyId\": {\n"
        + "        \"urn\": \"urn:policy:test:bla\"\n"
        + "      },\n"
        + "      \"xmlValid\": true,\n"
        + "      \"languageValid\": true,\n"
        + "      \"solutionValid\": true,\n"
        + "      \"deployed\": true,\n"
        + "      \"description\": \"bla123\",\n"
        + "      \"policy\": \"\\u003cpolicy id\\u003d\\u0027urn:policy:test:access-data-ABC-123-connection-CONN-123-bis\\u0027\\n        description\\u003d\\u0027This is the generated access policy for connection CONN-123 to dataset ABC-123. Time of generation: 2019-03-28T15:52:24.322Z\\u0027\\n        xmlns\\u003d\\u0027http://www.mydata-control.de/4.0/mydataLanguage\\u0027\\n        xmlns:parameter\\u003d\\u0027http://www.mydata-control.de/4.0/parameter\\u0027\\n        xmlns:event\\u003d\\u0027http://www.mydata-control.de/4.0/event\\u0027\\n        xmlns:constant\\u003d\\u0027http://www.mydata-control.de/4.0/constant\\u0027\\n\\u003e\\n    \\u003cmechanism event\\u003d\\u0027urn:action:test:read\\u0027\\u003e\\n        \\u003cif\\u003e\\n           \\u003clessEqual\\u003e\\n                    \\u003ccount\\u003e\\n                        \\u003ceventOccurrence event\\u003d\\u0027urn:action:test:read\\u0027\\u003e\\n                            \\u003cparameter:object name\\u003d\\u0027User\\u0027 jsonPathQuery\\u003d\\\"$.name\\\"\\u003e\\n\\t\\t\\t                \\u003cevent:string eventParameter\\u003d\\u0027User\\u0027 default\\u003d\\u0027\\u0027 jsonPathQuery\\u003d\\u0027$.name\\u0027/\\u003e\\n\\t\\t\\t              \\u003c/parameter:object\\u003e\\n                        \\u003c/eventOccurrence\\u003e\\n                        \\u003cwhen fixedTime\\u003d\\u0027thisHour\\u0027/\\u003e\\n                    \\u003c/count\\u003e\\n                    \\u003cconstant:number value\\u003d\\u00275\\u0027/\\u003e\\n                \\u003c/lessEqual\\u003e\\n            \\u003cthen\\u003e\\n                \\u003callow/\\u003e\\n            \\u003c/then\\u003e\\n        \\u003c/if\\u003e\\n        \\u003celse\\u003e\\n            \\u003cinhibit/\\u003e\\n        \\u003c/else\\u003e\\n    \\u003c/mechanism\\u003e\\n\\u003c/policy\\u003e\",\n"
        + "      \"modificationTime\": 42\n"
        + "    }\n"
        + "  ]\n"
        + "}";
    this.TIMER_CACHE_FILE_CONTENT = "{\n"
        + "  \"lastUpdate\": "
        + Instant.now().toEpochMilli()
        + ",\n"
        + "  \"deployedTimerCache\": [\n"
        + "    {\n"
        + "      \"timerId\": {\n"
        + "        \"urn\": \"urn:timer:test:bla\"\n"
        + "      },\n"
        + "      \"xmlValid\": true,\n"
        + "      \"languageValid\": true,\n"
        + "      \"solutionValid\": true,\n"
        + "      \"deployed\": true,\n"
        + "      \"description\": \"bla123\",\n"
        + "      \"timer\": \"\\u003ctimer id\\u003d\\u0027urn:timer:test:access-data-ABC-123-connection-CONN-123-bis\\u0027\\n   cron\\u003d\\u00270/55 * * * * ?\\u0027        description\\u003d\\u0027This is the generated access timer for connection CONN-123 to dataset ABC-123. Time of generation: 2019-03-28T15:52:24.322Z\\u0027\\n        xmlns\\u003d\\u0027http://www.mydata-control.de/4.0/mydataLanguage\\u0027\\n        xmlns:parameter\\u003d\\u0027http://www.mydata-control.de/4.0/parameter\\u0027\\n        xmlns:event\\u003d\\u0027http://www.mydata-control.de/4.0/event\\u0027\\n        xmlns:constant\\u003d\\u0027http://www.mydata-control.de/4.0/constant\\u0027\\n\\u003e\\n    \\u003cmechanism event\\u003d\\u0027urn:action:test:read\\u0027\\u003e\\n        \\u003cif\\u003e\\n           \\u003clessEqual\\u003e\\n                    \\u003ccount\\u003e\\n                        \\u003ceventOccurrence event\\u003d\\u0027urn:action:test:read\\u0027\\u003e\\n                            \\u003cparameter:object name\\u003d\\u0027User\\u0027 jsonPathQuery\\u003d\\\"$.name\\\"\\u003e\\n\\t\\t\\t                \\u003cevent:string eventParameter\\u003d\\u0027User\\u0027 default\\u003d\\u0027\\u0027 jsonPathQuery\\u003d\\u0027$.name\\u0027/\\u003e\\n\\t\\t\\t              \\u003c/parameter:object\\u003e\\n                        \\u003c/eventOccurrence\\u003e\\n                        \\u003cwhen fixedTime\\u003d\\u0027thisHour\\u0027/\\u003e\\n                    \\u003c/count\\u003e\\n                    \\u003cconstant:number value\\u003d\\u00275\\u0027/\\u003e\\n                \\u003c/lessEqual\\u003e\\n            \\u003cthen\\u003e\\n                \\u003callow/\\u003e\\n            \\u003c/then\\u003e\\n        \\u003c/if\\u003e\\n        \\u003celse\\u003e\\n            \\u003cinhibit/\\u003e\\n        \\u003c/else\\u003e\\n    \\u003c/mechanism\\u003e\\n\\u003c/timer\\u003e\",\n"
        + "      \"modificationTime\": 42\n"
        + "    }\n"
        + "  ]\n"
        + "}";

  }

  @AfterEach
  void cleanup() throws IOException {
    Files.deleteIfExists(Paths.get(POLICY_CACHE_PATH));
    Files.deleteIfExists(Paths.get(TIMER_CACHE_PATH));
  }

  private void initAsMasterClient() throws ParseException {
    this.testSubject = new CloudSynchronizer(this.instantSupplier,
        new ClientId("urn:client:test:test"), this.policyService, this.timerService,
        this.policyDecisionPoint, this.managementService, true, POLICY_CACHE_PATH, TIMER_CACHE_PATH,
        "PT5M", "0 * * * * ?", true, this.scheduler);
  }

  private void initAsNonMasterClient() throws ParseException {
    this.testSubject = new CloudSynchronizer(this.instantSupplier,
        new ClientId("urn:client:test:test"), this.policyService, this.timerService,
        this.policyDecisionPoint, this.managementService, true, POLICY_CACHE_PATH, TIMER_CACHE_PATH,
        "PT5M", "0 * * * * ?", false, this.scheduler);
  }

  private void initWithNoCache() throws ParseException {
    this.testSubject = new CloudSynchronizer(this.instantSupplier,
        new ClientId("urn:client:test:test"), this.policyService, this.timerService,
        this.policyDecisionPoint, this.managementService, false, null, TIMER_CACHE_PATH, "PT5M",
        "0 * * * * ?", false, this.scheduler);
  }

  private void initWithCache() throws ParseException {
    this.testSubject = new CloudSynchronizer(this.instantSupplier,
        new ClientId("urn:client:test:test"), this.policyService, this.timerService,
        this.policyDecisionPoint, this.managementService, true, POLICY_CACHE_PATH, TIMER_CACHE_PATH,
        "PT5M", "0 * * * * ?", false, this.scheduler);
  }

  @Test
  void givenWeAreMasterClient_whenPushPip_thenAddPipOnManagementService() throws Exception {
    this.initAsMasterClient();
    this.testSubject.pushPip(PIP_COMPONENT_INFORMATION);
    Mockito.verify(this.managementService).addPip(PIP_COMPONENT_INFORMATION);
  }

  @Test
  void givenWeAreMasterClient_whenPushPxp_thenAddPxpOnManagementService() throws Exception {
    this.initAsMasterClient();
    this.testSubject.pushPxp(PXP_COMPONENT_INFORMATION);
    Mockito.verify(this.managementService).addPxp(PXP_COMPONENT_INFORMATION);
  }

  @Test
  void givenWeAreMasterClient_whenPushPep_thenAddPepOnManagementService() throws Exception {
    this.initAsMasterClient();
    this.testSubject.pushPep(PEP_COMPONENT_INFORMATION);
    Mockito.verify(this.managementService).addPep(PEP_COMPONENT_INFORMATION);
  }

  @Test
  void givenWeAreNoMasterClient_whenPushPip_thenNop() throws Exception {
    this.initAsNonMasterClient();
    this.testSubject.pushPip(PIP_COMPONENT_INFORMATION);
    this.testSubject.pushPip(null);
    Mockito.verifyNoMoreInteractions(this.managementService);
  }

  @Test
  void givenWeAreNoMasterClient_whenPushPxp_thenNop() throws Exception {
    this.initAsNonMasterClient();
    this.testSubject.pushPxp(PXP_COMPONENT_INFORMATION);
    this.testSubject.pushPxp(null);
    Mockito.verifyNoMoreInteractions(this.managementService);
  }

  @Test
  void givenWeAreNoMasterClient_whenPushPep_thenNop() throws Exception {
    this.initAsNonMasterClient();
    this.testSubject.pushPep(PEP_COMPONENT_INFORMATION);
    this.testSubject.pushPep(null);
    Mockito.verifyNoMoreInteractions(this.managementService);
  }

  @Test
  void givenWeHaveCacheEnabled_whenNoDataAvailable_thenSetPdpToFailureMode() throws Exception {
    this.initWithCache();
    // no data available
    Mockito.when(this.managementService.getDeployedPolicies(any())).thenThrow(IOException.class);

    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
    this.testSubject.sync();
    Mockito.verify(this.managementService).getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyDecisionPoint).setFailureMode(true);
    Mockito.verify(this.managementService).notifySync(Mockito.any(SyncNotification.class));
    Mockito.verifyNoMoreInteractions(this.managementService);

    assertFalse(Files.exists(Paths.get(POLICY_CACHE_PATH)));
  }

  @Test
  void givenWeHaveCacheDisabled_whenNoDataAvailable_thenSetPdpToFailureMode() throws Exception {
    this.initWithNoCache();
    Mockito.when(this.managementService.getDeployedPolicies(any())).thenThrow(IOException.class);
    // no data available

    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
    this.testSubject.sync();
    Mockito.verify(this.managementService).getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyDecisionPoint).setFailureMode(true);
    Mockito.verify(this.managementService).notifySync(Mockito.any(SyncNotification.class));
    Mockito.verifyNoMoreInteractions(this.managementService);

    assertFalse(Files.exists(Paths.get(POLICY_CACHE_PATH)));
  }

  @Test
  void givenWeHaveCacheEnabled_whenManagementServiceAnswers_thenDeployAndUnsetFailureModeAndSaveCache()
      throws Exception {
    this.initWithCache();
    Mockito.when(this.managementService.getDeployedPolicies(any()))
        .thenReturn(MyDataEntity.getGson().fromJson(POLICY_SET_JSON, new TypeToken<Set<Policy>>() {
        }.getType())); // MS returns data
    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());

    this.testSubject.sync();
    Mockito.verify(this.managementService).getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyService).addPolicy(any(Policy.class));
    Mockito.verify(this.policyService).deployPolicy(any(PolicyId.class));
    Mockito.verify(this.policyDecisionPoint).setFailureMode(false);

    Mockito.verify(this.managementService).notifySync(
        ArgumentMatchers.argThat(argument -> argument.getDeployedPolicyVersions().size() == 1
            && !argument.isInFailureMode()));
    // success, cache updated
    assertTrue(Files.exists(Paths.get(POLICY_CACHE_PATH)));
  }

  @Test
  void givenWeHaveCacheDisabled_whenManagementServiceAnswers_thenDeployAndUnsetFailureMode()
      throws Exception {
    this.initWithNoCache();
    Mockito.when(this.managementService.getDeployedPolicies(any()))
        .thenReturn(MyDataEntity.getGson().fromJson(POLICY_SET_JSON, new TypeToken<Set<Policy>>() {
        }.getType())); // MS returns data
    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());

    this.testSubject.sync();
    Mockito.verify(this.managementService).getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyService).addPolicy(any(Policy.class));
    Mockito.verify(this.policyService).deployPolicy(any(PolicyId.class));
    Mockito.verify(this.policyDecisionPoint).setFailureMode(false);
    // 2 times because called through policy and timer sync

    Mockito.verify(this.managementService).notifySync(
        ArgumentMatchers.argThat(argument -> argument.getDeployedPolicyVersions().size() == 1
            && !argument.isInFailureMode()));
    // there is no cache
    assertFalse(Files.exists(Paths.get(POLICY_CACHE_PATH)));
  }

  @Test
  void givenWeHaveCacheDisabled_whenManagementServiceAnswersAndThereIsAPolicyUpdate_thenUpdateTheModifiedPolicies()
      throws Exception {
    this.initWithNoCache();
    final String POLICY_SET2_JSON = "[{\"policyId\":{\"urn\":\"urn:policy:test:bla\"},\"xmlValid\":true,\"languageValid\":true,\"solutionValid\":true,\"deployed\":true,\"description\":\"bla123\",\"policy\":\"\\u003cpolicy id\\u003d\\u0027urn:policy:test:access-data-ABC-123-connection-CONN-123-bis\\u0027\\n        description\\u003d\\u0027This is the generated access policy for connection CONN-123 to dataset ABC-123. Time of generation: 2019-03-28T15:52:24.322Z\\u0027\\n        xmlns\\u003d\\u0027http://www.mydata-control.de/4.0/mydataLanguage\\u0027\\n        xmlns:parameter\\u003d\\u0027http://www.mydata-control.de/4.0/parameter\\u0027\\n        xmlns:event\\u003d\\u0027http://www.mydata-control.de/4.0/event\\u0027\\n        xmlns:constant\\u003d\\u0027http://www.mydata-control.de/4.0/constant\\u0027\\n\\u003e\\n    \\u003cmechanism event\\u003d\\u0027urn:action:test:read\\u0027\\u003e\\n        \\u003cif\\u003e\\n           \\u003clessEqual\\u003e\\n                    \\u003ccount\\u003e\\n                        \\u003ceventOccurrence event\\u003d\\u0027urn:action:test:read\\u0027\\u003e\\n                            \\u003cparameter:object name\\u003d\\u0027User\\u0027 jsonPathQuery\\u003d\\\"$.name\\\"\\u003e\\n\\t\\t\\t                \\u003cevent:string eventParameter\\u003d\\u0027User\\u0027 default\\u003d\\u0027\\u0027 jsonPathQuery\\u003d\\u0027$.name\\u0027/\\u003e\\n\\t\\t\\t              \\u003c/parameter:object\\u003e\\n                        \\u003c/eventOccurrence\\u003e\\n                        \\u003cwhen fixedTime\\u003d\\u0027thisHour\\u0027/\\u003e\\n                    \\u003c/count\\u003e\\n                    \\u003cconstant:number value\\u003d\\u00275\\u0027/\\u003e\\n                \\u003c/lessEqual\\u003e\\n            \\u003cthen\\u003e\\n                \\u003callow/\\u003e\\n            \\u003c/then\\u003e\\n        \\u003c/if\\u003e\\n        \\u003celse\\u003e\\n            \\u003cinhibit/\\u003e\\n        \\u003c/else\\u003e\\n    \\u003c/mechanism\\u003e\\n\\u003c/policy\\u003e\",\"modificationTime\":81}]";
    final Set<Policy> policySet1 = MyDataEntity.getGson().fromJson(POLICY_SET_JSON,
        new TypeToken<Set<Policy>>() {
        }.getType());
    final Set<Policy> policySet2 = MyDataEntity.getGson().fromJson(POLICY_SET2_JSON,
        new TypeToken<Set<Policy>>() {
        }.getType());
    // MS returns data
    Mockito.when(this.managementService.getDeployedPolicies(any())).thenReturn(policySet2);
    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
    Mockito.when(this.policyService.getPolicies(any())).thenReturn(policySet1);
    Mockito.when(this.policyService.getDeployedPolicies(any())).thenReturn(policySet1);

    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(1))
        .getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyService).getPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyService).getDeployedPolicies(new SolutionId("urn:solution:test"));
    // updated, is not up to date
    Mockito.verify(this.policyService)
        .updatePolicy(ArgumentMatchers.argThat(argument -> argument.getModificationTime() == 81));
    Mockito.verifyNoMoreInteractions(this.policyService);
    Mockito.verify(this.policyDecisionPoint).setFailureMode(false);

    Mockito.verify(this.managementService).notifySync(
        ArgumentMatchers.argThat(argument -> argument.getDeployedPolicyVersions().size() == 1
            && !argument.isInFailureMode() && argument.getDeployedPolicyVersions()
                .get(new PolicyId("urn:policy:test:bla")) == 81));

    Mockito.when(this.policyService.getPolicies(any())).thenReturn(policySet2);
    Mockito.when(this.policyService.getDeployedPolicies(any())).thenReturn(policySet2);

    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(2))
        .getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyService, Mockito.times(2))
        .getPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyService, Mockito.times(2))
        .getDeployedPolicies(new SolutionId("urn:solution:test"));

    // not-updated, is up to date
    Mockito.verifyNoMoreInteractions(this.policyService);
    Mockito.verify(this.policyDecisionPoint, Mockito.times(2)).setFailureMode(false);
    Mockito.verifyNoMoreInteractions(this.policyDecisionPoint);

    Mockito.verify(this.managementService, Mockito.times(2)).notifySync(
        ArgumentMatchers.argThat(argument -> argument.getDeployedPolicyVersions().size() == 1
            && !argument.isInFailureMode() && argument.getDeployedPolicyVersions()
                .get(new PolicyId("urn:policy:test:bla")) == 81));
  }

  @Test
  void givenWeHaveCacheDisabled_whenManagementServiceAnswersAndThereIsAPolicyRemoved_thenRevokeAndRemoveItLocal()
      throws Exception {
    this.initWithNoCache();
    final String POLICY_SET2_JSON = "[]";
    final Set<Policy> policySet1 = MyDataEntity.getGson().fromJson(POLICY_SET_JSON,
        new TypeToken<Set<Policy>>() {
        }.getType());
    final Set<Policy> policySet2 = MyDataEntity.getGson().fromJson(POLICY_SET2_JSON,
        new TypeToken<Set<Policy>>() {
        }.getType());
    // MS returns data
    Mockito.when(this.managementService.getDeployedPolicies(any())).thenReturn(policySet2);
    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());

    Mockito.when(this.policyService.getPolicies(any())).thenReturn(policySet1);
    Mockito.when(this.policyService.getDeployedPolicies(any())).thenReturn(policySet1);

    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(1))
        .getDeployedPolicies(new SolutionId("urn:solution:test"));
    // revoked and deleted
    Mockito.verify(this.policyService, Mockito.times(1))
        .getPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyService, Mockito.times(1))
        .getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyService, Mockito.times(1))
        .revokePolicy(new PolicyId("urn:policy:test:bla"));
    Mockito.verify(this.policyService, Mockito.times(1))
        .deletePolicy(new PolicyId("urn:policy:test:bla"));
    Mockito.verifyNoMoreInteractions(this.policyService);
    Mockito.verify(this.policyDecisionPoint).setFailureMode(false);

    Mockito.verify(this.managementService).notifySync(
        ArgumentMatchers.argThat(argument -> argument.getDeployedPolicyVersions().size() == 0
            && !argument.isInFailureMode()));
  }

  @Test
  void givenWeHaveCacheDisabled_whenManagementServiceAnswersAndThereIsATimerUpdate_thenUpdateTheModifiedTimer()
      throws Exception {
    this.initWithNoCache();
    final String TIMER_SET2_JSON = "[{\"timerId\":{\"urn\":\"urn:timer:test:bla\"},\"xmlValid\":true,\"languageValid\":true,\"solutionValid\":true,\"deployed\":true,\"description\":\"bla123\",\"cron\":\"0/55 * * * * ?\",\"timer\":\"\\u003ctimer id\\u003d\\u0027urn:timer:test:access-data-ABC-123-connection-CONN-123-bis\\u0027\\n        description\\u003d\\u0027This is the generated access timer for connection CONN-123 to dataset ABC-123. Time of generation: 2019-03-28T15:52:24.322Z\\u0027\\n        xmlns\\u003d\\u0027http://www.mydata-control.de/4.0/mydataLanguage\\u0027\\n        xmlns:parameter\\u003d\\u0027http://www.mydata-control.de/4.0/parameter\\u0027\\n        xmlns:event\\u003d\\u0027http://www.mydata-control.de/4.0/event\\u0027\\n        xmlns:constant\\u003d\\u0027http://www.mydata-control.de/4.0/constant\\u0027\\n\\u003e\\n    \\u003cmechanism event\\u003d\\u0027urn:action:test:read\\u0027\\u003e\\n        \\u003cif\\u003e\\n           \\u003clessEqual\\u003e\\n                    \\u003ccount\\u003e\\n                        \\u003ceventOccurrence event\\u003d\\u0027urn:action:test:read\\u0027\\u003e\\n                            \\u003cparameter:object name\\u003d\\u0027User\\u0027 jsonPathQuery\\u003d\\\"$.name\\\"\\u003e\\n\\t\\t\\t                \\u003cevent:string eventParameter\\u003d\\u0027User\\u0027 default\\u003d\\u0027\\u0027 jsonPathQuery\\u003d\\u0027$.name\\u0027/\\u003e\\n\\t\\t\\t              \\u003c/parameter:object\\u003e\\n                        \\u003c/eventOccurrence\\u003e\\n                        \\u003cwhen fixedTime\\u003d\\u0027thisHour\\u0027/\\u003e\\n                    \\u003c/count\\u003e\\n                    \\u003cconstant:number value\\u003d\\u00275\\u0027/\\u003e\\n                \\u003c/lessEqual\\u003e\\n            \\u003cthen\\u003e\\n                \\u003callow/\\u003e\\n            \\u003c/then\\u003e\\n        \\u003c/if\\u003e\\n        \\u003celse\\u003e\\n            \\u003cinhibit/\\u003e\\n        \\u003c/else\\u003e\\n    \\u003c/mechanism\\u003e\\n\\u003c/timer\\u003e\",\"modificationTime\":81}]";
    final Set<Timer> timerSet1 = MyDataEntity.getGson().fromJson(TIMER_SET_JSON,
        new TypeToken<Set<Timer>>() {
        }.getType());
    final Set<Timer> timerSet2 = MyDataEntity.getGson().fromJson(TIMER_SET2_JSON,
        new TypeToken<Set<Timer>>() {
        }.getType());
    // MS returns data
    Mockito.when(this.managementService.getDeployedTimers(any())).thenReturn(timerSet2);
    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());

    Mockito.when(this.timerService.getTimers(any())).thenReturn(timerSet1);
    Mockito.when(this.timerService.getDeployedTimers(any())).thenReturn(timerSet1);

    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(1))
        .getDeployedTimers(new SolutionId("urn:solution:test"));
    // updated
    Mockito.verify(this.timerService, Mockito.times(1))
        .getTimers(new SolutionId("urn:solution:test"));
    Mockito.verify(this.timerService, Mockito.times(1))
        .getDeployedTimers(new SolutionId("urn:solution:test"));
    Mockito.verify(this.timerService, Mockito.times(1))
        .updateTimer(ArgumentMatchers.argThat(argument -> argument.getModificationTime() == 81));
    Mockito.verifyNoMoreInteractions(this.timerService);
    Mockito.verify(this.policyDecisionPoint).setFailureMode(false);

    Mockito.verify(this.managementService).notifySync(ArgumentMatchers.argThat(
        argument -> argument.getDeployedTimerVersions().size() == 1 && !argument.isInFailureMode()
            && argument.getDeployedTimerVersions().get(new TimerId("urn:timer:test:bla")) == 81));

    Mockito.when(this.timerService.getTimers(any())).thenReturn(timerSet2);
    Mockito.when(this.timerService.getDeployedTimers(any())).thenReturn(timerSet2);

    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(2))
        .getDeployedTimers(new SolutionId("urn:solution:test"));
    // not-updated
    Mockito.verify(this.timerService, Mockito.times(2))
        .getTimers(new SolutionId("urn:solution:test"));
    Mockito.verify(this.timerService, Mockito.times(2))
        .getDeployedTimers(new SolutionId("urn:solution:test"));
    Mockito.verifyNoMoreInteractions(this.timerService);
    Mockito.verify(this.policyDecisionPoint, Mockito.times(2)).setFailureMode(false);

    Mockito.verify(this.managementService, Mockito.times(2)).notifySync(ArgumentMatchers.argThat(
        argument -> argument.getDeployedTimerVersions().size() == 1 && !argument.isInFailureMode()
            && argument.getDeployedTimerVersions().get(new TimerId("urn:timer:test:bla")) == 81));
  }

  @Test
  void givenWeHaveCacheDisabled_whenManagementServiceAnswersAndThereIsATimerRemoved_thenRevokeAndRemoveItLocal()
      throws Exception {
    this.initWithNoCache();
    final String TIMER_SET2_JSON = "[]";
    final Set<Timer> timerSet1 = MyDataEntity.getGson().fromJson(TIMER_SET_JSON,
        new TypeToken<Set<Timer>>() {
        }.getType());
    final Set<Timer> timerSet2 = MyDataEntity.getGson().fromJson(TIMER_SET2_JSON,
        new TypeToken<Set<Timer>>() {
        }.getType());
    // MS returns data
    Mockito.when(this.managementService.getDeployedTimers(any())).thenReturn(timerSet2);
    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());

    Mockito.when(this.timerService.getTimers(any())).thenReturn(timerSet1);
    Mockito.when(this.timerService.getDeployedTimers(any())).thenReturn(timerSet1);

    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(1))
        .getDeployedTimers(new SolutionId("urn:solution:test"));
    // revoked and deleted
    Mockito.verify(this.timerService, Mockito.times(1))
        .getTimers(new SolutionId("urn:solution:test"));
    Mockito.verify(this.timerService, Mockito.times(1))
        .getDeployedTimers(new SolutionId("urn:solution:test"));
    Mockito.verify(this.timerService, Mockito.times(1))
        .revokeTimer(new TimerId("urn:timer:test:bla"));
    Mockito.verify(this.timerService, Mockito.times(1))
        .deleteTimer(new TimerId("urn:timer:test:bla"));
    Mockito.verifyNoMoreInteractions(this.timerService);
    Mockito.verify(this.policyDecisionPoint).setFailureMode(false);

    Mockito.verify(this.managementService).notifySync(
        ArgumentMatchers.argThat(argument -> argument.getDeployedTimerVersions().size() == 0
            && !argument.isInFailureMode()));
  }

  @Test
  void givenWeHaveCacheDisabled_whenManagementServiceDoesNotAnswerAboutPoliciesOnSecondRequestInRange_thenDoNothing()
      throws Exception {
    this.initWithNoCache();
    Mockito.when(this.managementService.getDeployedPolicies(any()))
        .thenReturn(MyDataEntity.getGson().fromJson(POLICY_SET_JSON, new TypeToken<Set<Policy>>() {
        }.getType())); // MS returns data
    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(1))
        .getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.managementService).notifySync(
        ArgumentMatchers.argThat(argument -> argument.getDeployedPolicyVersions().size() == 1
            && !argument.isInFailureMode()));

    Mockito.when(this.managementService.getDeployedPolicies(any())).thenThrow(IOException.class);
    // no data available

    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(2))
        .getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.managementService, Mockito.times(2))
        .getDeployedTimers(new SolutionId("urn:solution:test"));

    Mockito.verify(this.policyService, Mockito.times(1)).addPolicy(any(Policy.class));
    Mockito.verify(this.policyService, Mockito.times(1)).deployPolicy(any(PolicyId.class));
    Mockito.verify(this.policyDecisionPoint, Mockito.times(2)).setFailureMode(false);
    Mockito.verify(this.managementService, Mockito.times(2))
        .notifySync(Mockito.any(SyncNotification.class));
    Mockito.verifyNoMoreInteractions(this.managementService);
    Mockito.verifyNoMoreInteractions(this.policyDecisionPoint);
  }

  @Test
  void givenWeHaveCacheDisabled_whenManagementServiceDoesNotAnswerAboutTimersOnSecondRequestInRange_thenDoNothing()
      throws Exception {
    this.initWithNoCache();
    Mockito.when(this.managementService.getDeployedTimers(any()))
        .thenReturn(MyDataEntity.getGson().fromJson(TIMER_SET_JSON, new TypeToken<Set<Timer>>() {
        }.getType())); // MS returns data
    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(1))
        .getDeployedTimers(new SolutionId("urn:solution:test"));
    Mockito.verify(this.managementService).notifySync(
        ArgumentMatchers.argThat(argument -> argument.getDeployedTimerVersions().size() == 1
            && !argument.isInFailureMode()));

    Mockito.when(this.managementService.getDeployedTimers(any())).thenThrow(IOException.class);
    // no data available

    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(2))
        .getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.managementService, Mockito.times(2))
        .getDeployedTimers(new SolutionId("urn:solution:test"));

    Mockito.verify(this.timerService, Mockito.times(1)).addTimer(any(Timer.class));
    Mockito.verify(this.timerService, Mockito.times(1)).deployTimer(any(TimerId.class));
    Mockito.verify(this.policyDecisionPoint, Mockito.times(2)).setFailureMode(false);
    Mockito.verify(this.managementService, Mockito.times(2))
        .notifySync(Mockito.any(SyncNotification.class));
    Mockito.verifyNoMoreInteractions(this.managementService);
    Mockito.verifyNoMoreInteractions(this.policyDecisionPoint);
  }

  @Test
  void givenWeHaveCacheDisabled_whenManagementServiceDoesNotAnswerAboutPoliciesOnSecondRequestOutOfRange_thenSetPdpToFailureMode()
      throws Exception {
    this.initWithNoCache();
    Mockito.when(this.managementService.getDeployedPolicies(any()))
        .thenReturn(MyDataEntity.getGson().fromJson(POLICY_SET_JSON, new TypeToken<Set<Policy>>() {
        }.getType())); // MS returns data
    Mockito.when(this.instantSupplier.get())
        .thenAnswer(invocation -> Instant.now().minus(10, ChronoUnit.MINUTES));
    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(1))
        .getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyDecisionPoint).setFailureMode(false);
    Mockito.verify(this.managementService).notifySync(
        ArgumentMatchers.argThat(argument -> argument.getDeployedPolicyVersions().size() == 1
            && !argument.isInFailureMode()));

    Mockito.when(this.managementService.getDeployedPolicies(any())).thenThrow(IOException.class);
    // no data available

    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(2))
        .getDeployedPolicies(new SolutionId("urn:solution:test"));
    // only one time because simulates an exception when getdeployedpolicies
    Mockito.verify(this.managementService, Mockito.times(1))
        .getDeployedTimers(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyDecisionPoint).setFailureMode(true);
    // 2 times because 2 calls to testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(2))
        .notifySync(Mockito.any(SyncNotification.class));
    Mockito.verifyNoMoreInteractions(this.managementService);
    Mockito.verifyNoMoreInteractions(this.policyDecisionPoint);
  }

  @Test
  void givenWeHaveCacheDisabled_whenManagementServiceDoesNotAnswerAboutTimersOnSecondRequestOutOfRange_thenSetPdpToFailureMode()
      throws Exception {
    this.initWithNoCache();
    Mockito.when(this.managementService.getDeployedTimers(any()))
        .thenReturn(MyDataEntity.getGson().fromJson(TIMER_SET_JSON, new TypeToken<Set<Timer>>() {
        }.getType())); // MS returns data
    Mockito.when(this.instantSupplier.get())
        .thenAnswer(invocation -> Instant.now().minus(10, ChronoUnit.MINUTES));
    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(1))
        .getDeployedTimers(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyDecisionPoint).setFailureMode(false);
    Mockito.verify(this.managementService).notifySync(
        ArgumentMatchers.argThat(argument -> argument.getDeployedTimerVersions().size() == 1
            && !argument.isInFailureMode()));

    Mockito.when(this.managementService.getDeployedTimers(any())).thenThrow(IOException.class);
    // no data available

    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
    this.testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(2))
        .getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.managementService, Mockito.times(2))
        .getDeployedTimers(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyDecisionPoint).setFailureMode(true);
    // 2 times because 2 calls to testSubject.sync();
    Mockito.verify(this.managementService, Mockito.times(2))
        .notifySync(Mockito.any(SyncNotification.class));
    Mockito.verifyNoMoreInteractions(this.managementService);
    Mockito.verifyNoMoreInteractions(this.policyDecisionPoint);
  }

  @Test
  void givenValidCacheAvailable_whenSyncFromCache_thenCacheWillNotBeUpdated() throws Exception {
    Files.write(Paths.get(POLICY_CACHE_PATH),
        this.POLICY_CACHE_FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
    this.initWithCache();
    assertTrue(Files.exists(Paths.get(POLICY_CACHE_PATH)));
    final FileTime timeCreated = Files.getLastModifiedTime(Paths.get(POLICY_CACHE_PATH));
    {
      Mockito.when(this.managementService.getDeployedPolicies(any())).thenThrow(IOException.class);
      // fallback to cache
      Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
      this.testSubject.sync();
      Mockito.verify(this.managementService)
          .getDeployedPolicies(new SolutionId("urn:solution:test"));
      Mockito.verify(this.policyService).addPolicy(any(Policy.class));
      Mockito.verify(this.policyService).deployPolicy(any(PolicyId.class));
      Mockito.verify(this.policyDecisionPoint).setFailureMode(false);

      Mockito.verify(this.managementService).notifySync(
          ArgumentMatchers.argThat(argument -> argument.getDeployedPolicyVersions().size() == 1
              && !argument.isInFailureMode()));
    }
    final FileTime time = Files.getLastModifiedTime(Paths.get(POLICY_CACHE_PATH));
    assertEquals(0, timeCreated.compareTo(time));
    // read from cache should not lead to cache update
  }

  @Test
  void givenValidTimerCacheAvailable_whenSyncFromCache_thenCacheWillNotBeUpdated()
      throws Exception {
    Files.write(Paths.get(TIMER_CACHE_PATH),
        this.TIMER_CACHE_FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
    this.initWithCache();
    assertTrue(Files.exists(Paths.get(TIMER_CACHE_PATH)));
    final FileTime timeCreated = Files.getLastModifiedTime(Paths.get(TIMER_CACHE_PATH));
    {
      Mockito.when(this.managementService.getDeployedPolicies(any())).thenReturn(
          MyDataEntity.getGson().fromJson(POLICY_SET_JSON, new TypeToken<Set<Policy>>() {
          }.getType())); // MS returns data

      Mockito.when(this.managementService.getDeployedTimers(any())).thenThrow(IOException.class);
      // fallback to cache
      Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
      this.testSubject.sync();
      Mockito.verify(this.managementService).getDeployedTimers(new SolutionId("urn:solution:test"));
      Mockito.verify(this.timerService).addTimer(any(Timer.class));
      Mockito.verify(this.timerService).deployTimer(any(TimerId.class));
      Mockito.verify(this.policyDecisionPoint).setFailureMode(false);

      Mockito.verify(this.managementService).notifySync(
          ArgumentMatchers.argThat(argument -> argument.getDeployedPolicyVersions().size() == 1
              && !argument.isInFailureMode()));
    }
    final FileTime time = Files.getLastModifiedTime(Paths.get(TIMER_CACHE_PATH));
    assertEquals(0, timeCreated.compareTo(time));
    // read from cache should not lead to cache update
  }

  @Test
  void givenValidCacheAvailable_whenSyncFromManagementService_thenCacheShouldBeUpdated()
      throws Exception {
    Files.write(Paths.get(POLICY_CACHE_PATH),
        this.POLICY_CACHE_FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
    this.initWithCache();
    assertTrue(Files.exists(Paths.get(POLICY_CACHE_PATH)));
    final FileTime timeCreated = Files.getLastModifiedTime(Paths.get(POLICY_CACHE_PATH));
    // needs a time.sleep because script is to fast, the millisecond is not
    // detected
    Thread.sleep(1000);
    {
      Mockito.when(this.managementService.getDeployedPolicies(any())).thenReturn(
          MyDataEntity.getGson().fromJson(POLICY_SET_JSON, new TypeToken<Set<Policy>>() {
          }.getType())); // MS returns data
      Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
      this.testSubject.sync();
      Mockito.verify(this.managementService)
          .getDeployedPolicies(new SolutionId("urn:solution:test"));
      Mockito.verify(this.policyService).addPolicy(any(Policy.class));
      Mockito.verify(this.policyService).deployPolicy(any(PolicyId.class));
      Mockito.verify(this.policyDecisionPoint).setFailureMode(false);

      Mockito.verify(this.managementService).notifySync(
          ArgumentMatchers.argThat(argument -> argument.getDeployedPolicyVersions().size() == 1
              && !argument.isInFailureMode()));
      // success, cache updated
    }
    final FileTime time = Files.getLastModifiedTime(Paths.get(POLICY_CACHE_PATH));
    assertEquals(-1, timeCreated.compareTo(time));
    // read from cache should not lead to cache update
  }

  @Test
  void whenDeployFails_thenSetPdpToFailureModeAndReport() throws Exception {
    this.initWithNoCache();
    Mockito.when(this.managementService.getDeployedPolicies(any()))
        .thenReturn(MyDataEntity.getGson().fromJson(POLICY_SET_JSON, new TypeToken<Set<Policy>>() {
        }.getType())); // MS returns data
    Mockito.doThrow(ResourceUpdateException.class).when(this.policyService)
        .deployPolicy(ArgumentMatchers.any());
    // error while deploying
    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
    this.testSubject.sync();
    Mockito.verify(this.managementService).getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyService).addPolicy(any(Policy.class));
    Mockito.verify(this.policyService).deployPolicy(any(PolicyId.class));

    Mockito.verify(this.policyDecisionPoint, Mockito.times(1)).setFailureMode(true);
    Mockito.verify(this.managementService).notifySync(
        ArgumentMatchers.argThat(argument -> argument.getDeployedPolicyVersions().size() == 0
            && argument.isInFailureMode()));
  }

  @Test
  void whenDeployTimerFails_thenSetPdpToFailureModeAndReport() throws Exception {
    this.initWithNoCache();
    Mockito.when(this.managementService.getDeployedPolicies(any()))
        .thenReturn(MyDataEntity.getGson().fromJson(POLICY_SET_JSON, new TypeToken<Set<Policy>>() {
        }.getType())); // MS returns data
    Mockito.when(this.managementService.getDeployedTimers(any()))
        .thenReturn(MyDataEntity.getGson().fromJson(TIMER_SET_JSON, new TypeToken<Set<Timer>>() {
        }.getType())); // MS returns data
    Mockito.doThrow(ResourceUpdateException.class).when(this.timerService)
        .deployTimer(ArgumentMatchers.any());
    // error while deploying
    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
    this.testSubject.sync();
    Mockito.verify(this.managementService).getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyService).addPolicy(any(Policy.class));
    Mockito.verify(this.policyService).deployPolicy(any(PolicyId.class));

    Mockito.verify(this.managementService).getDeployedTimers(new SolutionId("urn:solution:test"));
    Mockito.verify(this.timerService).addTimer(any(Timer.class));
    Mockito.verify(this.timerService).deployTimer(any(TimerId.class));

    Mockito.verify(this.policyDecisionPoint).setFailureMode(true);
    Mockito.verifyNoMoreInteractions(this.policyDecisionPoint);

    Mockito.verify(this.managementService).notifySync(ArgumentMatchers.argThat(
        argument -> argument.getDeployedTimerVersions().size() == 0 && argument.isInFailureMode()));
  }

  @Test
  void whenSubmitOfSyncNotificationFails_thenNoException() throws Exception {
    this.initWithNoCache();
    Mockito.when(this.managementService.getDeployedPolicies(any()))
        .thenReturn(MyDataEntity.getGson().fromJson(POLICY_SET_JSON, new TypeToken<Set<Policy>>() {
        }.getType())); // MS returns data
    Mockito.doThrow(IOException.class).when(this.managementService).notifySync(any());
    // submit of SyncNotification fails
    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
    this.testSubject.sync();
    Mockito.verify(this.managementService).getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyService).addPolicy(any(Policy.class));
    Mockito.verify(this.policyService).deployPolicy(any(PolicyId.class));
    Mockito.verify(this.policyDecisionPoint).setFailureMode(false);

    Mockito.verify(this.managementService).notifySync(
        ArgumentMatchers.argThat(argument -> argument.getDeployedPolicyVersions().size() == 1
            && !argument.isInFailureMode()));
    // no exception occurs in this case
  }

  @Test
  void simpleLifeCycle() throws Exception {
    this.initWithNoCache();
    Mockito.when(this.managementService.getDeployedPolicies(any()))
        .thenReturn(MyDataEntity.getGson().fromJson(POLICY_SET_JSON, new TypeToken<Set<Policy>>() {
        }.getType())); // MS returns data
    Mockito.doAnswer(invocation -> {
      final JobExecutionContext jobExecutionContext = this.constructFakeJobExecutionContext();
      final CloudSynchronizer.SyncJob syncJob = new CloudSynchronizer.SyncJob();
      syncJob.execute(jobExecutionContext);
      return null;
    }).when(this.scheduler).triggerJob(any());
    Mockito.when(this.instantSupplier.get()).thenAnswer(invocation -> Instant.now());
    this.testSubject.start();
    Mockito.verify(this.scheduler).start();
    Mockito.verify(this.scheduler).scheduleJob(any(), any());
    Mockito.verify(this.scheduler).triggerJob(any());
    Mockito.verify(this.managementService).getDeployedPolicies(new SolutionId("urn:solution:test"));
    Mockito.verify(this.policyService).addPolicy(any(Policy.class));
    Mockito.verify(this.policyService).deployPolicy(any(PolicyId.class));
    Mockito.verify(this.policyDecisionPoint).setFailureMode(false);
    Mockito.verify(this.managementService).notifySync(
        ArgumentMatchers.argThat(argument -> argument.getDeployedPolicyVersions().size() == 1
            && !argument.isInFailureMode()));
    this.testSubject.stop();
    Mockito.verify(this.scheduler).deleteJob(any());
  }

  private JobExecutionContext constructFakeJobExecutionContext() {
    final JobExecutionContext jobExecutionContextMock = Mockito.mock(JobExecutionContext.class);
    final JobDetail jobDetailMock = Mockito.mock(JobDetail.class);
    final JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put("syncService", CloudSynchronizerTest.this.testSubject);
    Mockito.when(jobExecutionContextMock.getJobDetail()).thenReturn(jobDetailMock);
    Mockito.when(jobDetailMock.getJobDataMap()).thenReturn(jobDataMap);
    return jobExecutionContextMock;
  }

}

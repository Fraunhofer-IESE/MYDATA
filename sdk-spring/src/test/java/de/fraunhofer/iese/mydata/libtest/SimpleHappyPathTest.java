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

package de.fraunhofer.iese.mydata.libtest;

import de.fraunhofer.iese.mydata.IMyDataEnvironment;
import de.fraunhofer.iese.mydata.MyDataEnvironmentManager;
import de.fraunhofer.iese.mydata.eventhistory.EnableEventHistory;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.exception.InhibitException;
import de.fraunhofer.iese.mydata.util.MyDataUtil;

import com.google.gson.Gson;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableAutoConfiguration
@ActiveProfiles("test")
@EnableEventHistory
@Transactional
@ComponentScan
@DirtiesContext
public class SimpleHappyPathTest {
  // TODO maybe we want to relocate this test

  private static final String POLICY_STR = "<policy id='urn:policy:test:bla' description='Test' xmlns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:tns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:parameter='http://www.mydata-control.de/4.0/parameter' xmlns:pip='http://www.mydata-control.de/4.0/pip' xmlns:function='http://www.mydata-control.de/4.0/function' xmlns:event='http://www.mydata-control.de/4.0/event' xmlns:constant='http://www.mydata-control.de/4.0/constant' xmlns:variable='http://www.mydata-control.de/4.0/variable' xmlns:variableDeclaration='http://www.mydata-control.de/4.0/variableDeclaration' xmlns:valueChanged='http://www.mydata-control.de/4.0/valueChanged' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:date='http://www.mydata-control.de/4.0/date' xmlns:time='http://www.mydata-control.de/4.0/time' xmlns:day='http://www.mydata-control.de/4.0/day'>\n"
      + "  <mechanism event='urn:action:test:read'>\n"
      + "    <if>\n"
      + "      <and>\n"
      + "        <equals>\n"
      + "          <constant:string value='test 123'/>\n"
      + "          <event:string eventParameter='id' default=''/>\n"
      + "        </equals>\n"
      + "        <lessEqual>\n"
      + "          <count>\n"
      + "            <eventOccurrence event='urn:action:test:read'>\n"
      + "              <parameter:string name='id' value='test 123'/>\n"
      + "            </eventOccurrence>\n"
      + "            <when fixedTime='today'/>\n"
      + "          </count>\n"
      + "          <constant:number value='5'/>\n"
      + "        </lessEqual>\n"
      + "      </and>\n"
      + "      <then>\n"
      + "        <allow/>\n"
      + "      </then>\n"
      + "    </if>\n"
      + "    <elseif>\n"
      + "      <equals>\n"
      + "        <constant:string value='test 123'/>\n"
      + "        <event:string eventParameter='id' default=''/>\n"
      + "      </equals>\n"
      + "      <then>\n"
      + "        <inhibit/>\n"
      + "      </then>\n"
      + "    </elseif>\n"
      + "  </mechanism>\n"
      + "</policy>";

  @Autowired
  private IMyDataEnvironment myDataEnvironment;

  @Autowired
  private LibTestPep libTestPep;

  @BeforeClass
  public static void testSetup() {
    MyDataEnvironmentManager.enableOverwritingOfExistingMyDataEnvironments();
  }

  @AfterClass
  public static void testTeardown() {
    MyDataEnvironmentManager.disableOverwritingOfExistingMyDataEnvironments();
  }

  @Test
  public void eventCountRestrictionTest() {
    Assert.assertNotNull(this.myDataEnvironment);
    Assert.assertNotNull(this.libTestPep);
    final PolicyId policyId = new PolicyId("urn:policy:test:bla");

    final String dataStr = "{\"geheim\":\"test\"}";
    final Gson gson = new Gson();
    final Object data = gson.fromJson(dataStr, Object.class);
    {
      // no policy -> allow
      try {
        final Event event = MyDataUtil
            .checkedBlockingGet(this.libTestPep.enforceRead("test 123", data));
      } catch (IOException | EvaluationUndecidableException | InhibitException e) {
        Assert.fail();
      }
    }
    {
      // create policy for connection
      try {
        this.myDataEnvironment.getPmp()
            .deployPolicy(this.myDataEnvironment.getPmp().addPolicy(new Policy(POLICY_STR)));
      } catch (IOException | ResourceUpdateException | InvalidEntityException
          | ConflictingResourceException | NoSuchEntityException e) {
        Assert.fail();
      }
    }

    // we can access the data 5 times (as configured)
    for (int i = 0; i < 5; i++) {
      Event event = null;
      try {
        event = MyDataUtil.checkedBlockingGet(this.libTestPep.enforceRead("test 123", data));
      } catch (InhibitException | EvaluationUndecidableException | IOException e) {
        Assert.fail();
      }
      Assert.assertEquals(gson.toJson(data), gson.toJson(event.getValueForName("data")));
    }
    {
      // the 6. access is forbidden
      try {
        final Event event = MyDataUtil
            .checkedBlockingGet(this.libTestPep.enforceRead("test 123", data));
        Assert.fail();
      } catch (IOException | EvaluationUndecidableException e) {
        Assert.fail();
      } catch (final InhibitException e) {
        // expected
      }
    }
    {
      // policies can be deleted/revoked
      try {
        this.myDataEnvironment.getPmp().revokePolicy(policyId);
        this.myDataEnvironment.getPmp().deletePolicy(policyId);
      } catch (IOException | ResourceUpdateException | InvalidEntityException
          | NoSuchEntityException | ConflictingResourceException e) {
        Assert.fail();
      }
    }
    {
      // and still: no policy -> allow
      try {
        final Event event = MyDataUtil
            .checkedBlockingGet(this.libTestPep.enforceRead("test 123", data));
      } catch (IOException | EvaluationUndecidableException | InhibitException e) {
        Assert.fail();
      }
    }

  }

}

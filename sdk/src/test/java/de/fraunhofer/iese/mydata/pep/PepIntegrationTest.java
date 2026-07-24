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

package de.fraunhofer.iese.mydata.pep;

import de.fraunhofer.iese.mydata.User;
import de.fraunhofer.iese.mydata.User.CreditCardInfo;

import java.util.HashMap;

public class PepIntegrationTest {

  public static final int ONE_SECOND = 1000;

  private User getUser() {

    final User user = new User();
    user.setName("Denis Feth");
    user.setPhoneNo(new Long[] {
        4915234767022L, 919748087957L
    });

    final HashMap<String, User.CreditCardInfo> sahw = new HashMap<>();
    sahw.put("1234-5678-9101-1121", new CreditCardInfo("1111", "STADPARKASSE"));
    sahw.put("3141-5161-7181-9202", new CreditCardInfo("2222", "Commercez"));
    user.setAccountDetails(sahw);

    return user;
  }

  // @Test
  // @Ignore
  // public void integrationTest() throws Exception {
  // DecisionEnforcer<Object> jvmNativeDecisionEnforcer = new
  // JVMNativeDecisionEnforcer();
  // jvmNativeDecisionEnforcer.setObjectModifier(new JVMObjectModifier());
  // jvmNativeDecisionEnforcer.addModifier(new BlurModifierActor());
  // jvmNativeDecisionEnforcer.addModifier(new AppendModifierActor());
  // jvmNativeDecisionEnforcer.addModifier(new DeleteModifierActor());
  //
  // PolicyEnforcementPoint enforcementPoint = new
  // PolicyEnforcementPoint(jvmNativeDecisionEnforcer,
  // new ComponentId("urn:component:ind2uce:pep:test"), new
  // URI("http://localhost:8080/ws"),
  // this.getPepInterfaceDescription(),new URI("http://localhost:8080/alive"),
  // true);
  // Event event = new
  // EventBuilder("urn:action:junit:test").withParameter(this.getUser(),
  // "user").atTime(System.currentTimeMillis() + ONE_SECOND).getEvent();
  // enforcementPoint.enforce(event);
  // User modified = (User)event.getParameterForName("user").getValue();
  // System.out.println(new Gson().toJson(modified));
  //
  // }

}

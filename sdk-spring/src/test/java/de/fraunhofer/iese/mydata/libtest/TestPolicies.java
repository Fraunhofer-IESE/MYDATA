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

public class TestPolicies {
  public static final String PIP_TEST_POLICY_1 = "<policy id='urn:policy:test:pip-test-1' description='pip-test-1' xmlns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:tns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:parameter='http://www.mydata-control.de/4.0/parameter' xmlns:pip='http://www.mydata-control.de/4.0/pip' xmlns:function='http://www.mydata-control.de/4.0/function' xmlns:event='http://www.mydata-control.de/4.0/event' xmlns:constant='http://www.mydata-control.de/4.0/constant' xmlns:variable='http://www.mydata-control.de/4.0/variable' xmlns:variableDeclaration='http://www.mydata-control.de/4.0/variableDeclaration' xmlns:valueChanged='http://www.mydata-control.de/4.0/valueChanged' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:date='http://www.mydata-control.de/4.0/date' xmlns:time='http://www.mydata-control.de/4.0/time' xmlns:day='http://www.mydata-control.de/4.0/day'>\n"
      + "  <mechanism event='urn:action:test:pip-test-1'>\n" + "    <if>\r\n" + "    <equals>\r\n" + "        <constant:false/>\r\n"
      + "        <pip:boolean method='urn:info:test:not' default='true'>\r\n" + "          <parameter:boolean name='value' value='true'/>\r\n" + "        </pip:boolean>\r\n" + "      </equals>\r\n"
      + "      <then>\r\n" + "        <allow/>\r\n" + "      </then>\r\n" + "    </if>" + "    <else>\r\n" + "      <inhibit/>\r\n" + "    </else>" + "  </mechanism>\n" + "</policy>";

  public static final String PIP_TEST_POLICY_2 = "<policy id='urn:policy:test:pip-test-2' description='pip-test-2' xmlns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:tns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:parameter='http://www.mydata-control.de/4.0/parameter' xmlns:pip='http://www.mydata-control.de/4.0/pip' xmlns:function='http://www.mydata-control.de/4.0/function' xmlns:event='http://www.mydata-control.de/4.0/event' xmlns:constant='http://www.mydata-control.de/4.0/constant' xmlns:variable='http://www.mydata-control.de/4.0/variable' xmlns:variableDeclaration='http://www.mydata-control.de/4.0/variableDeclaration' xmlns:valueChanged='http://www.mydata-control.de/4.0/valueChanged' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:date='http://www.mydata-control.de/4.0/date' xmlns:time='http://www.mydata-control.de/4.0/time' xmlns:day='http://www.mydata-control.de/4.0/day'>\n"
      + "  <mechanism event='urn:action:test:pip-test-2'>\n" + "    <if>\r\n" + "    <equals>\r\n" + "        <constant:false/>\r\n"
      + "        <pip:boolean method='urn:info:test:not' default='true'>\r\n" + "          <parameter:boolean name='value' value='false'/>\r\n" + "        </pip:boolean>\r\n" + "      </equals>\r\n"
      + "      <then>\r\n" + "        <allow/>\r\n" + "      </then>\r\n" + "    </if>" + "    <else>\r\n" + "      <inhibit/>\r\n" + "    </else>" + "  </mechanism>\n" + "</policy>";

  public static final String PXP_TEST_POLICY_1 = "<policy id='urn:policy:test:pxp-test-1' description='pxp-test-1' xmlns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:tns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:parameter='http://www.mydata-control.de/4.0/parameter' xmlns:pip='http://www.mydata-control.de/4.0/pip' xmlns:function='http://www.mydata-control.de/4.0/function' xmlns:event='http://www.mydata-control.de/4.0/event' xmlns:constant='http://www.mydata-control.de/4.0/constant' xmlns:variable='http://www.mydata-control.de/4.0/variable' xmlns:variableDeclaration='http://www.mydata-control.de/4.0/variableDeclaration' xmlns:valueChanged='http://www.mydata-control.de/4.0/valueChanged' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:date='http://www.mydata-control.de/4.0/date' xmlns:time='http://www.mydata-control.de/4.0/time' xmlns:day='http://www.mydata-control.de/4.0/day'>\n"
      + "  <mechanism event='urn:action:test:pxp-test-1'>\n" + "    <if>\r\n" + "    <equals>\r\n" + "        <constant:true/>\r\n" + "        <execute action='urn:action:test:log'>\r\n"
      + "          <parameter:string name='text' value='test message'/>\r\n" + "        </execute>\r\n" + "      </equals>\r\n" + "      <then>\r\n" + "        <allow/>\r\n" + "      </then>\r\n"
      + "    </if>" + "    <else>\r\n" + "      <inhibit/>\r\n" + "    </else>" + "  </mechanism>\n" + "</policy>";

  public static final String PXP_TEST_POLICY_2 = "<policy id='urn:policy:test:pxp-test-2' description='pxp-test-2' xmlns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:tns='http://www.mydata-control.de/4.0/mydataLanguage' xmlns:parameter='http://www.mydata-control.de/4.0/parameter' xmlns:pip='http://www.mydata-control.de/4.0/pip' xmlns:function='http://www.mydata-control.de/4.0/function' xmlns:event='http://www.mydata-control.de/4.0/event' xmlns:constant='http://www.mydata-control.de/4.0/constant' xmlns:variable='http://www.mydata-control.de/4.0/variable' xmlns:variableDeclaration='http://www.mydata-control.de/4.0/variableDeclaration' xmlns:valueChanged='http://www.mydata-control.de/4.0/valueChanged' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:date='http://www.mydata-control.de/4.0/date' xmlns:time='http://www.mydata-control.de/4.0/time' xmlns:day='http://www.mydata-control.de/4.0/day'>\n"
      + "  <mechanism event='urn:action:test:pxp-test-2'>\n" + "    <if>\r\n" + "    <equals>\r\n" + "        <constant:true/>\r\n" + "        <execute action='urn:action:test:log'>\r\n"
      + "          <parameter:string name='text' value='test message'/>\r\n" + "        </execute>\r\n" + "      </equals>\r\n" + "      <then>\r\n" + "        <inhibit/>\r\n" + "      </then>\r\n"
      + "    </if>" + "    <else>\r\n" + "      <allow/>\r\n" + "    </else>" + "  </mechanism>\n" + "</policy>";

}

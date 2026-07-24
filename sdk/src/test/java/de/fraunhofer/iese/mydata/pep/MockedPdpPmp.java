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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.User;
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.InputParameterDescription;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.information.method.PepInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IBasicManagementService;
import de.fraunhofer.iese.mydata.component.interfaces.IPolicyDecisionPoint;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.DecisionId;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.ModifierList;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MockedPdpPmp {
  public static AuthorizationDecision getAuthorizationDecision() {
    final AuthorizationDecision pdpAuthorizationDecision = mock(AuthorizationDecision.class);
    final ModifierList modifierList = new ModifierList();
    final Modifier userNameModifier = new Modifier("user");
    userNameModifier.setExpression("$.name");
    final ModifierEngine appendModifierEngine = new ModifierEngine("append");
    appendModifierEngine.addParameter(new Parameter<>("prefix", "Mr. "));
    userNameModifier.addEngine(appendModifierEngine);

    final Modifier firstPhoneNoModifier = new Modifier("user");
    firstPhoneNoModifier.setExpression("$.phoneNo[0]");
    final ModifierEngine deleteModifierEngine = new ModifierEngine("delete");
    firstPhoneNoModifier.addEngine(deleteModifierEngine);

    modifierList.add(userNameModifier);
    modifierList.add(firstPhoneNoModifier);
    when(pdpAuthorizationDecision.getModifiers()).thenReturn(modifierList);
    when(pdpAuthorizationDecision.getId()).thenReturn(new DecisionId("urn:decision:remotePDP"));
    when(pdpAuthorizationDecision.isEventAllowed()).thenReturn(true);

    return pdpAuthorizationDecision;
  }

  public static AuthorizationDecision getAuthorizationDecisionWithoutModifier() {
    final AuthorizationDecision pdpAuthorizationDecision = mock(AuthorizationDecision.class);
    final ModifierList modifierList = new ModifierList();
    when(pdpAuthorizationDecision.getModifiers()).thenReturn(modifierList);
    when(pdpAuthorizationDecision.getId()).thenReturn(new DecisionId("urn:decision:remotePDP"));
    when(pdpAuthorizationDecision.isEventAllowed()).thenReturn(true);
    return pdpAuthorizationDecision;
  }

  public static PdpComponentInformation getMockedPDPComponent() throws URISyntaxException {
    final PdpComponentInformation pdpComponent = mock(PdpComponentInformation.class);
    final List<URI> uris = new ArrayList<URI>();
    uris.add(new URI("http://remotePDP.ind2uce.de/"));
    uris.add(new URI("https://remotePDP.ind2uce.de/"));
    when(pdpComponent.getUrls()).thenReturn(uris);

    return pdpComponent;
  }

  public static List<MethodInterfaceDescription> getMethodInterfaceDescription() {
    final MethodInterfaceDescription modifierInterfaceDescription = new MethodInterfaceDescription(
        "replace", String.class, "Replaces a sequence of chars with a new sequence",
        new InputParameterDescription("before", "string which is to be replaced", String.class),
        new InputParameterDescription("after", "string by which to be replaced", String.class));

    final MethodInterfaceDescription modifierInterfaceDescription1 = new MethodInterfaceDescription(
        "append", String.class, "appends the given string to the parameter",
        new InputParameterDescription("sufix", "string which is to be appened at the start",
            String.class),
        new InputParameterDescription("prefix", "string by which to be appened at the end",
            String.class));

    final MethodInterfaceDescription modifierInterfaceDescription2 = new MethodInterfaceDescription(
        "delete", Object.class, "delete the paraameter");

    final MethodInterfaceDescription modifierInterfaceDescription3 = new MethodInterfaceDescription(
        "anagram", String.class, "anagram the paraameter", new InputParameterDescription(
            "percentage", "the percentage you need to anagram", Integer.class));

    final List<MethodInterfaceDescription> modifierInterfaceDescriptions = new ArrayList<MethodInterfaceDescription>();
    modifierInterfaceDescriptions.add(modifierInterfaceDescription);
    modifierInterfaceDescriptions.add(modifierInterfaceDescription1);
    modifierInterfaceDescriptions.add(modifierInterfaceDescription2);
    modifierInterfaceDescriptions.add(modifierInterfaceDescription3);
    return modifierInterfaceDescriptions;
  }

  public static List<PepInterfaceDescription> getPepInterfaceDescription() {
    final List<PepInterfaceDescription> pepInterfaceDescriptions = new ArrayList<PepInterfaceDescription>();

    final InputParameterDescription inputParameterDescription = new InputParameterDescription(
        "user", "this is description of User", User.class);
    final List<InputParameterDescription> inputParameterDescriptions = new ArrayList<InputParameterDescription>();
    inputParameterDescriptions.add(inputParameterDescription);

    final PepInterfaceDescription pepInterfaceDescription = new PepInterfaceDescription(
        new ActionId("urn:solution:ind2uce:test"), true, "", inputParameterDescriptions);

    pepInterfaceDescriptions.add(pepInterfaceDescription);

    return pepInterfaceDescriptions;
  }

  public static IPolicyDecisionPoint mockedPDP()
      throws IllegalArgumentException, IOException, EvaluationUndecidableException {
    return mockedPDP(getAuthorizationDecision());
  }

  public static IPolicyDecisionPoint mockedPDP(AuthorizationDecision decision)
      throws IllegalArgumentException, IOException, EvaluationUndecidableException {
    final IPolicyDecisionPoint mockedPDP = mock(IPolicyDecisionPoint.class);
    when(mockedPDP.decisionRequest(any(Event.class))).thenReturn(decision);
    return mockedPDP;
  }

  public static IPolicyDecisionPoint mockedPDPWithoutAuthorizationDecision()
      throws IllegalArgumentException, IOException, EvaluationUndecidableException {
    final IPolicyDecisionPoint mockedPDP = mock(IPolicyDecisionPoint.class);
    final AuthorizationDecision authorizationDecision = getAuthorizationDecisionWithoutModifier();
    when(mockedPDP.decisionRequest(any(Event.class))).thenReturn(authorizationDecision);
    return mockedPDP;
  }

  public static IBasicManagementService mockedPMP()
      throws IOException, URISyntaxException, NoSuchEntityException {
    final IBasicManagementService mockedPMP = mock(IBasicManagementService.class);
    // when(mockedPMP.registerPmp(any(PmpComponentInformation.class))).thenReturn(true);
    // TODO needed?
    // when(mockedPMP.addPep(any(PepComponentInformation.class))).thenReturn(true);
    final PdpComponentInformation pdpComponent = getMockedPDPComponent();
    when(mockedPMP.getPdp()).thenReturn(pdpComponent);

    return mockedPMP;
  }

}

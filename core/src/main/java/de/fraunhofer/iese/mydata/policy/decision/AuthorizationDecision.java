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

package de.fraunhofer.iese.mydata.policy.decision;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.policy.parameter.ModifierList;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * Information about the permissiveness of the event. The decision may include instructions for
 * modification or delaying the event.Decisions are provided by the PDP and enforced by a PEP.
 *
 * @author Fraunhofer IESE
 */

@Getter
public class AuthorizationDecision extends MyDataEntity {

  /**
   * The default decision for a plain allow.
   */
  public static final AuthorizationDecision DECISION_ALLOW = new AuthorizationDecision(
      new DecisionId("urn:decision:allow"), true);

  /**
   * The default decision for a plain inhibit.
   */
  public static final AuthorizationDecision DECISION_INHIBIT = new AuthorizationDecision(
      new DecisionId("urn:decision:inhibit"), false);

  /**
   * The component_id of the decision.
   */
  @NotNull
  @Valid
  private final DecisionId id;

  /**
   * Indicates whether an event should be allowed or blocked.
   */
  @Setter
  private boolean eventAllowed;

  /**
   * The name(s) of the corresponding AutorizationAction(s) that led to the decision.
   */
  @Nullable
  private final List<String> authorizationActionNames;

  /**
   * List of modifications that need to be enforced for an event.
   */
  @Valid
  private ModifierList modifiers = new ModifierList();

  /**
   * Instantiates a new authorization decision. Default constructor for JPA
   */
  public AuthorizationDecision() {
    this.id = new DecisionId("urn:decision:notset");
    this.authorizationActionNames = null;
    // Needed for JPA
  }

  /**
   * Constructor that takes the component_id and eventAllowed from Prototype.
   *
   * @param prototype Prototype to create a new instance.
   */
  public AuthorizationDecision(AuthorizationDecision prototype) {
    this(prototype.getId(), prototype.isEventAllowed());
  }

  /**
   * Instantiates a new authorization decision.
   *
   * @param id         the id
   * @param allowEvent the allow event
   */
  private AuthorizationDecision(DecisionId id, boolean allowEvent) {
    this(id, allowEvent, null);
  }

  /**
   * Instantiates a new authorization decision.
   *
   * @param id                       the id
   * @param eventAllowed             the event allowed
   * @param authorizationActionNames the authorization action names
   * @param modifiers                the modifiers
   */
  public AuthorizationDecision(DecisionId id, boolean eventAllowed,
      List<String> authorizationActionNames, Modifier... modifiers) {
    this(id, eventAllowed, authorizationActionNames, new ModifierList(modifiers));
  }

  /**
   * Instantiates a new authorization decision.
   *
   * @param id                       the id
   * @param eventAllowed             the event allowed
   * @param authorizationActionNames the authorization action names
   * @param modifiers                the modifiers
   */
  public AuthorizationDecision(DecisionId id, boolean eventAllowed,
      @Nullable List<String> authorizationActionNames, ModifierList modifiers) {
    this.id = id;
    this.eventAllowed = eventAllowed;
    this.authorizationActionNames = authorizationActionNames;
    if (eventAllowed) {
      this.modifiers = modifiers;
    }
  }

  /**
   * Get a clone of DECISION_ALLOW.
   *
   * @return a clone of DECISION_ALLOW.
   */
  public static AuthorizationDecision getDecisionAllow() {
    return new AuthorizationDecision(DECISION_ALLOW);

  }

  /**
   * Get a clone of DECISION_INHIBIT.
   *
   * @return DECISION_INHIBIT
   */
  public static AuthorizationDecision getDecisionInhibit() {
    return new AuthorizationDecision(DECISION_INHIBIT);

  }

  /**
   * Adds the modifier.
   *
   * @param param the param
   */
  public void addModifier(Modifier param) {
    this.modifiers.add(param);
  }

  /**
   * Clear modifiers.
   */
  public void clearParameters() {
    this.modifiers.clear();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof AuthorizationDecision)) {
      return false;
    }

    final AuthorizationDecision that = (AuthorizationDecision) o;

    return new EqualsBuilder().append(this.isEventAllowed(), that.isEventAllowed())
        .append(this.getId(), that.getId())
        .append(this.getAuthorizationActionNames(), that.getAuthorizationActionNames())
        .append(this.getModifiers(), that.getModifiers()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(this.getId()).append(this.isEventAllowed())
        .append(this.getAuthorizationActionNames()).append(this.getModifiers()).toHashCode();
  }
}

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

package de.fraunhofer.iese.mydata.pdp;

import de.fraunhofer.iese.mydata.pdp.interfaces.IPolicyStore;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyDecision;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyMechanism;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyModify;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.VariableDeclaration;
import de.fraunhofer.iese.mydata.pdp.language.model.function.bool.ExecuteFunction;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.decision.AuthorizationDecision;
import de.fraunhofer.iese.mydata.policy.decision.Modifier;
import de.fraunhofer.iese.mydata.policy.decision.ModifierEngine;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * The Class DecisionMaker.
 * <p>
 * This callable class returns either ALLOW or INHIBIT Authorization Decision.
 * </p>
 */
public class DecisionMaker implements Callable<AuthorizationDecision> {

  public static final String EVENT_TRACE_ID_PARAM_NAME = "event_traceId";

  public static final String EVENT_ACTION_ID_PARAM_NAME = "event_actionId";

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DecisionMaker.class);

  /**
   * The event.
   */
  private final Event event;

  /**
   * The store of all policies.
   */
  private final IPolicyStore policyStore;

  /**
   * ThreadPool to use for pxpExecutions.
   */
  private final ExecutorService threadPoolForPxpActions;

  private final boolean isWhitelistModeEnabled = PolicyDecisionPoint.getInstance()
      .isWhitelistModeEnabled();

  /**
   * Instantiates a new decision maker.
   *
   * @param evt                     the evt
   * @param policyStore             the policy store
   * @param threadPoolForPxpActions the thread pool for PXP actions
   */
  public DecisionMaker(final Event evt, final IPolicyStore policyStore,
      final ExecutorService threadPoolForPxpActions) {
    this.event = evt;
    this.policyStore = policyStore;
    this.threadPoolForPxpActions = threadPoolForPxpActions;
  }

  /*
   * (non-Javadoc)
   * @see java.util.concurrent.Callable#call()
   */
  @Override
  public AuthorizationDecision call() throws Exception {

    MDC.put(EVENT_ACTION_ID_PARAM_NAME, this.event.getActionId().getUrn());
    MDC.put(EVENT_TRACE_ID_PARAM_NAME, Long.toHexString(ThreadLocalRandom.current().nextLong()));
    LOG.debug("Starting event processing for event with action [{}]", this.event.getActionId());

    try {
      this.policyStore.readLockStore();
      // The final decision is ALLOW if there is no policy addressing the event.
      if (this.policyStore.isEmpty()) {
        // if whitelist mode is enabled return inhibit
        if (this.isWhitelistModeEnabled) {
          LOG.info("No policies deployed, returning INHIBIT for event with action [{}]",
              this.event.getActionId());
          return AuthorizationDecision.getDecisionInhibit();
        }
        LOG.info("No policies deployed, returning ALLOW for event with action [{}].",
            this.event.getActionId());
        return AuthorizationDecision.getDecisionAllow();
      }

      // The final decision is INHIBIT if any error occurs during the process of
      // finding the decision.
      final AuthorizationDecision finalAuthorizationDecision = this.processChain();
      if (finalAuthorizationDecision == null) {
        LOG.info("Error during decision aggregation. finalDecision == null. Returning INHIBIT.");
        return AuthorizationDecision.DECISION_INHIBIT;
      }
      LOG.info("Done event processing for {} with result {}", this.event.getActionId(),
          finalAuthorizationDecision.isEventAllowed());
      return finalAuthorizationDecision;
    } finally {
      this.policyStore.readUnlockStore();
      MDC.remove(EVENT_TRACE_ID_PARAM_NAME);
      MDC.remove(EVENT_ACTION_ID_PARAM_NAME);
    }
  }

  /**
   * Regular decision chain.<br/>
   * <ol>
   * <li>Collect Matching Mechanisms</li>
   * <li>Aggregate Decision</li>
   * <li>Finalize Decision</li>
   * <li>Execute the Execute Actions</li>
   * </ol>
   *
   * @return                                INHIBIT, or ALLOW
   * @throws EvaluationUndecidableException In case of an error during policy evaluation
   */
  private AuthorizationDecision processChain() throws EvaluationUndecidableException {
    // IPolicyStore holds a set of policies and their corresponding mechanisms
    // grouped by event actionIds.
    // This set is being updated any time that deploy(add) or revoke(remove)
    // policy occurs.
    // check if event matches
    final Set<Entry<PolicyId, Set<PolicyMechanism>>> matchingPolicyMechanisms = this.policyStore
        .getMatchingMechanisms(this.event.getActionId());
    if (LOG.isDebugEnabled()) {
      LOG.debug("Following policies are matching: {}",
          matchingPolicyMechanisms.stream().map(Entry::getKey).collect(Collectors.toList()));
    }
    if (matchingPolicyMechanisms.isEmpty()) {
      // if whitelist mode is enabled return inhibit, otherwise allow
      if (this.isWhitelistModeEnabled) {
        LOG.debug("No matching policy mechanisms, returning INHIBIT");
        return AuthorizationDecision.DECISION_INHIBIT;
      }
      LOG.debug("No matching policy mechanisms, returning ALLOW");
      return AuthorizationDecision.DECISION_ALLOW;
    }

    try {
      boolean decisionAvailable = false;
      // evaluate all if blocks (conditions) and collect the decisions
      final PolicyDecision finalPolicyDecision = new PolicyDecision(true);
      for (final Entry<PolicyId, Set<PolicyMechanism>> e : matchingPolicyMechanisms) {
        for (final PolicyMechanism m : e.getValue()) {
          m.setPolicy(this.policyStore.getPolicyByPolicyId(e.getKey()));
          m.setZoneId(this.policyStore.getZoneidByPolicyId(e.getKey()));
          final PolicyDecision d = m.evaluate(this.event);
          // TODO do we want that modify implies an allow or should it be specified additionaly in the policy
          Optional<Boolean> isAllowedOptional = d.isAllowed();
          if (d != PolicyDecision.NO_DECISION && !isAllowedOptional.isPresent()
              && !d.getModifiers().isEmpty()) {
            d.setAllowed(true);
          }
          isAllowedOptional = d.isAllowed();
          if (isAllowedOptional.isPresent()) {
            if (d != PolicyDecision.NO_DECISION) {
              decisionAvailable = true;
            }
            if (Boolean.FALSE.equals(isAllowedOptional.get())) {
              finalPolicyDecision.setAllowed(false);
            }
          }
          finalPolicyDecision.getModifiers().addAll(d.getModifiers());
          finalPolicyDecision.getExecuteActions().addAll(d.getExecuteActions());

          // language feature "unconditional execution of an action" (executeActions @ mechanism)
          finalPolicyDecision.getExecuteActions().addAll(m.getExecuteActions());
        }
      }

      final Optional<Boolean> finalPolicyDecisionIsAllowed = finalPolicyDecision.isAllowed();
      if (finalPolicyDecisionIsAllowed.isPresent()
          && Boolean.FALSE.equals(finalPolicyDecisionIsAllowed.get())) {
        finalPolicyDecision.getModifiers().clear();
      }

      // execute optional execute actions (mandatory action have been executed
      // in the conditions)
      for (final ExecuteFunction action : finalPolicyDecision.getExecuteActions()) {
        this.threadPoolForPxpActions.submit(new PxpExecutor(action, this.event));
      }

      // Merge modifiers and draw final decision
      final AuthorizationDecision finalAuthorizationDecision = new AuthorizationDecision();

      // check if a decision has been made and if whitelist mode is enabled
      LOG.debug("Whitelist mode is {}", this.isWhitelistModeEnabled);
      if (!decisionAvailable || !finalPolicyDecisionIsAllowed.isPresent()) {
        if (this.isWhitelistModeEnabled) {
          LOG.info("No decision was made, returning INHIBIT for event {}",
              this.event.getActionId());
          finalAuthorizationDecision.setEventAllowed(false);
        } else {
          LOG.info("No decision was made, returning ALLOW for event {}", this.event.getActionId());
          finalAuthorizationDecision.setEventAllowed(true);
        }
      } else {
        LOG.info("Decision was made, returning allowed={} for event {}",
            finalPolicyDecisionIsAllowed.get(), this.event.getActionId());
        finalAuthorizationDecision.setEventAllowed(finalPolicyDecisionIsAllowed.get());
      }

      // check if modifiers are available
      if (finalPolicyDecision.getModifiers() != null) {
        for (final PolicyModify policyModify : finalPolicyDecision.getModifiers()) {
          final Modifier m = new Modifier(policyModify.getEventParameter());
          m.setExpression(policyModify.getJsonPathQuery());
          final ModifierEngine engine = new ModifierEngine(policyModify.getMethod());
          if (policyModify.getParams() != null) {
            for (final PolicyParameter<?> parameter : policyModify.getParams()) {
              engine.addParameter(parameter.getName(), parameter.evaluate(this.event).getValue());
            }
          }
          m.addEngine(engine);
          finalAuthorizationDecision.addModifier(m);
        }
      }
      return finalAuthorizationDecision;
    } finally {
      matchingPolicyMechanisms.stream().map(Entry::getValue).flatMap(Collection::stream)
          .forEach(policyMechanism -> {
            policyMechanism.accept(new ClearVariableDeclarationCacheVisitor());
            if (policyMechanism.getPolicy() != null) {
              policyMechanism.getPolicy().getVariableDeclarations()
                  .forEach(VariableDeclaration::reset);
            }
          });
    }
  }

}

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

package de.fraunhofer.iese.mydata.pdp.language.model.function.number;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.pdp.PolicyDecisionPoint;
import de.fraunhofer.iese.mydata.pdp.language.model.EventOccurrence;
import de.fraunhofer.iese.mydata.pdp.language.model.OccurrenceTime;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.When;
import de.fraunhofer.iese.mydata.pdp.language.model.function.Function;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEvent;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEventParameter;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.time.TimeUtil;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

@SuppressWarnings("javadoc")
public class CountFunction extends Function {
  private static final Logger LOG = LoggerFactory.getLogger(CountFunction.class);

  private When<?> when = null;

  private EventOccurrence eventOccurrence;

  @Override
  public DataObject<?> evaluate(Event evt) throws EvaluationUndecidableException {
    LOG.debug("Entering evaluate(name={}, parameters={}, event={})", this.getName(),
        this.getParameters(), evt.getActionId());
    final Optional<IEventRepository> eventRepositoryOptional = PolicyDecisionPoint.getInstance()
        .getEventRepository();
    if (!eventRepositoryOptional.isPresent()) {
      final int count = 0;
      LOG.warn("EventHistory not supported, returning count {}", count);
      return new DataObject<>(count);
    }

    final ZoneId zoneId = evt.getZoneId();
    final ZonedDateTime zdtNowPolicyTime = ZonedDateTime.now(zoneId);
    if (this.getParameters().isEmpty()) {
      LOG.warn("Insufficient parameters for count function");
      throw new IllegalArgumentException("Insufficent parameters");
    }

    EventOccurrence countEO = null;
    String fixedTime = null;

    for (final IFunction op : this.getParameters()) {
      final DataObject<?> evalResult = op.evaluate(evt);
      if (evalResult instanceof EventOccurrence) {
        countEO = (EventOccurrence) evalResult;
      } else if (op instanceof When) {
        this.when = (When<?>) op;
        fixedTime = this.when.getFixedTime();
        this.when.evaluate(evt);
      } else {
        throw new IllegalArgumentException("Only When and EventOccurrences are supported");
      }
    }
    final Pair<Long, Long> startAndEnd = this.convertToServerTime(evt, this.when, fixedTime, zoneId,
        zdtNowPolicyTime);
    final Long start = startAndEnd.getLeft();
    final Long end = startAndEnd.getRight();

    if (start == null || end == null) {
      return new DataObject<>(0);
    }
    if (null == countEO) {
      throw new EvaluationUndecidableException("invalid policy");
    }

    final List<HistoricEventParameter> hep = this.extractEventsFromPolicyEvaluation(countEO, evt);
    LOG.info("Count Function : number of HistoricEvent with parameters: {}", hep.size());

    LOG.info("Count Function : start and end in ms since epoch and event: {} {} {}", start, end,
        evt.getMillisecondSinceEpoch());
    List<HistoricEvent> eventsFiltredByParam;

    final IEventRepository eventRepository = eventRepositoryOptional.get();
    if (!hep.isEmpty()) {
      eventsFiltredByParam = eventRepository
          .findByActionIdAndHistoricEventParametersAndOccurredAtMsBetween(
              new ActionId(countEO.getEvent()), hep, start, end);
    } else {
      eventsFiltredByParam = eventRepository.findByActionIdAndOccurredAtMsBetweenParamIndependant(
          new ActionId(countEO.getEvent()), start, end);
    }
    final int count = eventsFiltredByParam.size();
    LOG.info("Count Function returned {}", count);
    return new DataObject<>(count);

  }

  /**
   * @return the eventOccurrence
   */
  public EventOccurrence getEventOccurrence() {
    return this.eventOccurrence;
  }

  /**
   * @param eventOccurrence the eventOccurrence to set
   */
  public void setEventOccurrence(EventOccurrence eventOccurrence) {
    this.eventOccurrence = eventOccurrence;
  }

  private List<HistoricEventParameter> extractEventsFromPolicyEvaluation(EventOccurrence countEO,
      Event evt) throws EvaluationUndecidableException {

    final List<IFunction> eventParameters = countEO.getSubOperators();
    final List<HistoricEventParameter> hep = new ArrayList<>();
    if (eventParameters != null) {
      for (final IFunction policyParameter : eventParameters) {
        final DataObject<?> param = policyParameter.evaluate(evt);
        final Object parameterValue = param.getValue();
        final String parameterTypeName = param.getTypeName();
        @Nullable
        final Class<?> parameterTypeClass = param.getType();
        final String stringValue;
        final String epKey = policyParameter.getName();
        final String epJsonPath;
        if (policyParameter instanceof PolicyParameter) {
          epJsonPath = ((PolicyParameter) policyParameter).getJsonPathQuery();
        } else {
          epJsonPath = null;
        }

        if (!ClassUtils.isPrimitiveOrWrapper(parameterTypeClass)
            && !(parameterTypeName.equals(String.class.getName()))) {
          LOG.debug("Parameter {} is object or json-string", epKey);
          if (parameterValue instanceof String) {
            LOG.debug("parameterValue instanceof String");
            stringValue = (String) parameterValue;
          } else {
            LOG.debug("parameterValue !instanceof String");
            stringValue = MyDataEntity.getGson().toJson(parameterValue);
          }
        } else {
          LOG.debug("Parameter {} is primitive or real string", epKey);
          stringValue = String.valueOf(parameterValue);
        }
        hep.add(new HistoricEventParameter(stringValue, epKey, epJsonPath));
      }
    }
    return hep;
  }

  private Pair<Long, Long> convertToServerTime(Event evt, When<?> when, String fixedTime,
      ZoneId zoneIdOfSolution, ZonedDateTime zdtNowPolicyTime)
      throws EvaluationUndecidableException {
    ZonedDateTime start = null;
    ZonedDateTime end = null;
    if (when != null) {
      if (fixedTime == null) {
        try {
          if (when.getStart() != null) {
            start = this.extractTime(evt, when.getStart(), zoneIdOfSolution, zdtNowPolicyTime);
          }
          if (when.getEnd() != null) {
            end = this.extractTime(evt, when.getEnd(), zoneIdOfSolution, zdtNowPolicyTime);
          }
        } catch (final IllegalArgumentException e) {
          LOG.debug("Time could not get converted to server time: {}", e.getMessage(), e);
        }
      } else {

        try {
          start = TimeUtil.getInterval(fixedTime, zdtNowPolicyTime).getStartTime();
          end = TimeUtil.getInterval(fixedTime, zdtNowPolicyTime).getEndTime();
        } catch (final IllegalArgumentException e) {
          LOG.debug("Time could not get converted to server time {}", e.getMessage(), e);
        }

      }
      if (start == null) {
        start = Instant.EPOCH.atZone(zoneIdOfSolution);
      }
      if (end == null) {
        end = zdtNowPolicyTime;
      }
      return ImmutablePair.of(start.toInstant().toEpochMilli(), end.toInstant().toEpochMilli());
    }
    return ImmutablePair.nullPair();
  }

  private ZonedDateTime extractTime(Event evt, OccurrenceTime<?> t, ZoneId zoneIdOfSolution,
      ZonedDateTime zdtNowPolicyTime)
      throws IllegalArgumentException, EvaluationUndecidableException {
    final EventOccurrence eo = t.getEventOccurrence();
    ZonedDateTime zdt;
    if (eo != null) {
      // time relative to event
      zdt = this.getTime(eo, t.getTime(), evt, zoneIdOfSolution, zdtNowPolicyTime);
    } else {
      // time relative to now
      zdt = this.getTime(null, t.getTime(), evt, zoneIdOfSolution, zdtNowPolicyTime);
    }
    return zdt;
  }

  /**
   * @param  eo
   * @param  time
   * @param  evt
   * @return                                a zoneddatetime relative to an event if exists
   * @throws IllegalArgumentException
   * @throws EvaluationUndecidableException
   */
  private ZonedDateTime getTime(EventOccurrence eo, String time, Event evt, ZoneId zoneIdOfSolution,
      ZonedDateTime zdtNowPolicyTime)
      throws IllegalArgumentException, EvaluationUndecidableException {
    // is relative to an event
    if (eo != null) {
      final List<HistoricEventParameter> policyEventParameters = this
          .extractEventsFromPolicyEvaluation(eo, evt);

      // the mode only plays a role when we want to get a time relative to an
      // event occurrence
      final Optional<IEventRepository> eventRepositoryOptional = PolicyDecisionPoint.getInstance()
          .getEventRepository();
      if (!eventRepositoryOptional.isPresent()) {
        return null; // TODO
      }
      final IEventRepository eventRepository = eventRepositoryOptional.get();
      final HistoricEvent he = eventRepository.findByActionIdAndMode(new ActionId(eo.getEvent()),
          eo.getMode(), policyEventParameters);
      if (he == null) {
        return null;
      }
      final Instant instant = Instant.ofEpochMilli(he.getOccurredAtMs());
      final ZonedDateTime timeRelativeTo = instant.atZone(zoneIdOfSolution);
      return TimeUtil.parseExpressionToZonedDateTime(time, timeRelativeTo);
    } else {
      // is relative to now
      return TimeUtil.parseExpressionToZonedDateTime(time, zdtNowPolicyTime);
    }

  }

}

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

package de.fraunhofer.iese.mydata.pdp.language.model.function.bool;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.pdp.PolicyDecisionPoint;
import de.fraunhofer.iese.mydata.pdp.language.model.EventOccurrence;
import de.fraunhofer.iese.mydata.pdp.language.model.OccurrenceTime;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyConstant;
import de.fraunhofer.iese.mydata.pdp.language.model.PolicyParameter;
import de.fraunhofer.iese.mydata.pdp.language.model.When;
import de.fraunhofer.iese.mydata.pdp.language.model.function.IFunction;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEvent;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEventParameter;
import de.fraunhofer.iese.mydata.policy.event.history.IEventRepository;
import de.fraunhofer.iese.mydata.policy.exception.EvaluationUndecidableException;
import de.fraunhofer.iese.mydata.policy.parameter.DataObject;
import de.fraunhofer.iese.mydata.policy.time.TimeUtil;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import org.jspecify.annotations.Nullable;

/**
 * The Class ContinuousOccurrence.
 */
@Getter
public class ContinuousOccurrenceFunction extends BooleanFunction {
  private static final Logger LOG = LoggerFactory.getLogger(ContinuousOccurrenceFunction.class);

  When<?> when = null;

  String interval;

  int minOccurrences = 0;

  int maxOccurrences = -1;

  private EventOccurrence eventOccurrence;

  /**
   * Instantiates a new greater function.
   */
  public ContinuousOccurrenceFunction() {
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

  /**
   * @return the minOccurrences
   */
  @XmlAttribute
  public int getMinOccurrences() {
    return this.minOccurrences;
  }

  /**
   * @param minOccurrences the minOccurrences to set
   */
  public void setMinOccurrences(int minOccurrences) {
    this.minOccurrences = minOccurrences;
  }

  /**
   * @return the maxOccurrences
   */
  @XmlAttribute
  public int getMaxOccurrences() {
    return this.maxOccurrences;
  }

  /**
   * @param maxOccurrences the maxOccurrences to set
   */
  public void setMaxOccurrences(int maxOccurrences) {
    this.maxOccurrences = maxOccurrences;
  }

  /**
   * @return the start
   */
  @XmlAttribute
  public String getInterval() {
    return this.interval;
  }

  /**
   * set the time interval
   *
   * @param interval
   */
  public void setInterval(String interval) {
    this.interval = interval;
  }

  @Override
  public DataObject<Boolean> evaluate(Event evt) throws EvaluationUndecidableException {
    LOG.debug("Entering evaluate continuousOccurrence(name={}, parameters={}, event={})",
        this.getName(), this.getParameters(), evt);
    final Optional<IEventRepository> eventRepositoryOptional = PolicyDecisionPoint.getInstance()
        .getEventRepository();
    if (!eventRepositoryOptional.isPresent()) {
      LOG.warn("EventHistory not supported, returning result {}", PolicyConstant.FALSE);
      return PolicyConstant.FALSE;
    }
    final IEventRepository eventRepository = eventRepositoryOptional.get();
    final ZoneId zoneIdOfSolution = evt.getZoneId();
    final ZonedDateTime zdtNowPolicyTime = ZonedDateTime.now(zoneIdOfSolution);
    if (this.getParameters().isEmpty()) {
      LOG.warn("Insufficient parameters for continuousoccurrence function");
      throw new IllegalArgumentException("Insufficent parameters");
    }
    EventOccurrence countEO = null;
    String fixedTime = null;
    // extract EventOccurrence Objects and Time (when) from the block
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
    // event occurrences are saved in server time
    final Pair<Long, Long> startAndEnd = this.convertToServerTime(evt, this.when, fixedTime,
        eventRepository, zoneIdOfSolution, zdtNowPolicyTime);
    final Long start = startAndEnd.getLeft();
    final Long end = startAndEnd.getRight();

    if (start == null || end == null) {
      return new DataObject<>(false);
    }
    // no count set so it's always ok
    if (this.minOccurrences == -1 && this.maxOccurrences == -1) {
      return new DataObject<>(true);
    }
    if (null == countEO) {
      throw new EvaluationUndecidableException("invalid policy");
    }
    // get the linked parameters to that EventOccurrence from the policy ie. it
    // has to count the number of events with the parameter foo with value bar
    final List<HistoricEventParameter> hep = this.extractEventsFromPolicyEvaluation(countEO, evt);
    List<HistoricEvent> eventsFiltredByParam;
    final long timeInterval = this.getMillis();
    boolean conditionFullfiled = true;
    long startTime = start;
    final long endTime = end;
    final Instant endInstant = Instant.ofEpochMilli(end);
    Instant later = Instant.ofEpochMilli(start).plusMillis(timeInterval);
    while (later.isBefore(endInstant) && conditionFullfiled) {
      if (hep != null && !hep.isEmpty()) {
        // get all events saved with the same parameters from the DB, with the
        // matching time stamps
        eventsFiltredByParam = eventRepository
            .findByActionIdAndHistoricEventParametersAndOccurredAtMsBetween(
                new ActionId(countEO.getEvent()), hep, startTime, later.toEpochMilli());
        // if an interval is defined in the policy, check if the events occur
        // with this time step
        conditionFullfiled = this.checkInInterval(eventsFiltredByParam);
      } else {
        // get all events saved regardless of the parameters from the DB, with
        // the matching time stamps
        eventsFiltredByParam = eventRepository.findByActionIdAndOccurredAtMsBetweenParamIndependant(
            new ActionId(countEO.getEvent()), startTime, later.toEpochMilli());
        // if an interval is defined in the policy, check if the events occur
        // with this time step
        conditionFullfiled = this.checkInInterval(eventsFiltredByParam);
      }
      if (later.plusMillis(timeInterval).isBefore(endInstant)) {
        startTime = later.toEpochMilli();
      }
      later = later.plusMillis(timeInterval);
    }
    // if the whole interval has been validated except the last step (ie. from
    // 0->14 with a 5min step, it checked 0-5, 5-10 and still needs to check
    // 10-14)
    if (later.isAfter(endInstant) && conditionFullfiled) {
      if (hep != null && !hep.isEmpty()) {
        eventsFiltredByParam = eventRepository
            .findByActionIdAndHistoricEventParametersAndOccurredAtMsBetween(
                new ActionId(countEO.getEvent()), hep, startTime, endTime);
        conditionFullfiled = this.checkInInterval(eventsFiltredByParam);
      } else {
        eventsFiltredByParam = eventRepository.findByActionIdAndOccurredAtMsBetweenParamIndependant(
            new ActionId(countEO.getEvent()), startTime, endTime);
        conditionFullfiled = this.checkInInterval(eventsFiltredByParam);
      }
    }
    if (conditionFullfiled) {
      return PolicyConstant.TRUE;
    } else {
      return PolicyConstant.FALSE;
    }

  }

  private boolean checkInInterval(List<HistoricEvent> eventsFiltredByParam) {
    final int nbContinuousOccurrence = eventsFiltredByParam.size();
    if (this.minOccurrences > -1 && this.maxOccurrences > -1
        && nbContinuousOccurrence >= this.minOccurrences
        && nbContinuousOccurrence <= this.maxOccurrences) {
      return true;
    }
    if (this.minOccurrences > -1 && this.maxOccurrences == -1
        && nbContinuousOccurrence >= this.minOccurrences) {
      return true;
    }
    if (this.minOccurrences == -1 && this.maxOccurrences > -1
        && nbContinuousOccurrence <= this.maxOccurrences && nbContinuousOccurrence >= 0) {
      return true;
    }
    return false;
  }

  private long getMillis() {
    // Sanity check
    if (this.interval == null || this.interval.length() < 2) {
      return 0;
    }
    // Init
    final BigInteger maxValue = BigInteger.valueOf(Long.MAX_VALUE);
    BigInteger millisecondsForTTL = BigInteger.ZERO;
    // Tokenize TTL String and add up the values
    final StringTokenizer ttlTokenizer = new StringTokenizer(this.interval, "ywdhms", true);
    while (ttlTokenizer.hasMoreTokens()) {
      final String valueRaw = ttlTokenizer.nextToken();
      String unit = "";
      // Sanity check
      if (ttlTokenizer.hasMoreTokens()) {
        unit = ttlTokenizer.nextToken();
      } else { // No more tokens? Unit is missing -> Wrong format
        LOG.error("Wrong TTL format supplied. Ignoring TTL (TTL = 0).");
        return 0;
      }
      BigInteger valueAsBigInteger;
      try {
        valueAsBigInteger = BigInteger.valueOf(Long.valueOf(valueRaw));
      } catch (final NumberFormatException e) {
        LOG.error("Number of TTL Value is to big or has a wrong format (unit: {}; value: {})!",
            unit, valueRaw);
        continue;
      }
      BigInteger multiplyFactor;
      switch (unit) {
        case "y":
          multiplyFactor = BigInteger.valueOf((long) 1000 * 60 * 60 * 24 * 365);
          break;
        case "w":
          multiplyFactor = BigInteger.valueOf((long) 1000 * 60 * 60 * 24 * 7);
          break;
        case "d":
          multiplyFactor = BigInteger.valueOf((long) 1000 * 60 * 60 * 24);
          break;
        case "h":
          multiplyFactor = BigInteger.valueOf((long) 1000 * 60 * 60);
          break;
        case "m":
          multiplyFactor = BigInteger.valueOf((long) 1000 * 60);
          break;
        case "s":
          multiplyFactor = BigInteger.valueOf(1000);
          break;
        default: // no valid unit -> wrong format
          LOG.error("Wrong TTL format supplied. Ignoring TTL (TTL = 0).");
          return 0;
      }
      millisecondsForTTL = millisecondsForTTL.add(valueAsBigInteger.multiply(multiplyFactor));
      // Sanity check - ensure that value does not exceed Long.MAX_VALUE ->
      // prevent long overflow
      if (millisecondsForTTL.compareTo(maxValue) > 0) {
        LOG.error(
            "Number of TTL Value is to big. Using Long.MAX_VALUE as this is the maximum waiting time!");
        return Long.MAX_VALUE;
      }
    }
    return millisecondsForTTL.longValue();
  }

  private List<HistoricEventParameter> extractEventsFromPolicyEvaluation(EventOccurrence countEO,
      Event evt) throws EvaluationUndecidableException {
    final List<IFunction> eventParameters = countEO.getSubOperators();
    final List<HistoricEventParameter> hep = new ArrayList<>();
    if (eventParameters != null) {
      for (final IFunction policyParameter : eventParameters) {
        final String epKey = policyParameter.getName();
        final DataObject<?> param = policyParameter.evaluate(evt);
        final String epJsonPath;
        if (policyParameter instanceof PolicyParameter) {
          epJsonPath = ((PolicyParameter) policyParameter).getJsonPathQuery();
        } else {
          epJsonPath = null;
        }

        final Object parameterValue = param.getValue();
        final String parameterTypeName = param.getTypeName();
        @Nullable
        final Class<?> parameterTypeClass = param.getType();
        final String stringValue;

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
      IEventRepository eventRepository, ZoneId zoneIdOfSolution, ZonedDateTime zdtNowPolicyTime)
      throws EvaluationUndecidableException {
    ZonedDateTime start = null;
    ZonedDateTime end = null;

    if (when != null) {
      if (fixedTime == null) {
        try {
          if (when.getStart() != null) {
            start = this.extractTime(evt, when.getStart(), eventRepository, zdtNowPolicyTime);
          }
          if (when.getEnd() != null) {
            end = this.extractTime(evt, when.getEnd(), eventRepository, zdtNowPolicyTime);
          }
        } catch (final IllegalArgumentException e) {
          LOG.warn("Time conversion failed", e);
        }
      } else {
        try {
          start = TimeUtil.getInterval(fixedTime, zdtNowPolicyTime).getStartTime();
          end = TimeUtil.getInterval(fixedTime, zdtNowPolicyTime).getEndTime();
        } catch (final IllegalArgumentException e) {
          LOG.debug("Could not convert to server time {}", e.getMessage(), e);
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

  private ZonedDateTime extractTime(Event evt, OccurrenceTime<?> t,
      IEventRepository eventRepository, ZonedDateTime zdtNowPolicyTime)
      throws IllegalArgumentException, EvaluationUndecidableException {
    final EventOccurrence eo = t.getEventOccurrence();
    ZonedDateTime zdt;
    if (eo != null) {
      // time relative to event
      zdt = this.getTime(eo, t.getTime(), evt, eventRepository, zdtNowPolicyTime);
    } else {
      // time relative to now
      zdt = this.getTime(null, t.getTime(), evt, eventRepository, zdtNowPolicyTime);
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
  private ZonedDateTime getTime(EventOccurrence eo, String time, Event evt,
      IEventRepository eventRepository, ZonedDateTime zdtNowPolicyTime)
      throws IllegalArgumentException, EvaluationUndecidableException {
    // is relative to an event
    if (eo != null) {
      final List<HistoricEventParameter> policyEventParameters = this
          .extractEventsFromPolicyEvaluation(eo, evt);
      // the mode only plays a role when we want to get a time relative to an
      // event occurrence
      final HistoricEvent he = eventRepository.findByActionIdAndMode(new ActionId(eo.getEvent()),
          eo.getMode(), policyEventParameters);
      if (he == null) {
        return null;
      }
      final Instant instant = Instant.ofEpochMilli(he.getOccurredAtMs());
      final ZonedDateTime timeRelativeTo = instant.atZone(evt.getZoneId());
      return TimeUtil.parseExpressionToZonedDateTime(time, timeRelativeTo);
    } else {
      // is relative to now
      return TimeUtil.parseExpressionToZonedDateTime(time, zdtNowPolicyTime);
    }

  }

}

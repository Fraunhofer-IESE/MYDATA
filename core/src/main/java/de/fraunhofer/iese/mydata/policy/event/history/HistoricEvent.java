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

package de.fraunhofer.iese.mydata.policy.event.history;

import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.parameter.Parameter;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

@Entity(name = "historic_event")
@Table(indexes = {
    @Index(name = "action_id", columnList = "action_id"),
    @Index(name = "occurredAtIdx", columnList = "occurred_at_ms")
}, name = "historic_event")
@Getter
@Setter
public class HistoricEvent {

  /**
   * Logger.
   */
  @Transient
  private static final Logger log = LoggerFactory.getLogger(HistoricEvent.class);

  /**
   * Unique ID.
   */
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Id
  @Column(name = "historic_event_id")
  private long id;

  /**
   * Action ID.
   */
  @Valid
  @Embedded
  private ActionId actionId;

  /**
   * Event parameters.
   */
  @OneToMany(orphanRemoval = true, mappedBy = "historicEvent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Collection<@Valid HistoricEventParameter> historicEventParameters;

  /**
   * Event occurred at. millisecond since epoch
   */
  @Column(name = "occurred_at_ms")
  @NotNull
  @Min(1)
  private Long occurredAtMs;

  /**
   * Default constructor for JPA.
   */
  public HistoricEvent() {
    // required by JPA
  }

  /**
   * New instance from event.
   *
   * @param event      event to be stored
   * @param trackItems parameters to be saved
   */
  public HistoricEvent(Event event, Set<HistoricEventTrackItem> trackItems) {
    this.actionId = event.getActionId();
    this.occurredAtMs = event.getMillisecondSinceEpoch();
    if (trackItems != null) {
      for (final Parameter<?> param : event.getParameters()) {
        this.handleHistoricEventParameter(param, trackItems);
      }
    }
  }

  /**
   * Add parameters from event when it occurs. The params to be saved have been listed when the
   * policy was deployed, so that not the whole event has to be stored in the DB but only the
   * elements that will be used for the comparisons.
   *
   * @param parameter  parameter to be added
   * @param trackItems parameters to be saved
   */
  private void handleHistoricEventParameter(Parameter<?> parameter,
      Set<HistoricEventTrackItem> trackItems) {
    if (this.historicEventParameters == null) {
      this.historicEventParameters = new ArrayList<>();
    }
    if (log.isDebugEnabled()) {
      log.debug("handleHistoricEventParameter({}, {})", parameter,
          MyDataEntity.getGson().toJson(trackItems));
    }
    final String stringValue;
    final boolean isComplexObject;
    final String parameterTypeName = parameter.getTypeName();
    final Class<?> parameterTypeClass = parameter.getType();
    final Object parameterValue = parameter.getValue();

    if (!ClassUtils.isPrimitiveOrWrapper(parameterTypeClass)
        && !(parameterTypeName.equals(String.class.getName()))) {
      log.debug("Parameter {} is object or json-string", parameter.getName());
      isComplexObject = true;
      if (parameterValue instanceof String) {
        log.debug("parameterValue instanceof String");
        stringValue = (String) parameterValue;
      } else {
        log.debug("parameterValue !instanceof String");
        stringValue = MyDataEntity.getGson().toJson(parameterValue);
      }

    } else {
      log.debug("Parameter {} is primitive or real string", parameter.getName());
      isComplexObject = false;
      stringValue = String.valueOf(parameterValue);
    }

    for (final HistoricEventTrackItem trackItem : trackItems) {
      final String key = trackItem.getEventParamName();
      final String jsonPath = trackItem.getJsonPathQuery();
      final String staticValue = trackItem.getStaticValue();

      if (parameter.getName().equals(key)) {
        if (jsonPath == null) {
          this.extractAndSave(key, jsonPath, staticValue, stringValue);
        } else {
          if (!isComplexObject) {
            log.warn("jsonPath on primitives or simple strings not supported");
            continue;
          } else {
            if (JsonPath.isPathDefinite(jsonPath)) {
              log.debug("definite jsonPath {}", jsonPath);
            } else {
              log.debug("indefinite jsonPath {}", jsonPath);
            }
            Object o;
            try {
              o = JsonPath.read(stringValue, jsonPath);
              log.debug("JsonPath {} lookup found: {} of type {}", jsonPath, o, o.getClass());
            } catch (final Exception e) {
              log.info("JsonPath {} lookup lead to exception", jsonPath, e);
              o = null;
            }
            final String stringResultOfLookup;
            if (o instanceof String) {
              stringResultOfLookup = (String) o;
            } else {
              if (null == o) {
                stringResultOfLookup = ""; // TODO check if this value is okay
                                          // here
              } else {
                stringResultOfLookup = MyDataEntity.getGson().toJson(o);
              }
            }
            log.debug("Transformed {} (type {}) to {} (type {})", o,
                o == null ? "null" : o.getClass(), stringResultOfLookup,
                stringResultOfLookup.getClass());
            this.extractAndSave(key, jsonPath, staticValue, stringResultOfLookup);
          }
        }
      }
    }
  }

  private void extractAndSave(String key, String jsonPath, String staticValue, String stringValue) {
    log.debug("extractAndSave({},{},{},{})", key, jsonPath, staticValue, stringValue);
    if (staticValue != null) {
      if (staticValue.equals(stringValue)) {
        this.save(stringValue, key, jsonPath);
      }
    } else {
      this.save(stringValue, key, jsonPath);
    }
  }

  private void save(String value, String key, String jsonPath) {
    log.debug("save({},{},{})", value, key, jsonPath);
    this.addHistoricEventParameter(new HistoricEventParameter(value, key, jsonPath));
  }

  /**
   * Add historic event parameter.
   *
   * @param historicEventParameter parameter to be added
   */
  public void addHistoricEventParameter(HistoricEventParameter historicEventParameter) {
    if (log.isDebugEnabled()) {
      log.debug("addHistoricEventParameter({})", historicEventParameter.toJson(true));
    }
    this.historicEventParameters.add(historicEventParameter);
    historicEventParameter.setHistoricEvent(this);
  }

  /**
   * Remove historic event parameter. Not used yet as it depends from the Garbage strategy TO BE
   * DONE
   *
   * @param historicEventParameter parameter to be removed
   */
  public void removeHistoricEventParameter(HistoricEventParameter historicEventParameter) {
    this.historicEventParameters.remove(historicEventParameter);
    historicEventParameter.setHistoricEvent(null);
    // TBD
  }

  public Collection<HistoricEventParameter> getHistoricEventParameters() {
    if (this.historicEventParameters == null) {
      this.historicEventParameters = new ArrayList<>();
    }
    return this.historicEventParameters;
  }

  public void setHistoricEventParameters(
      Collection<HistoricEventParameter> historicEventParameters) {
    this.getHistoricEventParameters();
    this.historicEventParameters.clear();
    this.historicEventParameters.addAll(historicEventParameters);
  }

}

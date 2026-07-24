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

import de.fraunhofer.iese.mydata.common.Hide;
import de.fraunhofer.iese.mydata.common.MyDataEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Historic event parameter.
 */
@Entity(name = "historic_event_parameter")
@Table(name = "historic_event_parameter", indexes = {
    @Index(name = "nameValueIdx", columnList = "name, value")
})
@Getter
@Setter
public class HistoricEventParameter extends MyDataEntity {

  private static final String UNDERSCORE = "\u0000";

  /**
   * Unique ID.
   */
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Id
  @Column(name = "historic_event_parameter_id")
  private long id;

  /**
   * Name of parameter.
   */
  @NotBlank
  private String name;

  @Column(name = "json_path")
  private String jsonPath;

  /**
   * Hash value of parameter
   */
  @NotBlank
  @Column(length = 1024)
  private String value;

  /**
   * Historic event linked to this parameter.
   */
  @NotNull
  @Hide
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "historic_event_id")
  private HistoricEvent historicEvent;

  /**
   * Logger.
   */
  @Transient
  private final static Logger LOG = LoggerFactory.getLogger(HistoricEventParameter.class); // NOSONAR

  /**
   * Default constructor for JPA
   */
  public HistoricEventParameter() {
    // required by JPA
  }

  /**
   * Constructor. Value of parameter is hashed and stored.
   *
   * @param key      name of parameter // * @param type type of parameter
   * @param value    value of parameter.
   * @param jsonPath
   */
  public HistoricEventParameter(String value, String key, String jsonPath) {
    if (key == null) {
      key = "";// NOSONAR
    }
    if (value == null) {
      value = "";// NOSONAR
    }
    this.name = key.replaceAll(UNDERSCORE, "");
    this.value = hashValue(value);
    this.jsonPath = jsonPath;
  }

  /**
   * Constructor. Value of parameter is hashed and stored.
   *
   * @param key   name of parameter
   * @param value value of parameter.
   */
  public HistoricEventParameter(String key, String value) {

    if (value == null) {
      value = "";// NOSONAR
    }
    this.name = key.replaceAll(UNDERSCORE, "");
    this.value = hashValue(value);
  }

  /**
   * Hash generator for all event parameters.
   *
   * @param  value object to be hashed
   * @return       Hash value in String
   */
  public static String hashValue(String value) {
    if (value == null) {
      value = "";// NOSONAR
    }
    return DigestUtils.sha256Hex(value);
  }

  /**
   * @param historicEvent the historicEvent to set
   */
  void setHistoricEvent(HistoricEvent historicEvent) {
    this.historicEvent = historicEvent;
  }

  /**
   * Equals method.
   *
   * @param  obj comparing object
   * @return     @boolean
   */
  @Override
  public boolean equals(Object obj) {// NOSONAR
    if (!(obj instanceof HistoricEventParameter)) {
      return false;
    }
    return this.name != null && this.value != null
        && this.name.equals(((HistoricEventParameter) obj).name)
        && this.value.equals(((HistoricEventParameter) obj).value);
  }

}

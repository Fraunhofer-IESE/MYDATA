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

package de.fraunhofer.iese.mydata.solution;

import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.InfoId;
import de.fraunhofer.iese.mydata.timer.TimerId;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class SolutionId extends MyDataEntity implements Serializable {
  private static final long serialVersionUID = -2957729915074119344L;

  private static final Logger logger = LoggerFactory.getLogger(SolutionId.class);

  private static final String URN_PREFIX = "urn:solution:";

  /***
   * pattern for solution ^urn:solution:[A-Za-z0-9-_\.]+$
   */
  @NotNull
  @Pattern(regexp = "^" + URN_PREFIX + "[a-z0-9-_\\.]+$")
  @Column(name = "solution_id")
  private String urn;

  public SolutionId() {
    // needed for JPA
  }

  /**
   * @param urnOrIdentifier will be set to the urn if starts with {@value #URN_PREFIX}, else the
   *                          prefix will be add and set as urn
   */
  public SolutionId(String urnOrIdentifier) {
    this.urn = this.transfromUrnOrIdentifierToUrn(Objects.requireNonNull(urnOrIdentifier));
  }

  public static SolutionId fromPolicyId(PolicyId pid) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(pid);
    return new SolutionId(URN_PREFIX + pid.getUrn().split(":")[2]);
  }

  public static SolutionId fromTimerId(TimerId tid) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(tid);
    return new SolutionId(URN_PREFIX + tid.getUrn().split(":")[2]);
  }

  public static SolutionId fromActionId(ActionId aid) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(aid);
    return new SolutionId(URN_PREFIX + aid.getUrn().split(":")[2]);
  }

  public static SolutionId fromComponentId(ComponentId componentId) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(componentId);
    return new SolutionId(URN_PREFIX + componentId.getUrn().split(":")[2]);
  }

  public static SolutionId fromClientId(ClientId clientId) {
    return new SolutionId(URN_PREFIX + clientId.getUrn().split(":")[2]);
  }

  public static SolutionId fromInfoId(InfoId iid) throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(iid);
    return new SolutionId(URN_PREFIX + iid.getUrn().split(":")[2]);
  }

  private String transfromUrnOrIdentifierToUrn(String urnOrIdentifier) {
    if (urnOrIdentifier.startsWith(URN_PREFIX)) {
      return urnOrIdentifier.toLowerCase();
    } else {
      logger.debug("id {} has no URN_PREFIX: {}, it will be added", urnOrIdentifier, URN_PREFIX);
      return (URN_PREFIX + urnOrIdentifier).toLowerCase();
    }
  }

  /**
   * @param urnOrIdentifier will be set to the urn if starts with {@value #URN_PREFIX}, else the
   *                          prefix will be add and set as urn
   */
  public void setUrn(String urnOrIdentifier) {
    if (urnOrIdentifier != null) {
      this.urn = this.transfromUrnOrIdentifierToUrn(urnOrIdentifier);
    }
  }

  @Override
  public String toString() {
    return this.urn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof SolutionId)) {
      return false;
    }

    final SolutionId that = (SolutionId) o;

    return new EqualsBuilder().append(this.getUrn(), that.getUrn()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(this.getUrn()).toHashCode();
  }

  @Transient
  public String getIdentifier() throws InvalidEntityException {
    MyDataEntity.validateAndNullCheck(this);
    return this.getUrn().split(URN_PREFIX)[1];
  }

  @Transient
  public void setIdentifier(String identifier) throws InvalidEntityException {
    this.urn = URN_PREFIX + identifier;
    MyDataEntity.validate(this);
  }
}

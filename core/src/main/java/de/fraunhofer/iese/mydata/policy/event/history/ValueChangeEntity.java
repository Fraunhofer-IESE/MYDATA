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
import de.fraunhofer.iese.mydata.policy.Policy;

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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Stores value change entity identifier comes from policy Object
 */
@Entity(name = "value_change_entity")
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "block_hash", "block_id", "policy_id", "value"
    })

}, indexes = {
    @Index(columnList = "block_hash"), @Index(columnList = "value"), @Index(columnList = "block_id")
}, name = "value_change_entity")
@Getter
@Setter
public class ValueChangeEntity extends MyDataEntity {

  /**
   * Database component_id.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Hide
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "policy_id")
  private Policy policy;

  /**
   * Policy block component_id
   */
  @Column(nullable = false, name = "block_id", updatable = false)
  @NotBlank
  private String blockId;

  /**
   * Hash of policy usage of the block.
   */
  @Column(nullable = false, name = "block_hash", updatable = false, length = 1024)
  @NotBlank
  private String blockHash;

  /**
   * Value.
   */
  @Column(nullable = true)
  private String value;

  /**
   * Default constructor for JPA
   */
  public ValueChangeEntity() {
    // required by JPA
  }

  /**
   * Main constructor.
   *
   * @param policy    policy
   * @param blockHash hash of value-change block
   * @param blockId   the block id
   * @param value     value of block
   */
  public ValueChangeEntity(Policy policy, String blockHash, String blockId, String value) {
    this.policy = policy;
    this.blockHash = blockHash;
    this.value = value;
    this.blockId = blockId;
  }

  /**
   * Equals method.
   *
   * @param  obj comparing object
   * @return     @boolean
   */
  @Override
  public boolean equals(Object obj) {// NOSONAR
    if (!(obj instanceof ValueChangeEntity)) {
      return false;
    }

    return this.policy != null && this.blockHash != null && this.blockId != null
        && this.value != null && this.policy.equals(((ValueChangeEntity) obj).policy)
        && this.blockHash.equals(((ValueChangeEntity) obj).blockHash)
        && this.blockId.equals(((ValueChangeEntity) obj).blockId)
        && this.value.equals(((ValueChangeEntity) obj).value);
  }

}

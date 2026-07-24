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

package de.fraunhofer.iese.mydata.client;

import de.fraunhofer.iese.mydata.common.Hide;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.solution.Solution;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;

@Entity
@Table(name = "library_client", indexes = {
    @Index(columnList = "client_id", unique = true)
})
@Getter
@Setter
public class LibraryClient extends MyDataEntity {

  @NotNull
  @Valid
  @EmbeddedId
  @Column
  private ClientId clientId;

  @OneToOne(orphanRemoval = true, mappedBy = "libraryClient", fetch = FetchType.LAZY, cascade = {
      CascadeType.ALL
  })
  private SyncNotification latestSyncNotification;

  @Hide
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "scope")
  // has to be named scope for the OAuthService
  private Solution solution;

  @Column
  private boolean isMaster;

  /**
   * Default constructor for JPA
   */
  public LibraryClient() {
    // required by JPA
  }

  /**
   * Generates a new client
   *
   */
  public LibraryClient(ClientId clientId) {
    this.clientId = clientId;
  }

  /**
   * Sets the last time this client synchronised with the master pmp
   *
   * @param latestSyncNotification
   */
  public void setLatestSyncNotification(@NonNull SyncNotification latestSyncNotification) {
    this.latestSyncNotification = latestSyncNotification;
    latestSyncNotification.setLibraryClient(this);
  }

  /**
   * needed for JPA/Hibernate remove the SyncNotification if it matches the configured on
   *
   * @param latest : the SyncNotification to remove
   */
  public void removeLatestSyncNotification(@NonNull SyncNotification latest) {
    if ((latest.equals(this.latestSyncNotification)) || (this.latestSyncNotification == null)) {
      this.latestSyncNotification = null;
      latest.setLibraryClient(null);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }

    final LibraryClient that = (LibraryClient) o;

    return Objects.equals(this.getClientId(), that.getClientId());
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder(17, 31);
    builder.append(this.clientId);
    return builder.toHashCode();
  }

}

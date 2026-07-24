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

package de.fraunhofer.iese.mydata.client.dto;

import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.client.LibraryClient;
import de.fraunhofer.iese.mydata.oauth.dto.OAuthClientDetailsDTO;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import lombok.Data;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

@Data
public class LibraryClientResponseDTO {
  ClientId clientId;

  SolutionId solutionId;

  @Nullable
  SyncNotificationDTO syncNotification;

  @Nullable
  OAuthClientDetailsDTO oAuthClientDetails;

  boolean isMaster;

  public LibraryClientResponseDTO(LibraryClient libraryClient) {
    this.clientId = libraryClient.getClientId();
    this.solutionId = libraryClient.getSolution().getSolutionId();
    this.isMaster = libraryClient.isMaster();
    this.syncNotification = Optional.ofNullable(libraryClient.getLatestSyncNotification())
        .map(SyncNotificationDTO::new).orElse(null);
  }

}

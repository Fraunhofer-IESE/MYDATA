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

import de.fraunhofer.iese.mydata.client.dto.LibraryClientResponseDTO;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.oauth.dto.OAuthClientDetailsDTO;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.timer.TimerId;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface ILibraryClientService {

  /**
   * @param  syncNotification
   * @throws IOException
   * @throws InvalidEntityException if the sync notification is not valid
   * @throws NoSuchEntityException  if there is no client with the id specified in the sync
   *                                  notification
   */
  void notifySync(SyncNotification syncNotification)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * @param  libraryClientId
   * @return                        a map of policyIds and policyVersions for a specific clientId
   * @throws IOException
   * @throws InvalidEntityException if the client id is not valid
   * @throws NoSuchEntityException  if there is no library client with the client id given
   */
  Map<PolicyId, Long> listDeployedPolicyVersions(ClientId libraryClientId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * @param  policyId
   * @return                        a map of libraryClientIds and policyVersions for a specific
   *                                policyId
   * @throws IOException
   * @throws InvalidEntityException if the policy id is not valid
   * @throws NoSuchEntityException  if there is no policy with the policy id given
   */
  Map<ClientId, Long> listClientsForPolicy(PolicyId policyId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * @param  libraryClientId
   * @return                        a map of timerIds and timerVersions for a specific
   *                                libraryClientId
   * @throws IOException
   * @throws InvalidEntityException if the client id is not valid
   * @throws NoSuchEntityException  if there is no library client with the client id given
   */
  Map<TimerId, Long> listDeployedTimerVersions(ClientId libraryClientId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * @param  timerId
   * @return                        a map of libraryClientIds and policyVersions for a specific
   *                                policyId
   * @throws IOException
   * @throws InvalidEntityException if the timer id is not valid
   * @throws NoSuchEntityException  if there is not imer with the timer id given
   */
  Map<ClientId, Long> listClientsForTimer(TimerId timerId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * @param  clientIdStr                  a ClientId as String for this new LibraryClient
   * @param  isMasterClient
   * @return
   * @throws InvalidEntityException       if the client id string is not valid
   * @throws IOException                  clientIdStr must fit the soultionIdStr
   * @throws ConflictingResourceException if there already exists a library client with the same id
   *                                        or already a master client
   * @throws NoSuchEntityException        if the solution does not exist
   */
  LibraryClientResponseDTO createLibraryClient(String clientIdStr, boolean isMasterClient)
      throws InvalidEntityException, IOException, ConflictingResourceException,
      NoSuchEntityException;

  /**
   * @param  clientId               the Client of the LibraryClient you want to delete
   * @param  username               the username of the User who wants to accesss this method it
   *                                  will delete also the regarding OAtuhClientDetails if existing
   * @throws NoSuchEntityException  if the client does not exist
   * @throws InvalidEntityException if the client id is not valid
   * @throws IOException
   */
  void deleteLibraryClient(ClientId clientId, String username)
      throws NoSuchEntityException, InvalidEntityException, IOException;

  /**
   * @param  clientIdStr            as String the Client of the LibraryClient you want to delete
   * @param  username               the username of the User who wants to accesss this method it
   *                                  will delete also the regarding OAtuhClientDetails if existing
   * @throws InvalidEntityException
   * @throws NoSuchEntityException  if the client does not exist
   * @throws IOException
   */

  void deleteLibraryClient(String clientIdStr, String username)
      throws InvalidEntityException, NoSuchEntityException, IOException;

  /**
   * Get a client by its ID
   * 
   * @param  clientId               the client ID
   * @return
   * @throws NoSuchEntityException  if the client does not exists
   * @throws InvalidEntityException if the client id is not valid
   * @throws IOException
   */
  LibraryClient getLibraryClient(ClientId clientId)
      throws NoSuchEntityException, InvalidEntityException, IOException;

  /**
   * Get a set of library clients by solution id
   * 
   * @param  solutionId             Solution ID
   * @return                        Set of library clients
   * @throws NoSuchEntityException  if the solution does not exist
   * @throws InvalidEntityException if the solution id is not valid
   * @throws IOException
   */
  Set<LibraryClient> getLibraryClients(SolutionId solutionId)
      throws NoSuchEntityException, InvalidEntityException, IOException;

  /**
   * Class returns created ClientDetails for an LibraryClient with the username of the
   * Auhtentificated which is trying create it.
   *
   * @param  libraryClient
   * @param  username
   * @return
   * @throws InvalidEntityException       if the library client is not valid
   * @throws ConflictingResourceException if the library client already exists
   * @throws IOException
   */
  OAuthClientDetailsDTO createOAuthClientDetails(LibraryClientResponseDTO libraryClient,
      String username) throws InvalidEntityException, ConflictingResourceException, IOException;

}

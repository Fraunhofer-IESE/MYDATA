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

package de.fraunhofer.iese.mydata.connectors.rest;

import de.fraunhofer.iese.mydata.affiliation.Affiliation;
import de.fraunhofer.iese.mydata.affiliation.AffiliationId;
import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.client.LibraryClient;
import de.fraunhofer.iese.mydata.client.SyncNotification;
import de.fraunhofer.iese.mydata.client.dto.LibraryClientRequestDTO;
import de.fraunhofer.iese.mydata.client.dto.LibraryClientResponseDTO;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.connector.Authentication;
import de.fraunhofer.iese.mydata.component.connector.Connector;
import de.fraunhofer.iese.mydata.component.connector.OAuthCredentials;
import de.fraunhofer.iese.mydata.component.interfaces.IManagementService;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.ForbiddenException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.MessagingException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.oauth.PasswordResetToken;
import de.fraunhofer.iese.mydata.oauth.dto.OAuthClientDetailsDTO;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.solution.Solution;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.solution.Timezone;
import de.fraunhofer.iese.mydata.timer.TimerId;
import de.fraunhofer.iese.mydata.user.MyDataRole;
import de.fraunhofer.iese.mydata.user.User;
import de.fraunhofer.iese.mydata.user.dto.UpdatePasswordDTO;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Rest based connector interface for the PMP service.
 */
@Connector(protocol = {
    "http", "https"
}, type = ComponentType.MS)
public class ManagementServiceRestConnector extends PmpRestConnector implements IManagementService {

  private static final String REST_INTERFACE_DOES_NOT_SUPPORT_THIS_METHOD = "REST Interface does not support this method.";

  private static final Logger log = LoggerFactory.getLogger(ManagementServiceRestConnector.class);

  private static final String PARAM_AFFILIATION_ID = "affiliation-id";

  private static final String PARAM_INCLUDE_LOCKED = "include-locked";

  private static final String PARAM_POLICY_ID = "policy-id";

  private static final String PARAM_SOLUTION_ID = "solution-id";

  private static final String PARAM_TIMER_ID = "timer-id";

  private static final String PARAM_TOKEN = "token";

  private static final String PARAM_USER_ID = "user-id";

  private static final String PARAM_USERNAME = "username";

  private static final String PARAM_ZONE_ID = "zone-id";

  private static final String USERS_PREFIX = "users";

  private static final String USER_IDS_PREFIX = "user-ids";

  private static final String LIBRARY_CLIENTS_PREFIX = "library-clients";

  private static final String OAUTH_CLIENTS_PREFIX = "oauthclients";

  private static final String SELF_SERVICE_PREFIX = "self-service";

  private static final String TIMEZONE_PREFIX = "timezones";

  /**
   * Constructor of {@link #ManagementServiceRestConnector} which uses an uri string to identify the
   * component.
   *
   * @param  uri                      Used to identify the component.
   * @throws RemoteException          in case of a communication error.
   * @throws IllegalArgumentException in case of a wrong or unknown uri.
   */
  public ManagementServiceRestConnector(String uri) throws RemoteException {
    this(URI.create(uri));
  }

  /**
   * Constructor of {@link #ManagementServiceRestConnector} which uses an uri string to identify the
   * component.
   *
   * @param  uri                      Used to identify the component.
   * @param  credentials              {@link Authentication} with client_id and secret
   * @throws RemoteException          in case of a communication error.
   * @throws IllegalArgumentException in case of a wrong or unknown uri.
   */
  public ManagementServiceRestConnector(final String uri, final Authentication credentials)
      throws RemoteException {
    this(URI.create(uri), credentials);
  }

  /**
   * Constructor of {@link #ManagementServiceRestConnector} which uses an uri string and oauth
   * credentials to identify the component.
   *
   * @param  uri                      Used to identify the component.
   * @param  credentials              {@link OAuthCredentials} with client_id and secret
   * @throws RemoteException          in case of a communication error.
   * @throws IllegalArgumentException in case of a wrong or unknown uri.
   */
  public ManagementServiceRestConnector(String uri, OAuthCredentials credentials)
      throws RemoteException {
    this(URI.create(uri), credentials);
  }

  /**
   * Constructor of {@link #ManagementServiceRestConnector} which uses (instead of an uri) a scheme,
   * host, name string as well as port to identify the component.
   *
   * @param scheme used scheme of the component (e.g. http). See also {@link URI#getScheme()}
   * @param host   hostname of the component. See also {@link URI#getHost()}
   * @param port   port of the component. See also {@link URI#getPort()}
   * @param name   An identifying name of the component.
   */
  public ManagementServiceRestConnector(String scheme, String host, int port, String name) {
    super(scheme, host, port, name);
  }

  /**
   * Constructor of {@link #ManagementServiceRestConnector} which uses an uri object to identify the
   * component.
   *
   * @param  uri             Used to identify the component.
   * @throws RemoteException in case of a communication error.
   */
  public ManagementServiceRestConnector(URI uri) throws RemoteException {
    super(uri);
  }

  /**
   * Constructor of {@link #ManagementServiceRestConnector} which uses an uri object to identify the
   * component.
   *
   * @param  uri             Used to identify the component.
   * @param  credentials     {@link Authentication} with client_id and secret
   * @throws RemoteException in case of a communication error.
   */
  public ManagementServiceRestConnector(URI uri, final Authentication credentials)
      throws RemoteException {
    super(uri, credentials);
  }

  /**
   * Constructor of {@link #ManagementServiceRestConnector} which uses an uri object and oauth
   * credentials to identify the component.
   *
   * @param  uri             Used to identify the component.
   * @param  credentials     {@link OAuthCredentials} with client_id and secret
   * @throws RemoteException in case of a communication error.
   */
  public ManagementServiceRestConnector(URI uri, OAuthCredentials credentials)
      throws RemoteException {
    super(uri, credentials);
  }

  @Override
  public SolutionId addSolution(Solution solution, AffiliationId affiliationId)
      throws IOException, InvalidEntityException, ConflictingResourceException,
      ResourceUpdateException, NoSuchEntityException { // checked
    try {
      return this.httpClient.postForObject(
          this.getBaseUrl() + AFFILIATION_PREFIX + "/" + affiliationId.getUrn() + "/solution",
          solution, SolutionId.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Solution getSolution(SolutionId solutionId)
      throws InvalidEntityException, IOException, NoSuchEntityException { // checked
    try {
      return this.httpClient.getForObject(this.getBaseUrl() + SOLUTION_PREFIX + "/" + solutionId,
          Solution.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public ZoneId getZoneId(SolutionId solutionId)
      throws IOException, NoSuchEntityException, InvalidEntityException {// checked
    try {
      final String s = this.httpClient.getForObject(
          this.getBaseUrl() + SOLUTION_PREFIX + "/" + solutionId + "/zoneid", String.class);
      Objects.requireNonNull(s); // TODO check exception handling...
      if ("null".equalsIgnoreCase(s)) {
        return null;
      } else {
        return ZoneId.of(Objects.requireNonNull(s));
      }
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void updateSolution(Solution solution)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    try {
      this.httpClient.put(this.getBaseUrl() + SOLUTION_PREFIX + "/" + solution.getSolutionId(),
          solution);
    } catch (final RestClientException sce) {
      this.handleAndRethrowNoSuchEntityException(sce);
      this.handleAndRethrowNoSuchEntityException(sce);
      this.handleAndRethrowResourceUpdateException(sce);
      throw new IOException(sce);
    }
  }

  @Override
  public Set<SolutionId> listSolutions(AffiliationId affiliationId, boolean includeLocked)
      throws IOException, NoSuchEntityException, InvalidEntityException {// checked
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (affiliationId != null) {
        queryParams.put(PARAM_AFFILIATION_ID, affiliationId.getUrn());
      }
      queryParams.put(PARAM_INCLUDE_LOCKED, includeLocked);
      final SolutionId[] solutionIds = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + SOLUTION_IDS_PREFIX, queryParams)),
          SolutionId[].class);
      return solutionIds != null ? Sets.newHashSet(solutionIds) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<Solution> getSolutions(AffiliationId affiliationId, boolean includeLocked)
      throws IOException, NoSuchEntityException, InvalidEntityException {// checked
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (affiliationId != null) {
        queryParams.put(PARAM_AFFILIATION_ID, affiliationId.getUrn());
      }
      queryParams.put(PARAM_INCLUDE_LOCKED, includeLocked);
      final Solution[] solutions = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + SOLUTION_PREFIX, queryParams)),
          Solution[].class);
      return solutions != null ? Sets.newHashSet(solutions) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<SolutionId> listSolutions(String userId, boolean includeLocked)
      throws IOException, NoSuchEntityException {// checked
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (userId != null) {
        queryParams.put(PARAM_USER_ID, userId);
      }
      queryParams.put(PARAM_INCLUDE_LOCKED, includeLocked);
      final SolutionId[] solutionIds = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + SOLUTION_IDS_PREFIX, queryParams)),
          SolutionId[].class);
      return solutionIds != null ? Sets.newHashSet(solutionIds) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<Solution> getSolutions(String userId, boolean includeLocked)
      throws IOException, NoSuchEntityException {// checked
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (userId != null) {
        queryParams.put(PARAM_USER_ID, userId);
      }
      queryParams.put(PARAM_INCLUDE_LOCKED, includeLocked);
      final Solution[] solutions = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + SOLUTION_PREFIX, queryParams)),
          Solution[].class);
      return solutions != null ? Sets.newHashSet(solutions) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void deleteSolution(SolutionId solutionId)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    try {
      this.httpClient.delete(this.getBaseUrl() + SOLUTION_PREFIX + "/" + solutionId);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }

  }

  @Override
  public void assignUser(SolutionId solutionId, String userUUID)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    try {
      this.httpClient.postForObject(
          this.getBaseUrl() + SOLUTION_PREFIX + "/" + solutionId + "/users", userUUID, Void.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void unassignUser(SolutionId solutionId, String userUUID)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    try {
      this.httpClient
          .delete(this.getBaseUrl() + SOLUTION_PREFIX + "/" + solutionId + "/users/" + userUUID);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public boolean solutionExists(SolutionId solutionId) throws IOException, InvalidEntityException {
    try {
      this.httpClient.headForHeaders(this.getBaseUrl() + SOLUTION_PREFIX + "/" + solutionId);
      return true;
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      try {
        this.handleAndRethrowNoSuchEntityException(httpException);
      } catch (final NoSuchEntityException e) {
        return false;
      }
      throw new IOException(httpException);
    }
  }

  @Override
  public String addUser(User user, AffiliationId affiliationId)
      throws IOException, ConflictingResourceException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException, MessagingException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      queryParams.put(PARAM_AFFILIATION_ID, affiliationId.getUrn());

      return this.httpClient.postForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + USERS_PREFIX, queryParams)), user,
          String.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowMessagingException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public boolean userIdExists(String userId) throws IOException, NoSuchEntityException {
    try {
      this.httpClient.headForHeaders(this.getBaseUrl() + USERS_PREFIX + "/" + userId);
      return true;
    } catch (final HttpStatusCodeException sce) {
      if (sce.getStatusCode() == HttpStatus.NOT_FOUND) {
        return false;
      }
      throw new IllegalArgumentException(sce);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IllegalArgumentException(httpException);
    }
  }

  @Override
  public boolean usernameExists(String username) throws IOException, NoSuchEntityException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      queryParams.put(PARAM_USERNAME, username);

      this.httpClient.headForHeaders(
          URI.create(addQueryParameters(this.getBaseUrl() + USERS_PREFIX, queryParams)));
      return true;
    } catch (final HttpStatusCodeException sce) {
      if (sce.getStatusCode() == HttpStatus.NOT_FOUND) {
        return false;
      }
      throw new IllegalArgumentException(sce);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IllegalArgumentException(httpException);
    }
  }

  @Override
  public void updateUser(User user)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    try {
      this.httpClient.put(this.getBaseUrl() + USERS_PREFIX + "/" + user.getUserUUID(), user);
    } catch (final RestClientException sce) {
      this.handleAndRethrowNoSuchEntityException(sce);
      this.handleAndRethrowNoSuchEntityException(sce);
      this.handleAndRethrowResourceUpdateException(sce);
      throw new IOException(sce);
    }
  }

  @Override
  public User getUser(String userId) throws NoSuchEntityException, IOException {
    try {
      return this.httpClient
          .getForObject(URI.create(this.getBaseUrl() + USERS_PREFIX + "/" + userId), User.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<String> listUsers(boolean includeLocked) throws IOException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      queryParams.put(PARAM_INCLUDE_LOCKED, includeLocked);
      final String[] userIds = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + USER_IDS_PREFIX, queryParams)),
          String[].class);
      return userIds != null ? Sets.newHashSet(userIds) : new HashSet<>();
    } catch (final RestClientException httpException) {
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<String> listUsers(AffiliationId affiliationId, boolean includeLocked)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    return this.listUsersInternal(null, affiliationId, includeLocked);
  }

  @Override
  public Set<String> listUsers(SolutionId solutionId, boolean includeLocked)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    return this.listUsersInternal(solutionId, null, includeLocked);
  }

  private Set<String> listUsersInternal(SolutionId solutionId, AffiliationId affiliationId,
      boolean includeLocked) throws InvalidEntityException, IOException, NoSuchEntityException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (solutionId != null) {
        queryParams.put(PARAM_SOLUTION_ID, solutionId.getUrn());
      }
      if (affiliationId != null) {
        queryParams.put(PARAM_AFFILIATION_ID, affiliationId.getUrn());
      }
      queryParams.put(PARAM_INCLUDE_LOCKED, includeLocked);
      final String[] userIds = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + USER_IDS_PREFIX, queryParams)),
          String[].class);
      return userIds != null ? Sets.newHashSet(userIds) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<User> getUsers(boolean includeLocked) throws IOException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      queryParams.put(PARAM_INCLUDE_LOCKED, includeLocked);
      final User[] users = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + USERS_PREFIX, queryParams)),
          User[].class);
      return users != null ? Sets.newHashSet(users) : new HashSet<>();
    } catch (final RestClientException httpException) {
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<User> getUsers(AffiliationId affiliationId, boolean includeLocked)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    return this.getUsersInternal(null, affiliationId, includeLocked);
  }

  @Override
  public Set<User> getUsers(SolutionId solutionId, boolean includeLocked)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    return this.getUsersInternal(solutionId, null, includeLocked);
  }

  private Set<User> getUsersInternal(SolutionId solutionId, AffiliationId affiliationId,
      boolean includeLocked) throws InvalidEntityException, IOException, NoSuchEntityException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (solutionId != null) {
        queryParams.put(PARAM_SOLUTION_ID, solutionId.getUrn());
      }
      if (affiliationId != null) {
        queryParams.put(PARAM_AFFILIATION_ID, affiliationId.getUrn());
      }
      queryParams.put(PARAM_INCLUDE_LOCKED, includeLocked);
      final User[] users = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + USERS_PREFIX, queryParams)),
          User[].class);
      return users != null ? Sets.newHashSet(users) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void lockUser(String userId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    try {
      this.httpClient.patchForObject(this.getBaseUrl() + USERS_PREFIX + "/" + userId + "/locked",
          Boolean.TRUE, Void.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void unlockUser(String userId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    try {
      this.httpClient.patchForObject(this.getBaseUrl() + USERS_PREFIX + "/" + userId + "/locked",
          Boolean.FALSE, Void.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void deleteUser(String userId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    try {
      this.httpClient.delete(this.getBaseUrl() + USERS_PREFIX + "/" + userId);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void updatePassword(String userId, String oldPassword, String newPassword)
      throws IOException, ResourceUpdateException, NoSuchEntityException, MessagingException {
    final UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO();
    updatePasswordDTO.setCurrentPassword(oldPassword);
    updatePasswordDTO.setNewPassword(newPassword);
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (userId != null) {
        queryParams.put(PARAM_USER_ID, userId);
      }
      this.httpClient.postForObject(
          URI.create(addQueryParameters(
              this.getBaseUrl() + SELF_SERVICE_PREFIX + "/password-update", queryParams)),
          updatePasswordDTO, Void.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      this.handleAndRethrowMessagingException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void setPassword(String userId, String newPassword)
      throws IOException, ResourceUpdateException, NoSuchEntityException, MessagingException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String addSuperAdmin(User user) throws IOException, ConflictingResourceException,
      ResourceUpdateException, InvalidEntityException, MessagingException {
    try {
      return this.httpClient.postForObject(URI.create(this.getBaseUrl() + USERS_PREFIX), user,
          String.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowResourceUpdateException(httpException);
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowMessagingException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public AffiliationId addAffiliation(Affiliation affiliation) throws IOException,
      ConflictingResourceException, ResourceUpdateException, InvalidEntityException {
    try {
      return this.httpClient.postForObject(URI.create(this.getBaseUrl() + AFFILIATION_PREFIX),
          affiliation, AffiliationId.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowResourceUpdateException(httpException);
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public boolean affiliationIdExists(AffiliationId affiliationId)
      throws IOException, InvalidEntityException {
    try {
      this.httpClient
          .headForHeaders(this.getBaseUrl() + AFFILIATION_PREFIX + "/" + affiliationId.getUrn());
      return true;
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      try {
        this.handleAndRethrowNoSuchEntityException(httpException);
      } catch (final NoSuchEntityException e) {
        return false;
      }
      throw new IOException(httpException);
    }
  }

  @Override
  public void updateAffiliation(Affiliation affiliation)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    try {
      this.httpClient.put(
          this.getBaseUrl() + AFFILIATION_PREFIX + "/" + affiliation.getAffiliationId(),
          affiliation);
    } catch (final RestClientException sce) {
      this.handleAndRethrowNoSuchEntityException(sce);
      this.handleAndRethrowNoSuchEntityException(sce);
      this.handleAndRethrowResourceUpdateException(sce);
      throw new IOException(sce);
    }
  }

  @Override
  public Affiliation getAffiliation(AffiliationId affiliationId)
      throws IOException, NoSuchEntityException, InvalidEntityException {
    try {
      return this.httpClient.getForObject(
          this.getBaseUrl() + AFFILIATION_PREFIX + "/" + affiliationId.getUrn(), Affiliation.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<AffiliationId> listAffiliations(boolean includeLocked) throws IOException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      queryParams.put(PARAM_INCLUDE_LOCKED, includeLocked);
      final AffiliationId[] affiliationIds = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + AFFILIATION_IDS_PREFIX, queryParams)),
          AffiliationId[].class);
      return affiliationIds != null ? Sets.newHashSet(affiliationIds) : new HashSet<>();
    } catch (final RestClientException httpException) {
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<Affiliation> getAffiliations(boolean includeLocked) throws IOException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      queryParams.put(PARAM_INCLUDE_LOCKED, includeLocked);
      final Affiliation[] affiliations = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + AFFILIATION_PREFIX, queryParams)),
          Affiliation[].class);
      return affiliations != null ? Sets.newHashSet(affiliations) : new HashSet<>();
    } catch (final RestClientException httpException) {
      throw new IOException(httpException);
    }
  }

  @Override
  public void lockAffiliation(AffiliationId affiliationId)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    try {
      this.httpClient.patchForObject(
          this.getBaseUrl() + AFFILIATION_PREFIX + "/" + affiliationId + "/locked", Boolean.TRUE,
          Void.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void unlockAffiliation(AffiliationId affiliationId)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    try {
      this.httpClient.patchForObject(
          this.getBaseUrl() + AFFILIATION_PREFIX + "/" + affiliationId + "/locked", Boolean.FALSE,
          Void.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowResourceUpdateException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void deleteAffiliation(AffiliationId affiliationId)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    throw new UnsupportedOperationException(); // TODO implement me?
  }

  @Override
  public Affiliation getAffiliationByUserUUID(String userUUID)
      throws IOException, NoSuchEntityException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (userUUID != null) {
        queryParams.put(PARAM_USER_ID, userUUID);
      }
      final Affiliation[] affiliations = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + AFFILIATION_PREFIX, queryParams)),
          Affiliation[].class);
      return affiliations != null ? affiliations[0] : null;
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public AffiliationId getAffiliationIdByUserUUID(String userUUID)
      throws IOException, NoSuchEntityException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (userUUID != null) {
        queryParams.put(PARAM_USER_ID, userUUID);
      }
      final AffiliationId[] affiliationIds = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + AFFILIATION_IDS_PREFIX, queryParams)),
          AffiliationId[].class);
      return affiliationIds != null ? affiliationIds[0] : null;
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public AffiliationId getAffiliationIdBySolutionId(SolutionId solutionId)
      throws InvalidEntityException, IOException, NoSuchEntityException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (solutionId != null) {
        queryParams.put(PARAM_SOLUTION_ID, solutionId.getUrn());
      }
      final AffiliationId[] affiliationIds = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + AFFILIATION_IDS_PREFIX, queryParams)),
          AffiliationId[].class);
      return affiliationIds != null ? affiliationIds[0] : null;
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Affiliation getAffiliationBySolutionId(SolutionId solutionId)
      throws InvalidEntityException, NoSuchEntityException, IOException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (solutionId != null) {
        queryParams.put(PARAM_SOLUTION_ID, solutionId.getUrn());
      }
      final Affiliation[] affiliations = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + AFFILIATION_PREFIX, queryParams)),
          Affiliation[].class);
      return affiliations != null ? affiliations[0] : null;
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void notifySync(SyncNotification syncNotification)
      throws IOException, InvalidEntityException {
    MyDataEntity.validateAndNullCheck(syncNotification);
    try {
      final HttpEntity<SyncNotification> httpEntity = new HttpEntity<>(syncNotification);
      final ResponseEntity<Void> responseEntity = this.httpClient.exchange(this.getBaseUrl()
          + LIBRARY_CLIENTS_PREFIX
          + "/"
          + syncNotification.getClientId().getUrn()
          + "/latest-sync-notification", HttpMethod.PUT, httpEntity, Void.class);
      if (!responseEntity.getStatusCode().is2xxSuccessful()) {
        throw new IOException("Unexpected response status: " + responseEntity.getStatusCode());
      }
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Map<PolicyId, Long> listDeployedPolicyVersions(ClientId clientId)
      throws InvalidEntityException, IOException, NoSuchEntityException {
    MyDataEntity.validateAndNullCheck(clientId);
    try {
      final ParameterizedTypeReference<Map<PolicyId, Long>> responseType = new ParameterizedTypeReference<Map<PolicyId, Long>>() {
      };
      return this.httpClient.exchange(this.getBaseUrl()
          + LIBRARY_CLIENTS_PREFIX
          + "/"
          + clientId.getUrn()
          + "/deployed-policy-versions", HttpMethod.GET, null, responseType).getBody();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Map<ClientId, Long> listClientsForPolicy(PolicyId policyId)
      throws InvalidEntityException, IOException, NoSuchEntityException {
    MyDataEntity.validateAndNullCheck(policyId);
    try {
      final ParameterizedTypeReference<Map<ClientId, Long>> responseType = new ParameterizedTypeReference<Map<ClientId, Long>>() {
      };
      final Map<String, Object> queryParams = new HashMap<>();
      if (policyId != null) {
        queryParams.put(PARAM_POLICY_ID, policyId.getUrn());
      }
      return this.httpClient.exchange(
          URI.create(addQueryParameters(this.getBaseUrl() + LIBRARY_CLIENTS_PREFIX, queryParams)),
          HttpMethod.GET, null, responseType).getBody();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Map<TimerId, Long> listDeployedTimerVersions(ClientId clientId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    MyDataEntity.validateAndNullCheck(clientId);
    try {
      final ParameterizedTypeReference<Map<TimerId, Long>> responseType = new ParameterizedTypeReference<Map<TimerId, Long>>() {
      };
      return this.httpClient.exchange(this.getBaseUrl()
          + LIBRARY_CLIENTS_PREFIX
          + "/"
          + clientId.getUrn()
          + "/deployed-timer-versions", HttpMethod.GET, null, responseType).getBody();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Map<ClientId, Long> listClientsForTimer(TimerId timerId)
      throws IOException, InvalidEntityException, NoSuchEntityException {
    MyDataEntity.validateAndNullCheck(timerId);
    try {
      final ParameterizedTypeReference<Map<ClientId, Long>> responseType = new ParameterizedTypeReference<Map<ClientId, Long>>() {
      };
      final Map<String, Object> queryParams = new HashMap<>();
      if (timerId != null) {
        queryParams.put(PARAM_TIMER_ID, timerId.getUrn());
      }
      return this.httpClient.exchange(
          URI.create(addQueryParameters(this.getBaseUrl() + LIBRARY_CLIENTS_PREFIX, queryParams)),
          HttpMethod.GET, null, responseType).getBody();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public User getUserByName(String username) throws NoSuchEntityException, IOException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (username != null) {
        queryParams.put(PARAM_USERNAME, username);
      }
      final User[] users = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + USERS_PREFIX, queryParams)),
          User[].class);
      return users != null ? users[0] : null;
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void updateRole(String userId, MyDataRole newRole)
      throws NoSuchEntityException, InvalidEntityException, IOException {
    try {
      this.httpClient.patchForObject(this.getBaseUrl() + USERS_PREFIX + "/" + userId + "/role",
          newRole, Void.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public LibraryClientResponseDTO createLibraryClient(String clientIdStr, boolean isMasterClient)
      throws InvalidEntityException, NoSuchEntityException, IOException,
      ConflictingResourceException {
    final LibraryClientRequestDTO libraryClientRequestDTO = new LibraryClientRequestDTO();
    libraryClientRequestDTO.setClientIdAsString(clientIdStr);
    libraryClientRequestDTO.setMaster(isMasterClient);
    try {
      return this.httpClient.postForObject(this.getBaseUrl() + LIBRARY_CLIENTS_PREFIX,
          libraryClientRequestDTO, LibraryClientResponseDTO.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void deleteLibraryClient(ClientId clientId, String username)
      throws NoSuchEntityException, IOException, InvalidEntityException {
    try {
      this.httpClient.delete(this.getBaseUrl() + LIBRARY_CLIENTS_PREFIX + "/" + clientId.getUrn());
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void deleteLibraryClient(String clientIdStr, String username)
      throws InvalidEntityException, NoSuchEntityException, IOException {
    try {
      this.httpClient.delete(this.getBaseUrl() + LIBRARY_CLIENTS_PREFIX + "/" + clientIdStr);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public OAuthClientDetailsDTO createOAuthClientDetails(LibraryClientResponseDTO libraryClient,
      String username) throws InvalidEntityException, ConflictingResourceException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public LibraryClient getLibraryClient(ClientId clientId)
      throws NoSuchEntityException, InvalidEntityException, IOException {
    try {
      return this.httpClient.getForObject(
          this.getBaseUrl() + LIBRARY_CLIENTS_PREFIX + "/" + clientId.getUrn(),
          LibraryClient.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public Set<LibraryClient> getLibraryClients(SolutionId solutionId)
      throws NoSuchEntityException, InvalidEntityException, IOException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (solutionId != null) {
        queryParams.put(PARAM_SOLUTION_ID, solutionId.getUrn());
      }
      final LibraryClient[] libraryClients = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + LIBRARY_CLIENTS_PREFIX, queryParams)),
          LibraryClient[].class);
      return libraryClients != null ? Sets.newHashSet(libraryClients) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowInvalidEntityException(httpException);
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public PasswordResetToken generateToken(String username)
      throws IOException, NoSuchEntityException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (username != null) {
        queryParams.put(PARAM_USERNAME, username);
      }
      return this.httpClient
          .postForObject(
              URI.create(addQueryParameters(
                  this.getBaseUrl() + SELF_SERVICE_PREFIX + "/password-reset", queryParams)),
              null, PasswordResetToken.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public PasswordResetToken getToken(String token) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PasswordResetToken getTokenForUser(User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void validateToken(String userId, String token)
      throws IOException, ForbiddenException, NoSuchEntityException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      queryParams.put(PARAM_USER_ID, userId);
      queryParams.put(PARAM_TOKEN, token);
      this.httpClient.getForObject(URI.create(
          addQueryParameters(this.getBaseUrl() + SELF_SERVICE_PREFIX + "/password", queryParams)),
          Void.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowForbiddenException(httpException);
      throw new IOException(httpException);
    }

  }

  @Override
  public void validateTokenAndSetPassword(String userId, String token, String newPassword)
      throws NoSuchEntityException, ForbiddenException, IOException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (userId != null) {
        queryParams.put(PARAM_USER_ID, userId);
      }
      if (token != null) {
        queryParams.put(PARAM_TOKEN, token);
      }
      this.httpClient.postForObject(URI.create(
          addQueryParameters(this.getBaseUrl() + SELF_SERVICE_PREFIX + "/password", queryParams)),
          newPassword, Void.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      this.handleAndRethrowForbiddenException(httpException);
      throw new IOException(httpException);
    }
  }

  @Override
  public void deleteToken(String token) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteTokenByUserUuid(String userUuid) throws IOException, NoSuchEntityException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Timezone> getTimezones() throws IOException {
    try {
      final Timezone[] timezones = this.httpClient.getForObject(this.getBaseUrl() + TIMEZONE_PREFIX,
          Timezone[].class);
      return timezones == null ? Collections.emptyList() : Arrays.asList(timezones);
    } catch (final RestClientException httpException) {
      throw new IOException(httpException);
    }
  }

  @Override
  public Optional<Timezone> getTimezoneByZoneId(String zoneId)
      throws NoSuchEntityException, IOException {
    try {
      final Timezone timezone = this.httpClient
          .getForObject(this.getBaseUrl() + TIMEZONE_PREFIX + "/" + zoneId, Timezone.class);
      return Optional.ofNullable(timezone);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  /**
   * Add a new oauth client
   * 
   * @param  oauthClientsDetailsDTO       client dto
   * @return                              oauth client details with the generated client secret
   * @throws ConflictingResourceException if a client with the same id exists
   * @throws InvalidEntityException       if the client details are not valid
   * @throws IOException
   */
  public OAuthClientDetailsDTO addOAuthClientDetails(OAuthClientDetailsDTO oauthClientsDetailsDTO)
      throws ConflictingResourceException, InvalidEntityException, IOException {
    try {
      return this.httpClient.postForObject(this.getBaseUrl() + OAUTH_CLIENTS_PREFIX,
          oauthClientsDetailsDTO, OAuthClientDetailsDTO.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowConflictingResourceException(httpException);
      this.handleAndRethrowInvalidEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  /**
   * Get a oauth client by id
   * 
   * @param  oAuthClientId         the client id
   * @return                       the oauth client response dto without secret
   * @throws NoSuchEntityException if the client does not exist
   * @throws IOException
   */
  public OAuthClientDetailsDTO getOAuthClientDetails(String oAuthClientId)
      throws NoSuchEntityException, IOException {
    try {
      return this.httpClient.getForObject(
          this.getBaseUrl() + OAUTH_CLIENTS_PREFIX + "/" + oAuthClientId,
          OAuthClientDetailsDTO.class);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  /**
   * Return all oauth client details, optionally filtered by solution id
   * 
   * @param  solutionId            the solution id, can be null
   * @return                       List of Oauth Client Details without client secrets
   * @throws NoSuchEntityException if the solution does not exist
   * @throws IOException
   */
  public Set<OAuthClientDetailsDTO> getAllOAuthClientDetails(String solutionId)
      throws NoSuchEntityException, IOException {
    try {
      final Map<String, Object> queryParams = new HashMap<>();
      if (solutionId != null) {
        queryParams.put(PARAM_SOLUTION_ID, solutionId);
      }
      final OAuthClientDetailsDTO[] clients = this.httpClient.getForObject(
          URI.create(addQueryParameters(this.getBaseUrl() + OAUTH_CLIENTS_PREFIX, queryParams)),
          OAuthClientDetailsDTO[].class);
      return clients != null ? Sets.newHashSet(clients) : new HashSet<>();
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

  /**
   * Delete an oauth client by id
   * 
   * @param  clientId              The id of the client to be deleted
   * @throws NoSuchEntityException if the client does not exist
   * @throws IOException
   */
  public void deleteOAuthClient(String clientId) throws NoSuchEntityException, IOException {
    try {
      this.httpClient.delete(this.getBaseUrl() + OAUTH_CLIENTS_PREFIX + "/" + clientId);
    } catch (final RestClientException httpException) {
      this.handleAndRethrowNoSuchEntityException(httpException);
      throw new IOException(httpException);
    }
  }

}

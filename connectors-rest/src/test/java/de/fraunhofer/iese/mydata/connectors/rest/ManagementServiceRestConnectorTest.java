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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import de.fraunhofer.iese.mydata.affiliation.Affiliation;
import de.fraunhofer.iese.mydata.affiliation.AffiliationId;
import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.client.LibraryClient;
import de.fraunhofer.iese.mydata.client.SyncNotification;
import de.fraunhofer.iese.mydata.client.dto.LibraryClientRequestDTO;
import de.fraunhofer.iese.mydata.client.dto.LibraryClientResponseDTO;
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
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class ManagementServiceRestConnectorTest extends CommonTestSetup {
  @Mock
  protected RestTemplate mockRestTemplate;

  private static final String AFFILIATIONS_URL_PREFIX = "affiliations";

  private static final String AFFILIATION_IDS_URL_PREFIX = "affiliation-ids";

  private static final String SELF_SERVICE_URL_PREFIX = "self-service";

  private static final String LIBRARY_CLIENTS_URL_PREFIX = "library-clients";

  private static final String OAUTH_CLIENT_URL_PREFIX = "oauthclients";

  private static final String SOLUTIONS_URL_PREFIX = "solutions";

  private static final String SOLUTION_IDS_URL_PREFIX = "solution-ids";

  private static final String TIMEZONE_URL_PREFIX = "timezones";

  private static final String USERS_URL_PREFIX = "users";

  private static final String USER_IDS_URL_PREFIX = "user-ids";

  private static final RestClientException REST_CLIENT_EXCEPTION = new RestClientException(
      "Intended Test Exception");

  private static final HttpServerErrorException HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND = new HttpServerErrorException(
      HttpStatus.NOT_FOUND, "Intended Test Exception");

  private static final AffiliationId AFFILIATION_ID = new AffiliationId("urn:affiliation:test");

  private static final SolutionId SOLUTION_ID = new SolutionId("urn:solution:test");

  private static final SolutionId SOLUTION_ID_2 = new SolutionId("urn:solution:test-2");

  private static final ClientId LIBRARY_CLIENT_ID = new ClientId("urn:client:test:master-client");

  private ManagementServiceRestConnector connector;

  private Solution solution, solution2;

  private User user;

  private Affiliation affiliation;

  private LibraryClient libraryClient;

  @BeforeEach
  public void prepare() throws IllegalArgumentException, UnsupportedOperationException, IOException,
      InvalidEntityException {
    super.setUp();
    this.connector = new ManagementServiceRestConnector(Constants.BASE_URL);
    setInternalState(this.connector, "httpClient", this.mockRestTemplate);

    this.solution = new Solution(SOLUTION_ID);
    this.solution.setName("Test Solution");
    this.solution.setLockStatus(false);

    this.solution2 = new Solution(SOLUTION_ID_2);
    this.solution2.setName("Test Solution 2");
    this.solution2.setLockStatus(false);

    this.user = new User();
    this.user.setUsername("test-user");
    this.user.setAccountLocked(false);
    this.user.setRole(MyDataRole.SOLUTION_DEVELOPER);

    this.affiliation = new Affiliation(AFFILIATION_ID);
    this.affiliation.setName("test-affiliation");

    this.libraryClient = new LibraryClient(LIBRARY_CLIENT_ID);
    this.libraryClient.setSolution(this.solution);
    this.libraryClient.setMaster(true);
  }

  @Test
  public void addSolution() throws RestClientException, IOException, InvalidEntityException,
      ConflictingResourceException, ResourceUpdateException, NoSuchEntityException {
    when(this.mockRestTemplate.postForObject(
        Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "/" + AFFILIATION_ID.getUrn() + "/solution",
        this.solution, SolutionId.class)).thenReturn(SOLUTION_ID);

    final SolutionId result = this.connector.addSolution(this.solution, AFFILIATION_ID);

    Mockito.verify(this.mockRestTemplate).postForObject(
        Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "/" + AFFILIATION_ID.getUrn() + "/solution",
        this.solution, SolutionId.class);
    Assertions.assertEquals(SOLUTION_ID, result);
  }

  @Test
  public void addSolutionThrowsException()
      throws RestClientException, InvalidEntityException, ConflictingResourceException,
      ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.postForObject(
          Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "/" + AFFILIATION_ID.getUrn() + "/solution",
          this.solution, SolutionId.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.addSolution(this.solution, AFFILIATION_ID);
      Assertions.fail();
    });
  }

  @Test
  public void getSolution() throws InvalidEntityException, IOException, NoSuchEntityException {
    when(this.mockRestTemplate.getForObject(
        Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "/" + SOLUTION_ID.getUrn(), Solution.class))
        .thenReturn(this.solution);

    final Solution result = this.connector.getSolution(SOLUTION_ID);

    Mockito.verify(this.mockRestTemplate).getForObject(
        Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "/" + SOLUTION_ID.getUrn(), Solution.class);
    Assertions.assertEquals(this.solution, result);
  }

  @Test
  public void getSolutionThrowsException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      when(this.mockRestTemplate.getForObject(
          Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "/" + SOLUTION_ID.getUrn(), Solution.class))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getSolution(SOLUTION_ID);
      Assertions.fail();
    });
  }

  @Test
  public void getZoneId() throws IOException, NoSuchEntityException, InvalidEntityException {
    final ZoneId zoneId = ZoneId.of("Europe/Berlin");
    final String url = Constants.BASE_URL
        + SOLUTIONS_URL_PREFIX
        + "/"
        + SOLUTION_ID.getUrn()
        + "/zoneid";
    when(this.mockRestTemplate.getForObject(url, String.class)).thenReturn(zoneId.getId());
    final ZoneId result = this.connector.getZoneId(SOLUTION_ID);
    Mockito.verify(this.mockRestTemplate).getForObject(url, String.class);
    Assertions.assertEquals(zoneId, result);
  }

  @Test
  public void getZoneIdThrowsRestClientExceptio()
      throws NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL
          + SOLUTIONS_URL_PREFIX
          + "/"
          + SOLUTION_ID.getUrn()
          + "/zoneid";
      when(this.mockRestTemplate.getForObject(url, String.class)).thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getZoneId(SOLUTION_ID);
      Assertions.fail();
    });
  }

  @Test
  public void updateSolution() throws IOException, ResourceUpdateException, NoSuchEntityException {
    final Solution updatedSolution = this.solution;
    updatedSolution.setName("Updated solution");

    this.connector.updateSolution(updatedSolution);

    Mockito.verify(this.mockRestTemplate).put(
        Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "/" + SOLUTION_ID.getUrn(), updatedSolution);
  }

  @Test
  public void updateSolutionThrowsException()
      throws ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final Solution updatedSolution = this.solution;
      updatedSolution.setName("Updated solution");
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).put(
          Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "/" + SOLUTION_ID.getUrn(), updatedSolution);

      this.connector.updateSolution(updatedSolution);
      Assertions.fail();
    });
  }

  @Test
  public void listSolutionsByAffiliation()
      throws IOException, NoSuchEntityException, InvalidEntityException {
    final SolutionId[] solutionIdsArray = {
        SOLUTION_ID, SOLUTION_ID_2
    };

    final String url = Constants.BASE_URL
        + SOLUTION_IDS_URL_PREFIX
        + "?affiliation-id="
        + AFFILIATION_ID.getUrn()
        + "&include-locked=true";
    when(this.mockRestTemplate.getForObject(URI.create(url), SolutionId[].class))
        .thenReturn(solutionIdsArray);

    final Set<SolutionId> result = this.connector.listSolutions(AFFILIATION_ID, true);

    Mockito.verify(this.mockRestTemplate).getForObject(URI.create(url), SolutionId[].class);
    Assertions.assertEquals(Sets.newHashSet(solutionIdsArray), result);
  }

  @Test
  public void listSolutionsByAffiliationThrowsRestClientException()
      throws NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL
          + SOLUTION_IDS_URL_PREFIX
          + "?affiliation-id="
          + AFFILIATION_ID.getUrn()
          + "&include-locked=true";
      when(this.mockRestTemplate.getForObject(URI.create(url), SolutionId[].class))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.listSolutions(AFFILIATION_ID, true);
      Assertions.fail();
    });
  }

  @Test
  public void listSolutionsByUserId() throws IOException, NoSuchEntityException {
    final SolutionId[] solutionIdsArray = {
        SOLUTION_ID, SOLUTION_ID_2
    };

    final String userId = UUID.randomUUID().toString();

    final String url = Constants.BASE_URL
        + SOLUTION_IDS_URL_PREFIX
        + "?user-id="
        + userId
        + "&include-locked=true";
    when(this.mockRestTemplate.getForObject(URI.create(url), SolutionId[].class))
        .thenReturn(solutionIdsArray);

    final Set<SolutionId> result = this.connector.listSolutions(userId, true);

    Mockito.verify(this.mockRestTemplate).getForObject(URI.create(url), SolutionId[].class);
    Assertions.assertEquals(Sets.newHashSet(solutionIdsArray), result);
  }

  @Test
  public void listSolutionsByUserIdThrowsRestClientException()
      throws NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String userId = UUID.randomUUID().toString();

      final String url = Constants.BASE_URL
          + SOLUTION_IDS_URL_PREFIX
          + "?user-id="
          + userId
          + "&include-locked=true";
      when(this.mockRestTemplate.getForObject(URI.create(url), SolutionId[].class))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.listSolutions(userId, true);
      Assertions.fail();
    });
  }

  @Test
  public void getSolutionsByAffiliationId()
      throws IOException, NoSuchEntityException, InvalidEntityException {
    final Solution[] solutions = {
        this.solution, this.solution2
    };

    final URI uri = URI.create(Constants.BASE_URL
        + SOLUTIONS_URL_PREFIX
        + "?affiliation-id="
        + AFFILIATION_ID.getUrn()
        + "&include-locked=true");
    when(this.mockRestTemplate.getForObject(uri, Solution[].class)).thenReturn(solutions);

    final Set<Solution> result = this.connector.getSolutions(AFFILIATION_ID, true);

    Mockito.verify(this.mockRestTemplate).getForObject(uri, Solution[].class);
    Assertions.assertEquals(Sets.newHashSet(solutions), result);
  }

  @Test
  public void getSolutionsByAffiliationIdThrowsRestClientException()
      throws NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {

      final URI uri = URI.create(Constants.BASE_URL
          + SOLUTIONS_URL_PREFIX
          + "?affiliation-id="
          + AFFILIATION_ID.getUrn()
          + "&include-locked=true");
      when(this.mockRestTemplate.getForObject(uri, Solution[].class))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getSolutions(AFFILIATION_ID, true);
      Assertions.fail();
    });
  }

  @Test
  public void getSolutionsByUserId()
      throws IOException, NoSuchEntityException, InvalidEntityException {
    final Solution[] solutions = {
        this.solution, this.solution2
    };

    final String userId = UUID.randomUUID().toString();

    final URI uri = URI.create(
        Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "?user-id=" + userId + "&include-locked=true");
    when(this.mockRestTemplate.getForObject(uri, Solution[].class)).thenReturn(solutions);

    final Set<Solution> result = this.connector.getSolutions(userId, true);

    Mockito.verify(this.mockRestTemplate).getForObject(uri, Solution[].class);
    Assertions.assertEquals(Sets.newHashSet(solutions), result);
  }

  @Test
  public void getSolutionsByUserIdThrowsRestClientException()
      throws NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String userId = UUID.randomUUID().toString();
      final URI uri = URI.create(
          Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "?user-id=" + userId + "&include-locked=true");
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).getForObject(uri,
          Solution[].class);
      this.connector.getSolutions(userId, true);
      Assertions.fail();
    });
  }

  @Test
  public void deleteSolution()
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    this.connector.deleteSolution(SOLUTION_ID);

    Mockito.verify(this.mockRestTemplate)
        .delete(Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "/" + SOLUTION_ID);
  }

  @Test
  public void deleteSolutionThrowsRestClientException()
      throws ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate)
          .delete(Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "/" + SOLUTION_ID);
      this.connector.deleteSolution(SOLUTION_ID);
      Assertions.fail();
    });
  }

  @Test
  public void assignUser() throws IOException, ResourceUpdateException, NoSuchEntityException {
    final String userId = UUID.randomUUID().toString();

    this.connector.assignUser(SOLUTION_ID, userId);

    Mockito.verify(this.mockRestTemplate).postForObject(
        Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "/" + SOLUTION_ID + "/users", userId,
        Void.class);
  }

  @Test
  public void assignUserThrowsException()
      throws ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String userId = UUID.randomUUID().toString();

      when(this.mockRestTemplate.postForObject(
          Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "/" + SOLUTION_ID + "/users", userId,
          Void.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.assignUser(SOLUTION_ID, userId);
      Assertions.fail();
    });
  }

  @Test
  public void unassignUser() throws IOException, ResourceUpdateException, NoSuchEntityException {
    final String userId = UUID.randomUUID().toString();

    this.connector.unassignUser(SOLUTION_ID, userId);

    Mockito.verify(this.mockRestTemplate)
        .delete(Constants.BASE_URL
            + SOLUTIONS_URL_PREFIX
            + "/"
            + SOLUTION_ID.getUrn()
            + "/users/"
            + userId);
  }

  @Test
  public void unassignUserThrowsException()
      throws ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String userId = UUID.randomUUID().toString();
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate)
          .delete(Constants.BASE_URL
              + SOLUTIONS_URL_PREFIX
              + "/"
              + SOLUTION_ID.getUrn()
              + "/users/"
              + userId);

      this.connector.unassignUser(SOLUTION_ID, userId);
      Assertions.fail();
    });
  }

  @Test
  public void solutionExists() throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "/" + SOLUTION_ID.getUrn();
    final boolean result = this.connector.solutionExists(SOLUTION_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertEquals(true, result);
  }

  @Test
  public void solutionExists_solutionDoesNotExists() throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "/" + SOLUTION_ID.getUrn();
    when(this.mockRestTemplate.headForHeaders(url))
        .thenThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND);

    final boolean result = this.connector.solutionExists(SOLUTION_ID);
    Assertions.assertEquals(false, result);
  }

  @Test
  public void solutionExistsThrowsRestClientException() throws InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + SOLUTIONS_URL_PREFIX + "/" + SOLUTION_ID.getUrn();
      when(this.mockRestTemplate.headForHeaders(url)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.solutionExists(SOLUTION_ID);
      Assertions.fail();
    });
  }

  @Test
  public void addUser() throws IOException, ConflictingResourceException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException, MessagingException {

    final String userId = UUID.randomUUID().toString();
    final URI uri = URI.create(
        Constants.BASE_URL + USERS_URL_PREFIX + "?affiliation-id=" + AFFILIATION_ID.getUrn());

    when(this.mockRestTemplate.postForObject(uri, this.user, String.class)).thenReturn(userId);

    final String result = this.connector.addUser(this.user, AFFILIATION_ID);

    Mockito.verify(this.mockRestTemplate).postForObject(uri, this.user, String.class);
    Assertions.assertEquals(userId, result);
  }

  @Test
  public void addUserThrowsException() throws ConflictingResourceException,
      ResourceUpdateException, InvalidEntityException, NoSuchEntityException, MessagingException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI.create(
          Constants.BASE_URL + USERS_URL_PREFIX + "?affiliation-id=" + AFFILIATION_ID.getUrn());

      when(this.mockRestTemplate.postForObject(uri, this.user, String.class))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.addUser(this.user, AFFILIATION_ID);
      Assertions.fail();
    });
  }

  @Test
  public void userIdExists() throws NoSuchEntityException, IOException {
    final String userId = UUID.randomUUID().toString();
    final String url = Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId;

    final boolean result = this.connector.userIdExists(userId);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertEquals(true, result);
  }

  @Test
  public void userIdExists_userDoesNotExist() throws NoSuchEntityException, IOException {
    final String userId = UUID.randomUUID().toString();
    final String url = Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId;
    when(this.mockRestTemplate.headForHeaders(url))
        .thenThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND);

    final boolean result = this.connector.userIdExists(userId);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertEquals(false, result);
  }

  @Test
  public void userIdExistsThrowsException() throws NoSuchEntityException, IOException {
    assertThrows(IllegalArgumentException.class, () -> {
      final String userId = UUID.randomUUID().toString();
      final String url = Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId;
      when(this.mockRestTemplate.headForHeaders(url)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.userIdExists(userId);
      Assertions.fail();
    });
  }

  @Test
  public void usernameExists() throws NoSuchEntityException, IOException {
    final String username = this.user.getUsername();
    final URI uri = URI.create(Constants.BASE_URL + USERS_URL_PREFIX + "?username=" + username);

    final boolean result = this.connector.usernameExists(username);
    Mockito.verify(this.mockRestTemplate).headForHeaders(uri);
    Assertions.assertEquals(true, result);
  }

  @Test
  public void usernameExists_userDoesNotExist() throws NoSuchEntityException, IOException {
    final String username = this.user.getUsername();
    final URI uri = URI.create(Constants.BASE_URL + USERS_URL_PREFIX + "?username=" + username);
    when(this.mockRestTemplate.headForHeaders(uri))
        .thenThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND);

    final boolean result = this.connector.usernameExists(username);
    Mockito.verify(this.mockRestTemplate).headForHeaders(uri);
    Assertions.assertEquals(false, result);
  }

  @Test
  public void usernameExistsThrowsException() throws NoSuchEntityException, IOException {
    assertThrows(IllegalArgumentException.class, () -> {
      final String username = this.user.getUsername();
      final URI uri = URI.create(Constants.BASE_URL + USERS_URL_PREFIX + "?username=" + username);
      when(this.mockRestTemplate.headForHeaders(uri)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.usernameExists(username);
      Assertions.fail();
    });
  }

  @Test
  public void updateUser()
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    final String userId = UUID.randomUUID().toString();
    final User updatedUser = this.user;
    updatedUser.setUserUUID(userId);
    final String url = Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId;
    this.connector.updateUser(this.user);
    Mockito.verify(this.mockRestTemplate).put(url, updatedUser);
  }

  @Test
  public void updateUserThrowsException()
      throws ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String userId = UUID.randomUUID().toString();
      final User updatedUser = this.user;
      updatedUser.setUserUUID(userId);
      final String url = Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId;
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).put(url, updatedUser);
      this.connector.updateUser(this.user);
      Assertions.fail();
    });
  }

  @Test
  public void getUser() throws IllegalArgumentException, NoSuchEntityException, IOException {
    final String userId = UUID.randomUUID().toString();
    final URI uri = URI.create(Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId);

    when(this.mockRestTemplate.getForObject(uri, User.class)).thenReturn(this.user);

    final User result = this.connector.getUser(userId);
    Mockito.verify(this.mockRestTemplate).getForObject(uri, User.class);
    Assertions.assertEquals(this.user, result);
  }

  @Test
  public void getUserThrowsException()
      throws IllegalArgumentException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String userId = UUID.randomUUID().toString();
      final URI uri = URI.create(Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId);

      when(this.mockRestTemplate.getForObject(uri, User.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getUser(userId);
      Assertions.fail();
    });
  }

  @Test
  public void getUserByUsername() throws NoSuchEntityException, IOException {
    final URI uri = URI
        .create(Constants.BASE_URL + USERS_URL_PREFIX + "?username=" + this.user.getUsername());

    when(this.mockRestTemplate.getForObject(uri, User[].class))
        .thenReturn(ArrayUtils.toArray(this.user));

    final User result = this.connector.getUserByName(this.user.getUsername());
    Mockito.verify(this.mockRestTemplate).getForObject(uri, User[].class);
    Assertions.assertEquals(this.user, result);
  }

  @Test
  public void getUserByUsernameThrowsException() throws NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI
          .create(Constants.BASE_URL + USERS_URL_PREFIX + "?username=" + this.user.getUsername());

      when(this.mockRestTemplate.getForObject(uri, User[].class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getUserByName(this.user.getUsername());
      Assertions.fail();
    });
  }

  @Test
  public void listUsersByAffiliationId()
      throws IOException, NoSuchEntityException, InvalidEntityException {

    final String[] userIds = {
        UUID.randomUUID().toString(), UUID.randomUUID().toString()
    };

    final URI uri = URI.create(Constants.BASE_URL
        + USER_IDS_URL_PREFIX
        + "?affiliation-id="
        + AFFILIATION_ID.getUrn()
        + "&include-locked=true");

    when(this.mockRestTemplate.getForObject(uri, String[].class)).thenReturn(userIds);

    final Set<String> result = this.connector.listUsers(AFFILIATION_ID, true);
    Mockito.verify(this.mockRestTemplate).getForObject(uri, String[].class);
    Assertions.assertEquals(Sets.newHashSet(userIds), result);
  }

  @Test
  public void listUsersByAffiliationIdThrowsRestClientException()
      throws NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI.create(Constants.BASE_URL
          + USER_IDS_URL_PREFIX
          + "?affiliation-id="
          + AFFILIATION_ID.getUrn()
          + "&include-locked=true");

      when(this.mockRestTemplate.getForObject(uri, String[].class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.listUsers(AFFILIATION_ID, true);
      Assertions.fail();
    });
  }

  @Test
  public void listUsersBySolutionId()
      throws IOException, NoSuchEntityException, InvalidEntityException {
    final String[] userIds = {
        UUID.randomUUID().toString(), UUID.randomUUID().toString()
    };

    final URI uri = URI.create(Constants.BASE_URL
        + USER_IDS_URL_PREFIX
        + "?solution-id="
        + SOLUTION_ID.getUrn()
        + "&include-locked=true");

    when(this.mockRestTemplate.getForObject(uri, String[].class)).thenReturn(userIds);

    final Set<String> result = this.connector.listUsers(SOLUTION_ID, true);
    Mockito.verify(this.mockRestTemplate).getForObject(uri, String[].class);
    Assertions.assertEquals(Sets.newHashSet(userIds), result);
  }

  @Test
  public void listUsersBySolutionIdThrowsRestClientException()
      throws NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {

      final URI uri = URI.create(Constants.BASE_URL
          + USER_IDS_URL_PREFIX
          + "?solution-id="
          + SOLUTION_ID.getUrn()
          + "&include-locked=true");

      when(this.mockRestTemplate.getForObject(uri, String[].class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.listUsers(SOLUTION_ID, true);
      Assertions.fail();
    });
  }

  @Test
  public void getUsersByAffiliationId()
      throws IOException, NoSuchEntityException, InvalidEntityException {
    final User[] users = {
        this.user
    };

    final URI uri = URI.create(Constants.BASE_URL
        + USERS_URL_PREFIX
        + "?affiliation-id="
        + AFFILIATION_ID.getUrn()
        + "&include-locked=true");

    when(this.mockRestTemplate.getForObject(uri, User[].class)).thenReturn(users);

    final Set<User> result = this.connector.getUsers(AFFILIATION_ID, true);
    Mockito.verify(this.mockRestTemplate).getForObject(uri, User[].class);
    Assertions.assertEquals(Sets.newHashSet(users), result);
  }

  @Test
  public void getUsersByAffiliationIdThrowsRestClientException()
      throws NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {

      final URI uri = URI.create(Constants.BASE_URL
          + USERS_URL_PREFIX
          + "?affiliation-id="
          + AFFILIATION_ID.getUrn()
          + "&include-locked=true");

      when(this.mockRestTemplate.getForObject(uri, User[].class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getUsers(AFFILIATION_ID, true);
      Assertions.fail();
    });
  }

  @Test
  public void getUsersBySolutionId()
      throws IOException, NoSuchEntityException, InvalidEntityException {
    final User[] users = {
        this.user
    };

    final URI uri = URI.create(Constants.BASE_URL
        + USERS_URL_PREFIX
        + "?solution-id="
        + SOLUTION_ID.getUrn()
        + "&include-locked=true");

    when(this.mockRestTemplate.getForObject(uri, User[].class)).thenReturn(users);

    final Set<User> result = this.connector.getUsers(SOLUTION_ID, true);
    Mockito.verify(this.mockRestTemplate).getForObject(uri, User[].class);
    Assertions.assertEquals(Sets.newHashSet(users), result);
  }

  @Test
  public void getUsersBySolutionIdThrowsRestClientException()
      throws NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI.create(Constants.BASE_URL
          + USERS_URL_PREFIX
          + "?solution-id="
          + SOLUTION_ID.getUrn()
          + "&include-locked=true");

      when(this.mockRestTemplate.getForObject(uri, User[].class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getUsers(SOLUTION_ID, true);
      Assertions.fail();
    });
  }

  @Test
  public void lockUser() throws IOException, ResourceUpdateException, NoSuchEntityException {
    final String userId = UUID.randomUUID().toString();

    this.connector.lockUser(userId);
    Mockito.verify(this.mockRestTemplate).patchForObject(
        Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId + "/locked", Boolean.TRUE, Void.class);
  }

  @Test
  public void lockUserThrowsRestClientException()
      throws ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String userId = UUID.randomUUID().toString();
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).patchForObject(
          Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId + "/locked", Boolean.TRUE, Void.class);
      this.connector.lockUser(userId);
      Assertions.fail();
    });
  }

  @Test
  public void unlockUser() throws IOException, ResourceUpdateException, NoSuchEntityException {
    final String userId = UUID.randomUUID().toString();

    this.connector.unlockUser(userId);
    Mockito.verify(this.mockRestTemplate).patchForObject(
        Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId + "/locked", Boolean.FALSE,
        Void.class);
  }

  @Test
  public void unlockUserThrowsRestClientException()
      throws ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String userId = UUID.randomUUID().toString();
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).patchForObject(
          Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId + "/locked", Boolean.FALSE,
          Void.class);
      this.connector.unlockUser(userId);
      Assertions.fail();
    });
  }

  @Test
  public void deleteUser() throws IOException, ResourceUpdateException, NoSuchEntityException {
    final String userId = UUID.randomUUID().toString();
    final String url = Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId;
    this.connector.deleteUser(userId);
    Mockito.verify(this.mockRestTemplate).delete(url);
  }

  @Test
  public void deleteUserThrowsRestClientException()
      throws ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String userId = UUID.randomUUID().toString();
      final String url = Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId;
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).delete(url);
      this.connector.deleteUser(userId);
      Assertions.fail();
    });
  }

  @Test
  public void updatePassword()
      throws IOException, ResourceUpdateException, NoSuchEntityException, MessagingException {
    final URI uri = URI.create(Constants.BASE_URL
        + SELF_SERVICE_URL_PREFIX
        + "/password-update?user-id="
        + this.user.getUserUUID());
    final String oldPassword = "old1234!";
    final String newPassword = "new1234!";
    final UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO();
    updatePasswordDTO.setCurrentPassword(oldPassword);
    updatePasswordDTO.setNewPassword(newPassword);
    this.connector.updatePassword(this.user.getUserUUID(), oldPassword, newPassword);
    Mockito.verify(this.mockRestTemplate).postForObject(uri, updatePasswordDTO, Void.class);
  }

  @Test
  public void updatePasswordThrowsRestClientException()
      throws ResourceUpdateException, NoSuchEntityException, MessagingException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI.create(Constants.BASE_URL
          + SELF_SERVICE_URL_PREFIX
          + "/password-update?user-id="
          + this.user.getUserUUID());
      final String oldPassword = "old1234!";
      final String newPassword = "new1234!";
      final UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO();
      updatePasswordDTO.setCurrentPassword(oldPassword);
      updatePasswordDTO.setNewPassword(newPassword);
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).postForObject(uri,
          updatePasswordDTO, Void.class);
      this.connector.updatePassword(this.user.getUserUUID(), oldPassword, newPassword);
      Assertions.fail();
    });
  }

  @Test
  public void setPassword()
      throws IOException, ResourceUpdateException, NoSuchEntityException, MessagingException {
    assertThrows(UnsupportedOperationException.class, () ->
        this.connector.setPassword(this.user.getUserUUID(), "not-possible"));
  }

  @Test
  public void addSuperAdmin() throws IOException, ConflictingResourceException,
      ResourceUpdateException, InvalidEntityException, NoSuchEntityException, MessagingException {
    this.user.setRole(MyDataRole.SUPER_ADMIN);
    final String userId = UUID.randomUUID().toString();
    final URI uri = URI.create(Constants.BASE_URL + USERS_URL_PREFIX);

    when(this.mockRestTemplate.postForObject(uri, this.user, String.class)).thenReturn(userId);

    final String result = this.connector.addSuperAdmin(this.user);

    Mockito.verify(this.mockRestTemplate).postForObject(uri, this.user, String.class);
    Assertions.assertEquals(userId, result);
  }

  @Test
  public void addSuperAdminThrowsRestClientException()
      throws ConflictingResourceException, ResourceUpdateException,
      InvalidEntityException, MessagingException {
    assertThrows(IOException.class, () -> {
      this.user.setRole(MyDataRole.SUPER_ADMIN);
      final URI uri = URI.create(Constants.BASE_URL + USERS_URL_PREFIX);

      when(this.mockRestTemplate.postForObject(uri, this.user, String.class))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.addSuperAdmin(this.user);
      Assertions.fail();
    });
  }

  @Test
  public void updateUserRole()
      throws IllegalArgumentException, NoSuchEntityException, InvalidEntityException, IOException {
    final String userId = UUID.randomUUID().toString();
    final MyDataRole newRole = MyDataRole.ADMINISTRATOR;

    this.connector.updateRole(userId, newRole);

    Mockito.verify(this.mockRestTemplate).patchForObject(
        Constants.BASE_URL + USERS_URL_PREFIX + "/" + userId + "/role", newRole, Void.class);
  }

  @Test
  public void addAffiliation() throws IOException, ConflictingResourceException,
      ResourceUpdateException, InvalidEntityException {
    final URI uri = URI.create(Constants.BASE_URL + AFFILIATIONS_URL_PREFIX);
    when(this.mockRestTemplate.postForObject(uri, this.affiliation, AffiliationId.class))
        .thenReturn(AFFILIATION_ID);

    final AffiliationId result = this.connector.addAffiliation(this.affiliation);

    Mockito.verify(this.mockRestTemplate).postForObject(uri, this.affiliation, AffiliationId.class);
    Assertions.assertEquals(AFFILIATION_ID, result);
  }

  @Test
  public void addAffiliationThrowsRestClientException() throws
      ConflictingResourceException, ResourceUpdateException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI.create(Constants.BASE_URL + AFFILIATIONS_URL_PREFIX);
      when(this.mockRestTemplate.postForObject(uri, this.affiliation, AffiliationId.class))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.addAffiliation(this.affiliation);
      Assertions.fail();
    });
  }

  @Test
  public void affiliationIdExists() throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "/" + AFFILIATION_ID.getUrn();
    final boolean result = this.connector.affiliationIdExists(AFFILIATION_ID);
    Mockito.verify(this.mockRestTemplate).headForHeaders(url);
    Assertions.assertEquals(true, result);
  }

  @Test
  public void affialitionIdExists_affiliationDoesNotExist()
      throws IOException, InvalidEntityException {
    final String url = Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "/" + AFFILIATION_ID.getUrn();
    when(this.mockRestTemplate.headForHeaders(url))
        .thenThrow(HTTP_SERVER_ERROR_EXCEPTION_NOT_FOUND);

    final boolean result = this.connector.affiliationIdExists(AFFILIATION_ID);
    Assertions.assertEquals(false, result);
  }

  @Test
  public void affiliationIdExistsThrowsRestClientException()
      throws InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "/" + AFFILIATION_ID.getUrn();
      when(this.mockRestTemplate.headForHeaders(url)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.affiliationIdExists(AFFILIATION_ID);
      Assertions.fail();
    });
  }

  @Test
  public void updateAffiliation()
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    final Affiliation updatedAffiliation = this.affiliation;
    this.affiliation.setName("new name");

    this.connector.updateAffiliation(updatedAffiliation);

    Mockito.verify(this.mockRestTemplate).put(
        Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "/" + AFFILIATION_ID.getUrn(),
        updatedAffiliation);
  }

  @Test
  public void updateAffiliationThrowsRestClientException()
      throws ResourceUpdateException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final Affiliation updatedAffiliation = this.affiliation;
      this.affiliation.setName("new name");

      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).put(
          Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "/" + AFFILIATION_ID.getUrn(),
          updatedAffiliation);

      this.connector.updateAffiliation(updatedAffiliation);
      Assertions.fail();
    });
  }

  @Test
  public void getAffiliation() throws IOException, NoSuchEntityException, InvalidEntityException {
    final String url = Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "/" + AFFILIATION_ID.getUrn();
    when(this.mockRestTemplate.getForObject(url, Affiliation.class)).thenReturn(this.affiliation);
    final Affiliation result = this.connector.getAffiliation(AFFILIATION_ID);
    Mockito.verify(this.mockRestTemplate).getForObject(url, Affiliation.class);
    Assertions.assertEquals(this.affiliation, result);
  }

  @Test
  public void getAffiliationThrowsRestClientException()
      throws NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "/" + AFFILIATION_ID.getUrn();
      when(this.mockRestTemplate.getForObject(url, Affiliation.class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getAffiliation(AFFILIATION_ID);
      Assertions.fail();
    });
  }

  @Test
  public void listAffiliations() throws IOException {
    final AffiliationId[] affiliationIds = {
        AFFILIATION_ID
    };
    final URI uri = URI
        .create(Constants.BASE_URL + AFFILIATION_IDS_URL_PREFIX + "?include-locked=true");
    when(this.mockRestTemplate.getForObject(uri, AffiliationId[].class)).thenReturn(affiliationIds);
    final Set<AffiliationId> result = this.connector.listAffiliations(true);
    Mockito.verify(this.mockRestTemplate).getForObject(uri, AffiliationId[].class);
    Assertions.assertEquals(Sets.newHashSet(affiliationIds), result);
  }

  @Test
  public void listAffiliationThrowsRestClientException() {
    assertThrows(IOException.class, () -> {
      final URI uri = URI
          .create(Constants.BASE_URL + AFFILIATION_IDS_URL_PREFIX + "?include-locked=true");
      when(this.mockRestTemplate.getForObject(uri, AffiliationId[].class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.listAffiliations(true);
      Assertions.fail();
    });
  }

  @Test
  public void getAffiliations() throws IOException {
    final Affiliation[] affiliations = {
        this.affiliation
    };
    final URI uri = URI
        .create(Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "?include-locked=true");
    when(this.mockRestTemplate.getForObject(uri, Affiliation[].class)).thenReturn(affiliations);
    final Set<Affiliation> result = this.connector.getAffiliations(true);
    Mockito.verify(this.mockRestTemplate).getForObject(uri, Affiliation[].class);
    Assertions.assertEquals(Sets.newHashSet(affiliations), result);
  }

  @Test
  public void getAffiliationsThrowRestClientException() {
    assertThrows(IOException.class, () -> {
      final URI uri = URI
          .create(Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "?include-locked=true");
      when(this.mockRestTemplate.getForObject(uri, Affiliation[].class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getAffiliations(true);
      Assertions.fail();
    });
  }

  @Test
  public void lockAffiliation()
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    final String url = Constants.BASE_URL
        + AFFILIATIONS_URL_PREFIX
        + "/"
        + AFFILIATION_ID.getUrn()
        + "/locked";
    this.connector.lockAffiliation(AFFILIATION_ID);
    Mockito.verify(this.mockRestTemplate).patchForObject(url, Boolean.TRUE, Void.class);
  }

  @Test
  public void lockAffiliationThrowsRestClientException()
      throws ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL
          + AFFILIATIONS_URL_PREFIX
          + "/"
          + AFFILIATION_ID.getUrn()
          + "/locked";
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).patchForObject(url,
          Boolean.TRUE, Void.class);
      this.connector.lockAffiliation(AFFILIATION_ID);
      Assertions.fail();
    });
  }

  @Test
  public void unlockAffiliation()
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    final String url = Constants.BASE_URL
        + AFFILIATIONS_URL_PREFIX
        + "/"
        + AFFILIATION_ID.getUrn()
        + "/locked";
    this.connector.unlockAffiliation(AFFILIATION_ID);
    Mockito.verify(this.mockRestTemplate).patchForObject(url, Boolean.FALSE, Void.class);
  }

  @Test
  public void unlockAffiliationThrowsRestClientException()
      throws ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final String url = Constants.BASE_URL
          + AFFILIATIONS_URL_PREFIX
          + "/"
          + AFFILIATION_ID.getUrn()
          + "/locked";
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).patchForObject(url,
          Boolean.FALSE, Void.class);
      this.connector.unlockAffiliation(AFFILIATION_ID);
      Assertions.fail();
    });
  }

  @Test
  public void getAffiliationByUserUUID() throws IOException, NoSuchEntityException {
    final String userId = UUID.randomUUID().toString();
    final URI uri = URI.create(Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "?user-id=" + userId);
    when(this.mockRestTemplate.getForObject(uri, Affiliation[].class))
        .thenReturn(ArrayUtils.toArray(this.affiliation));
    final Affiliation result = this.connector.getAffiliationByUserUUID(userId);
    Mockito.verify(this.mockRestTemplate).getForObject(uri, Affiliation[].class);
    Assertions.assertEquals(this.affiliation, result);
  }

  @Test
  public void getAffiliationByUserUUIDThrowsRestClientException()
      throws NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String userId = UUID.randomUUID().toString();
      final URI uri = URI.create(Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "?user-id=" + userId);
      when(this.mockRestTemplate.getForObject(uri, Affiliation[].class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getAffiliationByUserUUID(userId);
      Assertions.fail();
    });
  }

  @Test
  public void getAffiliationIdByUserUUID() throws IOException, NoSuchEntityException {
    final String userId = UUID.randomUUID().toString();
    final URI uri = URI
        .create(Constants.BASE_URL + AFFILIATION_IDS_URL_PREFIX + "?user-id=" + userId);
    when(this.mockRestTemplate.getForObject(uri, AffiliationId[].class))
        .thenReturn(ArrayUtils.toArray(AFFILIATION_ID));
    final AffiliationId result = this.connector.getAffiliationIdByUserUUID(userId);
    Mockito.verify(this.mockRestTemplate).getForObject(uri, AffiliationId[].class);
    Assertions.assertEquals(AFFILIATION_ID, result);
  }

  @Test
  public void getAffiliationIdByUserUUIDThrowsRestClientException()
      throws NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String userId = UUID.randomUUID().toString();
      final URI uri = URI
          .create(Constants.BASE_URL + AFFILIATION_IDS_URL_PREFIX + "?user-id=" + userId);
      when(this.mockRestTemplate.getForObject(uri, AffiliationId[].class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getAffiliationIdByUserUUID(userId);
      Assertions.fail();
    });
  }

  @Test
  public void getAffiliationBySolutionId()
      throws RestClientException, InvalidEntityException, NoSuchEntityException, IOException {
    final URI uri = URI
        .create(Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "?solution-id=" + SOLUTION_ID);
    when(this.mockRestTemplate.getForObject(uri, Affiliation[].class))
        .thenReturn(ArrayUtils.toArray(this.affiliation));
    final Affiliation result = this.connector.getAffiliationBySolutionId(SOLUTION_ID);
    Mockito.verify(this.mockRestTemplate).getForObject(uri, Affiliation[].class);
    Assertions.assertEquals(this.affiliation, result);
  }

  @Test
  public void getAffiliationBySolutionIdThrowsRestClientException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI
          .create(Constants.BASE_URL + AFFILIATIONS_URL_PREFIX + "?solution-id=" + SOLUTION_ID);
      when(this.mockRestTemplate.getForObject(uri, Affiliation[].class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getAffiliationBySolutionId(SOLUTION_ID);
      Assertions.fail();
    });
  }

  @Test
  public void getAffiliationIdBySolutionId()
      throws RestClientException, InvalidEntityException, IOException, NoSuchEntityException {
    final URI uri = URI
        .create(Constants.BASE_URL + AFFILIATION_IDS_URL_PREFIX + "?solution-id=" + SOLUTION_ID);
    when(this.mockRestTemplate.getForObject(uri, AffiliationId[].class))
        .thenReturn(ArrayUtils.toArray(AFFILIATION_ID));
    final AffiliationId result = this.connector.getAffiliationIdBySolutionId(SOLUTION_ID);
    Mockito.verify(this.mockRestTemplate).getForObject(uri, AffiliationId[].class);
    Assertions.assertEquals(AFFILIATION_ID, result);
  }

  @Test
  public void getAffiliationIdBySolutionIdThrowsRestClientException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI
          .create(Constants.BASE_URL + AFFILIATION_IDS_URL_PREFIX + "?solution-id=" + SOLUTION_ID);
      when(this.mockRestTemplate.getForObject(uri, AffiliationId[].class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getAffiliationIdBySolutionId(SOLUTION_ID);
      Assertions.fail();
    });
  }

  @Test
  public void notifySync() throws IOException, InvalidEntityException {
    final SyncNotification syncNotification = new SyncNotification();
    syncNotification.setClientId(new ClientId("urn:client:test:test-client"));
    syncNotification.setSyncTime(1L);
    final HttpEntity<SyncNotification> httpEntity = new HttpEntity<>(syncNotification);
    final ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.OK);

    final String url = Constants.BASE_URL
        + LIBRARY_CLIENTS_URL_PREFIX
        + "/"
        + syncNotification.getClientId().getUrn()
        + "/latest-sync-notification";

    when(this.mockRestTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class))
        .thenReturn(responseEntity);
    this.connector.notifySync(syncNotification);
    Mockito.verify(this.mockRestTemplate).exchange(url, HttpMethod.PUT, httpEntity, Void.class);
  }

  @Test
  public void notifySync_responseStatusIsNot2xx_throwsException()
      throws InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final SyncNotification syncNotification = new SyncNotification();
      syncNotification.setClientId(new ClientId("urn:client:test:test-client"));
      syncNotification.setSyncTime(1L);
      final HttpEntity<SyncNotification> httpEntity = new HttpEntity<>(syncNotification);
      final ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

      final String url = Constants.BASE_URL
          + LIBRARY_CLIENTS_URL_PREFIX
          + "/"
          + syncNotification.getClientId().getUrn()
          + "/latest-sync-notification";

      when(this.mockRestTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class))
          .thenReturn(responseEntity);
      this.connector.notifySync(syncNotification);
    });
  }

  @Test
  public void notifySyncThrowsRestClientException() throws InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final SyncNotification syncNotification = new SyncNotification();
      syncNotification.setClientId(new ClientId("urn:client:test:test-client"));
      syncNotification.setSyncTime(1L);
      final HttpEntity<SyncNotification> httpEntity = new HttpEntity<>(syncNotification);

      final String url = Constants.BASE_URL
          + LIBRARY_CLIENTS_URL_PREFIX
          + "/"
          + syncNotification.getClientId().getUrn()
          + "/latest-sync-notification";

      when(this.mockRestTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.notifySync(syncNotification);
    });
  }

  @Test
  public void listDeployedPolicyVersions()
      throws InvalidEntityException, IOException, NoSuchEntityException {
    final ClientId clientId = new ClientId("urn:client:test:test-client");
    final Map<PolicyId, Long> policyVersions = new HashMap<>();
    policyVersions.put(new PolicyId("urn:policy:test:policy1"), 1234L);
    policyVersions.put(new PolicyId("urn:policy:test:policy2"), 2345L);

    final String url = Constants.BASE_URL
        + LIBRARY_CLIENTS_URL_PREFIX
        + "/"
        + clientId.getUrn()
        + "/deployed-policy-versions";
    final ParameterizedTypeReference<Map<PolicyId, Long>> responseType = new ParameterizedTypeReference<Map<PolicyId, Long>>() {
    };

    when(this.mockRestTemplate.exchange(url, HttpMethod.GET, null, responseType))
        .thenReturn(new ResponseEntity<>(policyVersions, HttpStatus.OK));

    //    when(this.mockRestTemplate.getForObject(url, Map.class)).thenReturn(policyVersions);

    final Map<PolicyId, Long> result = this.connector.listDeployedPolicyVersions(clientId);
    Mockito.verify(this.mockRestTemplate).exchange(url, HttpMethod.GET, null, responseType);
    Assertions.assertEquals(policyVersions, result);
  }

  @Test
  public void listDeployedPolicyVersionsThrowsRestClientException()
      throws RestClientException, InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final ClientId clientId = new ClientId("urn:client:test:test-client");

      final String url = Constants.BASE_URL
          + LIBRARY_CLIENTS_URL_PREFIX
          + "/"
          + clientId.getUrn()
          + "/deployed-policy-versions";
      final ParameterizedTypeReference<Map<PolicyId, Long>> responseType = new ParameterizedTypeReference<Map<PolicyId, Long>>() {
      };
      when(this.mockRestTemplate.exchange(url, HttpMethod.GET, null, responseType))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.listDeployedPolicyVersions(clientId);
      Assertions.fail();
    });
  }

  @Test
  public void listClientsForPolicy()
      throws InvalidEntityException, IOException, NoSuchEntityException {
    final PolicyId policyId = new PolicyId("urn:policy:test:test-policy");
    final Map<ClientId, Long> clientIds = new HashMap<>();
    final URI uri = URI.create(
        Constants.BASE_URL + LIBRARY_CLIENTS_URL_PREFIX + "?policy-id=" + policyId.getUrn());
    final ParameterizedTypeReference<Map<ClientId, Long>> responseType = new ParameterizedTypeReference<Map<ClientId, Long>>() {
    };
    when(this.mockRestTemplate.exchange(uri, HttpMethod.GET, null, responseType))
        .thenReturn(new ResponseEntity<>(clientIds, HttpStatus.OK));
    final Map<ClientId, Long> result = this.connector.listClientsForPolicy(policyId);
    Mockito.verify(this.mockRestTemplate).exchange(uri, HttpMethod.GET, null, responseType);
    Assertions.assertEquals(clientIds, result);
  }

  @Test
  public void listClientsForPolicyThrowsRestClientException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final PolicyId policyId = new PolicyId("urn:policy:test:test-policy");
      final URI uri = URI.create(
          Constants.BASE_URL + LIBRARY_CLIENTS_URL_PREFIX + "?policy-id=" + policyId.getUrn());
      final ParameterizedTypeReference<Map<ClientId, Long>> responseType = new ParameterizedTypeReference<Map<ClientId, Long>>() {
      };
      when(this.mockRestTemplate.exchange(uri, HttpMethod.GET, null, responseType))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.listClientsForPolicy(policyId);
      Assertions.fail();
    });
  }

  @Test
  public void listDeployedTimerVersions()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final ClientId clientId = new ClientId("urn:client:test:test-client");
    final Map<TimerId, Long> timerVersions = new HashMap<>();
    timerVersions.put(new TimerId("urn:timer:test:policy1"), 1234L);
    timerVersions.put(new TimerId("urn:timer:test:policy2"), 2345L);

    final String url = Constants.BASE_URL
        + LIBRARY_CLIENTS_URL_PREFIX
        + "/"
        + clientId.getUrn()
        + "/deployed-timer-versions";
    final ParameterizedTypeReference<Map<TimerId, Long>> responseType = new ParameterizedTypeReference<Map<TimerId, Long>>() {
    };

    when(this.mockRestTemplate.exchange(url, HttpMethod.GET, null, responseType))
        .thenReturn(new ResponseEntity<>(timerVersions, HttpStatus.OK));

    final Map<TimerId, Long> result = this.connector.listDeployedTimerVersions(clientId);
    Mockito.verify(this.mockRestTemplate).exchange(url, HttpMethod.GET, null, responseType);
    Assertions.assertEquals(timerVersions, result);
  }

  @Test
  public void listDeployedTimerVersionsThrowsException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final ClientId clientId = new ClientId("urn:client:test:test-client");

      final String url = Constants.BASE_URL
          + LIBRARY_CLIENTS_URL_PREFIX
          + "/"
          + clientId.getUrn()
          + "/deployed-timer-versions";
      final ParameterizedTypeReference<Map<TimerId, Long>> responseType = new ParameterizedTypeReference<Map<TimerId, Long>>() {
      };
      when(this.mockRestTemplate.exchange(url, HttpMethod.GET, null, responseType))
          .thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.listDeployedTimerVersions(clientId);
      Assertions.fail();
    });
  }

  @Test
  public void listClientsForTimer()
      throws IOException, InvalidEntityException, NoSuchEntityException {
    final TimerId timerId = new TimerId("urn:timer:test:test-timer");
    final Map<ClientId, Long> clientIds = new HashMap<>();
    final URI uri = URI
        .create(Constants.BASE_URL + LIBRARY_CLIENTS_URL_PREFIX + "?timer-id=" + timerId.getUrn());

    final ParameterizedTypeReference<Map<ClientId, Long>> responseType = new ParameterizedTypeReference<Map<ClientId, Long>>() {
    };
    when(this.mockRestTemplate.exchange(uri, HttpMethod.GET, null, responseType))
        .thenReturn(new ResponseEntity<>(clientIds, HttpStatus.OK));

    //    when(this.mockRestTemplate.getForObject(uri, Map.class)).thenReturn(clientIds);
    final Map<ClientId, Long> result = this.connector.listClientsForTimer(timerId);
    Mockito.verify(this.mockRestTemplate).exchange(uri, HttpMethod.GET, null, responseType);
    Assertions.assertEquals(clientIds, result);
  }

  @Test
  public void listClientsForTimerThrowsRestClientException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final TimerId timerId = new TimerId("urn:timer:test:test-timer");
      final URI uri = URI
          .create(Constants.BASE_URL + LIBRARY_CLIENTS_URL_PREFIX + "?timer-id=" + timerId.getUrn());
      final ParameterizedTypeReference<Map<ClientId, Long>> responseType = new ParameterizedTypeReference<Map<ClientId, Long>>() {
      };
      when(this.mockRestTemplate.exchange(uri, HttpMethod.GET, null, responseType))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.listClientsForTimer(timerId);
      Assertions.fail();
    });
  }

  @Test
  public void createLibraryClient() throws InvalidEntityException, NoSuchEntityException,
      IOException, ConflictingResourceException {
    final LibraryClientRequestDTO libraryClientRequestDTO = new LibraryClientRequestDTO();
    libraryClientRequestDTO.setClientIdAsString(this.libraryClient.getClientId().getUrn());
    libraryClientRequestDTO.setMaster(this.libraryClient.isMaster());
    final LibraryClientResponseDTO libraryClientResponseDTO = new LibraryClientResponseDTO(
        this.libraryClient);

    when(this.mockRestTemplate.postForObject(Constants.BASE_URL + LIBRARY_CLIENTS_URL_PREFIX,
        libraryClientRequestDTO, LibraryClientResponseDTO.class))
        .thenReturn(libraryClientResponseDTO);

    final LibraryClientResponseDTO result = this.connector.createLibraryClient(
        this.libraryClient.getClientId().getUrn(), this.libraryClient.isMaster());

    Mockito.verify(this.mockRestTemplate).postForObject(
        Constants.BASE_URL + LIBRARY_CLIENTS_URL_PREFIX, libraryClientRequestDTO,
        LibraryClientResponseDTO.class);
    Assertions.assertEquals(libraryClientResponseDTO, result);
  }

  @Test
  public void createLibraryClientThrowsRestClientException() throws InvalidEntityException,
      NoSuchEntityException, ConflictingResourceException {
    assertThrows(IOException.class, () -> {
      final LibraryClientRequestDTO libraryClientRequestDTO = new LibraryClientRequestDTO();
      libraryClientRequestDTO.setClientIdAsString(this.libraryClient.getClientId().getUrn());
      libraryClientRequestDTO.setMaster(this.libraryClient.isMaster());

      when(this.mockRestTemplate.postForObject(Constants.BASE_URL + LIBRARY_CLIENTS_URL_PREFIX,
          libraryClientRequestDTO, LibraryClientResponseDTO.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.createLibraryClient(this.libraryClient.getClientId().getUrn(),
          this.libraryClient.isMaster());
      Assertions.fail();
    });
  }

  @Test
  public void deleteLibraryClientIdAsString()
      throws InvalidEntityException, NoSuchEntityException, IOException {
    this.connector.deleteLibraryClient(this.libraryClient.getClientId().getUrn(),
        this.user.getUsername());
    Mockito.verify(this.mockRestTemplate)
        .delete(Constants.BASE_URL
            + LIBRARY_CLIENTS_URL_PREFIX
            + "/"
            + this.libraryClient.getClientId().getUrn());
  }

  @Test
  public void deleteLibraryClientIdAsStringThrowsRestClientException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate)
          .delete(Constants.BASE_URL
              + LIBRARY_CLIENTS_URL_PREFIX
              + "/"
              + this.libraryClient.getClientId().getUrn());
      this.connector.deleteLibraryClient(this.libraryClient.getClientId().getUrn(),
          this.user.getUsername());
      Assertions.fail();
    });
  }

  @Test
  public void deleteLibraryClient()
      throws InvalidEntityException, NoSuchEntityException, IOException {
    this.connector.deleteLibraryClient(this.libraryClient.getClientId(), this.user.getUsername());
    Mockito.verify(this.mockRestTemplate)
        .delete(Constants.BASE_URL
            + LIBRARY_CLIENTS_URL_PREFIX
            + "/"
            + this.libraryClient.getClientId().getUrn());
  }

  @Test
  public void deleteLibraryClientThrowsRestClientException()
      throws InvalidEntityException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate)
          .delete(Constants.BASE_URL
              + LIBRARY_CLIENTS_URL_PREFIX
              + "/"
              + this.libraryClient.getClientId().getUrn());
      this.connector.deleteLibraryClient(this.libraryClient.getClientId(), this.user.getUsername());
      Assertions.fail();
    });
  }

  @Test
  public void createOAuthClientDetails()
      throws InvalidEntityException, ConflictingResourceException, IOException {
    assertThrows(UnsupportedOperationException.class, () ->
        this.connector.createOAuthClientDetails(null, null));
  }

  @Test
  public void getLibraryClient() throws NoSuchEntityException, InvalidEntityException, IOException {
    final ClientId clientId = new ClientId("urn:client:test:library-client");
    final LibraryClient libraryClient = new LibraryClient(clientId);
    when(this.mockRestTemplate.getForObject(
        Constants.BASE_URL + LIBRARY_CLIENTS_URL_PREFIX + "/" + clientId.getUrn(),
        LibraryClient.class)).thenReturn(libraryClient);

    final LibraryClient result = this.connector.getLibraryClient(clientId);

    Assertions.assertEquals(libraryClient, result);
  }

  @Test
  public void getLibraryClientThrowsRestClientException()
      throws NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final ClientId clientId = new ClientId("urn:client:test:library-client");
      when(this.mockRestTemplate.getForObject(
          Constants.BASE_URL + LIBRARY_CLIENTS_URL_PREFIX + "/" + clientId.getUrn(),
          LibraryClient.class)).thenThrow(REST_CLIENT_EXCEPTION);

      this.connector.getLibraryClient(clientId);
    });
  }

  @Test
  public void getLibraryClients()
      throws NoSuchEntityException, InvalidEntityException, IOException {
    final SolutionId solutionId = new SolutionId("urn:solution:test");
    final ClientId clientId1 = new ClientId("urn:client:test:library-client1");
    final ClientId clientId2 = new ClientId("urn:client:test:library-client2");
    final ClientId clientId3 = new ClientId("urn:client:test:library-client3");

    final LibraryClient existingLibraryClient1 = new LibraryClient(clientId1);
    final LibraryClient existingLibraryClient2 = new LibraryClient(clientId2);
    final LibraryClient existingLibraryClient3 = new LibraryClient(clientId3);

    final LibraryClient[] libraryClients = new LibraryClient[]{
        existingLibraryClient1, existingLibraryClient2, existingLibraryClient3
    };

    final URI uri = URI.create(
        Constants.BASE_URL + LIBRARY_CLIENTS_URL_PREFIX + "?solution-id=" + solutionId.getUrn());

    when(this.mockRestTemplate.getForObject(uri, LibraryClient[].class)).thenReturn(libraryClients);

    final Set<LibraryClient> result = this.connector.getLibraryClients(solutionId);

    Assertions.assertEquals(Sets.newHashSet(libraryClients), result);
  }

  @Test
  public void getLibraryClientsThrowsRestClientException()
      throws NoSuchEntityException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI.create(
          Constants.BASE_URL + LIBRARY_CLIENTS_URL_PREFIX + "?solution-id=" + SOLUTION_ID.getUrn());

      when(this.mockRestTemplate.getForObject(uri, LibraryClient[].class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getLibraryClients(SOLUTION_ID);
    });
  }

  @Test
  public void generateToken() throws IOException, NoSuchEntityException {
    final URI uri = URI.create(Constants.BASE_URL
        + SELF_SERVICE_URL_PREFIX
        + "/password-reset?username="
        + this.user.getUsername());

    this.connector.generateToken(this.user.getUsername());

    Mockito.verify(this.mockRestTemplate).postForObject(uri, null, PasswordResetToken.class);
  }

  @Test
  public void whenValidateToken_thenOk()
      throws IOException, ForbiddenException, NoSuchEntityException {
    this.connector.validateToken("1234", "5678");
    Mockito.verify(this.mockRestTemplate).getForObject(
        URI.create(
            Constants.BASE_URL + SELF_SERVICE_URL_PREFIX + "/password?user-id=1234&token=5678"),
        Void.class);
  }

  @Test
  public void whenValidateToken_thenThrowRestClientException()
      throws ForbiddenException, NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).getForObject(
          URI.create(
              Constants.BASE_URL + SELF_SERVICE_URL_PREFIX + "/password?user-id=1234&token=5678"),
          Void.class);

      this.connector.validateToken("1234", "5678");
      Assertions.fail();
    });
  }

  @Test
  public void whenValidateTokenAndSetPassword_thenOk()
      throws NoSuchEntityException, ForbiddenException, IOException {
    this.connector.validateTokenAndSetPassword("1234", "5678", "newPassword");
    final URI uri = URI
        .create(Constants.BASE_URL + SELF_SERVICE_URL_PREFIX + "/password?user-id=1234&token=5678");
    Mockito.verify(this.mockRestTemplate).postForObject(uri, "newPassword", Void.class);
  }

  @Test
  public void whenValidateTokenAndSetPassword_thenThrowRestClientException()
      throws NoSuchEntityException, ForbiddenException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI
          .create(Constants.BASE_URL + SELF_SERVICE_URL_PREFIX + "/password?user-id=1234&token=5678");
      Mockito.when(this.mockRestTemplate.postForObject(uri, "newPassword", Void.class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.validateTokenAndSetPassword("1234", "5678", "newPassword");
    });
  }

  @Test
  public void whenGetTimezones_thenOk() throws IOException {
    this.connector.getTimezones();
    Mockito.verify(this.mockRestTemplate).getForObject(Constants.BASE_URL + TIMEZONE_URL_PREFIX,
        Timezone[].class);
  }

  @Test
  public void whenGetTimezones_thenThrowRestClientException() {
    assertThrows(IOException.class, () -> {
      Mockito.when(this.mockRestTemplate.getForObject(Constants.BASE_URL + TIMEZONE_URL_PREFIX,
          Timezone[].class)).thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getTimezones();
      Assertions.fail();
    });
  }

  @Test
  public void whenGetTimezoneByZoneId_thenOk() throws NoSuchEntityException, IOException {
    this.connector.getTimezoneByZoneId("UTC");
    final String uri = Constants.BASE_URL + TIMEZONE_URL_PREFIX + "/UTC";
    Mockito.verify(this.mockRestTemplate).getForObject(uri, Timezone.class);
  }

  @Test
  public void whenGetTimezoneByZoneId_thenThrowRestClientException()
      throws NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String uri = Constants.BASE_URL + TIMEZONE_URL_PREFIX + "/UTC";
      Mockito.when(this.mockRestTemplate.getForObject(uri, Timezone.class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getTimezoneByZoneId("UTC");
      Assertions.fail();
    });
  }

  @Test
  public void whenAddOAuthClientDetails_thenOk()
      throws ConflictingResourceException, InvalidEntityException, IOException {
    final OAuthClientDetailsDTO oAuthClientDetailsDTO = new OAuthClientDetailsDTO();
    oAuthClientDetailsDTO.setAuthority("TECH_CLIENT");
    oAuthClientDetailsDTO.setClientId("urn:client:test:test-client");
    oAuthClientDetailsDTO.setClientSecret("super-secret");
    this.connector.addOAuthClientDetails(oAuthClientDetailsDTO);
    Mockito.verify(this.mockRestTemplate).postForObject(
        Constants.BASE_URL + OAUTH_CLIENT_URL_PREFIX, oAuthClientDetailsDTO,
        OAuthClientDetailsDTO.class);
  }

  @Test
  public void whenAddOAuthClientDetails_thenThrowRestClientException()
      throws ConflictingResourceException, InvalidEntityException {
    assertThrows(IOException.class, () -> {
      final OAuthClientDetailsDTO oAuthClientDetailsDTO = new OAuthClientDetailsDTO();
      oAuthClientDetailsDTO.setAuthority("TECH_CLIENT");
      oAuthClientDetailsDTO.setClientId("urn:client:test:test-client");
      oAuthClientDetailsDTO.setClientSecret("super-secret");
      Mockito.when(this.mockRestTemplate.postForObject(Constants.BASE_URL + OAUTH_CLIENT_URL_PREFIX,
          oAuthClientDetailsDTO, OAuthClientDetailsDTO.class)).thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.addOAuthClientDetails(oAuthClientDetailsDTO);
      Assertions.fail();
    });
  }

  @Test
  public void whenGetOAuthClientDetails_thenOk() throws NoSuchEntityException, IOException {
    final OAuthClientDetailsDTO oAuthClientDetailsDTO = new OAuthClientDetailsDTO();
    oAuthClientDetailsDTO.setAuthority("TECH_CLIENT");
    oAuthClientDetailsDTO.setClientId("urn:client:test:test-client");
    final String uri = Constants.BASE_URL
        + OAUTH_CLIENT_URL_PREFIX
        + "/"
        + oAuthClientDetailsDTO.getClientId();
    this.connector.getOAuthClientDetails(oAuthClientDetailsDTO.getClientId());
    Mockito.verify(this.mockRestTemplate).getForObject(uri, OAuthClientDetailsDTO.class);
  }

  @Test
  public void whenGetOAuthClientDetails_thenThrowRestClientException()
      throws NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final OAuthClientDetailsDTO oAuthClientDetailsDTO = new OAuthClientDetailsDTO();
      oAuthClientDetailsDTO.setAuthority("TECH_CLIENT");
      oAuthClientDetailsDTO.setClientId("urn:client:test:test-client");
      final String uri = Constants.BASE_URL
          + OAUTH_CLIENT_URL_PREFIX
          + "/"
          + oAuthClientDetailsDTO.getClientId();
      Mockito.when(this.mockRestTemplate.getForObject(uri, OAuthClientDetailsDTO.class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getOAuthClientDetails(oAuthClientDetailsDTO.getClientId());
      Assertions.fail();
    });
  }

  @Test
  public void whenGetAllOAuthClientDetailsWithoutSolutionId_thenOk()
      throws NoSuchEntityException, IOException {
    this.connector.getAllOAuthClientDetails(null);
    final URI uri = URI.create(Constants.BASE_URL + OAUTH_CLIENT_URL_PREFIX);
    Mockito.verify(this.mockRestTemplate).getForObject(uri, OAuthClientDetailsDTO[].class);
  }

  @Test
  public void whenGetAllOAuthClientDetailsWithSolutionId_thenOk()
      throws NoSuchEntityException, IOException {
    this.connector.getAllOAuthClientDetails("urn:solution:test");
    final URI uri = URI
        .create(Constants.BASE_URL + OAUTH_CLIENT_URL_PREFIX + "?solution-id=urn:solution:test");
    Mockito.verify(this.mockRestTemplate).getForObject(uri, OAuthClientDetailsDTO[].class);
  }

  @Test
  public void whenGetAllOauthClientDetailsWithSolutionId_thenThrowRestClientException()
      throws NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final URI uri = URI
          .create(Constants.BASE_URL + OAUTH_CLIENT_URL_PREFIX + "?solution-id=urn:solution:test");
      Mockito.when(this.mockRestTemplate.getForObject(uri, OAuthClientDetailsDTO[].class))
          .thenThrow(REST_CLIENT_EXCEPTION);
      this.connector.getAllOAuthClientDetails("urn:solution:test");
      Assertions.fail();
    });
  }

  @Test
  public void whenDeleteOAuthClientDetails_thenOk() throws NoSuchEntityException, IOException {
    final String clientId = "urn:client:test:test-client";
    final String uri = Constants.BASE_URL + OAUTH_CLIENT_URL_PREFIX + "/" + clientId;
    this.connector.deleteOAuthClient(clientId);
    Mockito.verify(this.mockRestTemplate).delete(uri);
  }

  @Test
  public void whenDeleteOAuthClientDetails_thenThrowRestClientException()
      throws NoSuchEntityException {
    assertThrows(IOException.class, () -> {
      final String clientId = "urn:client:test:test-client";
      final String uri = Constants.BASE_URL + OAUTH_CLIENT_URL_PREFIX + "/" + clientId;
      Mockito.doThrow(REST_CLIENT_EXCEPTION).when(this.mockRestTemplate).delete(uri);
      this.connector.deleteOAuthClient(clientId);
      Assertions.fail();
    });
  }
}

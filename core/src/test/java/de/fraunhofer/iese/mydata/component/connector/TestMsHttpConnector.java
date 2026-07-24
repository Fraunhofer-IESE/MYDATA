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

package de.fraunhofer.iese.mydata.component.connector;

import de.fraunhofer.iese.mydata.affiliation.Affiliation;
import de.fraunhofer.iese.mydata.affiliation.AffiliationId;
import de.fraunhofer.iese.mydata.client.ClientId;
import de.fraunhofer.iese.mydata.client.LibraryClient;
import de.fraunhofer.iese.mydata.client.SyncNotification;
import de.fraunhofer.iese.mydata.client.dto.LibraryClientResponseDTO;
import de.fraunhofer.iese.mydata.component.ComponentId;
import de.fraunhofer.iese.mydata.component.ComponentType;
import de.fraunhofer.iese.mydata.component.health.HealthStatus;
import de.fraunhofer.iese.mydata.component.information.PdpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.component.information.method.MethodInterfaceDescription;
import de.fraunhofer.iese.mydata.component.interfaces.IManagementService;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.ForbiddenException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.MessagingException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.oauth.PasswordResetToken;
import de.fraunhofer.iese.mydata.oauth.dto.OAuthClientDetailsDTO;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.solution.Solution;
import de.fraunhofer.iese.mydata.solution.SolutionId;
import de.fraunhofer.iese.mydata.solution.Timezone;
import de.fraunhofer.iese.mydata.timer.Timer;
import de.fraunhofer.iese.mydata.timer.TimerId;
import de.fraunhofer.iese.mydata.user.MyDataRole;
import de.fraunhofer.iese.mydata.user.User;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Connector(protocol = {
    "http", "https"
}, type = ComponentType.MS)
public class TestMsHttpConnector implements IManagementService {

  @Override
  public HealthStatus getHealth() throws IOException {
    return null;
  }

  @Override
  public PolicyId addPolicy(Policy policy)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    return null;
  }

  @Override
  public boolean policyExists(PolicyId policyId) throws IOException {
    return false;
  }

  @Override
  public Policy getPolicy(PolicyId policyId) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Policy> getPolicies(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PolicyId> listPolicies(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Policy> getDeployedPolicies() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Policy> getDeployedPolicies(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PolicyId> listDeployedPolicies(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isPolicyDeployed(PolicyId policyId) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public PolicyId updatePolicy(Policy policy)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deployPolicy(PolicyId policyId)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub

  }

  @Override
  public void revokePolicy(PolicyId policyId)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deletePolicy(PolicyId policyId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public TimerId addTimer(Timer timer)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Timer getTimer(TimerId timerId) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Timer> getTimers(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<TimerId> listTimers(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Timer> getDeployedTimers(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<TimerId> listDeployedTimers(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isTimerDeployed(TimerId timerId) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void deployTimer(TimerId timerId)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub

  }

  @Override
  public void revokeTimer(TimerId timerId)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteTimer(TimerId timerId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean timerExists(TimerId timerId) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public ComponentId addPdp(PdpComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId addPep(PepComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId addPip(PipComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId addPxp(PxpComponentInformation component)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean pdpExists(ComponentId componentId) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean pepExists(ComponentId componentId) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean pipExists(ComponentId componentId) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean pxpExists(ComponentId componentId) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public PdpComponentInformation getPdp() throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PepComponentInformation getPep(ComponentId id) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PipComponentInformation getPip(ComponentId id) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PxpComponentInformation getPxp(ComponentId id) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HealthStatus getPipState(ComponentId componentId)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HealthStatus getPxpState(ComponentId componentId)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<ComponentId, HealthStatus> getAllComponentStates(SolutionId solutionId)
      throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PepComponentInformation> lookupPep(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PipComponentInformation> lookupPip(SolutionId solutionId,
      MethodInterfaceDescription query) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PxpComponentInformation> lookupPxp(SolutionId solutionId,
      MethodInterfaceDescription query) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId updatePdp(PdpComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId updatePep(PepComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId updatePip(PipComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId updatePxp(PxpComponentInformation component)
      throws IOException, NoSuchEntityException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean solutionExists(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void updateSolution(Solution solution)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub
  }

  @Override
  public Solution getSolution(SolutionId solutionId) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<SolutionId> listSolutions(AffiliationId affiliationId, boolean includeLocked)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Solution> getSolutions(AffiliationId affiliationId, boolean includeLocked)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<SolutionId> listSolutions(String userId, boolean includeLocked)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Solution> getSolutions(String userId, boolean includeLocked)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteSolution(SolutionId solutionId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void assignUser(SolutionId solutionId, String userUUID)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void unassignUser(SolutionId solutionId, String userUUID)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public ZoneId getZoneId(SolutionId policyId) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String addUser(User user, AffiliationId affiliationId)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean userIdExists(String userId) throws IllegalArgumentException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean usernameExists(String username) throws IllegalArgumentException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void updateUser(User user)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public User getUser(String userId) throws IllegalArgumentException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<String> listUsers(AffiliationId affiliationId, boolean includeLocked)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<String> listUsers(SolutionId solutionId, boolean includeLocked)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<User> getUsers(AffiliationId affiliationId, boolean includeLocked)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<User> getUsers(SolutionId solutionId, boolean includeLocked)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void lockUser(String userId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void unlockUser(String userId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteUser(String userId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void updatePassword(String userId, String oldPassword, String newPassword)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public AffiliationId addAffiliation(Affiliation affiliation)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean affiliationIdExists(AffiliationId affiliationId) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void updateAffiliation(Affiliation affiliation)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub
  }

  @Override
  public Affiliation getAffiliation(AffiliationId affiliationId)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<AffiliationId> listAffiliations(boolean includeLocked) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Affiliation> getAffiliations(boolean includeLocked) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void lockAffiliation(AffiliationId affiliationId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void unlockAffiliation(AffiliationId affiliationId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteAffiliation(AffiliationId affiliationId)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void setPassword(String userId, String newPassword)
      throws IOException, ResourceUpdateException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public String addSuperAdmin(User user)
      throws IOException, ConflictingResourceException, ResourceUpdateException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ComponentId getId() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean reset() throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  //  @Override
  //  public IManagementService getPmp(ComponentId pmpId) {
  //    // TODO Auto-generated method stub
  //    return null;
  //  }

  @Override
  public AffiliationId getAffiliationIdBySolutionId(SolutionId solutionId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Affiliation getAffiliationBySolutionId(SolutionId solutionId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Policy> getRevokedPolicies(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<PolicyId> listRevokedPolicies(SolutionId solutionId) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void notifySync(SyncNotification syncNotification) {
    // TODO Auto-generated method stub

  }

  @Override
  public Map<PolicyId, Long> listDeployedPolicyVersions(ClientId libraryClientId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<ClientId, Long> listClientsForPolicy(PolicyId policyId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Affiliation getAffiliationByUserUUID(String userUUID)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AffiliationId getAffiliationIdByUserUUID(String userUUID)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Timer> getRevokedTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SolutionId addSolution(Solution solution, AffiliationId affiliationId) throws IOException,
      ConflictingResourceException, ResourceUpdateException, InvalidEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<TimerId> listRevokedTimers(SolutionId solId)
      throws IOException, InvalidEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TimerId updateTimer(Timer timer)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException {
    return null;
  }

  @Override
  public Map<TimerId, Long> listDeployedTimerVersions(ClientId libraryClientId)
      throws IOException, InvalidEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<ClientId, Long> listClientsForTimer(TimerId timerId)
      throws IOException, InvalidEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public User getUserByName(String username) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deletePep(ComponentId componentId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deletePip(ComponentId componentId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deletePxp(ComponentId componentId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateRole(String userId, MyDataRole newRole)
      throws NoSuchEntityException, IllegalArgumentException, InvalidEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public LibraryClientResponseDTO createLibraryClient(String clientIdStr, boolean isMasterClient)
      throws InvalidEntityException, IOException, ConflictingResourceException,
      NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  //  @Override
  //  public LibraryClient createLibraryClient(ClientId clientId, Solution solution, boolean isMasterClient) throws InvalidEntityException, ConflictingResourceException {
  //    // TODO Auto-generated method stub
  //    return null;
  //  }

  @Override
  public void deleteLibraryClient(ClientId clientId, String username) throws NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteLibraryClient(String clientIdStr, String username)
      throws InvalidEntityException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public OAuthClientDetailsDTO createOAuthClientDetails(LibraryClientResponseDTO libraryClient,
      String username) throws InvalidEntityException, ConflictingResourceException, IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LibraryClient getLibraryClient(ClientId clientId)
      throws NoSuchEntityException, InvalidEntityException, IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<LibraryClient> getLibraryClients(SolutionId solutionId)
      throws NoSuchEntityException, InvalidEntityException, IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<String> listUsers(boolean includeLocked) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<User> getUsers(boolean includeLocked) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PasswordResetToken generateToken(String username)
      throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PasswordResetToken getToken(String token) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PasswordResetToken getTokenForUser(User user) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void validateToken(String userId, String token)
      throws IOException, ForbiddenException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteToken(String token) {
    // TODO Auto-generated method stub

  }

  @Override
  public void validateTokenAndSetPassword(String userId, String token, String newPassword)
      throws IOException, ForbiddenException, NoSuchEntityException, ResourceUpdateException,
      MessagingException {
    // TODO Auto-generated method stub

  }

  @Override
  public List<Timezone> getTimezones() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<Timezone> getTimezoneByZoneId(String zoneId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteTokenByUserUuid(String userUuid) throws IOException, NoSuchEntityException {
    // TODO Auto-generated method stub

  }

}

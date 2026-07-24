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

package de.fraunhofer.iese.mydata.user;

import de.fraunhofer.iese.mydata.affiliation.AffiliationId;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.MessagingException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import java.io.IOException;
import java.util.Set;

/**
 * The Interface IUserService.
 */
public interface IUserService {

  /**
   * Adds the user.
   *
   * @param  userModel                    the user
   * @param  affiliationId                the affiliation id
   * @return                              the string
   * @throws IOException                  Signals that an I/O exception has occurred.
   * @throws ConflictingResourceException the conflicting resource exception
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException
   * @throws NoSuchEntityException        if the affiliation does not exist
   * @throws MessagingException           if the account created user could not be sent
   */
  String addUser(User userModel, AffiliationId affiliationId)
      throws IOException, ConflictingResourceException, ResourceUpdateException,
      InvalidEntityException, NoSuchEntityException, MessagingException;

  /**
   * User id exists.
   *
   * @param  userId                the user id
   * @return                       true, if successful
   * @throws IOException
   * @throws NoSuchEntityException
   */
  boolean userIdExists(String userId) throws IOException, NoSuchEntityException;

  /**
   * Username exists.
   *
   * @param  username              the username
   * @return                       true, if successful
   * @throws IOException
   * @throws NoSuchEntityException if the username does not exist
   */
  boolean usernameExists(String username) throws IOException, NoSuchEntityException;

  /**
   * Update user.
   *
   * @param  userModel               the user
   * @throws IOException             Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException the resource update exception
   * @throws NoSuchEntityException   the no such element exception
   * @throws InvalidEntityException
   */
  void updateUser(User userModel)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException;

  /**
   * Gets the user.
   *
   * @param  userId                the user id
   * @return                       the user
   * @throws IOException           Signals that an I/O exception has occurred.
   * @throws NoSuchEntityException the no such element exception
   */
  User getUser(String userId) throws IOException, NoSuchEntityException;

  Set<String> listUsers(boolean includeLocked) throws IOException;

  /**
   * Set users.
   *
   * @param  affiliationId          the affiliation component_id
   * @param  includeLocked          the include locked
   * @return                        the list
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws NoSuchEntityException  the no such element exception
   * @throws InvalidEntityException
   */
  Set<String> listUsers(AffiliationId affiliationId, boolean includeLocked)
      throws IOException, NoSuchEntityException, InvalidEntityException;

  /**
   * List users.
   *
   * @param  solutionId             the solution id
   * @param  includeLocked          the include locked
   * @return                        the sets the
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws InvalidEntityException
   * @throws NoSuchEntityException
   */
  Set<String> listUsers(SolutionId solutionId, boolean includeLocked)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  Set<User> getUsers(boolean includeLocked) throws IOException;

  /**
   * Gets the users.
   *
   * @param  affiliationId          the affiliation id
   * @param  includeLocked          the include locked
   * @return                        the users
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws NoSuchEntityException  the no such element exception
   * @throws InvalidEntityException
   */
  Set<User> getUsers(AffiliationId affiliationId, boolean includeLocked)
      throws IOException, NoSuchEntityException, InvalidEntityException;

  /**
   * Gets the users.
   *
   * @param  solutionId             the solution id
   * @param  includeLocked          the include locked
   * @return                        the users
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws InvalidEntityException
   * @throws NoSuchEntityException
   */
  Set<User> getUsers(SolutionId solutionId, boolean includeLocked)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Lock user.
   *
   * @param  userId                  the user id
   * @throws IOException             Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException the resource update exception
   * @throws NoSuchEntityException   the no such element exception
   */
  void lockUser(String userId) throws IOException, ResourceUpdateException, NoSuchEntityException;

  /**
   * Unlock user.
   *
   * @param  userId                  the user id
   * @throws IOException             Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException the resource update exception
   * @throws NoSuchEntityException   the no such element exception
   */
  void unlockUser(String userId) throws IOException, ResourceUpdateException, NoSuchEntityException;

  /**
   * Delete user.
   *
   * @param  userId                  the user id
   * @throws IOException             Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException the resource update exception
   * @throws NoSuchEntityException   the no such element exception
   */
  void deleteUser(String userId) throws IOException, ResourceUpdateException, NoSuchEntityException;

  /**
   * Update password.
   *
   * @param  userId                  the user id
   * @param  oldPassword             the old password
   * @param  newPassword             the new password
   * @throws IOException             Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException the resource update exception
   * @throws NoSuchEntityException   the no such element exception
   * @throws MessagingException      if the password updated message could not be sent
   */
  void updatePassword(String userId, String oldPassword, String newPassword)
      throws IOException, ResourceUpdateException, NoSuchEntityException, MessagingException;

  /**
   * Set password from static call.
   *
   * @param  userId                  the user UUID
   * @param  newPassword             the new password
   * @throws IOException             Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException the resource update exception
   * @throws NoSuchEntityException   the no such element exception
   * @throws MessagingException      if the password updated message could not be sent
   */
  void setPassword(String userId, String newPassword)
      throws IOException, ResourceUpdateException, NoSuchEntityException, MessagingException;

  /**
   * Adds the super admin.
   *
   * @param  userModel                    the user
   * @return                              the string
   * @throws IOException                  Signals that an I/O exception has occurred.
   * @throws ConflictingResourceException the conflicting resource exception
   * @throws ResourceUpdateException      the resource update exception
   * @throws InvalidEntityException
   * @throws MessagingException           if the account created message could not be sent
   */
  String addSuperAdmin(User userModel) throws IOException, ConflictingResourceException,
      ResourceUpdateException, InvalidEntityException, MessagingException;

  /**
   * Get a user by user name
   * 
   * @param  username              Name of the user
   * @return
   * @throws NoSuchEntityException
   * @throws IOException
   */
  User getUserByName(String username) throws NoSuchEntityException, IOException;

  /**
   * update the role of a user
   * 
   * @param  userId
   * @param  newRole
   * @throws NoSuchEntityException
   * @throws IllegalArgumentException
   * @throws InvalidEntityException
   * @throws IOException
   */
  void updateRole(String userId, MyDataRole newRole)
      throws NoSuchEntityException, InvalidEntityException, IOException;
}

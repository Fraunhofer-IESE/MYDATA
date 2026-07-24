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

import de.fraunhofer.iese.mydata.exception.ForbiddenException;
import de.fraunhofer.iese.mydata.exception.MessagingException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.oauth.PasswordResetToken;

import java.io.IOException;

public interface ITokenService {

  /**
   * Generate a token for the given user
   * 
   * @param  username              The user for whom the token will be created
   * @return                       the password reset token
   * @throws IOException
   * @throws NoSuchEntityException if the user does not exist
   * @throws ForbiddenException    if the maximum number of allowed password requests has been
   *                                 reached
   */
  PasswordResetToken generateToken(String username)
      throws IOException, NoSuchEntityException, ForbiddenException;

  PasswordResetToken getToken(String token);

  PasswordResetToken getTokenForUser(User user);

  /**
   * Validate the token
   * 
   * @param  userId                The id of the user for whom the token will be validated
   * @param  token                 the token in string format
   * @throws IOException
   * @throws ForbiddenException    If the token is not valid for this user
   * @throws NoSuchEntityException if the token or the user does not exist
   */
  void validateToken(String userId, String token)
      throws IOException, ForbiddenException, NoSuchEntityException;

  void validateTokenAndSetPassword(String userId, String token, String newPassword)
      throws IOException, ForbiddenException, NoSuchEntityException, ResourceUpdateException,
      MessagingException;

  void deleteToken(String token);

  void deleteTokenByUserUuid(String userUuid) throws IOException, NoSuchEntityException;
}

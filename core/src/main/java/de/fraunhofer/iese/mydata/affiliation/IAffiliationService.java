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

package de.fraunhofer.iese.mydata.affiliation;

import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import java.io.IOException;
import java.util.Set;

/**
 * The Interface IAffiliationService.
 */
public interface IAffiliationService {

  /**
   * Adds the affiliation.
   *
   * @param  affiliation                  the solution
   * @return                              true, if successful
   * @throws IOException                  Signals that an I/O exception has occurred.
   * @throws ConflictingResourceException
   * @throws ResourceUpdateException
   * @throws InvalidEntityException
   */
  AffiliationId addAffiliation(Affiliation affiliation) throws IOException,
      ConflictingResourceException, ResourceUpdateException, InvalidEntityException;

  /**
   * Checks if this affiliationId exists
   * 
   * @param  affiliationId
   * @return                        true if this id is already in the DB, false if not
   * @throws IOException
   * @throws InvalidEntityException
   */
  boolean affiliationIdExists(AffiliationId affiliationId)
      throws IOException, InvalidEntityException;

  /**
   * Update affiliation.
   *
   * @param  affiliation             the solution
   * @throws IOException             Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException
   * @throws NoSuchEntityException
   */
  void updateAffiliation(Affiliation affiliation)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException;

  /**
   * Gets the affiliation.
   *
   * @param  affiliationId          the affiliation component_id
   * @return                        the affiliation
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws InvalidEntityException
   * @throws NoSuchEntityException
   */
  Affiliation getAffiliation(AffiliationId affiliationId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Gets the affiliation ids.
   *
   * @param  includeLocked : if true, also includes locked affiliations
   * @return               the affiliation ids
   * @throws IOException   Signals that an I/O exception has occurred.
   */
  Set<AffiliationId> listAffiliations(boolean includeLocked) throws IOException;

  /**
   * Gets the affiliations.
   *
   * @param  includeLocked : if true, also includes locked affiliations
   * @return               the affiliations
   * @throws IOException   Signals that an I/O exception has occurred.
   */
  Set<Affiliation> getAffiliations(boolean includeLocked) throws IOException;

  /**
   * Lock affiliation.
   *
   * @param  affiliationId
   * @throws IOException             Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException
   * @throws NoSuchEntityException
   * @throws InvalidEntityException
   */
  void lockAffiliation(AffiliationId affiliationId)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException;

  /**
   * Unlock affiliation.
   *
   * @param  affiliationId
   * @throws IOException             Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException
   * @throws NoSuchEntityException
   * @throws InvalidEntityException
   */
  void unlockAffiliation(AffiliationId affiliationId)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException;

  /**
   * Delete affiliation.
   *
   * @param  affiliationId
   * @throws IOException             Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException
   * @throws NoSuchEntityException
   * @throws InvalidEntityException
   */
  void deleteAffiliation(AffiliationId affiliationId)
      throws IOException, ResourceUpdateException, NoSuchEntityException, InvalidEntityException;

  /**
   * Returns the affiliations for a specific user It can be a set as a super_admin is per default
   * assigned to all affiliations
   * 
   * @param  userUUID
   * @return                       a list(set) of affiliations
   * @throws IOException
   * @throws NoSuchEntityException
   */
  Affiliation getAffiliationByUserUUID(String userUUID) throws IOException, NoSuchEntityException;

  /**
   * Returns the affiliationids for a specific user It can be a set as a super_admin is per default
   * assigned to all affiliations
   * 
   * @param  userUUID
   * @return                       a list(set) of affiliationids
   * @throws IOException
   * @throws NoSuchEntityException
   */
  AffiliationId getAffiliationIdByUserUUID(String userUUID)
      throws IOException, NoSuchEntityException;

  /**
   * Returns the affiliationId a solutionid (solution) belongs to
   * 
   * @param  solutionId
   * @return                        an affiliationid
   * @throws IOException
   * @throws InvalidEntityException if the solution id is not valid
   * @throws NoSuchEntityException  if the solution does not exist
   */
  AffiliationId getAffiliationIdBySolutionId(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Returns the affiliation a solutionid (solution) belongs to
   * 
   * @param  solutionId
   * @return                        an affiliation
   * @throws IOException
   * @throws InvalidEntityException if the solution id is not valid
   * @throws NoSuchEntityException  if the solution does not exist
   */
  Affiliation getAffiliationBySolutionId(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

}

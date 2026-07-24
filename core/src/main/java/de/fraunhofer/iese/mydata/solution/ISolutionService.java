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

package de.fraunhofer.iese.mydata.solution;

import de.fraunhofer.iese.mydata.affiliation.AffiliationId;
import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Set;

/**
 * The Interface ISolutionService.
 */
public interface ISolutionService {

    /**
     * Adds the solution.
     *
     * @param  solution                     the solution
     * @param  affiliationId                the id of the affiliation the solution belongs to
     * @return                              the solution id of the newly added solution
     * @throws IOException                  Signals that an I/O exception has occurred.
     * @throws ConflictingResourceException if there is already a solution with the given id
     * @throws ResourceUpdateException      if add is not successful
     * @throws InvalidEntityException       if the given solution or the fiven affiliation id is not
     *                                          valid
     * @throws NoSuchEntityException        if the affiliation does not exist
     */
    SolutionId addSolution(Solution solution, AffiliationId affiliationId)
            throws IOException, ConflictingResourceException, ResourceUpdateException,
            InvalidEntityException, NoSuchEntityException;

    /**
     * Solution exists.
     *
     * @param  solutionId             the id of the solution to check
     * @return                        true, if successful
     * @throws IOException            Signals that an I/O exception has occurred.
     * @throws InvalidEntityException if the given solution id is not valid
     */
    boolean solutionExists(SolutionId solutionId) throws IOException, InvalidEntityException;

    /**
     * Update solution.
     *
     * @param  solution                the solution
     * @throws IOException             Signals that an I/O exception has occurred.
     * @throws ResourceUpdateException if update is not successful
     * @throws NoSuchEntityException   if there is no solution to update
     * @throws InvalidEntityException  if the given solution is not valid
     */
    void updateSolution(Solution solution) throws IOException, ResourceUpdateException,
            NoSuchEntityException, InvalidEntityException;

    /**
     * Gets the solution.
     *
     * @param  solutionId             the solution component_id
     * @return                        the solution
     * @throws IOException            Signals that an I/O exception has occurred.
     * @throws NoSuchEntityException  if there is no solution with the given id
     * @throws InvalidEntityException if the given solution id not valid
     */
    Solution getSolution(SolutionId solutionId)
            throws IOException, NoSuchEntityException, InvalidEntityException;

    /**
     * Set solutions.
     *
     * @param  affiliationId          the affiliation id
     * @param  includeLocked          the include locked
     * @return                        the list
     * @throws IOException            Signals that an I/O exception has occurred.
     * @throws NoSuchEntityException  if there is no affilition with the given id
     * @throws InvalidEntityException if the given affiliation id is not valid
     */
    Set<SolutionId> listSolutions(AffiliationId affiliationId, boolean includeLocked)
            throws IOException, NoSuchEntityException, InvalidEntityException;

    /**
     * Set solutions.
     *
     * @param  affiliationId          the affiliation id
     * @param  includeLocked          the include locked
     * @return                        the list
     * @throws IOException            Signals that an I/O exception has occurred.
     * @throws NoSuchEntityException  if there is no affilition with the given id
     * @throws InvalidEntityException if the given {@link AffiliationId} is not valiud
     */
    Set<Solution> getSolutions(AffiliationId affiliationId, boolean includeLocked)
            throws IOException, NoSuchEntityException, InvalidEntityException;

    /**
     * Set solutions.
     *
     * @param  userId                the user id
     * @param  includeLocked         the include locked
     * @return                       the list
     * @throws IOException           Signals that an I/O exception has occurred.
     * @throws NoSuchEntityException if there is no user with the given id
     */
    Set<SolutionId> listSolutions(String userId, boolean includeLocked)
            throws IOException, NoSuchEntityException;

    /**
     * Set solutions.
     *
     * @param  userId                the user id
     * @param  includeLocked         the include locked
     * @return                       the list
     * @throws IOException           Signals that an I/O exception has occurred.
     * @throws NoSuchEntityException if there is no user with the given id
     */
    Set<Solution> getSolutions(String userId, boolean includeLocked)
            throws IOException, NoSuchEntityException;

    /**
     * Delete solution.
     *
     * @param  solutionId              the solution id
     * @throws IOException             Signals that an I/O exception has occurred.
     * @throws ResourceUpdateException if delete is not successful
     * @throws NoSuchEntityException   the no such element exception
     * @throws InvalidEntityException  if the given solution id is not valid
     */
    void deleteSolution(SolutionId solutionId) throws IOException, ResourceUpdateException,
            NoSuchEntityException, InvalidEntityException;

    /**
     * Assign user.
     *
     * @param  solutionId              the solution id
     * @param  userUUID                the user component_id
     * @throws IOException             Signals that an I/O exception has occurred.
     * @throws ResourceUpdateException if it was not possible to assign the user to the solution
     * @throws NoSuchEntityException   if there is no solution or user with the given id
     * @throws InvalidEntityException  if the given solution id is not valid
     */
    void assignUser(SolutionId solutionId, String userUUID) throws IOException,
            ResourceUpdateException, NoSuchEntityException, InvalidEntityException;

    /**
     * Unassign user.
     *
     * @param  solutionId              the solution id
     * @param  userUUID                the user component_id
     * @throws IOException             Signals that an I/O exception has occurred.
     * @throws ResourceUpdateException if it was not possible to unassign the user to the solution
     * @throws NoSuchEntityException   if there is no solution or user with the given id
     * @throws InvalidEntityException  if the given solution id is not valid
     */
    void unassignUser(SolutionId solutionId, String userUUID) throws IOException,
            ResourceUpdateException, NoSuchEntityException, InvalidEntityException;

    /**
     * Returns the timezone for the policy's solution.
     *
     * @param  solutionId             the solution id
     * @return                        ZoneId
     * @throws IOException            Signals that an I/O exception has occurred.
     * @throws NoSuchEntityException  the no such element exception
     * @throws InvalidEntityException if the given solution id is not valid
     */
    ZoneId getZoneId(SolutionId solutionId)
            throws IOException, NoSuchEntityException, InvalidEntityException;
}

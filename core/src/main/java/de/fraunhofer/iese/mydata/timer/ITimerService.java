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

package de.fraunhofer.iese.mydata.timer;

import de.fraunhofer.iese.mydata.exception.ConflictingResourceException;
import de.fraunhofer.iese.mydata.exception.InvalidEntityException;
import de.fraunhofer.iese.mydata.exception.NoSuchEntityException;
import de.fraunhofer.iese.mydata.exception.ResourceUpdateException;
import de.fraunhofer.iese.mydata.solution.SolutionId;

import java.io.IOException;
import java.util.Set;

/**
 * The Interface ITimerService.
 */
public interface ITimerService {

  /**
   * Adds a timer.
   *
   * @param  timer                        the timer
   * @return                              true, if the timer was added successfully, false otherwise
   * @throws IOException                  Signals that an I/O exception has occurred.
   * @throws ConflictingResourceException if a timer with the same id already exists
   * @throws ResourceUpdateException      if it was not possible to add the timer
   * @throws InvalidEntityException       if the given timer is not valid
   * @throws NoSuchEntityException        if the solution for the timer does not exist
   */
  TimerId addTimer(Timer timer) throws IOException, ConflictingResourceException,
      ResourceUpdateException, InvalidEntityException, NoSuchEntityException;

  /**
   * Gets the timer.
   *
   * @param  timerId                the timer timerid
   * @return                        the latest timer
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws NoSuchEntityException  the no such element exception
   * @throws InvalidEntityException if the given timer id is not valid
   */
  Timer getTimer(TimerId timerId) throws IOException, NoSuchEntityException, InvalidEntityException;

  /**
   * Gets all timers of the solution
   *
   * @param  solutionId             the solutionId
   * @return                        all timer
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws InvalidEntityException if the given solution id is not valid
   * @throws NoSuchEntityException  if the solution does not exist
   */
  Set<Timer> getTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Gets all timer ids of the solution
   *
   * @param  solutionId             the solutionid
   * @return                        all timer IDs
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws InvalidEntityException if the given solution id is not valid
   * @throws NoSuchEntityException  if the solution does not exist
   */
  Set<TimerId> listTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Gets all deployed timers of the solution.
   *
   * @param  solutionId             the solutionId
   * @return                        all timer
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws InvalidEntityException if the given solution id is not valid
   * @throws NoSuchEntityException  if the solution does not exist
   */
  Set<Timer> getDeployedTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Gets all timer of the solution.
   *
   * @param  solutionId             the solutionId
   * @return                        all timer
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws InvalidEntityException if the given solution id is not valid
   * @throws NoSuchEntityException  if the solution does not exist
   */
  Set<Timer> getRevokedTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Provides a list of currently deployed timers.
   *
   * @param  solutionId             the solution id
   * @return                        list of currently deployed timer ids
   * @throws IOException            communication failure
   * @throws InvalidEntityException if the given solution id is not valid
   * @throws NoSuchEntityException  if the solution does not exist
   */
  Set<TimerId> listDeployedTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Provides a list of currently revoked timers.
   *
   * @param  solutionId             the solution id
   * @return                        list of currently revoked timer ids
   * @throws IOException            communication failure
   * @throws InvalidEntityException if the given solution id is not valid
   * @throws NoSuchEntityException  if the solution does not exist
   */
  Set<TimerId> listRevokedTimers(SolutionId solutionId)
      throws IOException, InvalidEntityException, NoSuchEntityException;

  /**
   * Checks if a timer is deployed.
   *
   * @param  timerId                the timer timerid
   * @return                        true, if is deployed
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws NoSuchEntityException  the no such element exception
   * @throws InvalidEntityException if the given timer id is not valid
   */
  boolean isTimerDeployed(TimerId timerId)
      throws IOException, NoSuchEntityException, InvalidEntityException;

  /**
   * Deploys a timer at the local PDP of the solution.
   *
   * @param  timerId                 the timerid of the timer to be deployed. Will be retrieved via
   *                                   the PRP
   * @throws IOException             if connection problem occurs
   * @throws NoSuchEntityException   timer not found in PMP database
   * @throws ResourceUpdateException if it was not possible to deploy the timer
   * @throws InvalidEntityException  if the given timer id is not valid
   */
  void deployTimer(TimerId timerId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException;

  /**
   * Revokes a currently deployed timer.
   *
   * @param  timerId                 the ID of the timer to be revoked
   * @throws IOException             if connection problem occurs
   * @throws NoSuchEntityException   the no such element exception
   * @throws ResourceUpdateException if it was not possible to remove the timer
   * @throws InvalidEntityException  if the given timer id is not valid
   */
  void revokeTimer(TimerId timerId)
      throws IOException, NoSuchEntityException, ResourceUpdateException, InvalidEntityException;

  /**
   * Removes the timer.
   *
   * @param  timerId                      the timerId to delete
   * @throws IOException                  Signals that an I/O exception has occurred.
   * @throws ResourceUpdateException      if it was not possible to delete the timer
   * @throws NoSuchEntityException        the no such element exception
   * @throws InvalidEntityException       if the given timer id is not valid
   * @throws ConflictingResourceException if the timer to be deleted is deployed
   */
  void deleteTimer(TimerId timerId) throws IOException, ResourceUpdateException,
      NoSuchEntityException, InvalidEntityException, ConflictingResourceException;

  /**
   * Updates a currently active timer.
   *
   * @param  timer                        the timer to be updated
   * @return                              the timer id
   * @throws IOException
   * @throws ResourceUpdateException      if it was not possible to update the timer
   * @throws NoSuchEntityException
   * @throws InvalidEntityException       if the given timer is not valid
   * @throws ConflictingResourceException
   */
  TimerId updateTimer(Timer timer) throws IOException, ResourceUpdateException,
      NoSuchEntityException, InvalidEntityException, ConflictingResourceException;

  /**
   * Timer exists.
   *
   * @param  timerId                the timer id
   * @return                        true, if successful
   * @throws IOException            Signals that an I/O exception has occurred.
   * @throws InvalidEntityException if the given timer id is not valid
   */
  boolean timerExists(TimerId timerId) throws IOException, InvalidEntityException;
}

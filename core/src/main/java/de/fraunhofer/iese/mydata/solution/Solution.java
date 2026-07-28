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

import de.fraunhofer.iese.mydata.affiliation.Affiliation;
import de.fraunhofer.iese.mydata.client.LibraryClient;
import de.fraunhofer.iese.mydata.common.Hide;
import de.fraunhofer.iese.mydata.common.MyDataEntity;
import de.fraunhofer.iese.mydata.component.information.PepComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PipComponentInformation;
import de.fraunhofer.iese.mydata.component.information.PxpComponentInformation;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.solution.validation.OnlyMatchingChildren;
import de.fraunhofer.iese.mydata.timer.Timer;
import de.fraunhofer.iese.mydata.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@OnlyMatchingChildren
@Table(indexes = {
    @Index(columnList = "solution_id", unique = true)
})
public class Solution extends MyDataEntity {

  private static final String PXP_COMPONENT_INFORMATION_CAN_NOT_BE_NULL = "PxpComponentInformation can not be null";

  private static final String PIP_COMPONENT_INFORMATION_CAN_NOT_BE_NULL = "PipComponentInformation can not be null";

  private static final String PEP_COMPONENT_INFORMATION_CAN_NOT_BE_NULL = "PepComponentInformation can not be null";

  private static final String USER_CAN_NOT_BE_NULL = "User can not be null";

  private static final String POLICY_CAN_NOT_BE_NULL = "Policy can not be null";

  private static final String TIMER_CAN_NOT_BE_NULL = "Timer can not be null";

  private static final String LIBRARY_CLIENT_CAN_NOT_BE_NULL = "LibraryClient can not be null";

  @NotNull
  @Valid
  @EmbeddedId
  private SolutionId solutionId;

  @NotBlank
  @Column
  private String name;

  @NotNull
  @Column
  private Boolean lockStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @NotNull
  @Valid
  private Timezone timezone;

  @Column
  private String firstDayOfWeek;

  @Hide
  @JoinTable(name = "solution_user", indexes = {
      @Index(columnList = "solution_id, user_uuid", unique = true)
  }, joinColumns = {
      @JoinColumn(name = "solution_id")
  }, inverseJoinColumns = {
      @JoinColumn(name = "user_uuid")
  })
  @ManyToMany(fetch = FetchType.LAZY, cascade = {
      CascadeType.PERSIST, CascadeType.MERGE
  })
  private Set<@Valid User> users;

  @OneToMany(mappedBy = "solution", orphanRemoval = true, fetch = FetchType.LAZY, cascade = {
      CascadeType.ALL
  })
  private Set<@Valid PepComponentInformation> peps;

  @OneToMany(mappedBy = "solution", orphanRemoval = true, fetch = FetchType.LAZY, cascade = {
      CascadeType.ALL
  })
  private Set<@Valid PxpComponentInformation> pxps;

  @OneToMany(mappedBy = "solution", orphanRemoval = true, fetch = FetchType.LAZY, cascade = {
      CascadeType.ALL
  })
  private Set<@Valid PipComponentInformation> pips;

  @OneToMany(mappedBy = "solution", orphanRemoval = true, fetch = FetchType.LAZY, cascade = {
      CascadeType.ALL
  })
  private Set<@Valid Timer> timers;

  @OneToMany(mappedBy = "solution", orphanRemoval = true, fetch = FetchType.LAZY, cascade = {
      CascadeType.ALL
  })
  private Set<@Valid Policy> policies;

  @OneToMany(mappedBy = "solution", orphanRemoval = true, fetch = FetchType.LAZY, cascade = {
      CascadeType.PERSIST, CascadeType.MERGE
  })
  private Set<@Valid LibraryClient> libraryClients;

  @Hide
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "affiliation_id")
  private Affiliation affiliation;

  /**
   * required by JPA
   */
  public Solution() {
    // required by JPA
  }

  /**
   * @param sid : the solutionid for this solution. It can not be null.
   */
  public Solution(SolutionId sid) {
    if (sid == null) {
      throw new IllegalArgumentException("SolutionId can not be null");
    }
    this.solutionId = sid;
    this.lockStatus = false;
    this.timezone = new Timezone();
  }

  /**
   * @param sid : the solutionid for this solution. It can not be null.
   */
  public void setSolutionId(SolutionId sid) {
    if (sid == null) {
      throw new IllegalArgumentException("SolutionId can not be null");
    }
    this.solutionId = sid;
  }

  /**
   * @param urn : the solutionid as a string for this solution. It can not be null.
   */
  public void setSolutionId(String urn) {
    if (urn == null) {
      throw new IllegalArgumentException("URN can not be null");
    }
    final SolutionId sid = new SolutionId(urn);
    this.solutionId = sid;
  }

  /**
   * @return the list of users for the current solution
   */
  public Set<User> getUsers() {
    if (this.users == null) {
      this.users = new HashSet<>();
    }
    return this.users;
  }

  /**
   * @param users : the list of users to be set to the current solution. It can not be null.
   */
  public void setUsers(Set<User> users) {
    if (users == null) {
      throw new IllegalArgumentException("Users can not be null");
    }
    this.getUsers();
    this.users.clear();
    this.users.addAll(users);
  }

  /**
   * @return the list of policies for the current solution
   */
  public Set<Policy> getPolicies() {
    if (this.policies == null) {
      this.policies = new HashSet<>();
    }
    return this.policies;
  }

  /**
   * @param policies : the list of policies to be set to the current solution. It can not be null.
   */
  public void setPolicies(Set<Policy> policies) {
    if (policies == null) {
      throw new IllegalArgumentException("Policies can not be null");
    }
    this.getPolicies();
    this.policies.clear();
    this.policies.addAll(policies);
  }

  /**
   * @return the list of timers for the current solution
   */
  public Set<Timer> getTimers() {
    if (this.timers == null) {
      this.timers = new HashSet<>();
    }
    return this.timers;
  }

  /**
   * @param timers : the list of timers to be set to the current solution. It can not be null.
   */
  public void setTimers(Set<Timer> timers) {
    if (timers == null) {
      throw new IllegalArgumentException("Timers can not be null");
    }
    this.getTimers();
    this.timers.clear();
    this.timers.addAll(timers);
  }

  /**
   * @return the list of PIPs for the current solution
   */
  public Set<PipComponentInformation> getPips() {
    if (this.pips == null) {
      this.pips = new HashSet<>();
    }
    return this.pips;
  }

  /**
   * @param pips : the list of PIPs to be set to the current solution. It can not be null.
   */
  public void setPips(Set<PipComponentInformation> pips) {
    if (pips == null) {
      throw new IllegalArgumentException(PIP_COMPONENT_INFORMATION_CAN_NOT_BE_NULL);
    }
    this.getPips();
    this.pips.clear();
    this.pips.addAll(pips);
  }

  /**
   * @return the list of PEPs for the current solution
   */
  public Set<PepComponentInformation> getPeps() {
    if (this.peps == null) {
      this.peps = new HashSet<>();
    }
    return this.peps;
  }

  /**
   * @param peps : the list of PEPs to be set to the current solution. It can not be null.
   */
  public void setPeps(Set<PepComponentInformation> peps) {
    if (peps == null) {
      throw new IllegalArgumentException(PEP_COMPONENT_INFORMATION_CAN_NOT_BE_NULL);
    }
    this.getPeps();
    this.peps.clear();
    this.peps.addAll(peps);
  }

  /**
   * @return the list of PXPs for the current solution
   */
  public Set<PxpComponentInformation> getPxps() {
    if (this.pxps == null) {
      this.pxps = new HashSet<>();
    }
    return this.pxps;
  }

  /**
   * @param pxps : the list of PXPs to be set to the current solution. It can not be null.
   */
  public void setPxps(Set<PxpComponentInformation> pxps) {
    if (pxps == null) {
      throw new IllegalArgumentException(PXP_COMPONENT_INFORMATION_CAN_NOT_BE_NULL);
    }
    this.getPxps();
    this.pxps.clear();
    this.pxps.addAll(pxps);
  }

  /**
   * @return true if solutions is locked, false otherwise
   */
  public Boolean getLockStatus() {
    return this.lockStatus;
  }

  /**
   * @return true if solution is locked, false otherwise
   */
  public Boolean isSolutionLocked() {
    return this.lockStatus;
  }

  /**
   * @param component : add a PXP to the current solution. It can not be null.
   */
  public void addPxpComponentInformation(PxpComponentInformation component) {
    if (component != null) {
      this.getPxps().add(component);
      component.setSolution(this);
    } else {
      throw new IllegalArgumentException(PXP_COMPONENT_INFORMATION_CAN_NOT_BE_NULL);
    }
  }

  /**
   * @param component : remove a PXP from the current solution. It can not be null.
   */
  public void removePxpComponentInformation(PxpComponentInformation component) {
    if (component != null) {
      this.getPxps().remove(component);
      component.setSolution(null);
    } else {
      throw new IllegalArgumentException(PXP_COMPONENT_INFORMATION_CAN_NOT_BE_NULL);
    }
  }

  /**
   * @param component : add a PEP to the current solution. It can not be null.
   */
  public void addPepComponentInformation(PepComponentInformation component) {
    if (component != null) {
      this.getPeps().add(component);
      component.setSolution(this);
    } else {
      throw new IllegalArgumentException(PEP_COMPONENT_INFORMATION_CAN_NOT_BE_NULL);
    }
  }

  /**
   * @param component : remove a PEP from the current solution. It can not be null.
   */
  public void removePepComponentInformation(PepComponentInformation component) {
    if (component != null) {
      this.getPeps().remove(component);
      component.setSolution(null);
    } else {
      throw new IllegalArgumentException(PEP_COMPONENT_INFORMATION_CAN_NOT_BE_NULL);
    }
  }

  /**
   * @param component : add a PIP to the current solution. It can not be null.
   */
  public void addPipComponentInformation(PipComponentInformation component) {
    if (component != null) {
      this.getPips().add(component);
      component.setSolution(this);
    } else {
      throw new IllegalArgumentException(PIP_COMPONENT_INFORMATION_CAN_NOT_BE_NULL);
    }
  }

  /**
   * @param component : remove a PIP from the current solution. It can not be null.
   */
  public void removePipComponentInformation(PipComponentInformation component) {
    if (component != null) {
      this.getPips().remove(component);
      component.setSolution(null);
    } else {
      throw new IllegalArgumentException(PIP_COMPONENT_INFORMATION_CAN_NOT_BE_NULL);
    }
  }

  /**
   * @param p : add a Policy to the current solution. It can not be null.
   */
  public void addPolicy(Policy p) {
    if (p != null) {
      this.getPolicies().add(p);
      p.setSolution(this);
    } else {
      throw new IllegalArgumentException(POLICY_CAN_NOT_BE_NULL);
    }
  }

  /**
   * @param p : remove a policy from the current solution. It can not be null.
   */
  public void removePolicy(Policy p) {
    if (p != null) {
      this.getPolicies().remove(p);
      p.setSolution(null);
    } else {
      throw new IllegalArgumentException(POLICY_CAN_NOT_BE_NULL);
    }
  }

  /**
   * @param t : add a timer to the current solution. It can not be null.
   */
  public void addTimer(Timer t) {
    if (t != null) {
      this.getTimers().add(t);
      t.setSolution(this);
    } else {
      throw new IllegalArgumentException(TIMER_CAN_NOT_BE_NULL);
    }
  }

  /**
   * @param t : remove a timer from the current solution. It can not be null.
   */
  public void removeTimer(Timer t) {
    if (t != null) {
      this.getTimers().remove(t);
      t.setSolution(null);
    } else {
      throw new IllegalArgumentException(TIMER_CAN_NOT_BE_NULL);
    }
  }

  /**
   * @param u : add an user to the current solution. It can not be null.
   */
  public void addUser(User u) {
    if (u != null) {
      this.getUsers().add(u);
      u.addSolution(this);
    } else {
      throw new IllegalArgumentException(USER_CAN_NOT_BE_NULL);
    }
  }

  /**
   * @param u : remove an user from the current solution. It can not be null.
   */
  public void removeUser(User u) {
    if (u != null) {
      this.getUsers().remove(u);
      u.removeSolution(this);
    } else {
      throw new IllegalArgumentException(USER_CAN_NOT_BE_NULL);
    }
  }

  /**
   * @return the list of clients for the current solution
   */
  public Set<LibraryClient> getLibraryClients() {
    if (this.libraryClients == null) {
      this.libraryClients = new HashSet<>();
    }
    return this.libraryClients;
  }

  /**
   * @param libraryClients : the list of clients to be set to the current solution. It can not be
   *                         null.
   */
  public void setLibraryClients(Set<LibraryClient> libraryClients) {
    if (libraryClients == null) {
      throw new IllegalArgumentException("Clients can not be null");
    }
    this.getLibraryClients();
    this.libraryClients.clear();
    this.libraryClients.addAll(libraryClients);
  }

  /**
   * @param libraryClient add a library cliuent to the this solution. It can not be null.
   */
  public void addLibraryClient(LibraryClient libraryClient) {
    if (libraryClient != null) {
      this.getLibraryClients().add(libraryClient);
      libraryClient.setSolution(this);
    } else {
      throw new IllegalArgumentException(LIBRARY_CLIENT_CAN_NOT_BE_NULL);
    }
  }

  /**
   * @param libraryClient remove a library client from this solution. It can not be null.
   */
  public void removeLibraryClient(LibraryClient libraryClient) {
    if (libraryClient != null) {
      this.getLibraryClients().remove(libraryClient);
      libraryClient.setSolution(null);
    } else {
      throw new IllegalArgumentException(LIBRARY_CLIENT_CAN_NOT_BE_NULL);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Solution)) {
      return false;
    }

    final Solution solution = (Solution) o;

    return new EqualsBuilder().append(this.solutionId, solution.solutionId).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(this.solutionId).toHashCode();
  }

}

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

package de.fraunhofer.iese.mydata.eventhistory;

import de.fraunhofer.iese.mydata.pdp.interfaces.AbstractEventRepository;
import de.fraunhofer.iese.mydata.policy.Policy;
import de.fraunhofer.iese.mydata.policy.PolicyId;
import de.fraunhofer.iese.mydata.policy.event.ActionId;
import de.fraunhofer.iese.mydata.policy.event.Event;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEvent;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEventParameter;
import de.fraunhofer.iese.mydata.policy.event.history.HistoricEventTrackItem;
import de.fraunhofer.iese.mydata.policy.event.history.ValueChangeEntity;
import de.fraunhofer.iese.mydata.policy.exception.ValueNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component("mydataEventRepository")
public class EventRepository extends AbstractEventRepository {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EventRepository.class);

  @Autowired
  private HistoricEventDao historicEventDao;

  @Autowired
  private ValueChangeEntityDao valueChangeEntityDao;

  @PersistenceContext(unitName = "mydata")
  private EntityManager entityManager;

  @Autowired
  @Qualifier("mydataEntityManagerFactory")
  private EntityManagerFactory emf;

  @Override
  public List<HistoricEvent> findByActionIdAndHistoricEventParametersAndOccurredAtMsBetween(
      ActionId actionId, List<HistoricEventParameter> p, long start, long end) {
    List<HistoricEvent> results = null;
    String query = "select he from historic_event he  where ";
    final List<String> params = new ArrayList<>();
    int cnt = 1;
    final List<Object> parameters = new ArrayList<>();

    for (final HistoricEventParameter historicEventParameter : p) {
      if (historicEventParameter.getJsonPath() == null) {
        params.add(
            "EXISTS ( SELECT hep.historicEvent.id from historic_event_parameter hep where hep.name=?"
                + cnt
                + " and hep.value=?"
                + (cnt + 1)
                + " and he.id=hep.historicEvent.id )");
        parameters.add(historicEventParameter.getName());
        parameters.add(historicEventParameter.getValue());
        cnt += 2;
      } else {
        params.add(
            "EXISTS ( SELECT hep.historicEvent.id from historic_event_parameter hep where hep.name=?"
                + cnt
                + " and hep.value=?"
                + (cnt + 1)
                + " and hep.jsonPath=?"
                + (cnt + 2)
                + " and he.id=hep.historicEvent.id )");
        parameters.add(historicEventParameter.getName());
        parameters.add(historicEventParameter.getValue());
        parameters.add(historicEventParameter.getJsonPath());
        cnt += 3;
      }
    }
    query += StringUtils.join(params, " AND ") + "  ";
    query += " and ( he.actionId.urn=?"
        + cnt
        + " and he.occurredAtMs>=?"
        + (cnt + 1)
        + " and he.occurredAtMs<=?"
        + (cnt + 2)
        + ")";
    parameters.add(actionId.getUrn());
    parameters.add(start);
    parameters.add(end);

    try {
      final TypedQuery<HistoricEvent> q = this.entityManager.createQuery(query,
          HistoricEvent.class);
      int k = 1;
      for (final Object par : parameters) {
        q.setParameter(k, par);
        k++;
      }
      results = q.getResultList();
    } catch (final Exception e) {
      LOG.error("Something went wrong by selecting the stored event", e);
    }
    return results;
  }

  @Override
  public List<HistoricEvent> findByActionIdAndOccurredAtMsBetweenParamIndependant(ActionId actionId,
      long start, long end) {

    List<HistoricEvent> results = null;
    final String query = "select he from historic_event he ";
    final String queryWhere = " where he.actionId.urn=?1  and he.occurredAtMs>=?2 and he.occurredAtMs<=?3";
    final List<Object> parameters = new ArrayList<>();
    parameters.add(actionId.getUrn());
    parameters.add(start);
    parameters.add(end);

    try {
      final TypedQuery<HistoricEvent> q = this.entityManager.createQuery(query + queryWhere,
          HistoricEvent.class);
      int k = 1;
      for (final Object par : parameters) {
        q.setParameter(k, par);
        k++;
      }
      results = q.getResultList();
    } catch (final Exception e) {
      LOG.error("Something went wrong by selecting the stored event (parameter independant)", e);
    }
    return results;
  }

  @Override
  @Async
  public void saveEventOccurrence(Event event, Set<HistoricEventTrackItem> trackItems) {
    this.historicEventDao.save(new HistoricEvent(event, trackItems));
  }

  @Override
  public List<HistoricEvent> findByOccurredAtMsBetweenAndActionId(long start, long end,
      ActionId actionId) {
    return this.historicEventDao.findByOccurredAtMsBetweenAndActionId(start, end, actionId);
  }

  @Override
  public long findByOccurredAtMsBeforeAndActionId(long end, ActionId actionId) {
    return this.historicEventDao.findByOccurredAtMsBeforeAndActionId(end, actionId).size();
  }

  @Override
  public long findByOccurredAtMsAfterAndActionId(long start, ActionId actionId) {
    return this.historicEventDao.findByOccurredAtMsAfterAndActionId(start, actionId).size();
  }

  @Override
  public List<HistoricEvent> findByActionId(ActionId actionId) {
    return this.historicEventDao.findOccurredAtMsByActionId(actionId);
  }

  @Override
  public HistoricEvent findByActionIdAndMode(ActionId actionId, String mode,
      List<HistoricEventParameter> parameters) {
    final List<HistoricEvent> notFiltred = this.historicEventDao
        .findByActionIdOrderByOccurredAtMs(actionId);
    return this.filterHistoricEventByModeAndParameters(mode, notFiltred, parameters);
  }

  /**
   * @param  mode       : get the first or the last occurrence of the historic events
   * @param  notFiltred : the list of historic events for a specific actionid
   * @param  parameters : if no parameters are provided in the policy, it means the user is only
   *                      interested when the historic events occurred, independently of the
   *                      parameters it had
   * @return            {@link HistoricEvent}
   */
  public HistoricEvent filterHistoricEventByModeAndParameters(String mode,
      List<HistoricEvent> notFiltred, List<HistoricEventParameter> parameters) {
    List<HistoricEvent> matchingHE = new ArrayList<>();
    for (final HistoricEvent eventFromDB : notFiltred) {
      final Collection<HistoricEventParameter> paramFromDB = eventFromDB
          .getHistoricEventParameters();
      if (parameters != null && !parameters.isEmpty()) {
        for (final HistoricEventParameter policyParameter : parameters) {
          if (paramFromDB.contains(policyParameter)) {
            matchingHE.add(eventFromDB);
          }
        }
      } else {
        matchingHE = notFiltred;
      }
    }
    if (!matchingHE.isEmpty()) {
      if (mode == null || "FIRST".equalsIgnoreCase(mode)) {
        return matchingHE.get(0);
      } else if ("LAST".equalsIgnoreCase(mode)) {
        return matchingHE.get(matchingHE.size() - 1);
      }
    }
    return null;
  }

  @Override
  public List<HistoricEvent> findAll() {
    return this.historicEventDao.findAll();
  }

  @Override
  public void deleteEventOccurrenceByActionId(ActionId actionId) {
    final EntityManager deleteManager = this.emf.createEntityManager();
    final List<Object> parameters = new ArrayList<>();
    parameters.add(actionId.getUrn());
    try {
      final Query query1 = deleteManager.createQuery(
          "delete from historic_event_parameter hep where hep.historicEvent in (select he from historic_event he where he.actionId.urn=?1)");
      final Query query2 = deleteManager
          .createQuery("delete from historic_event he where he.actionId.urn=?1 ");
      int k = 1;
      for (final Object par : parameters) {
        query1.setParameter(k, par);
        query2.setParameter(k, par);
        k++;
      }
      deleteManager.getTransaction().begin();
      query1.setFlushMode(FlushModeType.COMMIT);
      query2.setFlushMode(FlushModeType.COMMIT);
      query1.executeUpdate();
      query2.executeUpdate();
      deleteManager.getTransaction().commit();
    } catch (final Exception e) {
      LOG.error("Something went wrong while deleting event", e);
    } finally {
      if (deleteManager.isOpen()) {
        deleteManager.close();
      }
    }
  }

  public int getNumberOfEntries() {
    final EntityManager queryManager = this.emf.createEntityManager();
    int nbRec = -1;
    try {
      final TypedQuery<HistoricEvent> q = this.entityManager
          .createQuery("select he from historic_event he", HistoricEvent.class);
      nbRec = q.getResultList().size();
    } catch (final Exception e) {
      LOG.error("Something went wrong while couting the events", e);
    } finally {
      if (queryManager != null && queryManager.isOpen()) {
        queryManager.close();
      }
    }
    return nbRec;
  }

  @Override
  public void updateValueChangeBlockPolicyId(Policy newPolicy, PolicyId oldPolicyId) {
    final List<ValueChangeEntity> vceList = this.valueChangeEntityDao
        .findAllByPolicy_PolicyId(oldPolicyId);
    for (final ValueChangeEntity valueChangeEntity : vceList) {
      valueChangeEntity.setPolicy(newPolicy);
      this.valueChangeEntityDao.saveAndFlush(valueChangeEntity);
    }
  }

  @Override
  @Transactional
  public void saveValueChangeBlock(Policy policy, Map<String, String> variableValueChangeBlock) {
    List<ValueChangeEntity> vceList = this.valueChangeEntityDao
        .findAllByPolicy_PolicyId(policy.getPolicyId());
    for (final Map.Entry<String, String> entry : variableValueChangeBlock.entrySet()) {
      final String blockId = entry.getKey();
      final String blockHash = entry.getValue();
      final ValueChangeEntity vce = new ValueChangeEntity(policy, blockHash, blockId, null);
      try {
        if (vceList.stream().noneMatch(v -> Objects.equals(v.getBlockId(), vce.getBlockId()))) {
          this.valueChangeEntityDao.save(vce);
        }
      } catch (final Exception e) {
        LOG.debug(
            "This line does alread exist in the DB, maybe something went wrong by last deployment {}",
            vce, e);
      }
    }

    // Fetch ValueChangeEntity again for newly added blocks and remove all
    // entries that are not in block
    vceList = this.valueChangeEntityDao.findAllByPolicy_PolicyId(policy.getPolicyId());
    final List<ValueChangeEntity> collect = vceList.stream()
        .filter(e -> !variableValueChangeBlock.containsKey(e.getBlockId()))
        .collect(Collectors.toList());
    collect.forEach(e -> this.valueChangeEntityDao.delete(e));
  }

  @Override
  public void deleteValueChangeBlock(PolicyId policyId,
      Map<String, String> variableValueChangeBlock) {
    final EntityManager deleteManager = this.emf.createEntityManager();
    final List<Object> parameters = new ArrayList<>();
    parameters.add(policyId);
    parameters.add(variableValueChangeBlock.keySet());
    try {
      final Query query = deleteManager.createQuery(
          "delete from value_change_entity he where he.policy.policyId=?1 and he.blockId=?2");
      int k = 1;
      for (final Object par : parameters) {
        query.setParameter(k, par);
        k++;
      }
      deleteManager.getTransaction().begin();
      query.setFlushMode(FlushModeType.COMMIT);
      query.executeUpdate();
      deleteManager.getTransaction().commit();
    } catch (final Exception e) {
      LOG.error("Something went wrong while deleting the valuechangeblock", e);
    } finally {
      if (deleteManager.isOpen()) {
        deleteManager.close();
      }
    }

  }

  @Override
  public String getValueChanged(String id, Policy policy) {
    if (this.valueChangeEntityDao.findByPolicy_PolicyIdAndBlockId(policy.getPolicyId(),
        id) != null) {
      return this.valueChangeEntityDao.findByPolicy_PolicyIdAndBlockId(policy.getPolicyId(), id)
          .getValue();
    } else {
      return null;
    }
  }

  @Override
  @Transactional
  public void setValueChanged(Policy policy, String id, String valueInPolicy)
      throws ValueNotFoundException {
    final ValueChangeEntity vce = this.valueChangeEntityDao
        .findByPolicy_PolicyIdAndBlockId(policy.getPolicyId(), id);
    if (vce != null) {
      vce.setValue(valueInPolicy);
      this.valueChangeEntityDao.save(vce);
    } else {
      throw new ValueNotFoundException("Changedvalue not found in the database for policyid "
          + policy.getPolicyId()
          + " and changeentity "
          + id);
    }
  }

  public void deleteValueChangedByPolicyId(String pid) {
    final EntityManager deleteManager = this.emf.createEntityManager();
    final List<Object> parameters = new ArrayList<>();
    parameters.add(pid);
    try {
      final Query query = deleteManager
          .createQuery("delete from value_change_entity he where he.policy.policyId=?1 ");
      int k = 1;
      for (final Object par : parameters) {
        query.setParameter(k, par);
        k++;
      }
      deleteManager.getTransaction().begin();
      query.setFlushMode(FlushModeType.COMMIT);
      query.executeUpdate();
      deleteManager.getTransaction().commit();
    } catch (final Exception e) {
      LOG.error("Something went wrong while deleting the valuechangeentity", e);
    } finally {
      if (deleteManager.isOpen()) {
        deleteManager.close();
      }
    }
  }

}

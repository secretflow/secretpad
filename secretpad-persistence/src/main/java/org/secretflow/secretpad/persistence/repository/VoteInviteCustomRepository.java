/*
 * Copyright 2023 Ant Group Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.secretflow.secretpad.persistence.repository;

import org.secretflow.secretpad.persistence.entity.VoteInviteDO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * VoteInviteCustomRepository.
 *
 * @author cml
 * @date 2023/10/20
 */
@Repository
public class VoteInviteCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<VoteInviteDO> pageQuery(String nodeID, Boolean isProcessed, String type, String keyWord, Pageable page) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<VoteInviteDO> query = cb.createQuery(VoteInviteDO.class);
        Root<VoteInviteDO> root = query.from(VoteInviteDO.class);
        query.select(root);
        query.orderBy(cb.desc(root.get("gmtCreate")));
        List<Predicate> predicates = new ArrayList<>();
        Predicate p1 = cb.equal(root.get("upk").get("votePartitionID"), nodeID);
        predicates.add(p1);
        if (Objects.nonNull(isProcessed)) {
            Predicate action = isProcessed ? cb.notEqual(root.get("action"), "REVIEWING") : cb.equal(root.get("action"), "REVIEWING");
            predicates.add(action);
        }
        if (StringUtils.isNotBlank(keyWord)) {
            predicates.add(cb.like(root.get("desc").as(String.class), "%" + keyWord + "%"));
        }
        if (StringUtils.isNotBlank(type)) {
            predicates.add(cb.equal(root.get("type"), type));
        }
        query.where(predicates.toArray(new Predicate[predicates.size()]));
        TypedQuery<VoteInviteDO> query1 = entityManager.createQuery(query);
        query1.setFirstResult(page.getPageNumber() * page.getPageSize());
        query1.setMaxResults(page.getPageSize());
        List<VoteInviteDO> resultList = query1.getResultList();
        return resultList;
    }

    public Long queryCount(String nodeID, Boolean isProcessed, String type, String keyWord) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VoteInviteDO> root = query.from(VoteInviteDO.class);
        List<Predicate> predicates = new ArrayList<>();
        Predicate p1 = cb.equal(root.get("upk").get("votePartitionID"), nodeID);
        predicates.add(p1);
        if (Objects.nonNull(isProcessed)) {
            Predicate action = isProcessed ? cb.notEqual(root.get("action"), "REVIEWING") : cb.equal(root.get("action"), "REVIEWING");
            predicates.add(action);
        }
        if (StringUtils.isNotBlank(keyWord)) {
            predicates.add(cb.like(root.get("desc").as(String.class), "%" + keyWord + "%"));
        }
        if (StringUtils.isNotBlank(type)) {
            predicates.add(cb.equal(root.get("type"), type));
        }
        query.select(cb.count(root))
                .where(cb.and(predicates.toArray(new Predicate[0])));
        Long singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }
}

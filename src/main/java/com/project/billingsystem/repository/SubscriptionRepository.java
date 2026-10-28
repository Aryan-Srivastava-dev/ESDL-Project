package com.project.billingsystem.repository;

import com.project.billingsystem.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByCustomer_Id(long customerId);
    boolean existsByCustomer_IdAndPlan_IdAndStatus(long customerId, long planId, String status);
}

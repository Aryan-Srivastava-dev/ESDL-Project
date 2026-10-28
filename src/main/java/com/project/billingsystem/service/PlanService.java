package com.project.billingsystem.service;

import com.project.billingsystem.entity.Plan;
import com.project.billingsystem.exception.BusinessRuleException;
import com.project.billingsystem.repository.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanService {
    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public Plan createPlan(Plan plan) {
        if(plan.getPrice() < 0)
            throw new BusinessRuleException("Price cannot be negative");

        return planRepository.save(plan);
    }

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }
}

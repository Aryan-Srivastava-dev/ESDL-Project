package com.project.billingsystem.controller;

import com.project.billingsystem.entity.Plan;
import com.project.billingsystem.service.PlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/plans")
public class PlanController {
    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ResponseEntity<List<Plan>> getAllPlans() {
        List<Plan> plans = planService.getAllPlans();

        return ResponseEntity.ok(plans);
    }

    @PostMapping
    public ResponseEntity<Plan> createPlan(@RequestBody Plan plan) {
        Plan newPlan = planService.createPlan(plan);

        return new ResponseEntity<>(newPlan, HttpStatus.CREATED);
    }
}

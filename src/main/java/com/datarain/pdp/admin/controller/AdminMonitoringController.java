package com.datarain.pdp.admin.controller;

import com.datarain.pdp.admin.dto.AdminSystemOverviewRequest;
import com.datarain.pdp.admin.dto.AdminSystemOverviewResponse;
import com.datarain.pdp.admin.service.AdminMonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminMonitoringController {

    private final AdminMonitoringService adminMonitoringService;

    @GetMapping("/system-overview")
    public AdminSystemOverviewResponse getSystemOverview(
            @Valid @ParameterObject AdminSystemOverviewRequest request,
            HttpServletRequest httpRequest
    ) {
        return adminMonitoringService.getSystemOverview(request, httpRequest);
    }
}

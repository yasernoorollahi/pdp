package com.datarain.pdp.admin.controller;

import com.datarain.pdp.admin.dto.JobControlChangeResponse;
import com.datarain.pdp.admin.dto.JobControlResponse;
import com.datarain.pdp.admin.dto.JobControlUpdateRequest;
import com.datarain.pdp.admin.service.AdminJobControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/jobs")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin / Jobs", description = "Admin job control and runtime toggles.")
public class AdminJobControlController {

    private final AdminJobControlService adminJobControlService;

    // Access policy (choose one):
    // - ADMIN only
    @GetMapping
    @Operation(summary = "Get effective job control status.")
    public JobControlResponse getStatus() {
        return adminJobControlService.getStatus();
    }

    // Access policy (choose one):
    // - ADMIN only
    @GetMapping("/latest")
    @Operation(summary = "Get latest job control change.")
    public JobControlChangeResponse getLatestChange() {
        return adminJobControlService.getLatestChange();
    }

    // Access policy (choose one):
    // - ADMIN only
    @PutMapping
    @Operation(summary = "Update job control overrides.")
    public JobControlResponse update(
            @Valid @RequestBody JobControlUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return adminJobControlService.update(request, httpServletRequest);
    }
}

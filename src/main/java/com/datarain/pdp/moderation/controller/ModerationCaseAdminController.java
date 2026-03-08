package com.datarain.pdp.moderation.controller;

import com.datarain.pdp.moderation.dto.ModerationCaseActionRequest;
import com.datarain.pdp.moderation.dto.ModerationCaseCreateRequest;
import com.datarain.pdp.moderation.dto.ModerationCaseResponse;
import com.datarain.pdp.moderation.entity.ModerationStatus;
import com.datarain.pdp.moderation.service.ModerationCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/moderation/cases")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ModerationCaseAdminController {

    private final ModerationCaseService moderationCaseService;

    @GetMapping
    public Page<ModerationCaseResponse> getCases(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) ModerationStatus status
    ) {
        return moderationCaseService.findAll(pageable, status);
    }

    @GetMapping("/{id}")
    public ModerationCaseResponse getById(@PathVariable UUID id) {
        return moderationCaseService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_SYSTEM')")
    public ModerationCaseResponse createCase(@Valid @RequestBody ModerationCaseCreateRequest request) {
        return moderationCaseService.createCase(request);
    }

    @PostMapping("/{id}/approve")
    public ModerationCaseResponse approve(@PathVariable UUID id,
                                          @Valid @RequestBody(required = false) ModerationCaseActionRequest request) {
        return moderationCaseService.approve(id, normalizeRequest(request));
    }

    @PostMapping("/{id}/reject")
    public ModerationCaseResponse reject(@PathVariable UUID id,
                                         @Valid @RequestBody(required = false) ModerationCaseActionRequest request) {
        return moderationCaseService.reject(id, normalizeRequest(request));
    }

    @PostMapping("/{id}/auto-block")
    public ModerationCaseResponse autoBlock(@PathVariable UUID id,
                                            @Valid @RequestBody(required = false) ModerationCaseActionRequest request) {
        return moderationCaseService.autoBlock(id, normalizeRequest(request));
    }

    private ModerationCaseActionRequest normalizeRequest(ModerationCaseActionRequest request) {
        return request == null ? new ModerationCaseActionRequest(null) : request;
    }
}

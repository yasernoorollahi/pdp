package com.datarain.pdp.message.controller;

import com.datarain.pdp.message.dto.UserMessageCreateRequest;
import com.datarain.pdp.message.dto.UserMessageProcessedRequest;
import com.datarain.pdp.message.dto.UserMessageResponse;
import com.datarain.pdp.message.dto.UserMessageUpdateRequest;
import com.datarain.pdp.message.service.UserMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-messages")
@PreAuthorize("hasAuthority('ROLE_USER')")
@Tag(name = "User Messages", description = "User message CRUD, processing status, and timeline queries.")
public class UserMessageController {

    private final UserMessageService userMessageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user message for analysis.")
    public UserMessageResponse create(@Valid @RequestBody UserMessageCreateRequest request) {
        return userMessageService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user message by id.")
    public UserMessageResponse getById(@PathVariable UUID id) {
        return userMessageService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user message by id.")
    public UserMessageResponse update(@PathVariable UUID id, @Valid @RequestBody UserMessageUpdateRequest request) {
        return userMessageService.update(id, request);
    }

    @PatchMapping("/{id}/processed")
    @Operation(summary = "Set a user message processed status.")
    public UserMessageResponse setProcessed(@PathVariable UUID id,
                                            @Valid @RequestBody UserMessageProcessedRequest request) {
        return userMessageService.setProcessed(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a user message by id.")
    public void delete(@PathVariable UUID id) {
        userMessageService.delete(id);
    }

    @GetMapping
    @Operation(summary = "List user messages with filters and pagination.")
    public Page<UserMessageResponse> getAll(
            @ParameterObject @PageableDefault(size = 20, sort = "messageDate") Pageable pageable,
            @RequestParam(required = false) Boolean processed,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate
    ) {
        return userMessageService.getAll(pageable, processed, fromDate, toDate);
    }
}

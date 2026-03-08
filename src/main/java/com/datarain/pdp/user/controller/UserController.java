package com.datarain.pdp.user.controller;

import com.datarain.pdp.user.dto.UserResponse;
import com.datarain.pdp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // اصلاح شد: به جای return entity، از UserResponse DTO استفاده میکنیم (passwordHash لیک نشه)
    // اصلاح شد: hasRole → hasAuthority چون در DB مقادیر ROLE_ADMIN/ROLE_USER هستن و prefix دوباره اضافه نمیشه
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Page<UserResponse> findAll(
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return userService.findAll(pageable);
    }



    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
    public UserResponse me() {
        return userService.me();
    }

    // اضافه شد: endpoint برای گرفتن user خاص توسط admin
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponse getById(@PathVariable UUID id) {
        return userService.getById(id);
    }

    // اضافه شد: admin میتونه اکانت user رو disable/enable کنه
    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setEnabled(@PathVariable UUID id, @RequestParam boolean enabled) {
        userService.setEnabled(id, enabled);
    }

    // اضافه شد: admin میتونه اکانت قفل شده رو باز کنه
    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlock(@PathVariable UUID id) {
        userService.unlock(id);
    }
}

package com.datarain.pdp.infrastructure.job;

import com.datarain.pdp.auth.entity.RefreshToken;
import com.datarain.pdp.auth.repository.RefreshTokenRepository;
import com.datarain.pdp.infrastructure.job.monitoring.JobExecutionLog;
import com.datarain.pdp.infrastructure.job.monitoring.JobExecutionLogRepository;
import com.datarain.pdp.infrastructure.job.monitoring.JobExecutionStatus;
import com.datarain.pdp.infrastructure.job.monitoring.JobMonitoringService;
import com.datarain.pdp.infrastructure.security.Role;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditLog;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditLogRepository;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import com.datarain.pdp.item.entity.Item;
import com.datarain.pdp.item.entity.ItemStatus;
import com.datarain.pdp.item.entity.ItemType;
import com.datarain.pdp.item.repository.ItemRepository;
import com.datarain.pdp.notification.entity.NotificationEntity;
import com.datarain.pdp.notification.entity.NotificationStatus;
import com.datarain.pdp.notification.repository.NotificationRepository;
import com.datarain.pdp.moderation.entity.ModerationCase;
import com.datarain.pdp.moderation.entity.ModerationReasonCategory;
import com.datarain.pdp.moderation.entity.ModerationSource;
import com.datarain.pdp.moderation.entity.ModerationStatus;
import com.datarain.pdp.moderation.entity.ModerationTargetType;
import com.datarain.pdp.moderation.repository.ModerationCaseRepository;
import com.datarain.pdp.user.entity.User;
import com.datarain.pdp.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "jobs.test-data.enabled", havingValue = "true")
public class TestDataSeedingJob extends AbstractMonitoredJob implements ApplicationRunner {

    private static final String SEED_MARKER_EMAIL = "seed.user.00@pdp.local";

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotificationRepository notificationRepository;
    private final ModerationCaseRepository moderationCaseRepository;
    private final SecurityAuditLogRepository securityAuditLogRepository;
    private final JobExecutionLogRepository jobExecutionLogRepository;
    private final PasswordEncoder passwordEncoder;

    private final int usersCount;
    private final int itemsPerUser;
    private final int refreshTokensPerUser;
    private final boolean forceSeed;

    public TestDataSeedingJob(UserRepository userRepository,
                              ItemRepository itemRepository,
                              RefreshTokenRepository refreshTokenRepository,
                              NotificationRepository notificationRepository,
                              ModerationCaseRepository moderationCaseRepository,
                              SecurityAuditLogRepository securityAuditLogRepository,
                              JobExecutionLogRepository jobExecutionLogRepository,
                              PasswordEncoder passwordEncoder,
                              JobMonitoringService jobMonitoringService,
                              @Value("${jobs.test-data.users:20}") int usersCount,
                              @Value("${jobs.test-data.items-per-user:5}") int itemsPerUser,
                              @Value("${jobs.test-data.refresh-tokens-per-user:2}") int refreshTokensPerUser,
                              @Value("${jobs.test-data.force:false}") boolean forceSeed) {
        super(jobMonitoringService);
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.notificationRepository = notificationRepository;
        this.moderationCaseRepository = moderationCaseRepository;
        this.securityAuditLogRepository = securityAuditLogRepository;
        this.jobExecutionLogRepository = jobExecutionLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.usersCount = usersCount;
        this.itemsPerUser = itemsPerUser;
        this.refreshTokensPerUser = refreshTokensPerUser;
        this.forceSeed = forceSeed;
    }

    @Override
    public void run(ApplicationArguments args) {
        long processed = executeMonitored("TestDataSeedingJob", this::seedAll);
        log.info("TestDataSeedingJob finished. inserted_records={}", processed);
    }

    private long seedAll() {
        boolean markerExists = userRepository.existsByEmail(SEED_MARKER_EMAIL);
        if (markerExists && !forceSeed) {
            log.info("TestDataSeedingJob skipped. seed marker user already exists: {}", SEED_MARKER_EMAIL);
            return 0;
        }
        if (markerExists) {
            log.warn("TestDataSeedingJob force mode is enabled. Seeding will run again despite existing marker user.");
        }

        List<User> users = markerExists ? loadOrCreateSeedUsers() : createUsers();
        List<Item> items = createItems(users);
        long refreshTokens = createRefreshTokens(users);
        long notifications = createNotifications(users, items);
        long moderationCases = createModerationCases(users, items);
        long audits = createSecurityAudits(users);
        long syntheticJobLogs = createSyntheticJobLogs();

        return users.size() + items.size() + refreshTokens + notifications + moderationCases + audits + syntheticJobLogs;
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>(usersCount);
        String encoded = passwordEncoder.encode("Pass@123456");

        for (int i = 0; i < usersCount; i++) {
            users.add(buildSeedUser(i, encoded));
        }

        return userRepository.saveAll(users);
    }

    private List<User> loadOrCreateSeedUsers() {
        List<User> users = new ArrayList<>(userRepository.findByEmailStartingWithOrderByEmailAsc("seed.user."));
        if (users.size() >= usersCount) {
            return new ArrayList<>(users.subList(0, usersCount));
        }

        String encoded = passwordEncoder.encode("Pass@123456");
        List<User> missing = new ArrayList<>(usersCount - users.size());
        for (int i = 0; i < usersCount; i++) {
            String email = String.format("seed.user.%02d@pdp.local", i);
            if (userRepository.existsByEmail(email)) {
                continue;
            }
            missing.add(buildSeedUser(i, encoded));
        }

        if (!missing.isEmpty()) {
            users.addAll(userRepository.saveAll(missing));
        }

        users.sort((u1, u2) -> u1.getEmail().compareToIgnoreCase(u2.getEmail()));
        return users.size() > usersCount
                ? new ArrayList<>(users.subList(0, usersCount))
                : users;
    }

    private User buildSeedUser(int i, String encodedPassword) {
        User user = new User();
        user.setEmail(String.format("seed.user.%02d@pdp.local", i));
        user.setPasswordHash(encodedPassword);
        user.setRoles(i == 0
                ? Set.of(Role.ROLE_ADMIN, Role.ROLE_USER)
                : Set.of(Role.ROLE_USER));
        user.setEnabled(i % 11 != 0);
        user.setEmailVerified(i % 4 != 0);
        user.setFailedLoginAttempts(i % 6);

        if (i % 7 == 0) {
            user.setLockedUntil(Instant.now().plus(1, ChronoUnit.DAYS));
        } else if (i % 5 == 0) {
            user.setLockedUntil(Instant.now().minus(2, ChronoUnit.DAYS));
        }

        if (i % 3 == 0) {
            user.setEmailVerificationToken("verify-token-" + i);
        }

        if (i % 5 == 0) {
            user.setPasswordResetToken("reset-token-" + i);
            user.setPasswordResetTokenExpiry(Instant.now().plus(12, ChronoUnit.HOURS));
        }

        return user;
    }

    private List<Item> createItems(List<User> users) {
        List<Item> items = new ArrayList<>(users.size() * itemsPerUser);
        ItemType[] types = ItemType.values();

        for (int u = 0; u < users.size(); u++) {
            for (int i = 0; i < itemsPerUser; i++) {
                Item item = new Item();
                item.setTitle("Seed Item " + u + "-" + i);
                item.setDescription("Debug dataset for filtering/sorting and status flows");
                item.setContent("Generated content for user=" + u + ", item=" + i);
                item.setType(types[(u + i) % types.length]);

                if (i % 3 == 0) {
                    item.setStatus(ItemStatus.ARCHIVED);
                    item.setArchivedAt(Instant.now().minus((long) (i + u + 2), ChronoUnit.DAYS));
                } else {
                    item.setStatus(ItemStatus.ACTIVE);
                }

                items.add(item);
            }
        }

        return itemRepository.saveAll(items);
    }

    private long createRefreshTokens(List<User> users) {
        List<RefreshToken> tokens = new ArrayList<>(users.size() * refreshTokensPerUser);

        for (int u = 0; u < users.size(); u++) {
            User user = users.get(u);
            for (int i = 0; i < refreshTokensPerUser; i++) {
                RefreshToken token = new RefreshToken();
                token.setUser(user);
                token.setToken("seed-refresh-" + u + "-" + i + "-" + System.nanoTime());
                token.setDevice(i % 2 == 0 ? "Chrome" : "iOS Safari");
                token.setIpAddress("192.168." + (u % 10) + "." + (10 + i));

                boolean expired = i % 3 == 0;
                token.setExpiryDate(expired
                        ? Instant.now().minus(2, ChronoUnit.DAYS)
                        : Instant.now().plus(14, ChronoUnit.DAYS));

                boolean revoked = i % 4 == 0;
                token.setRevoked(revoked);
                if (revoked) {
                    token.setRevokedAt(Instant.now().minus(1, ChronoUnit.DAYS));
                }

                tokens.add(token);
            }
        }

        refreshTokenRepository.saveAll(tokens);
        return tokens.size();
    }

    private long createNotifications(List<User> users, List<Item> items) {
        List<NotificationEntity> notifications = new ArrayList<>();
        NotificationStatus[] statuses = NotificationStatus.values();

        for (int i = 0; i < items.size(); i++) {
            if (i % 2 != 0) {
                continue;
            }

            NotificationEntity notification = new NotificationEntity();
            notification.setItemId(items.get(i).getId());
            notification.setUserId(i % 5 == 0 ? null : users.get(i % users.size()).getId());
            notification.setStatus(statuses[i % statuses.length]);
            notifications.add(notification);
        }

        notificationRepository.saveAll(notifications);
        return notifications.size();
    }

    private long createSecurityAudits(List<User> users) {
        List<SecurityAuditLog> logs = new ArrayList<>(users.size() * 2);
        SecurityEventType[] eventTypes = SecurityEventType.values();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);

            SecurityAuditLog success = new SecurityAuditLog();
            success.setUserId(user.getId());
            success.setEmail(user.getEmail());
            success.setEventType(eventTypes[i % eventTypes.length]);
            success.setIpAddress("10.0.0." + (i + 1));
            success.setUserAgent(i % 2 == 0 ? "Mozilla/5.0" : "PostmanRuntime/7.0");
            success.setDetails("Seeded success event");
            success.setSuccess(true);
            success.setCreatedAt(Instant.now().minus((long) i, ChronoUnit.HOURS));
            logs.add(success);

            SecurityAuditLog failed = new SecurityAuditLog();
            failed.setUserId(user.getId());
            failed.setEmail(user.getEmail());
            failed.setEventType(SecurityEventType.LOGIN_FAILED);
            failed.setIpAddress("10.0.1." + (i + 1));
            failed.setUserAgent("Mozilla/5.0");
            failed.setDetails("Seeded failed login event");
            failed.setSuccess(false);
            failed.setCreatedAt(Instant.now().minus((long) (i + 1), ChronoUnit.HOURS));
            logs.add(failed);
        }

        securityAuditLogRepository.saveAll(logs);
        return logs.size();
    }

    private long createModerationCases(List<User> users, List<Item> items) {
        if (users.isEmpty()) {
            return 0;
        }

        List<ModerationCase> moderationCases = new ArrayList<>();
        ModerationReasonCategory[] reasonCategories = ModerationReasonCategory.values();
        ModerationStatus[] statuses = ModerationStatus.values();
        ModerationSource[] sources = ModerationSource.values();
        Instant now = Instant.now();

        int totalCases = Math.max(16, reasonCategories.length * 2);
        for (int i = 0; i < totalCases; i++) {
            ModerationCase moderationCase = new ModerationCase();

            ModerationTargetType preferredTargetType = i % 2 == 0 ? ModerationTargetType.USER : ModerationTargetType.CONTENT;
            ModerationTargetType targetType = preferredTargetType == ModerationTargetType.CONTENT && items.isEmpty()
                    ? ModerationTargetType.USER
                    : preferredTargetType;
            moderationCase.setTargetType(targetType);

            UUID targetId = targetType == ModerationTargetType.USER
                    ? users.get(i % users.size()).getId()
                    : items.get(i % items.size()).getId();
            moderationCase.setTargetId(targetId);

            ModerationStatus status = statuses[i % statuses.length];
            moderationCase.setStatus(status);

            int riskScore = switch (status) {
                case PENDING -> (i * 11) % 71;
                case APPROVED -> 10 + (i * 7) % 35;
                case REJECTED -> 45 + (i * 9) % 45;
                case AUTO_BLOCKED -> 80 + (i * 5) % 21;
            };
            if (i == 0) {
                riskScore = 0;
            } else if (i == 1) {
                riskScore = 100;
            }
            moderationCase.setRiskScore(riskScore);

            ModerationSource source = sources[i % sources.length];
            moderationCase.setSource(source);

            Double aiConfidence = null;
            if (source == ModerationSource.AI) {
                aiConfidence = switch (i % 4) {
                    case 0 -> 0.0d;
                    case 1 -> 1.0d;
                    case 2 -> 0.37d;
                    default -> 0.86d;
                };
            } else if (i % 5 == 0) {
                aiConfidence = 0.2d;
            }
            moderationCase.setAiConfidence(aiConfidence);

            moderationCase.setReasonCategory(reasonCategories[i % reasonCategories.length]);

            if (status == ModerationStatus.PENDING) {
                moderationCase.setComment("Awaiting moderation decision");
            } else {
                moderationCase.setReviewedBy(users.get((i + 1) % users.size()).getId());
                moderationCase.setReviewedAt(now.minus(i + 2L, ChronoUnit.HOURS));
                moderationCase.setComment("Seeded " + status.name().toLowerCase() + " decision for moderation test flow");
            }

            moderationCases.add(moderationCase);
        }

        moderationCaseRepository.saveAll(moderationCases);
        return moderationCases.size();
    }

    private long createSyntheticJobLogs() {
        List<JobExecutionLog> logs = new ArrayList<>();
        Instant now = Instant.now();

        logs.add(buildJobLog("NotificationEmailJob", now.minus(5, ChronoUnit.HOURS), 1200, JobExecutionStatus.SUCCESS, 18, null));
        logs.add(buildJobLog("PurgeExpiredRefreshTokensJob", now.minus(3, ChronoUnit.HOURS), 500, JobExecutionStatus.SUCCESS, 6, null));
        logs.add(buildJobLog("ExpireArchivedItemsJob", now.minus(2, ChronoUnit.HOURS), 850, JobExecutionStatus.SUCCESS, 4, null));
        logs.add(buildJobLog("NotificationEmailJob", now.minus(1, ChronoUnit.HOURS), 1400, JobExecutionStatus.FAILED, 0, "SMTP timeout during integration test"));

        jobExecutionLogRepository.saveAll(logs);
        return logs.size();
    }

    private JobExecutionLog buildJobLog(String jobName,
                                        Instant startedAt,
                                        long durationMs,
                                        JobExecutionStatus status,
                                        long processedCount,
                                        String errorMessage) {
        JobExecutionLog logEntry = new JobExecutionLog();
        logEntry.setJobName(jobName);
        logEntry.setStartedAt(startedAt);
        logEntry.setFinishedAt(startedAt.plusMillis(durationMs));
        logEntry.setDuration(durationMs);
        logEntry.setStatus(status);
        logEntry.setProcessedCount(processedCount);
        logEntry.setErrorMessage(errorMessage);
        return logEntry;
    }
}

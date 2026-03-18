package com.datarain.pdp.testdata.service.impl;

import com.datarain.pdp.exception.business.UserEmailNotFoundException;
import com.datarain.pdp.infrastructure.logging.TraceIdFilter;
import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import com.datarain.pdp.infrastructure.audit.BusinessEventService;
import com.datarain.pdp.infrastructure.audit.BusinessEventType;
import com.datarain.pdp.signal.normalization.entity.DailyBehaviorMetric;
import com.datarain.pdp.signal.normalization.repository.DailyBehaviorMetricRepository;
import com.datarain.pdp.testdata.service.DailyBehaviorMetricsSeedService;
import com.datarain.pdp.testdata.service.model.DailyBehaviorMetricsSeedResult;
import com.datarain.pdp.user.entity.User;
import com.datarain.pdp.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyBehaviorMetricsSeedServiceImpl implements DailyBehaviorMetricsSeedService {

    private static final List<String> ENTITY_POOL = List.of(
            "PersonalDataPlatform project",
            "girlfriend",
            "team",
            "manager",
            "client",
            "Kubernetes cluster",
            "home server",
            "daily planner"
    );

    private static final List<String> ACTIVITY_POOL = List.of(
            "woke up early",
            "made coffee",
            "started working",
            "writing backend code",
            "reviewing Kubernetes configs",
            "had meetings",
            "ordered food",
            "decided to go to the gym",
            "called girlfriend",
            "took a walk",
            "debugged an incident",
            "prepared a demo"
    );

    private static final List<String> PROJECT_POOL = List.of(
            "PersonalDataPlatform project",
            "Growth dashboard",
            "Signal normalization",
            "Infrastructure hardening"
    );

    private static final List<String> TOOL_POOL = List.of(
            "IntelliJ",
            "Docker",
            "PostgreSQL",
            "Kubernetes",
            "Figma",
            "Notion"
    );

    private static final List<String> LOCATION_POOL = List.of(
            "home",
            "office",
            "gym",
            "cafe",
            "co-working space"
    );

    private static final List<String> GOAL_POOL = List.of(
            "become more disciplined and productive this year",
            "consistently work on my projects and maintain my health",
            "keep stress low while shipping features",
            "build a sustainable routine"
    );

    private static final List<String> DECISION_POOL = List.of(
            "decided to go to the gym even though I felt mentally tired",
            "decided to refactor the service layer",
            "decided to block time for deep work",
            "decided to call a friend"
    );

    private static final List<String> LIKE_POOL = List.of(
            "working on my projects",
            "maintaining my health",
            "talking about plans with my girlfriend",
            "cleaning up my backlog",
            "learning new tools"
    );

    private static final List<String> DISLIKE_POOL = List.of(
            "feeling lonely",
            "feeling frustrated when progress is slow",
            "context switching too much",
            "unclear requirements"
    );

    private static final List<String> TIME_CONSTRAINT_POOL = List.of(
            "no time to cook",
            "back-to-back meetings",
            "short morning window",
            "late-night deadline"
    );

    private static final List<String> RESOURCE_CONSTRAINT_POOL = List.of(
            "working alone from home",
            "limited budget",
            "waiting on dependencies",
            "small team"
    );

    private static final List<String> TOPIC_POOL = List.of(
            "work",
            "productivity",
            "coding",
            "backend",
            "meetings",
            "health",
            "gym",
            "alone",
            "relationship",
            "planning",
            "infrastructure"
    );

    private static final List<String> DOMAIN_POOL = List.of(
            "personal",
            "professional",
            "health",
            "relationships"
    );

    private static final List<String> MOOD_POOL = List.of(
            "motivated",
            "tired",
            "focused",
            "lonely",
            "energized",
            "stressed",
            "calm",
            "optimistic"
    );

    private static final List<String> UNCERTAINTY_POOL = List.of("maybe", "not sure", "uncertain");
    private static final List<String> CONFIDENCE_POOL = List.of("definitely", "confident", "sure");

    private final DailyBehaviorMetricRepository dailyBehaviorMetricRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final BusinessEventService businessEventService;
    private final PdpMetrics metrics;

    @Override
    @Transactional
    public DailyBehaviorMetricsSeedResult seedForUser(String userEmail, int days, boolean force) {
        int boundedDays = Math.max(1, days);
        if (userEmail == null || userEmail.isBlank()) {
            throw new UserEmailNotFoundException("unknown");
        }
        String normalizedEmail = userEmail.trim();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserEmailNotFoundException(normalizedEmail));

        UUID userId = user.getId();
        LocalDate toDate = LocalDate.now(ZoneOffset.UTC);
        LocalDate fromDate = toDate.minusDays(boundedDays - 1L);
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);

        log.atInfo()
                .addKeyValue("event", "testdata.daily_behavior.seed.requested")
                .addKeyValue("userId", userId)
                .addKeyValue("userEmail", normalizedEmail)
                .addKeyValue("days", boundedDays)
                .addKeyValue("force", force)
                .addKeyValue("traceId", traceId)
                .log("Daily behavior metrics seeding requested");

        metrics.getTestDataDailyBehaviorSeedCounter().increment();

        if (force) {
            int deleted = dailyBehaviorMetricRepository.deleteByUserIdAndMetricDateBetween(userId, fromDate, toDate);
            log.atInfo()
                    .addKeyValue("event", "testdata.daily_behavior.seed.deleted")
                    .addKeyValue("userId", userId)
                    .addKeyValue("deleted", deleted)
                    .addKeyValue("traceId", traceId)
                    .log("Deleted existing daily behavior metrics before seeding");
        }

        Set<LocalDate> existingDates = new HashSet<>();
        if (!force) {
            dailyBehaviorMetricRepository
                    .findByUserIdAndMetricDateGreaterThanEqualOrderByMetricDateAsc(userId, fromDate)
                    .forEach(metric -> existingDates.add(metric.getMetricDate()));
        }

        List<DailyBehaviorMetric> metricsToInsert = new ArrayList<>();
        int skipped = 0;

        for (int i = 0; i < boundedDays; i++) {
            LocalDate metricDate = fromDate.plusDays(i);
            if (existingDates.contains(metricDate)) {
                skipped++;
                continue;
            }

            Random random = new Random(Objects.hash(userId, metricDate));
            ObjectNode rawSummary = buildRawSummary(random);
            DerivedMetrics derived = deriveMetrics(metricDate, rawSummary, random);

            DailyBehaviorMetric metric = new DailyBehaviorMetric();
            metric.setUserId(userId);
            metric.setMetricDate(metricDate);
            metric.setEnergyScore(derived.energyScore());
            metric.setEnergyScoreCount(derived.energyScore() == null ? 0 : 1);
            metric.setMotivationScore(derived.motivationScore());
            metric.setMotivationScoreCount(derived.motivationScore() == null ? 0 : 1);
            metric.setFrictionCount(derived.frictionCount());
            metric.setSocialMentionsCount(derived.socialMentionsCount());
            metric.setDisciplineEventsCount(derived.disciplineEventsCount());
            metric.setRawSummary(rawSummary);
            metric.setSignalCount(derived.signalCount());
            metric.setLastSignalAt(derived.lastSignalAt());

            metricsToInsert.add(metric);
        }

        if (!metricsToInsert.isEmpty()) {
            dailyBehaviorMetricRepository.saveAll(metricsToInsert);
        }

        String details = "Seeded daily behavior metrics for " + normalizedEmail +
                " from " + fromDate + " to " + toDate +
                " (inserted=" + metricsToInsert.size() + ", skipped=" + skipped + ")";
        businessEventService.log(BusinessEventType.TEST_DATA_SEEDED, normalizedEmail, userId, details, true);

        return new DailyBehaviorMetricsSeedResult(
                userId,
                normalizedEmail,
                fromDate,
                toDate,
                metricsToInsert.size(),
                skipped
        );
    }

    private ObjectNode buildRawSummary(Random random) {
        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode facts = root.putObject("facts");
        facts.set("entities", toArrayNode(pickMany(random, ENTITY_POOL, 1, 3)));
        facts.set("activities", toArrayNode(pickMany(random, ACTIVITY_POOL, 4, 8)));
        facts.set("projects", toArrayNode(pickMany(random, PROJECT_POOL, 1, 2)));
        facts.set("tools", toArrayNode(pickMany(random, TOOL_POOL, 1, 3)));
        facts.set("locations", toArrayNode(pickMany(random, LOCATION_POOL, 1, 2)));

        ObjectNode intent = root.putObject("intent");
        intent.set("goals", toArrayNode(pickMany(random, GOAL_POOL, 1, 2)));
        List<String> decisions = pickMany(random, DECISION_POOL, 0, 2);
        intent.set("plans", toArrayNode(List.of()));
        intent.set("commitments", toArrayNode(List.of()));
        intent.set("decisions", toArrayNode(decisions));
        intent.set("obligations", toArrayNode(List.of()));
        intent.put("temporal_scope", pickOne(random, List.of("morning to evening", "afternoon", "evening")));

        ObjectNode tone = root.putObject("tone");
        String motivationLevel = pickOne(random, List.of("low", "medium", "high"));
        String effortPerception = pickOne(random, List.of("low", "medium", "high"));
        boolean frictionDetected = random.nextInt(100) < 35;
        tone.put("sentiment", pickOne(random, List.of("positive", "neutral", "mixed", "negative")));
        tone.put("mood", String.join(", ", pickMany(random, MOOD_POOL, 2, 4)));
        tone.put("motivation_level", motivationLevel);
        tone.put("effort_perception", effortPerception);
        tone.put("friction_detected", frictionDetected);

        ObjectNode cognitive = root.putObject("cognitive");
        boolean hesitationDetected = random.nextInt(100) < 20;
        cognitive.set("uncertainty_language", toArrayNode(pickMany(random, UNCERTAINTY_POOL, 0, 1)));
        cognitive.set("confidence_language", toArrayNode(pickMany(random, CONFIDENCE_POOL, 0, 1)));
        cognitive.put("clarity_level", pickOne(random, List.of("low", "medium", "high")));
        cognitive.put("decision_state", pickOne(random, List.of("decided", "considering", "undecided")));
        cognitive.put("hesitation_detected", hesitationDetected);

        ObjectNode context = root.putObject("context");
        context.set("likes", toArrayNode(pickMany(random, LIKE_POOL, 1, 3)));
        context.set("dislikes", toArrayNode(pickMany(random, DISLIKE_POOL, 0, 2)));
        context.set("declared_avoidances", toArrayNode(List.of()));
        context.set("time_constraints", toArrayNode(pickMany(random, TIME_CONSTRAINT_POOL, 0, 1)));
        context.set("resource_constraints", toArrayNode(pickMany(random, RESOURCE_CONSTRAINT_POOL, 0, 1)));
        boolean collaborationDetected = random.nextInt(100) < 40;
        context.put("collaboration_detected", collaborationDetected);

        ObjectNode topics = root.putObject("topics");
        topics.set("topic_tags", toArrayNode(pickMany(random, TOPIC_POOL, 3, 7)));
        topics.set("domain_classification", toArrayNode(pickMany(random, DOMAIN_POOL, 1, 2)));

        return root;
    }

    private DerivedMetrics deriveMetrics(LocalDate metricDate, ObjectNode rawSummary, Random random) {
        String mood = rawSummary.path("tone").path("mood").asText("");
        String motivationLevel = rawSummary.path("tone").path("motivation_level").asText("medium");
        String effortPerception = rawSummary.path("tone").path("effort_perception").asText("medium");
        boolean frictionDetected = rawSummary.path("tone").path("friction_detected").asBoolean(false);
        boolean collaborationDetected = rawSummary.path("context").path("collaboration_detected").asBoolean(false);

        double motivationScore = baseScore(motivationLevel) + jitter(random);
        double energyScore = baseScore(effortPerception) + jitter(random);

        if (mood.contains("tired") || mood.contains("stressed")) {
            energyScore -= 1.0d;
        }
        if (frictionDetected) {
            energyScore -= 0.7d;
            motivationScore -= 0.4d;
        }

        int decisions = rawSummary.path("intent").path("decisions").size();
        int disciplineEvents = Math.min(5, decisions + (random.nextBoolean() ? 1 : 0));

        int frictionCount = frictionDetected ? 1 + random.nextInt(3) : 0;
        int socialMentions = collaborationDetected ? 1 + random.nextInt(3) : 0;

        int activities = rawSummary.path("facts").path("activities").size();
        int signalCount = Math.max(1, activities / 2 + random.nextInt(2));

        Instant lastSignalAt = metricDate.atStartOfDay()
                .plusHours(6 + random.nextInt(12))
                .plusMinutes(random.nextInt(60))
                .toInstant(ZoneOffset.UTC)
                .plus((long) random.nextInt(3), ChronoUnit.HOURS);

        return new DerivedMetrics(
                clamp(energyScore),
                clamp(motivationScore),
                frictionCount,
                socialMentions,
                disciplineEvents,
                signalCount,
                lastSignalAt
        );
    }

    private ArrayNode toArrayNode(List<String> values) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        values.forEach(arrayNode::add);
        return arrayNode;
    }

    private List<String> pickMany(Random random, List<String> pool, int min, int max) {
        int count = min + random.nextInt(Math.max(1, max - min + 1));
        List<String> shuffled = new ArrayList<>(pool);
        for (int i = shuffled.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String tmp = shuffled.get(i);
            shuffled.set(i, shuffled.get(j));
            shuffled.set(j, tmp);
        }
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    private String pickOne(Random random, List<String> pool) {
        return pool.get(random.nextInt(pool.size()));
    }

    private double baseScore(String level) {
        return switch (level) {
            case "low" -> 3.5d;
            case "high" -> 8.0d;
            default -> 6.0d;
        };
    }

    private double jitter(Random random) {
        return (random.nextDouble() - 0.5d) * 1.2d;
    }

    private double clamp(double value) {
        return Math.max(0.0d, Math.min(10.0d, value));
    }

    private record DerivedMetrics(
            Double energyScore,
            Double motivationScore,
            int frictionCount,
            int socialMentionsCount,
            int disciplineEventsCount,
            int signalCount,
            Instant lastSignalAt
    ) {
    }
}

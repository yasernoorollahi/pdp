package com.datarain.pdp.signal.normalization.service;

import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.dto.ActivitySignal;
import com.datarain.pdp.signal.normalization.dto.CognitiveSignal;
import com.datarain.pdp.signal.normalization.dto.CognitiveLanguageSignal;
import com.datarain.pdp.signal.normalization.dto.ContextSignal;
import com.datarain.pdp.signal.normalization.dto.EntitySignal;
import com.datarain.pdp.signal.normalization.dto.IntentSignal;
import com.datarain.pdp.signal.normalization.dto.ParsedSignal;
import com.datarain.pdp.signal.normalization.dto.PreferenceSignal;
import com.datarain.pdp.signal.normalization.dto.ProjectSignal;
import com.datarain.pdp.signal.normalization.dto.TopicSignal;
import com.datarain.pdp.signal.normalization.dto.ToneSignal;
import com.datarain.pdp.signal.normalization.dto.ToolSignal;
import com.datarain.pdp.signal.normalization.entity.CognitiveLanguageType;
import com.datarain.pdp.signal.normalization.entity.IntentType;
import com.datarain.pdp.signal.normalization.entity.PreferenceType;
import com.datarain.pdp.signal.normalization.entity.UserEntityType;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SignalParser {

    public ParsedSignal parse(MessageSignal messageSignal) {
        JsonNode root = messageSignal.getSignals();
        List<EntitySignal> entities = new ArrayList<>();
        List<ActivitySignal> activities = new ArrayList<>();
        List<TopicSignal> topicSignals = new ArrayList<>();
        List<IntentSignal> intents = new ArrayList<>();
        List<PreferenceSignal> preferences = new ArrayList<>();
        List<CognitiveLanguageSignal> cognitiveLanguages = new ArrayList<>();
        List<ToolSignal> tools = new ArrayList<>();
        List<ProjectSignal> projects = new ArrayList<>();
        ToneSignal toneSignal = new ToneSignal(null, null, null, null, null);
        CognitiveSignal cognitiveSignal = new CognitiveSignal(null, null, null);
        ContextSignal contextSignal = new ContextSignal(null);

        if (root != null && !root.isMissingNode()) {
            ParseState state = new ParseState(entities, activities, topicSignals, intents, preferences, cognitiveLanguages, tools, projects, toneSignal, cognitiveSignal, contextSignal);
            collectRecursive(root, state);
            toneSignal = state.tone;
            cognitiveSignal = state.cognitive;
            contextSignal = state.context;
        }

        return new ParsedSignal(
                Collections.unmodifiableList(entities),
                Collections.unmodifiableList(activities),
                Collections.unmodifiableList(topicSignals),
                Collections.unmodifiableList(intents),
                Collections.unmodifiableList(preferences),
                Collections.unmodifiableList(cognitiveLanguages),
                Collections.unmodifiableList(tools),
                Collections.unmodifiableList(projects),
                cognitiveSignal,
                toneSignal,
                contextSignal
        );
    }

    private void collectRecursive(JsonNode node, ParseState state) {
        if (node == null || node.isMissingNode()) {
            return;
        }
        if (node.isObject()) {
            boolean intentCandidate = hasAny(node, "goals", "plans", "commitments", "decisions", "obligations", "temporal_scope");
            if (intentCandidate) {
                state.intents.addAll(parseIntents(node));
            }

            boolean toneCandidate = hasAny(node, "sentiment", "mood", "motivation_level", "effort_perception", "friction_detected");
            if (toneCandidate) {
                state.tone = mergeTone(state.tone, parseTone(node));
            }

            boolean cognitiveCandidate = hasAny(node, "clarity_level", "decision_state", "hesitation_detected");
            if (cognitiveCandidate) {
                state.cognitive = mergeCognitive(state.cognitive, parseCognitive(node));
            }

            boolean contextCandidate = hasAny(node, "collaboration_detected");
            if (contextCandidate) {
                state.context = mergeContext(state.context, parseContext(node));
            }

            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                switch (key) {
                    case "entities" -> state.entities.addAll(parseEntities(value, UserEntityType.PERSON));
                    case "actors" -> state.entities.addAll(parseEntities(value, UserEntityType.PERSON));
                    case "locations" -> state.entities.addAll(parseEntities(value, UserEntityType.LOCATION));
                    case "projects" -> {
                        state.entities.addAll(parseEntities(value, UserEntityType.PROJECT));
                        state.projects.addAll(parseProjects(value));
                    }
                    case "tools" -> {
                        state.entities.addAll(parseEntities(value, UserEntityType.TOOL));
                        state.tools.addAll(parseTools(value));
                    }
                    case "activities" -> state.activities.addAll(parseActivities(value));
                    case "topic_tags" -> state.topics.addAll(parseTopics(value, false));
                    case "domain_classification" -> state.topics.addAll(parseTopics(value, true));
                    case "likes" -> state.preferences.addAll(parsePreferenceArray(value, PreferenceType.LIKE));
                    case "dislikes" -> state.preferences.addAll(parsePreferenceArray(value, PreferenceType.DISLIKE));
                    case "constraints", "time_constraints", "resource_constraints" ->
                            state.preferences.addAll(parsePreferenceArray(value, PreferenceType.CONSTRAINT));
                    case "avoidances", "declared_avoidances" ->
                            state.preferences.addAll(parsePreferenceArray(value, PreferenceType.AVOIDANCE));
                    case "uncertainty_language" ->
                            state.cognitiveLanguages.addAll(parseCognitiveLanguageArray(value, CognitiveLanguageType.UNCERTAINTY));
                    case "confidence_language" ->
                            state.cognitiveLanguages.addAll(parseCognitiveLanguageArray(value, CognitiveLanguageType.CONFIDENCE));
                    default -> {
                    }
                }

                collectRecursive(value, state);
            });
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                collectRecursive(child, state);
            }
        }
    }

    private boolean hasAny(JsonNode node, String... fields) {
        if (node == null || !node.isObject()) {
            return false;
        }
        for (String field : fields) {
            if (node.has(field)) {
                return true;
            }
        }
        return false;
    }

    private ToneSignal mergeTone(ToneSignal base, ToneSignal incoming) {
        if (incoming == null) {
            return base;
        }
        if (base == null) {
            return incoming;
        }
        return new ToneSignal(
                base.sentiment() != null ? base.sentiment() : incoming.sentiment(),
                base.mood() != null ? base.mood() : incoming.mood(),
                base.motivationLevel() != null ? base.motivationLevel() : incoming.motivationLevel(),
                base.effortPerception() != null ? base.effortPerception() : incoming.effortPerception(),
                base.frictionDetected() != null ? base.frictionDetected() : incoming.frictionDetected()
        );
    }

    private CognitiveSignal mergeCognitive(CognitiveSignal base, CognitiveSignal incoming) {
        if (incoming == null) {
            return base;
        }
        if (base == null) {
            return incoming;
        }
        return new CognitiveSignal(
                base.clarityLevel() != null ? base.clarityLevel() : incoming.clarityLevel(),
                base.decisionState() != null ? base.decisionState() : incoming.decisionState(),
                base.hesitationDetected() != null ? base.hesitationDetected() : incoming.hesitationDetected()
        );
    }

    private ContextSignal mergeContext(ContextSignal base, ContextSignal incoming) {
        if (incoming == null) {
            return base;
        }
        if (base == null) {
            return incoming;
        }
        return new ContextSignal(
                base.collaborationDetected() != null ? base.collaborationDetected() : incoming.collaborationDetected()
        );
    }

    private List<EntitySignal> parseEntities(JsonNode arrayNode, UserEntityType type) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return List.of();
        }
        List<EntitySignal> results = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            String name = readText(node, "name", "value", "label", "topic", "activity");
            if (name == null || name.isBlank()) {
                continue;
            }
            String canonical = readText(node, "canonical_name");
            if (canonical == null || canonical.isBlank()) {
                canonical = normalizeCanonical(name);
            }
            Double confidence = node.hasNonNull("confidence") && node.get("confidence").isNumber()
                    ? node.get("confidence").asDouble()
                    : null;
            results.add(new EntitySignal(name.trim(), canonical, type, confidence));
        }
        return results;
    }

    private List<ActivitySignal> parseActivities(JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return List.of();
        }
        List<ActivitySignal> results = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            String name = readText(node, "name", "activity", "value");
            if (name == null || name.isBlank()) {
                continue;
            }
            LocalDate date = readDate(node, "date", "activity_date");
            results.add(new ActivitySignal(name.trim(), date));
        }
        return results;
    }

    private List<TopicSignal> parseTopics(JsonNode arrayNode, boolean domainOnly) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return List.of();
        }
        List<TopicSignal> results = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            String value = domainOnly
                    ? readText(node, "domain", "topic", "name", "value", "label")
                    : readText(node, "topic", "name", "value", "label");
            if (value == null || value.isBlank()) {
                continue;
            }
            if (domainOnly) {
                results.add(new TopicSignal(null, value.trim()));
            } else {
                String domain = readText(node, "domain");
                results.add(new TopicSignal(value.trim(), domain));
            }
        }
        return results;
    }

    private List<IntentSignal> parseIntents(JsonNode intentNode) {
        if (intentNode == null || intentNode.isMissingNode()) {
            return List.of();
        }
        List<IntentSignal> results = new ArrayList<>();
        String globalTemporalScope = readText(intentNode, "temporal_scope");
        results.addAll(parseIntentArray(intentNode.path("goals"), IntentType.GOAL, globalTemporalScope));
        results.addAll(parseIntentArray(intentNode.path("plans"), IntentType.PLAN, globalTemporalScope));
        results.addAll(parseIntentArray(intentNode.path("commitments"), IntentType.COMMITMENT, globalTemporalScope));
        results.addAll(parseIntentArray(intentNode.path("decisions"), IntentType.DECISION, globalTemporalScope));
        results.addAll(parseIntentArray(intentNode.path("obligations"), IntentType.OBLIGATION, globalTemporalScope));
        return results;
    }

    private List<IntentSignal> parseIntentArray(JsonNode arrayNode, IntentType type, String fallbackTemporalScope) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return List.of();
        }
        List<IntentSignal> results = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            String description;
            if (node != null && node.isTextual()) {
                description = node.asText();
            } else {
                description = readText(node, "description", "text", "value", "name");
            }
            if (description == null || description.isBlank()) {
                continue;
            }
            String temporalScope = (node != null && node.isTextual())
                    ? null
                    : readText(node, "temporal_scope", "timeframe", "when");
            if (temporalScope == null || temporalScope.isBlank()) {
                temporalScope = fallbackTemporalScope;
            }
            results.add(new IntentSignal(type, description.trim(), temporalScope));
        }
        return results;
    }

    private List<PreferenceSignal> parsePreferences(JsonNode contextNode) {
        if (contextNode == null || contextNode.isMissingNode()) {
            return List.of();
        }
        List<PreferenceSignal> results = new ArrayList<>();
        results.addAll(parsePreferenceArray(contextNode.path("likes"), PreferenceType.LIKE));
        results.addAll(parsePreferenceArray(contextNode.path("dislikes"), PreferenceType.DISLIKE));
        results.addAll(parsePreferenceArray(contextNode.path("constraints"), PreferenceType.CONSTRAINT));
        results.addAll(parsePreferenceArray(contextNode.path("time_constraints"), PreferenceType.CONSTRAINT));
        results.addAll(parsePreferenceArray(contextNode.path("resource_constraints"), PreferenceType.CONSTRAINT));
        results.addAll(parsePreferenceArray(contextNode.path("avoidances"), PreferenceType.AVOIDANCE));
        results.addAll(parsePreferenceArray(contextNode.path("declared_avoidances"), PreferenceType.AVOIDANCE));
        return results;
    }

    private static class ParseState {
        private final List<EntitySignal> entities;
        private final List<ActivitySignal> activities;
        private final List<TopicSignal> topics;
        private final List<IntentSignal> intents;
        private final List<PreferenceSignal> preferences;
        private final List<CognitiveLanguageSignal> cognitiveLanguages;
        private final List<ToolSignal> tools;
        private final List<ProjectSignal> projects;
        private ToneSignal tone;
        private CognitiveSignal cognitive;
        private ContextSignal context;

        private ParseState(List<EntitySignal> entities,
                           List<ActivitySignal> activities,
                           List<TopicSignal> topics,
                           List<IntentSignal> intents,
                           List<PreferenceSignal> preferences,
                           List<CognitiveLanguageSignal> cognitiveLanguages,
                           List<ToolSignal> tools,
                           List<ProjectSignal> projects,
                           ToneSignal tone,
                           CognitiveSignal cognitive,
                           ContextSignal context) {
            this.entities = entities;
            this.activities = activities;
            this.topics = topics;
            this.intents = intents;
            this.preferences = preferences;
            this.cognitiveLanguages = cognitiveLanguages;
            this.tools = tools;
            this.projects = projects;
            this.tone = tone;
            this.cognitive = cognitive;
            this.context = context;
        }
    }

    private List<PreferenceSignal> parsePreferenceArray(JsonNode arrayNode, PreferenceType type) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return List.of();
        }
        List<PreferenceSignal> results = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            String value = readText(node, "value", "name", "text");
            if (value == null || value.isBlank()) {
                continue;
            }
            results.add(new PreferenceSignal(type, value.trim()));
        }
        return results;
    }

    private List<ToolSignal> parseTools(JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return List.of();
        }
        List<ToolSignal> results = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            String name = readText(node, "name", "value", "label", "tool");
            if (name == null || name.isBlank()) {
                continue;
            }
            String normalized = readText(node, "normalized_name", "canonical_name", "canonical");
            if (normalized == null || normalized.isBlank()) {
                normalized = normalizeCanonical(name);
            }
            String source = readText(node, "source");
            results.add(new ToolSignal(name.trim(), normalized, source));
        }
        return results;
    }

    private List<ProjectSignal> parseProjects(JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return List.of();
        }
        List<ProjectSignal> results = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            String name = readText(node, "name", "value", "label", "project");
            if (name == null || name.isBlank()) {
                continue;
            }
            String normalized = readText(node, "normalized_name", "canonical_name", "canonical");
            if (normalized == null || normalized.isBlank()) {
                normalized = normalizeCanonical(name);
            }
            String source = readText(node, "source");
            results.add(new ProjectSignal(name.trim(), normalized, source));
        }
        return results;
    }

    private List<CognitiveLanguageSignal> parseCognitiveLanguageArray(JsonNode arrayNode, CognitiveLanguageType type) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return List.of();
        }
        List<CognitiveLanguageSignal> results = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            String value = readText(node, "value", "text", "name");
            if (value == null || value.isBlank()) {
                continue;
            }
            results.add(new CognitiveLanguageSignal(value.trim(), type));
        }
        return results;
    }

    private CognitiveSignal parseCognitive(JsonNode cognitiveNode) {
        if (cognitiveNode == null || cognitiveNode.isMissingNode()) {
            return new CognitiveSignal(null, null, null);
        }
        String clarity = readText(cognitiveNode, "clarity_level");
        String decisionState = readText(cognitiveNode, "decision_state");
        Boolean hesitation = cognitiveNode.has("hesitation_detected")
                ? cognitiveNode.get("hesitation_detected").asBoolean()
                : null;
        return new CognitiveSignal(clarity, decisionState, hesitation);
    }

    private ContextSignal parseContext(JsonNode contextNode) {
        if (contextNode == null || contextNode.isMissingNode()) {
            return new ContextSignal(null);
        }
        Boolean collaboration = contextNode.has("collaboration_detected")
                ? contextNode.get("collaboration_detected").asBoolean()
                : null;
        return new ContextSignal(collaboration);
    }

    private ToneSignal parseTone(JsonNode toneNode) {
        if (toneNode == null || toneNode.isMissingNode()) {
            return new ToneSignal(null, null, null, null, null);
        }
        String sentiment = readText(toneNode, "sentiment");
        String mood = readText(toneNode, "mood");
        String motivation = readText(toneNode, "motivation_level");
        String effort = readText(toneNode, "effort_perception");
        Boolean frictionDetected = toneNode.has("friction_detected")
                ? toneNode.get("friction_detected").asBoolean()
                : null;
        return new ToneSignal(sentiment, mood, motivation, effort, frictionDetected);
    }

    private String readText(JsonNode node, String... fields) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value != null && value.isTextual()) {
                return value.asText();
            }
        }
        return null;
    }

    private LocalDate readDate(JsonNode node, String... fields) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value != null && value.isTextual()) {
                try {
                    return LocalDate.parse(value.asText());
                } catch (DateTimeParseException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private String normalizeCanonical(String name) {
        return name == null ? null : name.trim().toLowerCase();
    }
}

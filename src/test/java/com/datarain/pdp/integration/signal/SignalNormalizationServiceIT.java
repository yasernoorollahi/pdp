package com.datarain.pdp.integration.signal;

import com.datarain.pdp.infrastructure.security.Role;
import com.datarain.pdp.message.entity.UserMessage;
import com.datarain.pdp.message.repository.UserMessageRepository;
import com.datarain.pdp.signal.entity.MessageSignal;
import com.datarain.pdp.signal.normalization.entity.CognitiveState;
import com.datarain.pdp.signal.normalization.entity.DailyBehaviorMetric;
import com.datarain.pdp.signal.normalization.repository.CognitiveStateRepository;
import com.datarain.pdp.signal.normalization.repository.DailyBehaviorMetricRepository;
import com.datarain.pdp.signal.normalization.repository.IntentItemRepository;
import com.datarain.pdp.signal.normalization.repository.UserActivityRepository;
import com.datarain.pdp.signal.normalization.repository.UserEntityRepository;
import com.datarain.pdp.signal.normalization.repository.UserPreferenceRepository;
import com.datarain.pdp.signal.normalization.repository.UserTopicRepository;
import com.datarain.pdp.signal.normalization.service.SignalNormalizationService;
import com.datarain.pdp.signal.repository.MessageSignalRepository;
import com.datarain.pdp.support.AbstractIT;
import com.datarain.pdp.user.entity.User;
import com.datarain.pdp.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
class SignalNormalizationServiceIT extends AbstractIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMessageRepository userMessageRepository;

    @Autowired
    private MessageSignalRepository messageSignalRepository;

    @Autowired
    private SignalNormalizationService signalNormalizationService;

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private IntentItemRepository intentItemRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private UserTopicRepository userTopicRepository;

    @Autowired
    private CognitiveStateRepository cognitiveStateRepository;

    @Autowired
    private DailyBehaviorMetricRepository dailyBehaviorMetricRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_normalize_sample_signal_json() throws Exception {
        User user = new User();
        user.setEmail("user-" + System.currentTimeMillis() + "@pdp.local");
        user.setPasswordHash("hash");
        user.setRoles(Set.of(Role.ROLE_ADMIN));
        user.setEmailVerified(true);
        User savedUser = userRepository.save(user);

        UserMessage message = new UserMessage();
        message.setUserId(savedUser.getId());
        message.setContent("sample message");
        message.setMessageDate(LocalDate.now());
        UserMessage savedMessage = userMessageRepository.save(message);

        JsonNode signals = objectMapper.readTree(SAMPLE_JSON);
        MessageSignal messageSignal = new MessageSignal();
        messageSignal.setMessageId(savedMessage.getId());
        messageSignal.setUserId(savedUser.getId());
        messageSignal.setSignalVersion(1);
        messageSignal.setSignals(signals);
        messageSignal.setExtractorModel("test-model");
        messageSignal.setPipelineVersion("test-pipeline");
        messageSignal.setNormalized(false);
        messageSignalRepository.save(messageSignal);

        long processed = signalNormalizationService.processPendingSignals(10);
        assertEquals(1, processed);

        assertEquals(2, userEntityRepository.count());
        assertEquals(7, userActivityRepository.count());
        assertEquals(12, intentItemRepository.count());
        assertEquals(16, userPreferenceRepository.count());
        assertEquals(11, userTopicRepository.count());
        assertEquals(1, cognitiveStateRepository.count());
        assertEquals(1, dailyBehaviorMetricRepository.count());

        List<DailyBehaviorMetric> metrics = dailyBehaviorMetricRepository.findAll();
        DailyBehaviorMetric metric = metrics.getFirst();
        assertEquals(0, metric.getSocialMentionsCount());
        assertEquals(7, metric.getDisciplineEventsCount());
        assertEquals(1, metric.getFrictionCount());
        assertEquals(0.8, metric.getMotivationScore(), 0.0001);
        assertNotNull(metric.getRawSummary());

        CognitiveState cognitiveState = cognitiveStateRepository.findAll().getFirst();
        assertEquals("medium", cognitiveState.getClarityLevel());
        assertEquals("undecided", cognitiveState.getDecisionState());
        assertTrue(Boolean.TRUE.equals(cognitiveState.getHesitationDetected()));

        MessageSignal updatedSignal = messageSignalRepository.findById(messageSignal.getId()).orElseThrow();
        assertTrue(updatedSignal.isNormalized());
        assertNotNull(updatedSignal.getNormalizedAt());
    }

    private static final String SAMPLE_JSON = """
            {
              \"facts\": {
                \"entities\": [],
                \"activities\": [
                  \"woke up feeling good\",
                  \"exercised\",
                  \"made breakfast\",
                  \"had several heavy meetings\",
                  \"ordered food\",
                  \"worked out at the gym\",
                  \"called girlfriend\"
                ],
                \"projects\": [],
                \"tools\": [],
                \"locations\": [
                  \"home\",
                  \"gym\"
                ]
              },
              \"intent\": {
                \"goals\": [
                  \"be disciplined\",
                  \"conquer the day\",
                  \"go to the gym\"
                ],
                \"plans\": [
                  \"exercise\",
                  \"make breakfast\",
                  \"eat\"
                ],
                \"commitments\": [
                  \"push myself to go to the gym\"
                ],
                \"decisions\": [
                  \"order food\",
                  \"work out at the gym\"
                ],
                \"obligations\": [
                  \"be disciplined\",
                  \"conquer the day\",
                  \"go to the gym\"
                ],
                \"temporal_scope\": \"one day\"
              },
              \"tone\": {
                \"sentiment\": \"mixed\",
                \"mood\": \"full of energy, frustration, effort, silence, and reflection\",
                \"motivation_level\": \"high\",
                \"effort_perception\": \"high\",
                \"friction_detected\": true
              },
              \"cognitive\": {
                \"uncertainty_language\": [
                  \"strange days\",
                  \"sometimes feels blurry\",
                  \"probably because\",
                  \"not entirely good either\"
                ],
                \"confidence_language\": [
                  \"surprisingly good\",
                  \"felt proud\",
                  \"felt confident and capable\",
                  \"felt completely drained\"
                ],
                \"clarity_level\": \"medium\",
                \"decision_state\": \"undecided\",
                \"hesitation_detected\": true
              },
              \"context\": {
                \"likes\": [
                  \"feeling good in the morning\",
                  \"exercise\",
                  \"feeling proud of discipline\",
                  \"calm, focused energy\",
                  \"confidence and capability during work\"
                ],
                \"dislikes\": [
                  \"mental fatigue\",
                  \"bland and disappointing food\",
                  \"feeling lonely at the gym\",
                  \"colder house\",
                  \"tiredness\",
                  \"silence at home\",
                  \"not hearing from girlfriend\"
                ],
                \"declared_avoidances\": [],
                \"time_constraints\": [
                  \"heavy meetings back-to-back\",
                  \"no time or energy to cook\"
                ],
                \"resource_constraints\": [
                  \"cold house\",
                  \"loneliness\"
                ],
                \"collaboration_detected\": false
              },
              \"topics\": {
                \"topic_tags\": [
                  \"motivation\",
                  \"productivity\",
                  \"work\",
                  \"mental fatigue\",
                  \"frustration\",
                  \"loneliness\",
                  \"physical exhaustion\"
                ],
                \"domain_classification\": [
                  \"personal experience\",
                  \"daily life\",
                  \"emotions\",
                  \"work-life balance\"
                ]
              }
            }
            """;
}

package com.datarain.pdp.web.insights;

import com.datarain.pdp.insights.controller.InsightsController;
import com.datarain.pdp.insights.dto.CountTrendResponse;
import com.datarain.pdp.insights.dto.EnergyTrendResponse;
import com.datarain.pdp.insights.dto.InsightSnapshotResponse;
import com.datarain.pdp.insights.dto.InsightSummaryResponse;
import com.datarain.pdp.insights.dto.MoodWordResponse;
import com.datarain.pdp.insights.dto.MotivationTrendResponse;
import com.datarain.pdp.insights.dto.TimelinePointResponse;
import com.datarain.pdp.insights.dto.TrendPointResponse;
import com.datarain.pdp.insights.service.InsightsService;
import com.datarain.pdp.infrastructure.rate_limit.filter.RateLimitFilter;
import com.datarain.pdp.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.datarain.pdp.web.support.AbstractWebMvcTest;
import com.datarain.pdp.support.TestExpectations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = InsightsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = InsightsController.class)
class InsightsControllerTest extends AbstractWebMvcTest {

    @MockBean
    private InsightsService insightsService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void should_get_timeline() throws Exception {
        when(insightsService.getTimeline(any(), any(Pageable.class)))
                .thenReturn(List.of(new TimelinePointResponse(
                        LocalDate.of(2024, 1, 1), 0.7, 0.8, 1, 2, 3
                )));

        mockMvc.perform(get("/api/insights/timeline")
                        .param("days", "7")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$[0].date").value("2024-01-01"));
    }

    @Test
    void should_get_energy_trend() throws Exception {
        when(insightsService.getEnergyTrend(any(), any(Pageable.class)))
                .thenReturn(new EnergyTrendResponse(0.6,
                        List.of(new TrendPointResponse(LocalDate.of(2024, 1, 1), 0.6))));

        mockMvc.perform(get("/api/insights/energy").param("days", "7"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.averageEnergy").value(0.6));
    }

    @Test
    void should_get_motivation_trend() throws Exception {
        when(insightsService.getMotivationTrend(any(), any(Pageable.class)))
                .thenReturn(new MotivationTrendResponse(0.5,
                        List.of(new TrendPointResponse(LocalDate.of(2024, 1, 2), 0.5))));

        mockMvc.perform(get("/api/insights/motivation").param("days", "7"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.averageMotivation").value(0.5));
    }

    @Test
    void should_get_friction_heatmap() throws Exception {
        when(insightsService.getFrictionHeatmap(any(), any(Pageable.class)))
                .thenReturn(List.of(new TrendPointResponse(LocalDate.of(2024, 1, 3), 2.0)));

        mockMvc.perform(get("/api/insights/friction").param("days", "10"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$[0].value").value(2.0));
    }

    @Test
    void should_get_social_trend() throws Exception {
        when(insightsService.getSocialTrend(any(), any(Pageable.class)))
                .thenReturn(new CountTrendResponse(4,
                        List.of(new TrendPointResponse(LocalDate.of(2024, 1, 4), 1.0))));

        mockMvc.perform(get("/api/insights/social").param("days", "15"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.total").value(4));
    }

    @Test
    void should_get_discipline_trend() throws Exception {
        when(insightsService.getDisciplineTrend(any(), any(Pageable.class)))
                .thenReturn(new CountTrendResponse(2,
                        List.of(new TrendPointResponse(LocalDate.of(2024, 1, 5), 1.0))));

        mockMvc.perform(get("/api/insights/discipline").param("days", "15"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void should_get_summary() throws Exception {
        when(insightsService.getSummary(any()))
                .thenReturn(new InsightSummaryResponse("stable", "high", "low", "low", "low"));

        mockMvc.perform(get("/api/insights/summary").param("days", "7"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.energy").value("stable"));
    }

    @Test
    void should_get_today_snapshot() throws Exception {
        when(insightsService.getTodaySnapshot())
                .thenReturn(new InsightSnapshotResponse(0.5, 0.4, 1, 2, 3));

        mockMvc.perform(get("/api/insights/today"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$.energy").value(0.5));
    }

    @Test
    void should_get_moods() throws Exception {
        when(insightsService.getMoodCloud(any(), any(Pageable.class)))
                .thenReturn(List.of(new MoodWordResponse("happy", 3)));

        mockMvc.perform(get("/api/insights/moods")
                        .param("days", "30")
                        .param("limit", "20"))
                .andExpect(TestExpectations.status(200))
                .andExpect(jsonPath("$[0].word").value("happy"));
    }

    @Test
    void should_reject_invalid_range_request() throws Exception {
        mockMvc.perform(get("/api/insights/timeline").param("days", "0"))
                .andExpect(TestExpectations.status(400));

        verify(insightsService, never()).getTimeline(any(), any(Pageable.class));
    }
}

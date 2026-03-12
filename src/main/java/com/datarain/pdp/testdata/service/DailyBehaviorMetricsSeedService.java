package com.datarain.pdp.testdata.service;

import com.datarain.pdp.testdata.service.model.DailyBehaviorMetricsSeedResult;

public interface DailyBehaviorMetricsSeedService {

    DailyBehaviorMetricsSeedResult seedForUser(String userEmail, int days, boolean force);
}

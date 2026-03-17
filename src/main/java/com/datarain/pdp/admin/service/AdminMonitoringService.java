package com.datarain.pdp.admin.service;

import com.datarain.pdp.admin.dto.AdminSystemOverviewRequest;
import com.datarain.pdp.admin.dto.AdminSystemOverviewResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AdminMonitoringService {
    AdminSystemOverviewResponse getSystemOverview(AdminSystemOverviewRequest request, HttpServletRequest httpRequest);
}

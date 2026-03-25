package com.datarain.pdp.admin.service;

import com.datarain.pdp.admin.dto.JobControlChangeResponse;
import com.datarain.pdp.admin.dto.JobControlResponse;
import com.datarain.pdp.admin.dto.JobControlUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AdminJobControlService {
    JobControlResponse getStatus();
    JobControlChangeResponse getLatestChange();
    JobControlResponse update(JobControlUpdateRequest request, HttpServletRequest httpRequest);
}

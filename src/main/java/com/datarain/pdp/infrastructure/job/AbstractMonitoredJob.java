package com.datarain.pdp.infrastructure.job;

import com.datarain.pdp.infrastructure.job.monitoring.JobMonitoringService;
import com.datarain.pdp.infrastructure.job.monitoring.MonitoredJobAction;

public abstract class AbstractMonitoredJob {

    private final JobMonitoringService jobMonitoringService;

    protected AbstractMonitoredJob(JobMonitoringService jobMonitoringService) {
        this.jobMonitoringService = jobMonitoringService;
    }

    protected long executeMonitored(String jobName, MonitoredJobAction action) {
        return jobMonitoringService.execute(jobName, action);
    }
}

package com.datarain.pdp.infrastructure.job.monitoring;

@FunctionalInterface
public interface MonitoredJobAction {
    long run() throws Exception;
}

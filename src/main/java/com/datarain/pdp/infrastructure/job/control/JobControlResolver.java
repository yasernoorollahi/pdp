package com.datarain.pdp.infrastructure.job.control;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Service
public class JobControlResolver {

    public static final String GLOBAL_KEY = "__GLOBAL__";
    private final JobControlSettingRepository repository;
    private final Environment environment;

    public JobControlResolver(JobControlSettingRepository repository, Environment environment) {
        this.repository = repository;
        this.environment = environment;
    }

    public boolean isGlobalEnabled() {
        return readOverride(GLOBAL_KEY).orElse(true);
    }

    public Boolean globalOverride() {
        return readOverride(GLOBAL_KEY).orElse(null);
    }

    public boolean isJobEnabled(ManagedJob job) {
        return resolveEffective(job.getKey(), job.getPropertyKey());
    }

    public Map<ManagedJob, JobEffectiveState> resolveAll() {
        Map<ManagedJob, JobEffectiveState> result = new EnumMap<>(ManagedJob.class);
        boolean globalEnabled = isGlobalEnabled();
        boolean globalConfigured = true;
        Optional<Boolean> globalOverride = readOverride(GLOBAL_KEY);

        for (ManagedJob job : ManagedJob.values()) {
            boolean configuredEnabled = readBoolean(job.getPropertyKey(), true);
            Optional<Boolean> override = readOverride(job.getKey());
            boolean effectiveEnabled;
            if (override.isPresent()) {
                effectiveEnabled = override.get();
            } else if (globalOverride.isPresent()) {
                effectiveEnabled = globalOverride.get();
            } else {
                effectiveEnabled = configuredEnabled;
            }
            result.put(job, new JobEffectiveState(
                    globalConfigured,
                    globalOverride.orElse(null),
                    configuredEnabled,
                    override.orElse(null),
                    effectiveEnabled
            ));
        }
        return result;
    }

    private boolean resolveEffective(String key, String propertyKey) {
        Optional<Boolean> override = readOverride(key);
        if (override.isPresent()) {
            return override.get();
        }
        Optional<Boolean> globalOverride = readOverride(GLOBAL_KEY);
        if (globalOverride.isPresent()) {
            return globalOverride.get();
        }
        return readBoolean(propertyKey, true);
    }

    private Optional<Boolean> readOverride(String key) {
        return repository.findById(key).map(JobControlSetting::getEnabledOverride);
    }

    private boolean readBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(environment.getProperty(key, String.valueOf(defaultValue)));
    }

    public record JobEffectiveState(
            boolean globalConfigured,
            Boolean globalOverride,
            boolean configuredEnabled,
            Boolean overrideEnabled,
            boolean effectiveEnabled
    ) {}
}

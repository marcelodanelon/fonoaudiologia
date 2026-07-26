package com.fonoaudiologia.service;

import com.fonoaudiologia.entity.SystemConfig;
import com.fonoaudiologia.entity.User;
import com.fonoaudiologia.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SystemConfigService {

    private final SystemConfigRepository configRepository;

    public SystemConfigService(SystemConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public List<SystemConfig> findAll() {
        return configRepository.findAll();
    }

    public Optional<SystemConfig> findByKey(String key) {
        return configRepository.findByConfigKey(key);
    }

    public String getValue(String key, String defaultValue) {
        return configRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }

    public long getSessionTimeoutMinutes() {
        String val = getValue("session_timeout_minutes", "30");
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    public SystemConfig update(String key, String value, User updatedBy) {
        SystemConfig config = configRepository.findByConfigKey(key)
                .orElse(new SystemConfig(key, value, ""));
        config.setConfigValue(value);
        config.setUpdatedBy(updatedBy);
        return configRepository.save(config);
    }
}

package org.kevin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "limit-config")
public class LimitConfigProperties {
    /**
     * 描述每个分组包含哪些api
     * groups: groupName -> GroupConfig
     */
    private Map<String, GroupConfig> groups = new HashMap<>();

    /**
     * 描述每个user在特定group下的阈值
     * thresholds: user -> (groupName -> threshold)
     */
    private Map<String, Map<String, Integer>> thresholds = new HashMap<>();

    public static class GroupConfig {
        private List<String> apis = new ArrayList<>();

        public List<String> getApis() {
            return apis;
        }

        public void setApis(List<String> apis) {
            this.apis = apis;
        }
    }

    public Map<String, GroupConfig> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, GroupConfig> groups) {
        this.groups = groups;
    }

    public Map<String, Map<String, Integer>> getThresholds() {
        return thresholds;
    }

    public void setThresholds(Map<String, Map<String, Integer>> thresholds) {
        this.thresholds = thresholds;
    }
}

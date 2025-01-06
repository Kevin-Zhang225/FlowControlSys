package org.kevin.kafka;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ApiRequestProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String TOPIC = "api_requests";

    @Autowired
    public ApiRequestProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 发送 api 请求事件到kafka
     * @param userId
     * @param groupName
     * @param apiPath
     * @param timestamp
     */
    public void sendApiRequest(String userId, String groupName, String apiPath, long timestamp) {
        ApiRequestEvent event = new ApiRequestEvent(userId, groupName, apiPath, timestamp);

        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, userId, message);
        } catch (JsonProcessingException e) {
            // process exception
            e.printStackTrace();
        }
    }

    /**
     * Api 请求事件数据结构
     */
    public static class ApiRequestEvent {
        private String userId;
        private String groupName;
        private String apiPath;
        private long timestamp;

        public ApiRequestEvent() {
        }

        public ApiRequestEvent(String userId, String groupName, String apiPath, long timestamp) {
            this.userId = userId;
            this.groupName = groupName;
            this.apiPath = apiPath;
            this.timestamp = timestamp;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public String getApiPath() {
            return apiPath;
        }

        public void setApiPath(String apiPath) {
            this.apiPath = apiPath;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}

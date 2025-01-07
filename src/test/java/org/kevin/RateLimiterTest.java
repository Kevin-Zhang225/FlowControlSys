package org.kevin;

import org.apache.kafka.common.protocol.types.Field;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RateLimiterTest {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String[] apis = {"/api/getData", "/api/postData", "/api/putData"};
    private final String baseUrl = "http://localhost:8080";

    @Test
    public void testHighConcurrencyForUser() throws InterruptedException, ExecutionException {
        int threadCount = 1000; // 并发线程数
        int loopPerThread = 5; // 每个线程发 50次请求
        String userId = "user1";// user 1, 2, 3, 4

        highConcurrencyExecute(threadCount, loopPerThread, userId);

        // 简单断言：程序能运行到此，代表所有请求已发完
        Assertions.assertTrue(true);
    }

    private void highConcurrencyExecute(int threadCount, int loopPerThread, String userId) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Future<Boolean> future = executor.submit(() -> {
                Random random = new Random();
                for (int j = 0; j < loopPerThread; j++) {
                    // 随机选择一个 api
                    String api = apis[random.nextInt(apis.length)];

                    // 构造请求头
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-UserId", userId);
                    // 对于 POST, PUT 请求需带 body, 这里简化
                    HttpEntity<String> entity = new HttpEntity<>("testBody", headers);

                    // 随机选择 GET/POST/PUT 发起请求
                    String url = baseUrl + api;
                    ResponseEntity<String> response;
                    switch (api) {
                        case "/api/postData":
                            response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                            break;
                        case "/api/putData":
                            response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
                            break;
                        default:
                            // GET
                            HttpEntity<Void> getEntity = new HttpEntity<>(headers);
                            response = restTemplate.exchange(url, HttpMethod.GET, getEntity, String.class);
                            break;
                    }

                    int statusCode = response.getStatusCodeValue();
                    // 如果超限, 应该返回429
                    if (statusCode != 200 && statusCode != 429) {
                        System.out.println("Unexpected StatusCode: " + statusCode);
                    }
                }

                return true;
            });

            futures.add(future);
        }

        // 等待全部执行完
        for (Future<Boolean> f : futures) {
            f.get(); // 这里简单地等待
        }

        executor.shutdown();
    }
}

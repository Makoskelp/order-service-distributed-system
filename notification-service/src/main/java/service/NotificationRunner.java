package service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import service.controllers.NotificationListener;

@Component
public class NotificationRunner implements ApplicationRunner {

    @Value("${spring.artemis.host:localhost}")
    private String brokerHost;

    @Override
    public void run(ApplicationArguments args) {
        new Thread(() -> NotificationListener.main(new String[]{brokerHost})).start();
    }
}

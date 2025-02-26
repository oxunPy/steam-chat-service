package org.example.steamchatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SteamChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteamChatServiceApplication.class, args);
    }

}

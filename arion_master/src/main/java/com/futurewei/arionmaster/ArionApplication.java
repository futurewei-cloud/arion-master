package com.futurewei.arionmaster;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.hazelcast.repository.config.EnableHazelcastRepositories;


@SpringBootApplication
@EnableHazelcastRepositories
public class ArionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArionApplication.class, args);
    }
}
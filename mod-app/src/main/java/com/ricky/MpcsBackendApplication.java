package com.ricky;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@ComponentScan(basePackages = {"com.ricky.*"})
public class MpcsBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(MpcsBackendApplication.class, args);
    }
}

package com.smartlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * SmartLib: Unified University Library Hub
 * Stage 1: Project Foundation, MySQL Configuration, and Thymeleaf Homepage.
 */
@SpringBootApplication
public class SmartLibApplication {

    private static final Logger log = LoggerFactory.getLogger(SmartLibApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SmartLibApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "/");
        String hostAddress = "localhost";

        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using 'localhost' as fallback");
        }

        log.info("""
                
                --------------------------------------------------------------------------------
                \tApplication '{}' is running!
                \tAccess URLs:
                \t- Local:      {}://localhost:{}{}
                \t- External:   {}://{}:{}{}
                \t- Health:     {}://localhost:{}{}/api/health
                \t- Profiles:   {}
                \t- Stage:      Stage 1 (Project Foundation & Homepage)
                --------------------------------------------------------------------------------
                """,
                env.getProperty("spring.application.name", "smartlib"),
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                protocol, serverPort, contextPath,
                String.join(",", env.getActiveProfiles().length == 0 ? new String[]{"default"} : env.getActiveProfiles())
        );
    }
}

package com.vyxentra.vehicle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    private static final Logger logger = LoggerFactory.getLogger(ConfigServerApplication.class);

	public static void main(String[] args) {

        SpringApplication app=new SpringApplication(ConfigServerApplication.class);
        Environment env = app.run(args).getEnvironment();

        String protocol = env
                .getProperty("server.ssl.enabled", "false")
                .equals("true") ? "https" : "http";
        String port=env.getProperty("server.port", "8888");
        String path=env.getProperty("server.servlet.context-path", "");
        logger.info("""
                
                ----------------------------------------------------------
                \tConfig Server is running!
                \tAccess URL: {}://localhost:{}{}
                \tProfile(s): {}
                \tEncryption Enabled: {}
                \tEncryption Key Strength: {}
                ----------------------------------------------------------""",
                protocol,
                port,
                path,
                env.getActiveProfiles().length > 0 ? String.join(",", env.getActiveProfiles()) : "default",
                env.getProperty("encrypt.key-store.location") != null ? "Yes" : "No",
                env.getProperty("encrypt.key-strength", "256"));
    }

}

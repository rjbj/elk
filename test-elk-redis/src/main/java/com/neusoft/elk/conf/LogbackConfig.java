package com.neusoft.elk.conf;

import javax.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.neusoft.elk.logback.RedisAppender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@Component
public class LogbackConfig {

    @Value("${elk.host}")
    private String host;

    @Value("${elk.port}")
    private int port;

    @Value("${elk.level}")
    private String level;

    @Value("${elk.type}")
    private String type;

    private static String Key = "logstash";

    @PostConstruct
    public void init() {

        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        RedisAppender appender = new RedisAppender();
        appender.setName("stash");
        appender.setHost(host);
        appender.setPort(port);
        appender.setType(type);
        appender.setKey(Key);
        appender.setContext(rootLogger.getLoggerContext());
        appender.start();
        rootLogger.addAppender(appender);
        rootLogger.setLevel(Level.toLevel(level));
    }

}

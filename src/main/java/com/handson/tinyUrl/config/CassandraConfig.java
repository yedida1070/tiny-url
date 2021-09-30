package com.handson.tinyUrl.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Paths;

@Configuration
public class CassandraConfig {

    @Bean("cassandraSession")
    public CqlSession getCassandraSession() throws URISyntaxException {
        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress("18.118.156.136", 9042))
                .withKeyspace("tiny_keyspace")
                .withLocalDatacenter("datacenter1")
                .build();
    }

}


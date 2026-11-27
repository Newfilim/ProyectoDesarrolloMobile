package com.zaror.backend.config;

import java.time.Clock;
import java.time.ZoneId;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class Infraestructura {
    public static final ZoneId SANTIAGO = ZoneId.of("America/Santiago");

    @Bean
    Clock reloj() { return Clock.system(SANTIAGO); }

    @Bean
    ReentrantLock lockEscrituras() { return new ReentrantLock(); }

    @Bean
    MongoTransactionManager gestorMongo(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }

    @Bean
    TransactionTemplate transacciones(MongoTransactionManager gestorMongo) {
        return new TransactionTemplate(gestorMongo);
    }
}

package com.handson.tinyUrl.service;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;

import com.handson.tinyUrl.entities.UserClicks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.*;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.dropTable;

@Component
public class Cassandra {
    private static final String USER_CLICKS_TABLE = "user_clicks";
    private static final String ID_COLUMN = "id";
    private static final String NAME_COLUMN = "user_name";
    private static final String TIME_COLUMN = "click_time";
    private static final String CLICK_URL_COLUMN = "click_url";
    private static final String CLICK_TINY_COLUMN = "click_tiny";

    @Autowired
    private CqlSession cassandraSession;

    public void insertUserClicks(String userName, String tiny, String url) {
        cassandraSession.execute(
                insertInto(USER_CLICKS_TABLE)
                        .value(NAME_COLUMN, literal(userName))
                        .value(TIME_COLUMN, literal(new Date().getTime()))
                        .value(CLICK_TINY_COLUMN, literal(tiny))
                        .value(CLICK_URL_COLUMN, literal(url))
                        .ifNotExists()
                        .build());
    }

    public List<UserClicks> getClicks() {
        return cassandraSession.execute(
                        selectFrom(USER_CLICKS_TABLE).all()
                                .build())
                .all().stream().map(row -> UserClicks.parse(row))
                .collect(Collectors.toList());
    }

    public List<UserClicks> getUserClicks(String userId) {
        return  cassandraSession.execute(
                        selectFrom(USER_CLICKS_TABLE).all()
                                .whereColumn(NAME_COLUMN).isEqualTo(literal(userId))
                                .build())
                .all().stream().map(row -> UserClicks.parse(row))
                .collect(Collectors.toList());
    }

    public void dropUsersClicksTable() {
        drop(USER_CLICKS_TABLE);
    }

    public void drop(String tableName) {
        cassandraSession.execute(
                dropTable(tableName)
                        .ifExists()
                        .build());
    }

    @PostConstruct
    public void createUsersClicksTable() {
        cassandraSession.execute(
                SchemaBuilder.createTable(
                                "tiny_keyspace", USER_CLICKS_TABLE).ifNotExists()
                        .withPartitionKey(TIME_COLUMN, DataTypes.TIMESTAMP)
                        .withColumn(NAME_COLUMN, DataTypes.TEXT)
                        .withColumn(CLICK_URL_COLUMN, DataTypes.TEXT)
                        .withColumn(CLICK_TINY_COLUMN, DataTypes.TEXT)
                        .build());
    }
}


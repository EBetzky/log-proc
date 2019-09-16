package com.eb.services.impl;

import com.eb.model.ProcessedEventEntity;
import com.eb.services.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.String.format;
import static java.sql.DriverManager.getConnection;

public class HsqldbProcessedEventPersistence implements Output {

    private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    private static final String CREATE_EVENTS_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS events "
            + "(id VARCHAR(64) PRIMARY KEY, duration INTEGER, type VARCHAR(32), host VARCHAR(16), alert BOOLEAN)";
    private static final String INSERT_INTO_EVENTS_STATEMENT = "INSERT INTO events(id,duration,type,host,alert) VALUES('%s',%d,'%s','%s',%s)";
    private static final String HSQLDB_URL = "jdbc:hsqldb:file:./db/event_output_db";
    private static final String HSQLDB_USER = "SA";
    private static final String HSQLDB_PASSWORD = "";
    private static final Logger LOGGER = LogManager.getLogger(HsqldbProcessedEventPersistence.class);

    static {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException ex) {
            LOGGER.error("DB driver not found!");
        }
    }

    private Connection connection;

    public HsqldbProcessedEventPersistence() {
        try {
            connection = getConnection(HSQLDB_URL, HSQLDB_USER, HSQLDB_PASSWORD);
            Statement statement = connection.createStatement();
            int result = statement.executeUpdate(CREATE_EVENTS_TABLE_STATEMENT);

        } catch (SQLException e) {
            LOGGER.error("Problem with DB init: ", e);
        }
    }

    @Override
    public void write(ProcessedEventEntity entity) {

        //TODO: extract & test preparing statement
        final String insertStatement = format(
                INSERT_INTO_EVENTS_STATEMENT,
                entity.getId(),
                entity.getDuration(),
                entity.getType(),
                entity.getHost(),
                Boolean.toString(entity.isAlert()).toUpperCase());

        try {
            Statement statement = connection.createStatement();
            int result = statement.executeUpdate(insertStatement);
        } catch (SQLException e) {
            LOGGER.error("Problem during saving final event, id: {}, {} ", entity.getId(), e);
        }
        LOGGER.debug("Processed event has been saved, id: {}", entity.getId());
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            LOGGER.error("Problem during shutdown and closing connection: ", e);
        }
    }
}

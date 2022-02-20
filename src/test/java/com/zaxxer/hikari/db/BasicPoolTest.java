package com.zaxxer.hikari.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static com.zaxxer.hikari.pool.TestElf.getPool;
import static com.zaxxer.hikari.pool.TestElf.getUnsealedConfig;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class BasicPoolTest {

    @BeforeAll
    public static void setup() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(2);
        config.setConnectionTestQuery("SELECT 1");
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.addDataSourceProperty("url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        try (HikariDataSource ds = new HikariDataSource(config);
             Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS basic_pool_test");
            stmt.execute("CREATE TABLE basic_pool_test ("
                    + "id INTEGER NOT NULL PRIMARY KEY, "
                    + "timestamp TIMESTAMP, "
                    + "string VARCHAR(128), "
                    + "string_from_number NUMERIC "
                    + ")");
        }
    }

    @Test
    public void testIdleTimeout() throws InterruptedException, SQLException
    {
        HikariConfig config = new HikariConfig();
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(10);
        config.setConnectionTestQuery("SELECT 1");
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.addDataSourceProperty("url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        System.setProperty("com.zaxxer.hikari.housekeeping.periodMs", "1000");

        try (HikariDataSource ds = new HikariDataSource(config)) {
            getUnsealedConfig(ds).setIdleTimeout(3000);

            System.clearProperty("com.zaxxer.hikari.housekeeping.periodMs");

            SECONDS.sleep(1);

            HikariPool pool = getPool(ds);

            Assertions.assertEquals( 5, pool.getTotalConnections());
            Assertions.assertEquals( 5, pool.getIdleConnections());

            try (Connection connection = ds.getConnection()) {
                Assertions.assertNotNull(connection);

                MILLISECONDS.sleep(1500);

                Assertions.assertEquals( 6, pool.getTotalConnections());
                Assertions.assertEquals( 5, pool.getIdleConnections());
            }

            Assertions.assertEquals( 6, pool.getIdleConnections());

            MILLISECONDS.sleep(3000);

            Assertions.assertEquals( 5, pool.getTotalConnections());
            Assertions.assertEquals( 5, pool.getIdleConnections());
        }
    }

    @Test
    public void testIdleTimeout2() throws InterruptedException, SQLException
    {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(50);
        config.setConnectionTestQuery("SELECT 1");
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.addDataSourceProperty("url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        System.setProperty("com.zaxxer.hikari.housekeeping.periodMs", "1000");

        try (HikariDataSource ds = new HikariDataSource(config)) {
            System.clearProperty("com.zaxxer.hikari.housekeeping.periodMs");

            SECONDS.sleep(1);

            HikariPool pool = getPool(ds);

            getUnsealedConfig(ds).setIdleTimeout(3000);

            Assertions.assertEquals( 50, pool.getTotalConnections());
            Assertions.assertEquals( 50, pool.getIdleConnections());

            try (Connection connection = ds.getConnection()) {
                Assertions.assertNotNull(connection);

                MILLISECONDS.sleep(1500);

                Assertions.assertEquals( 50, pool.getTotalConnections());
                Assertions.assertEquals( 49, pool.getIdleConnections());
            }

            Assertions.assertEquals( 50, pool.getIdleConnections());

            SECONDS.sleep(3);

            Assertions.assertEquals( 50, pool.getTotalConnections());
            Assertions.assertEquals( 50, pool.getIdleConnections());
        }
    }

}

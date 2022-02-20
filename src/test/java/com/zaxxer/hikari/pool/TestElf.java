package com.zaxxer.hikari.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.lang.reflect.Field;

public class TestElf {

    public static HikariConfig getUnsealedConfig(final HikariDataSource ds)
    {
        try {
            HikariPool pool = getPool(ds);
            Field configField = PoolBase.class.getDeclaredField("config");
            configField.setAccessible(true);
            HikariConfig config = (HikariConfig) configField.get(pool);

            Field field = HikariConfig.class.getDeclaredField("sealed");
            field.setAccessible(true);
            field.setBoolean(config, false);
            return config;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HikariPool getPool(final HikariDataSource ds)
    {
        try {
            Field field = ds.getClass().getDeclaredField("pool");
            field.setAccessible(true);
            return (HikariPool) field.get(ds);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}

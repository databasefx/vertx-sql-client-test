package cn.navigational.vertx.client.test;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;

import java.util.List;

public class PgDateTimeSpeValTest {
    private static final Vertx VERTX = Vertx.vertx();
    private static final PgConnectOptions CON_OPTIONS = new PgConnectOptions()
            .setHost("localhost")
            .setUser("postgres")
            .setPassword("postgres")
            .setDatabase("postgres");
    private static final PoolOptions POOL_OPTIONS = new PoolOptions()
            .setMaxSize(10)
            .setMaxWaitQueueSize(10);
    private static final String DROP_SQL = "DROP ROLE IF EXISTS vertx_pg_user";

    private static final String SELECT_SQL = "SELECT * FROM pg_roles r WHERE r.rolname='vertx_pg_user'";

    private static final String CREATE_SQL = "CREATE ROLE vertx_pg_user LOGIN PASSWORD 'test' VALID UNTIL 'infinity'";

    public static void main(String[] args) {
        PgPool pool = PgPool.pool(VERTX, CON_OPTIONS, POOL_OPTIONS);
        pool.query(DROP_SQL).execute()
                .compose(rowSet -> {
                    int count = rowSet.rowCount();
                    if (count <= 0) {
                        System.out.println("vertx_pg_user role not exist,so So it can't be deleted!");
                    } else {
                        System.out.println("Success delete vertx_pg_user");
                    }
                    return pool.query(CREATE_SQL).execute();
                })
                .compose(rowSet -> {
                    System.out.println("Success create vertx_pg_user role");
                    return pool.query(SELECT_SQL).execute();
                })
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        System.out.println("Query result:");
                        List<String> columns = ar.result().columnsNames();
                        for (Row row : ar.result()) {
                            for (String column : columns) {
                                System.out.println(column + "=" + row.getValue(column));
                            }
                        }
                    } else {
                        ar.cause().printStackTrace();
                    }
                    pool.close();
                    VERTX.close();
                });
    }
}

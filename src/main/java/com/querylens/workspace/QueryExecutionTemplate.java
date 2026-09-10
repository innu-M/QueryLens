package com.querylens.workspace;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class QueryExecutionTemplate {
    private static final int MAX_ROWS = 100;

    public final RawQueryResult execute(Path databasePath, String sql, SqlQueryType type) {
        long started = System.nanoTime();
        try (Connection connection = openConnection(databasePath);
             Statement statement = connection.createStatement()) {
            if (type == SqlQueryType.SELECT) {
                return readRows(statement.executeQuery(sql), elapsedMillis(started));
            }
            int affectedRows = statement.executeUpdate(sql);
            return new RawQueryResult(false, List.of(), List.of(), affectedRows, elapsedMillis(started));
        } catch (Exception exception) {
            throw new IllegalStateException("Query execution failed: " + exception.getMessage(), exception);
        }
    }

    protected abstract Connection openConnection(Path databasePath) throws Exception;

    private RawQueryResult readRows(ResultSet resultSet, long durationMillis) throws Exception {
        try (resultSet) {
            ResultSetMetaData metadata = resultSet.getMetaData();
            List<String> columns = new ArrayList<>();
            for (int column = 1; column <= metadata.getColumnCount(); column++) columns.add(metadata.getColumnLabel(column));
            List<List<String>> rows = new ArrayList<>();
            while (resultSet.next() && rows.size() < MAX_ROWS) {
                List<String> row = new ArrayList<>();
                for (int column = 1; column <= metadata.getColumnCount(); column++) row.add(String.valueOf(resultSet.getObject(column)));
                rows.add(row);
            }
            return new RawQueryResult(true, columns, rows, 0, durationMillis);
        }
    }

    private long elapsedMillis(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    public record RawQueryResult(boolean returnsRows, List<String> columns, List<List<String>> rows,
                                 int affectedRows, long durationMillis) { }
}

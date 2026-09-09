package com.querylens.sandbox;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/** Tests a proposed index on a disposable database copy and never modifies the original. */
public final class SandboxIndexTester {
    public SandboxIndexResult test(Path sourceDatabase, String selectSql, ProposedIndex proposal) {
        if (!selectSql.trim().toUpperCase().startsWith("SELECT")) {
            throw new IllegalArgumentException("Sandbox tests accept SELECT queries only.");
        }
        Path sandbox = null;
        try {
            sandbox = Files.createTempFile("querylens-index-sandbox-", ".db");
            Files.copy(sourceDatabase, sandbox, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            QuerySample original = measure(sourceDatabase, selectSql);
            try (Connection connection = connect(sandbox); Statement statement = connection.createStatement()) {
                statement.execute(proposal.createStatement());
            }
            QuerySample indexed = measure(sandbox, selectSql);
            return new SandboxIndexResult(original.elapsedNanos(), indexed.elapsedNanos(), original.fingerprint().equals(indexed.fingerprint()));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not complete the sandbox index test.", exception);
        } finally {
            if (sandbox != null) {
                try { Files.deleteIfExists(sandbox); } catch (Exception ignored) { }
            }
        }
    }

    private QuerySample measure(Path database, String selectSql) throws Exception {
        StringBuilder fingerprint = new StringBuilder();
        long started = System.nanoTime();
        try (Connection connection = connect(database); Statement statement = connection.createStatement(); ResultSet results = statement.executeQuery(selectSql)) {
            int columns = results.getMetaData().getColumnCount();
            while (results.next()) {
                for (int column = 1; column <= columns; column++) fingerprint.append(results.getObject(column)).append('|');
                fingerprint.append('\n');
            }
        }
        return new QuerySample(System.nanoTime() - started, fingerprint.toString());
    }

    private Connection connect(Path database) throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:" + database.toAbsolutePath());
    }

    private record QuerySample(long elapsedNanos, String fingerprint) { }
}

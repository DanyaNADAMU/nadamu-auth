package mu.nada.nadamuauth.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import mu.nada.nadamuauth.config.PluginConfig;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final Path dataDirectory;
    private final PluginConfig.DatabaseSettings settings;
    private final Logger logger;
    private HikariDataSource dataSource;

    public DatabaseManager(Path dataDirectory, PluginConfig.DatabaseSettings settings, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.settings = settings;
        this.logger = logger;
    }

    public void initialize() {
        Path dbPath = dataDirectory.resolve(settings.fileName());

        HikariConfig config = new HikariConfig();
        config.setPoolName("NadamuAuth-H2-Pool");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:" + dbPath.toAbsolutePath() + ";MODE=MySQL;DATABASE_TO_UPPER=FALSE;DB_CLOSE_DELAY=-1");
        config.setMaximumPoolSize(settings.poolSize());
        config.setMinimumIdle(2);
        config.setIdleTimeout(60000);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(1800000);

        this.dataSource = new HikariDataSource(config);

        initSchema();
        logger.info("Database initialized successfully at {}", dbPath);
    }

    private void initSchema() {
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS users (
                    uuid VARCHAR(36) NOT NULL PRIMARY KEY,
                    username VARCHAR(32) NOT NULL,
                    username_lower VARCHAR(32) NOT NULL UNIQUE,
                    password_hash VARCHAR(100) NOT NULL,
                    last_ip VARCHAR(45) NOT NULL,
                    registered_at TIMESTAMP NOT NULL,
                    last_login_at TIMESTAMP NOT NULL,
                    is_premium BOOLEAN NOT NULL DEFAULT FALSE
                );
                CREATE INDEX IF NOT EXISTS idx_users_username_lower ON users(username_lower);
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createTableSql);
        } catch (SQLException e) {
            logger.error("Failed to initialize database schema!", e);
            throw new RuntimeException("Database schema initialization failed", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("Database is not initialized!");
        }
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed.");
        }
    }
}

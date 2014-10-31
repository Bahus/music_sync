package ru.bahusoff.storage;

import org.slf4j.Logger;
import ru.bahusoff.utils.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * SQLite global connection
 */
public class DBService {
    private static final Logger LOG = LoggerFactory.getLogger(DBService.class);
    public static final String NULL = "NULL";

    // global connection object
    private static Connection connection = null;
    private static final String DB_NAME = "storage.db";

    public static void initialize() {
        try {
            DBService.getConnection();
            DBService.createTables();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        LOG.info("Storage initialization completed successfully");
    }

    public static PreparedStatement getCursor(String sql) throws SQLException, ClassNotFoundException {
        return DBService.getConnection().prepareStatement(sql);
    }

    public static boolean executeQuery(String sql) throws SQLException, ClassNotFoundException {
        PreparedStatement cursor = getCursor(sql);
        boolean result = cursor.execute();
        cursor.close();
        return result;
    }

    private static void createTables() throws SQLException, ClassNotFoundException {
        UserSettings.createTables();
        UserTracks.createTables();
        LOG.debug("Tables created");
    }

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        if (connection == null) {
            Class.forName("org.sqlite.JDBC");
            String connection_string = String.format("jdbc:sqlite:%s", DB_NAME);
            connection = DriverManager.getConnection(connection_string);
            LOG.debug("Connection '{}' established successfully", connection_string);
        }
        return connection;
    }
}

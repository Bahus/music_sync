package ru.bahusoff.storage;

import org.slf4j.Logger;
import ru.bahusoff.console.Console;
import ru.bahusoff.utils.LoggerFactory;
import ru.bahusoff.net.VkApiClient;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.Scanner;


public class UserSettings {
    private static final Logger LOG = LoggerFactory.getLogger(UserSettings.class);
    private static final String defaultNickname = "default";

    private Long uid = null;
    private String access_token = null;
    private String first_name = null;
    private String last_name = null;
    private String nickname = null;
    private String download_folder = null;
    private Date date_last_updated = null;

    static class SQL {

        static final String TABLE_SETTINGS_NAME = "Settings";
        static final String CREATE_SETTINGS = String.format(
            "create table if not exists '%s' (" +
            "  'id' integer primary key autoincrement," +
            "  'uid' integer unique," +
            "  'access_token' text not null," +
            "  'first_name' text," +
            "  'last_name' text," +
            "  'nickname' text unique default '%s'," +
            "  'download_folder' text," +
            "  'date_last_updated' datetime," +
            "  'app_version' integer default 1);", TABLE_SETTINGS_NAME, defaultNickname);
        static final String SELECT_SETTINGS = String.format(
            "select * from '%s' where nickname=?;", TABLE_SETTINGS_NAME);
        static final String INSERT_SETTINGS = String.format(
            "insert into '%s' (access_token, first_name, last_name, " +
            "download_folder, nickname, date_last_updated) " +
            "values (?, ?, ?, ?, ?, ?); ", TABLE_SETTINGS_NAME);
        static final String UPDATE_SETTINGS = String.format(
            "update '%s' set access_token=?, first_name=?, last_name=?, download_folder=?, " +
            "uid=?, date_last_updated=? where nickname=?", TABLE_SETTINGS_NAME);
    }

    public UserSettings() {
        this(defaultNickname);
    }
    public UserSettings(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Asks user to fill required options
     */
    public static UserSettings setup() throws Exception {
        UserSettings settings = getOrCreate();

        Console.printf("Welcome, %s!%n", settings.getUserName());

        boolean saveRequired = settings.setupAccessToken();
        saveRequired |= settings.setupDownloadDirectory();

        if (saveRequired) {
            settings.save();
        }
        return settings;
    }

    private boolean setupDownloadDirectory() {

        if (download_folder != null && !download_folder.equals("")) {
            Console.printf("Directory to sync with: @|yellow \"%s\"|@%n", download_folder);
            return false;
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            SyncDirectory defaultDirectory = SyncDirectory.getRecommendedDirectory();
            System.out.printf(
                "Enter directory to sync with (hit 'Enter' to use default '%s'): ",
                defaultDirectory
            );

            String providedDirectory = scanner.nextLine();

            if (providedDirectory.equals("")) {
                providedDirectory = defaultDirectory.toString();
            }

            if (!SyncDirectory.isExists(providedDirectory)) {
                Console.println("Directory not found.");
                continue;
            }
            download_folder = providedDirectory;
            break;
        }

        return true;
    }

    private boolean setupAccessToken() throws Exception {

        if (access_token != null) {
            Console.println("Access token found.");
            return false;
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("Be prepared to copy your access token from the address bar.");
        System.out.print("Press 'Enter' to open browser window.");

        scanner.nextLine();

        VkApiClient.openAuthorizeUrl();

        while (true) {
            System.out.println();
            System.out.print("Copy access token here and press 'Enter': ");

            access_token = scanner.nextLine();

            if (access_token.equals("")) {
                System.out.println("Bye!");
                System.exit(1);
            }

            System.out.println("Validating token...");

            boolean result = VkApiClient.updateUserSettings(this);

            if (result) {
                Console.printf("Hello, %s!%n", getUserName());
                break;
            } else {
                Console.println("Validation failed, try again.");
            }
        }

        return true;
    }

    public String getUserName() {
        if (first_name == null && last_name == null) {
            if (nickname.equals(defaultNickname)) {
                return "Guest";
            } else {
                return nickname;
            }
        }

        return String.format("%s %s (%s)", first_name, last_name, uid);
    }

    /**
     * Saves current settings to db.
     *
     * @throws SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public synchronized void save() throws SQLException, ClassNotFoundException {
        PreparedStatement cursor = DBService.getCursor(SQL.UPDATE_SETTINGS);
        cursor.setString(1, access_token);
        cursor.setString(2, first_name);
        cursor.setString(3, last_name);
        cursor.setString(4, download_folder);
        cursor.setLong(5, uid);
        cursor.setDate(6, (java.sql.Date) date_last_updated);
        cursor.setString(7, nickname);

        if (cursor.executeUpdate() == 0) {
            LOG.error("Could not save UserSettings, Statement ({})", cursor.toString());
        }
        LOG.debug("Settings saved successfully");
        System.out.println("Settings saved!");
        cursor.close();
    }

    /**
     * Reloads current settings from db.
     *
     * @throws SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public boolean reload() throws SQLException, ClassNotFoundException {
        boolean exists = false;

        PreparedStatement cursor = DBService.getCursor(SQL.SELECT_SETTINGS);
        cursor.setString(1, nickname);
        ResultSet result = cursor.executeQuery();

        while (result.next()) {
            ResultSetMetaData meta = result.getMetaData();

            for (int i = 1; i < meta.getColumnCount() + 1; i++) {
                // everything except integers
                if (!meta.getColumnLabel(i).equals("id")) {
                    String columnLabel = meta.getColumnLabel(i);

                    try {

                        Field field = this.getClass().getDeclaredField(columnLabel);
                        field.setAccessible(true);

                        Class typeClass = field.getType();
                        Object value;

                        if (typeClass.equals(Date.class)) {
                            value = result.getDate(i);
                        } else if (typeClass.equals(Long.class)) {
                            value = result.getLong(i);
                        } else {
                            value = result.getString(i);
                            if (value != null && value.equals(DBService.NULL)) {
                                value = null;
                            }
                        }

                        field.set(this, value);

                    } catch (NoSuchFieldException e) {
                        LOG.warn("Could not find field: '{}'", columnLabel);
                    } catch (IllegalAccessException e) {
                        LOG.error("Could not set column: '{}'", columnLabel);
                    }
                }
            }
            exists = true;
        }

        result.close();
        cursor.close();

        LOG.debug("Settings reloaded");
        return exists;
    }

    /**
     * Receives default settings from db or creates it if not exists.
     * @return UserSettings
     */
    public synchronized static UserSettings getOrCreate() throws SQLException, ClassNotFoundException {
        UserSettings settings = new UserSettings();

        if (settings.reload()) {
            // exists
            LOG.info("Using settings for '{}'", settings.nickname);
            return settings;
        }

        // create default settings
        PreparedStatement cursor = DBService.getCursor(SQL.INSERT_SETTINGS);
        cursor.setString(1, DBService.NULL);
        cursor.setString(2, DBService.NULL);
        cursor.setString(3, DBService.NULL);
        cursor.setString(4, DBService.NULL);
        cursor.setString(5, UserSettings.defaultNickname);
        cursor.setString(6, DBService.NULL);

        if (cursor.executeUpdate() == 0) {
            LOG.error("Could not create default UserSettings, Statement ({})", cursor.toString());
        }
        cursor.close();

        return settings;
    }

    public synchronized static boolean createTables() throws SQLException, ClassNotFoundException {
        PreparedStatement cursor = DBService.getCursor(SQL.CREATE_SETTINGS);
        boolean result = cursor.execute();
        cursor.close();
        return result;
    }

    public SyncDirectory getSyncDirectory() {
        return new SyncDirectory(download_folder);
    }

    public Date getDateLastUpdated() {
        return date_last_updated;
    }

    public String getAccessToken() {
        return access_token;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public void setFirstName(String firstName) {
        this.first_name = firstName;
    }

    public void setLastName(String lastName) {
        this.last_name = lastName;
    }

    @Override
    public String toString() {
        return String.format(
            "UserSettings: access_token=%s, nickname=%s, download_folder=%s",
            access_token,
            nickname,
            download_folder);
    }
}

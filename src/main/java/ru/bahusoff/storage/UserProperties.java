package ru.bahusoff.storage;

import ru.bahusoff.console.Console;
import ru.bahusoff.net.VkApiClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Scanner;

/**
 * User Settings stored in config file
 */
public class UserProperties {
    private static final String defaultFileName = "config.properties";
    private static final String defaultNickname = "default";

    private static Properties storage = null;

    private Long uid = null;
    private String access_token = null;
    private String first_name = null;
    private String last_name = null;
    private String nickname = null;
    private String download_folder = null;
    private Long sync_tracks_count = null;

    public UserProperties() throws IOException {
        if (storage == null) {
            readConfigFile();
        }

        this.first_name = storage.getProperty("first_name");
        this.last_name = storage.getProperty("last_name");
        this.nickname = storage.getProperty("nickname", defaultNickname);
        this.access_token = storage.getProperty("access_token");
        this.uid = Long.parseLong(storage.getProperty("uid", "0"));
        this.download_folder = storage.getProperty("download_folder");
        this.sync_tracks_count = Long.parseLong(storage.getProperty("sync_tracks_count", "10"));
    }

    public static void readConfigFile() throws IOException {
        File config = new File(defaultFileName);

        if (!config.exists()) {
            config.createNewFile();
        }

        InputStream inputStream = new FileInputStream(config);

        storage = new Properties();
        storage.load(inputStream);
        inputStream.close();
    }

    public static void saveConfigFile() throws IOException {
        if (storage == null) {
            return;
        }

        File config = new File(defaultFileName);
        OutputStream outputStream = new FileOutputStream(config);
        storage.store(outputStream, "MusicSyncConfig");
        outputStream.close();
    }

    /**
     * Asks user to fill required options
     */
    public static UserProperties setup() throws Exception {
        UserProperties properties = new UserProperties();

        Console.printf("Welcome, %s!%n", properties.getUserName());

        boolean saveRequired = properties.setupAccessToken();
        saveRequired |= properties.setupDownloadDirectory();

        if (saveRequired) {
            properties.save();
        }
        return properties;
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
            System.out.print("Paste access token here and press 'Enter': ");

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

    public Long setupSyncCount() throws IOException {

        while (true) {
            Console.printf("How many tracks to sync? [%s]: ", sync_tracks_count);

            Scanner scanner = new Scanner(System.in);
            String tracksCount = scanner.nextLine();

            if (!tracksCount.equals("")) {
                try {
                    sync_tracks_count = Long.parseLong(tracksCount);
                } catch (NumberFormatException e) {
                    Console.printRed("Invalid number, try again.", true);
                    continue;
                }
                save();
            }
            break;
        }
        return sync_tracks_count;
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
     * Saves current settings to config file.
     */
    public synchronized void save() throws IOException {
        storage.setProperty("first_name", first_name);
        storage.setProperty("last_name", last_name);
        storage.setProperty("uid", uid.toString());
        storage.setProperty("nickname", nickname);
        storage.setProperty("access_token", access_token);
        storage.setProperty("download_folder", download_folder);
        storage.setProperty("sync_tracks_count", sync_tracks_count.toString());
        saveConfigFile();
    }

    public SyncDirectory getSyncDirectory() {
        return new SyncDirectory(download_folder);
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

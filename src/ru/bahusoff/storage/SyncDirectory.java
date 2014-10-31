package ru.bahusoff.storage;

import java.io.File;

/**
 * Works with directory to sync with
 */
public class SyncDirectory {
    public static final String MUSIC_DIRECTORY = "Music";
    private File directory;

    public SyncDirectory(String directory) {
        this.directory = new File(directory);
    }

    public static boolean isExists(String directory) {
        File file = new File(directory);
        return (file.exists() && file.isDirectory());
    }

    public static SyncDirectory getRecommendedDirectory() {
        String userHome = System.getProperty("user.home");
        File dir = new File(userHome, MUSIC_DIRECTORY);
        return new SyncDirectory(dir.getPath());
    }

    public File getFullFileName(String onlyFileName) {
        return new File(this.directory, onlyFileName);
    }

    @Override
    public String toString() {
        return directory.getPath();
    }
}

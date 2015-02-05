package ru.bahusoff;

import org.slf4j.Logger;
import ru.bahusoff.console.Console;
import ru.bahusoff.net.TrackDownloadManager;
import ru.bahusoff.net.VkApiClient;
import ru.bahusoff.storage.AudioTrack;
import ru.bahusoff.storage.DBService;
import ru.bahusoff.storage.SyncDirectory;
import ru.bahusoff.storage.UserProperties;
import ru.bahusoff.utils.LoggerFactory;


public class Main {
    static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        Console.reset();

        LOG.info("Starting");
        DBService.initialize();

        UserProperties userProperties = UserProperties.setup();
        SyncDirectory dir = userProperties.getSyncDirectory();
        TrackDownloadManager manager = new TrackDownloadManager(dir);
        VkApiClient client = new VkApiClient(userProperties);

        Console.println("Receiving total audio count.");
        long totalCount = client.getAudioCount();

        Console.printf("You have %s audios in total.%n", totalCount);

        long tracksAmount = userProperties.setupSyncCount();
        if (tracksAmount > totalCount) {
            tracksAmount = totalCount;
        }

        for (AudioTrack track : client.getAudioTracks(0, tracksAmount)) {
            manager.addTrackToDownloadQueue(track);
        }

        manager.startDownload();

        Console.println("Synchronization completed.");
        LOG.info("Done");

    }
}

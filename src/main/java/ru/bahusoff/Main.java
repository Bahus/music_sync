package ru.bahusoff;

import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import ru.bahusoff.console.Console;
import ru.bahusoff.net.TrackDownloadManager;
import ru.bahusoff.net.VkApiClient;
import ru.bahusoff.storage.AudioTrack;
import ru.bahusoff.storage.DBService;
import ru.bahusoff.storage.SyncDirectory;
import ru.bahusoff.storage.UserSettings;
import ru.bahusoff.utils.LoggerFactory;


public class Main {
    static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void test() {

        Console.println("Hello 111");
        Console.println("Hello 222");
        Console.println("Hello 333");
        Console.println("Hello 444");

        Console.print(Ansi.ansi().saveCursorPosition());

        Console.print(
            Ansi.ansi()
                .cursorDown(2)
                .cursorLeft(100)
                .a("Progress 1")
        );

        Console.print(Ansi.ansi().restorCursorPosition());

        Console.print(
            Ansi.ansi()
                .cursorDown(3)
                .cursorLeft(100)
                .a("Progress 2")
        );

        Console.print(Ansi.ansi().restorCursorPosition());

        Console.print(
            Ansi.ansi()
                .cursorDown(2)
                .cursorLeft(100)
                .a("Progress 111")
        );

        Console.setCursor(0, 10);
        Console.newLine();
    }

    public static void main(String[] args) throws Exception {
        Console.reset();

        LOG.info("Starting");
        DBService.initialize();

        UserSettings userSettings = UserSettings.setup();
        SyncDirectory dir = userSettings.getSyncDirectory();
        TrackDownloadManager manager = new TrackDownloadManager(dir);
        VkApiClient client = new VkApiClient(userSettings);

        Console.println("Receiving total audio count.");
        Console.printf("You have %s audios in total.%n", client.getAudioCount());

        int tracksAmount = 10;

        for (AudioTrack track : client.getAudioTracks(0, tracksAmount)) {
            manager.addTrackToDownloadQueue(track);

//            AudioTrack saved_track = UserTracks.getTrack(track.getTrackId());
//
//            if (saved_track != null) {
//                System.out.println("Track already exists");
//                System.out.println(saved_track.toString());
//            } else {
//                track.save();
//                Console.println("Track saved");
//            }
        }
        manager.startDownload();

        Console.println("Download completed.");
        LOG.info("Done");

    }
}

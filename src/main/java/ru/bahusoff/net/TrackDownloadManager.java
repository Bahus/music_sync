package ru.bahusoff.net;

import org.slf4j.Logger;
import ru.bahusoff.console.ProgressReporter;
import ru.bahusoff.console.Reportable;
import ru.bahusoff.storage.AudioTrack;
import ru.bahusoff.storage.SyncDirectory;
import ru.bahusoff.utils.LoggerFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for tracks download using multiple threads
 */
public class TrackDownloadManager {
    private static final Logger LOG = LoggerFactory.getLogger(TrackDownloadManager.class);

    // TODO: should be configurable
    private static final ExecutorService task_executor = Executors.newFixedThreadPool(3);

    private SyncDirectory directory;
    private ArrayList<Reportable> tasks;

    public TrackDownloadManager(SyncDirectory directory) {
        this.directory = directory;
        this.tasks = new ArrayList<Reportable>();
    }

    public void addTrackToDownloadQueue(AudioTrack track) throws MalformedURLException {
        DownloadTask task = new DownloadTask(track, directory);
        addTask(task);
    }

    public void addTask(Reportable task) {
        tasks.add(task);
    }

    public void startDownload() {
        ProgressReporter reporter = ProgressReporter.getInstance(this.tasks.size());

        for (Reportable task : this.tasks) {
            reporter.registerTask(task);
            task_executor.submit(task);
        }
        task_executor.shutdown();
        try {
            task_executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            if (!task_executor.isTerminated()) {
                LOG.warn("Forcing tasks shutdown");
                task_executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            reporter.close();
        }
    }
}


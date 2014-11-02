package ru.bahusoff.net;

import org.slf4j.Logger;
import ru.bahusoff.console.ProgressReporter;
import ru.bahusoff.console.Reportable;
import ru.bahusoff.storage.AudioTrack;
import ru.bahusoff.storage.SyncDirectory;
import ru.bahusoff.utils.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class DownloadTask implements Reportable {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadTask.class);

    private AudioTrack track;
    private SyncDirectory directory;

    public DownloadTask(AudioTrack track, SyncDirectory directory) {
        this.track = track;
        this.directory = directory;
    }

    @Override
    public void run() {
        try {
            download();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getTrackFile() {
        return directory.getFullFileName(track.getAcceptableFileName());
    }
    private URL getTrackUrl() throws MalformedURLException {
        return new URL(track.getUrl());
    }

    @Override
    public String getTitle() {
        return track.getHumanFriendlyString();
    }

    public void download() throws IOException {
        BufferedInputStream in = null;
        FileOutputStream out = null;
        File trackFile = getTrackFile();
        URL trackUrl = getTrackUrl();
        ProgressReporter reporter = ProgressReporter.getInstance();
        URLConnection connection = trackUrl.openConnection();

        int possibleSize = Integer.parseInt(connection.getHeaderField("Content-Length"));

        try {
            LOG.info("Reading file from: {}", trackUrl.toString());

            in = new BufferedInputStream(trackUrl.openStream());
            out = new FileOutputStream(trackFile);

            // TODO: make configurable
            int chunk_size = 1024 * 10;

            int count;
            int percent;
            float total_downloaded = 0;
            final byte data[] = new byte[chunk_size];

            while ((count = in.read(data, 0, chunk_size)) != -1) {
                out.write(data, 0, count);
                total_downloaded = total_downloaded + count;
                percent = (int)((total_downloaded / (float)possibleSize) * 100);
                reporter.drawProgress(this, percent);
            }

            reporter.setTaskCompleted(this);
            LOG.info("File saved to: {}", trackFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            reporter.setTaskFailed(this);
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }

        }
    }
}

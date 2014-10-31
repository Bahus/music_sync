package ru.bahusoff.storage;

import org.json.simple.JSONObject;

import java.sql.Date;
import java.util.Calendar;

public class AudioTrack {
    private long id = 0;

    private Long track_id = null;
    private Long duration = null;
    private String title = null;
    private String track_info = null;
    private Boolean downloaded = null;
    private Date date_downloaded = null;
    private String url = null;

    /**
     * @param audio - json representation of vk.com audio-track,
     *              see api-method documentation here
     *              http://vk.com/dev/audio.getById
     */
    public AudioTrack(JSONObject audio) {
        this(audio, null);
    }

    public AudioTrack(JSONObject audio, Date date_downloaded) {
        // {
        //      "duration":574,
        //      "artist":"PQM",
        //      "owner_id":238496,
        //      "id":291434805,
        //      "title":"You Are Sleeping (PQM & Luke Chable Remix)",
        //      "url":"http:\/\/cs4881.vk.me\/u14959881\/audios\/ca9ca49d257f.mp3?extra=2qcCqeQ5Efu0UZlajiKQASurgCOlEwtEQQyAclYQjrMzIbaHR7txGscAvK6-aDAj8Fd8lx9O0wzsIua_3Wh4dGAU",
        //      "genre_id":18,"lyrics_id":6112434
        // }
        this.track_id = (Long) audio.get("id");
        this.duration = (Long) audio.get("duration");
        this.track_info = audio.toJSONString();
        this.title = String.format("%s – %s", audio.get("artist"), audio.get("title"));
        this.url = (String) audio.get("url");
        this.downloaded = date_downloaded != null;
        this.date_downloaded = date_downloaded;
    }

    public void setDownloaded(Boolean downloaded) {
        this.downloaded = downloaded;
    }

    public void setDefaultDate() {
        java.util.Date date = Calendar.getInstance().getTime();
        this.date_downloaded = new Date(date.getTime());
    }

    public Boolean getDownloaded() {
        return downloaded;
    }

    public Date getDateDownloaded() {
        return date_downloaded;
    }

    public String getHumanFriendlyString() {
        int rest_seconds = duration.intValue() % 60;
        int minutes = (duration.intValue() - rest_seconds) / 60;
        return String.format("%s [%s:%02d]", title, minutes, rest_seconds);
    }

    public String getAcceptableFileName() {
        int i = this.url.lastIndexOf('.');
        String ext = "";

        if (i > 0 &&  i < this.url.length() - 1) {
            ext = this.url.substring(i + 1).toLowerCase();
            // url may ends with ".mp3?extra=hash"
            int qs = ext.indexOf('?');
            if (qs > 0) {
                ext = ext.substring(0, qs);
            }
        }
        String normalized_title = this.title.replaceAll("[^\\w\\d\\s–\\?!\\(\\)]+", "");
        return String.format("%s.%s", normalized_title, ext);
    }

    public Long getTrackId() {
        return track_id;
    }

    public String getTrackInfo() {
        return track_info;
    }

    /**
     * Inserts or updates row in UserTracks table
     * related to this track (base on id)
     *
     * @throws Exception
     */
    public void save() throws Exception {
        setId(UserTracks.saveTrack(this));
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "AudioTrack{" +
            "id=" + id +
            ", track_id=" + track_id +
            ", duration=" + duration +
            ", title='" + title +
            ", url='" + url +
            ", downloaded=" + downloaded +
            ", date_downloaded=" + date_downloaded +
            "}";
    }
}

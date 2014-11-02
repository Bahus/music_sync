package ru.bahusoff.storage;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import ru.bahusoff.utils.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents collection of audio tracks
 */
public class UserTracks {
    public static final Logger LOG = LoggerFactory.getLogger(UserTracks.class);

    static class SQL {
        static final String TABLE_TRACKS_NAME = "Tracks";
        static final String CREATE_TRACKS = String.format(
            "create table if not exists '%s' (" +
                "  'id' integer primary key autoincrement," +
                "  'track_id' integer unique," +  // VK audio id, considered unique
                "  'track_info' text not null," +  // track info in json format, see: http://vk.com/dev/audio.getById
                "  'downloaded' boolean default false," +
                "  'date_downloaded' datetime);", TABLE_TRACKS_NAME);
        static final String INSERT_TRACK = String.format(
            "insert into '%s' (track_id, track_info, downloaded, date_downloaded) " +
                "values (?, ?, ?, ?)", TABLE_TRACKS_NAME);
        static final String SELECT_TRACK = String.format(
            "select track_info, downloaded, date_downloaded from '%s' where track_id=?;",
            TABLE_TRACKS_NAME);
        static final String UPDATE_TRACK = String.format(
            "update '%s' set track_id=?, track_info=?, downloaded=?, date_downloaded=? where id=?",
            TABLE_TRACKS_NAME);
    }

    public synchronized static boolean createTables() throws SQLException, ClassNotFoundException {
        return DBService.executeQuery(SQL.CREATE_TRACKS);
    }

    public synchronized static long saveTrack(AudioTrack track) throws Exception {
        long trackId = track.getId();
        boolean update = trackId > 0;

        String sql = update ? SQL.UPDATE_TRACK : SQL.INSERT_TRACK;

        PreparedStatement cursor = DBService.getCursor(sql);
        ResultSet generatedKeys = null;

        cursor.setLong(1, track.getTrackId());
        cursor.setString(2, track.getTrackInfo());
        cursor.setBoolean(3, track.getDownloaded());
        cursor.setDate(4, track.getDateDownloaded());

        if (update) {
            cursor.setLong(5, trackId);
        }

        try {
            int result = cursor.executeUpdate();

            if (result == 0) {
                return result;
            }

            LOG.debug(
                "{} track: '{}'",
                update ? "Updated" : "Inserted",
                track.toString());

            if (!update) {
                generatedKeys = cursor.getGeneratedKeys();

                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }


        } finally {
            if (generatedKeys != null) {
                generatedKeys.close();
            }
            cursor.close();
        }

        return trackId;
    }

    public static AudioTrack getTrack(long track_id) throws Exception {
        AudioTrack track = null;

        PreparedStatement cursor = DBService.getCursor(SQL.SELECT_TRACK);
        cursor.setLong(1, track_id);

        ResultSet result = cursor.executeQuery();
        if (result.next()) {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject)parser.parse(result.getString(1));
            track = new AudioTrack(json, result.getDate(3));
            track.setDownloaded(result.getBoolean(2));
        }

        result.close();
        cursor.close();

        return track;
    }
}

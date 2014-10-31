package ru.bahusoff.net;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import ru.bahusoff.storage.AudioTrack;
import ru.bahusoff.utils.LoggerFactory;
import ru.bahusoff.storage.UserSettings;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class VkApiClient {
    private static final Logger LOG = LoggerFactory.getLogger(VkApiClient.class);

    private UserSettings userSettings;

    private static final int APP_ID = 4447251;
    private static final String API_VERSION = "5.22";
    private static final String AUTHORIZE_URL = String.format(
        "https://oauth.vk.com/authorize?client_id=%s&scope=friends,audio,offline" +
        "&redirect_uri=https://oauth.vk.com/blank.html&display=popup&response_type=token",
        APP_ID
    );
    private static final String API_USER_URL = "https://api.vk.com/method/users.get?access_token=%s";
    private static final String API_URL = "https://api.vk.com/method/";

    public VkApiClient(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    /**
     * Opens authorization URL in default browser
     */
    public static void openAuthorizeUrl() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI(AUTHORIZE_URL));
    }

    protected static Object doRequest(URL url) throws Exception {
        LOG.debug("Creating connection to '{}'", url);

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "VKMusicSync-bot");

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream content = connection.getInputStream();
            Reader in = new InputStreamReader(content, "UTF-8");

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject)parser.parse(in);
            LOG.debug("Received JSON: {}", json);

            Object response = json.get("response");
            if (response == null) {
                LOG.error("Invalid response from vk.com");
                return null;
            }
            return response;

        } else {
            LOG.error(
                "Wrong response code '{} ({})' received for '{}'",
                connection.getResponseCode(),
                connection.getResponseMessage(),
                url
            );
        }

        return null;
    }

    private Object doApiRequest(String method) throws Exception {
        return doApiRequest(method, new HashMap<String, Object>(0));
    }

    private Object doApiRequest(String method, Map<String,Object> urlParams) throws Exception {
        urlParams.put("v", API_VERSION);
        urlParams.put("access_token", userSettings.getAccessToken());

        String url = String.format(
            "%s%s?%s", API_URL, method, NetHelpers.buildURLParams(urlParams)
        );
        return doRequest(new URL(url));
    }

    /**
     * Returns collection of audio tracks
     * see: http://vk.com/dev/audio.get
     * @param offset
     * @param count
     * @throws Exception
     */
    public ArrayList<AudioTrack> getAudioTracks(long offset, long count) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>(4);
        params.put("owner_id", userSettings.getUid());
        params.put("offset", offset);
        params.put("count", count);

        // { count: X, items: [{ ... }, { ... }] }
        JSONObject response = (JSONObject) doApiRequest("audio.get", params);
        if (response == null) {
            return null;
        }

        JSONArray items = (JSONArray)response.get("items");
        ArrayList<AudioTrack> tracks = new ArrayList<AudioTrack>();

        for (Object audio : items) {
            tracks.add(new AudioTrack((JSONObject) audio));
        }
        return tracks;
    }

    /**
     * Returns total count of user's audio files,
     * see http://vk.com/dev/audio.getCount
     * @return integer
     */
    public long getAudioCount() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("owner_id", userSettings.getUid());
        return (Long)doApiRequest("audio.getCount", params);
    }

    public static boolean updateUserSettings(UserSettings settings) throws Exception {
        VkApiClient client = new VkApiClient(settings);

        JSONArray json_array = (JSONArray)client.doApiRequest("users.get");

        if (json_array != null) {
            JSONObject json = (JSONObject)json_array.get(0);
            settings.setUid(new Long(json.get("id").toString()));
            settings.setFirstName((String) json.get("first_name"));
            settings.setLastName((String) json.get("last_name"));
            return true;
        }

        return false;
    }

}

package ru.bahusoff.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;


public class NetHelpers {

    /**
     * Builds url query from provided map of parameters.
     *
     * @param urlParams - map of key and value pairs
     * @return Query string
     * @throws UnsupportedEncodingException
     */
    public static String buildURLParams(Map<?,?> urlParams) throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder();
        for (Map.Entry<?, ?> entry : urlParams.entrySet()) {
            if (url.length() > 0) {
                url.append('&');
            }
            url.append(
                String.format("%s=%s",
                    URLEncoder.encode(entry.getKey().toString(), "UTF-8"),
                    URLEncoder.encode(entry.getValue().toString(), "UTF-8")
                )
            );
        }
        return url.toString();
    }
}

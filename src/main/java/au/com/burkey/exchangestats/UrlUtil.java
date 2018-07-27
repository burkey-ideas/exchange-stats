package au.com.burkey.exchangestats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlUtil
{
    private static final Logger log = LoggerFactory.getLogger(UrlUtil.class);

    public static JsonObject getJsonUrl(final String formatUrl, final Object... args) throws IOException
    {
        return getJsonUrl(null, null, formatUrl, args);
    }

    public static JsonObject getJsonUrl(final Map<String, String> headers, final Map<String, String> post, final String formatUrl, final Object... args) throws IOException
    {
        log.info("URL connection: " + formatUrl);

        String url = String.format(formatUrl, args);
        log.debug("URL connection: " + url);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

        conn.setUseCaches(false);

        if (headers != null)
        {
            for (Map.Entry<String, String> header : headers.entrySet())
            {
                String key = header.getKey();
                String value = header.getValue();

                if ("Authorization".equalsIgnoreCase(key))
                {
                    String[] creds = String.format(value, args).split("[ ]");

                    String auth;

                    if ("Basic".equalsIgnoreCase(creds[0]))
                    {
                        auth = creds[0] + ' ' + new String(Base64.getEncoder().encode(creds[1].getBytes("UTF-8")));
                    }
                    else
                    {
                        auth = creds[0] + ' ' + creds[1];
                    }

                    conn.setRequestProperty(key, auth);
                }
                else
                {
                    conn.setRequestProperty(key, value);
                }
            }
        }

        if (post != null)
        {
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            StringBuilder data = new StringBuilder();

            for (Map.Entry<String, String> param : post.entrySet())
            {
                if (data.length() > 0)
                {
                    data.append("&");
                }

                data.append(param.getKey()).append("=").append(param.getValue());
            }

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(data.toString());
        }

        conn.connect();

        int responseCode = conn.getResponseCode();

        if (responseCode == 200)
        {
            JsonReader jsonReader = Json.createReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            JsonObject jsonObject = jsonReader.readObject();

            return jsonObject;
        }
        else
        {
            InputStream stream = conn.getInputStream();

            if (stream == null)
            {
                throw new WebException(responseCode, null, "Failed without response.", null);
            }
            else
            {
                String responseText = getResponse(conn.getInputStream(), "UTF-8");
                throw new WebException(responseCode, responseText, "Failed with response", null);
            }
        }
    }

    public static String getTextUrl(final String formatUrl, final Object... args) throws IOException
    {
        log.info("URL connection: " + formatUrl);

        String url = String.format(formatUrl, args);
        log.debug("URL connection: " + url);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

        int responseCode = conn.getResponseCode();

        if (responseCode == 200)
        {
            String responseText = getResponse(conn.getInputStream(), "UTF-8");
            return responseText;
        }
        else
        {
            InputStream stream = conn.getInputStream();

            if (stream == null)
            {
                throw new WebException(responseCode, null, "Failed without response.", null);
            }
            else
            {
                String responseText = getResponse(conn.getInputStream(), "UTF-8");
                throw new WebException(responseCode, responseText, "Failed with response", null);
            }
        }
    }

    public static String getResponse(final InputStream stream, final String charsetName) throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charsetName)))
        {
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null)
            {
                response.append(line);
            }
            reader.close();

            return response.toString();
        }
    }

    public static String toStringKeys(final Map<String, String> map)
    {
        StringBuilder buf = new StringBuilder();

        for (Iterator<String> i = map.keySet().iterator(); i.hasNext();)
        {
            buf.append(i.next());
            if (i.hasNext())
            {
                buf.append(',');
            }
        }

        return buf.toString();
    }
}

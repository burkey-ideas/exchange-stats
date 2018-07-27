package au.com.burkey.exchangestats;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyUtil
{
    private static Properties properties;

    private static final Logger log = LoggerFactory.getLogger(PropertyUtil.class);

    public static synchronized Properties get()
    {
        try
        {
            if (properties == null)
            {
                log.info("Loading properties.");

                try (InputStream stream = new FileInputStream("exchange-stats.properties"))
                {
                    Properties props = new Properties();

                    props.load(stream);

                    properties = props;
                }
            }

            return properties;
        }
        catch (IOException ex)
        {
            log.error("Unable to load properties. " + ex.getClass().getSimpleName() + ": " + ex.getMessage());

            return new Properties();
        }
    }

    public static Map<String, String> getSubProperties(final Properties props, final String prefix)
    {
        Map<String, String> sub = null;

        for (Object obj : props.keySet())
        {
            String key = (String) obj;
            if (key.startsWith(prefix))
            {
                if (sub == null)
                {
                    sub = new HashMap<String, String>();
                }

                sub.put(key, props.getProperty(key));
            }
        }

        return sub;
    }
}

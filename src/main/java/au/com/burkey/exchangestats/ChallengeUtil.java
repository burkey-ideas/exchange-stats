package au.com.burkey.exchangestats;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChallengeUtil
{
    private static Properties properties;

    private static final Logger log = LoggerFactory.getLogger(ChallengeUtil.class);

    public static synchronized Properties get()
    {
        try
        {
            if (properties == null)
            {
                log.info("Loading challenge properties.");

                try (InputStream stream = new FileInputStream("challenge.properties"))
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
            log.error("Unable to load challenge properties. " + ex.getClass().getSimpleName() + ": " + ex.getMessage());

            return new Properties();
        }
    }

    public static synchronized void persist(final Properties props)
    {
        try
        {
            if (properties == null)
            {
                properties = props;
            }
            else
            {
                properties.putAll(props);
            }

            log.info("Saving challenge properties.");

            try (OutputStream stream = new FileOutputStream("challenge.properties"))
            {
                properties.store(stream, " challenge: token = authorization");

                // Trigger reload from file.
                properties = null;
            }
        }
        catch (IOException ex)
        {
            log.error("Unable to save challenge properties. " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }
}

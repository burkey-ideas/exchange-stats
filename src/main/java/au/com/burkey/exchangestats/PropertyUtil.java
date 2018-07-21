package au.com.burkey.exchangestats;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
}

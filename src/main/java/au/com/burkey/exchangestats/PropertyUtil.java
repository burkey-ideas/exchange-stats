package au.com.burkey.exchangestats;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtil
{
    private static Properties properties;

    public static Properties getProperties()
    {
        try
        {
            if (properties == null)
            {
                System.out.println("Loading properties.");

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
            System.err.println("Unable to load properties. " + ex.getClass().getSimpleName() + ": " + ex.getMessage());

            return new Properties();
        }
    }
}

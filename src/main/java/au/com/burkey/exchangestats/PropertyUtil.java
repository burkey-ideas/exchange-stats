package au.com.burkey.exchangestats;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtil
{
    private static Properties properties;

    public static Properties getProperties() throws FileNotFoundException, IOException
    {
        if (properties == null)
        {
            System.out.println("Loading properties.");

            try (InputStream stream = new FileInputStream("exchange-stats.properties"))
            {
                properties = new Properties();

                properties.load(stream);
            }
        }

        return properties;
    }
}

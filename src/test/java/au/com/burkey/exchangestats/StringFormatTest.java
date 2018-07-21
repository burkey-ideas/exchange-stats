package au.com.burkey.exchangestats;

import java.util.Arrays;

import org.junit.Test;

public class StringFormatTest
{
    @Test
    public void test()
    {
        for (String url : Arrays.asList(
                "https://www.dtdns.com/api/autodns.cfm?id=%3$s&pw=%2$s&ip=%5$s",
                "https://%1$s:%2$s@freedns.afraid.org/nic/update?hostname=%3$s&myip=%5$s",
                "https://freedns.afraid.org/dynamic/update.php?%4$s&address=%5$s",
                "https://www.duckdns.org/update?domains=%3$s&token=%4$s&ip=%5$s&verbose=true"))
        {
            String formatted = String.format(url, "username", "password", "hostname", "apikey", "address");

            System.out.println(formatted);
        }
    }
}

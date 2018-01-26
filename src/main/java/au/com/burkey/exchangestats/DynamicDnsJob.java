package au.com.burkey.exchangestats;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Calendar;

import javax.json.JsonObject;

public class DynamicDnsJob implements Runnable
{
    @Override
    public void run()
    {
        System.out.println("Running: " + this.getClass().getSimpleName());

        final String ipifyApiUrl = "http://api.ipify.org?format=json";
        final String dtdnsUrl = "http://www.dtdns.com/api/autodns.cfm?id=%s&pw=%s&ip=%s";

        try
        {
            String hostname = PropertyUtil.getProperties().getProperty("dns.hostname");
            String password = PropertyUtil.getProperties().getProperty("dns.password");

            JsonObject ipifyResult = UrlUtil.getJsonUrl(ipifyApiUrl);

            InetAddress externalAddress = InetAddress.getByName(ipifyResult.getString("ip"));
            System.out.println("External Address: " + externalAddress.getHostAddress());

            InetAddress resolvedAddress = InetAddress.getByName(hostname);
            System.out.println("Resolved Address: " + resolvedAddress.getHostAddress());

            Calendar today = Calendar.getInstance();

            if (today.get(Calendar.DAY_OF_MONTH) == 1 && today.get(Calendar.HOUR_OF_DAY) == 1)
            {
                System.out.println("First day of month, forcing update to ensure service does not go stale.");

                String dtdnsResult = UrlUtil.getTextUrl(dtdnsUrl, hostname, password, externalAddress.getHostAddress());
                System.out.println(dtdnsResult);
            }
            else if (externalAddress.getHostAddress().equals(resolvedAddress.getHostAddress()))
            {
                System.out.println("Address currently matches, no update required.");
            }
            else
            {
                System.out.println("Address has changed, attempting update.");

                String dtdnsResult = UrlUtil.getTextUrl(dtdnsUrl, hostname, password, externalAddress.getHostAddress());
                System.out.println(dtdnsResult);
            }
        }
        catch (WebException ex)
        {
            System.err.println("Unable to examine Address. " + ex.getMessage());
            System.err.println("Response Code: " + ex.getResponseCode());
            System.err.println("Response Text: " + ex.getResponseText());
        }
        catch (IOException ex)
        {
            System.err.println("Unable to examine Address. " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

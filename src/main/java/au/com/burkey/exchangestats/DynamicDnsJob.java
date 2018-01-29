package au.com.burkey.exchangestats;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Calendar;

import javax.json.JsonObject;

import org.eclipse.jetty.util.security.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicDnsJob implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(DynamicDnsJob.class);

    @Override
    public void run()
    {
        log.info("Running: " + this.getClass().getSimpleName());

        final String ipifyApiUrl = "https://api.ipify.org?format=json";
        final String dtdnsUrl = "https://www.dtdns.com/api/autodns.cfm?id=%s&pw=%s&ip=%s";

        try
        {
            String hostname = PropertyUtil.getProperties().getProperty("dns.hostname");
            String password = PropertyUtil.getProperties().getProperty("dns.password");

            password = Credential.getCredential(password).toString();

            JsonObject ipifyResult = UrlUtil.getJsonUrl(ipifyApiUrl);

            InetAddress externalAddress = InetAddress.getByName(ipifyResult.getString("ip"));
            log.info("External Address: " + externalAddress.getHostAddress());

            InetAddress resolvedAddress = InetAddress.getByName(hostname);
            log.info("Resolved Address: " + resolvedAddress.getHostAddress());

            Calendar today = Calendar.getInstance();

            if (today.get(Calendar.DAY_OF_MONTH) == 1 && today.get(Calendar.HOUR_OF_DAY) == 1)
            {
                log.info("First day of month, forcing update to ensure service does not go stale.");

                String dtdnsResult = UrlUtil.getTextUrl(dtdnsUrl, hostname, password, externalAddress.getHostAddress());
                log.info(dtdnsResult);
            }
            else if (externalAddress.getHostAddress().equals(resolvedAddress.getHostAddress()))
            {
                log.info("Address currently matches, no update required.");
            }
            else
            {
                log.info("Address has changed, attempting update.");

                String dtdnsResult = UrlUtil.getTextUrl(dtdnsUrl, hostname, password, externalAddress.getHostAddress());
                log.info(dtdnsResult);
            }
        }
        catch (WebException ex)
        {
            log.error("Unable to examine Address. " + ex.getMessage());
            log.error("Response Code: " + ex.getResponseCode());
            log.error("Response Text: " + ex.getResponseText());
        }
        catch (IOException ex)
        {
            log.error("Unable to examine Address. " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

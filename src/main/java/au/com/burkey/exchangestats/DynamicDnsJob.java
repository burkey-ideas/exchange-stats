package au.com.burkey.exchangestats;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Properties;

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

        try
        {
            Properties props = PropertyUtil.get();

            String ipifyApiUrl = "https://api.ipify.org?format=json";

            String dnsUrl = props.getProperty("dns.url");
            String username = props.getProperty("dns.username");
            String password = props.getProperty("dns.password");
            String hostname = props.getProperty("dns.hostname");
            String apiKey = props.getProperty("dns.apiKey");

            String dnsAuthUrl = props.getProperty("dns.authUrl");
            String dnsRestUrl = props.getProperty("dns.restUrl");
            String jsonAuth = props.getProperty("dns.jsonAuth");

            password = password == null ? null : Credential.getCredential(password).toString();

            JsonObject ipifyResult = UrlUtil.getJsonUrl(ipifyApiUrl);

            InetAddress externalAddress = InetAddress.getByName(ipifyResult.getString("ip"));
            log.info("External Address: " + externalAddress.getHostAddress());

            InetAddress resolvedAddress = InetAddress.getByName(hostname);
            log.info("Resolved Address: " + resolvedAddress.getHostAddress());

            Calendar today = Calendar.getInstance();

            if (today.get(Calendar.DAY_OF_MONTH) == 1 && today.get(Calendar.HOUR_OF_DAY) == 1)
            {
                log.info("First day of month, forcing update to ensure service does not go stale.");

                String dnsResult = UrlUtil.getTextUrl(dnsUrl, username, password, hostname, apiKey, externalAddress.getHostAddress(), null, true, false);
                log.info(dnsResult);
            }
            else if (externalAddress.getHostAddress().equals(resolvedAddress.getHostAddress()))
            {
                log.info("Address currently matches, no update required.");
            }
            else
            {
                log.info("Address has changed, attempting update.");

                if (jsonAuth == null)
                {
                    String dnsResult = UrlUtil.getTextUrl(dnsUrl, username, password, hostname, apiKey, externalAddress.getHostAddress(), null, true, false);
                    log.info(dnsResult);
                }
                else
                {
                    String tokenKey = props.getProperty(jsonAuth);

                    JsonObject authResult = UrlUtil.getJsonUrl(
                        PropertyUtil.getSubProperties(props, "dns.header.auth."),
                        PropertyUtil.getSubProperties(props, "dns.post.auth."),
                        dnsAuthUrl, username, password);

                    String token = authResult.getString(tokenKey);

                    JsonObject restResult = UrlUtil.getJsonUrl(
                        PropertyUtil.getSubProperties(props, "dns.header.rest."),
                        PropertyUtil.getSubProperties(props, "dns.post.rest."),
                        dnsRestUrl, token);

                    log.info(restResult.toString());
                }
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
            log.error("Unable to examine Address. " + ex.getMessage(), ex);
        }
    }
}

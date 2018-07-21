package au.com.burkey.exchangestats;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class BackgroundJobManager implements ServletContextListener
{
    private ScheduledExecutorService scheduler;

    private static final Logger log = LoggerFactory.getLogger(BackgroundJobManager.class);

    @Override
    public void contextInitialized(final ServletContextEvent event)
    {
        log.info("Starting scheduler.");

        scheduler = Executors.newSingleThreadScheduledExecutor();

        if (Boolean.valueOf(PropertyUtil.get().getProperty("currency.enabled")))
        {
            scheduler.scheduleAtFixedRate(new CurrencyApiJob(), 0, 1, TimeUnit.HOURS);
        }

        if (Boolean.valueOf(PropertyUtil.get().getProperty("dns.enabled")))
        {
            scheduler.scheduleAtFixedRate(new DynamicDnsJob(), 0, 15, TimeUnit.MINUTES);
        }

        if (Boolean.valueOf(PropertyUtil.get().getProperty("letsencrypt.enabled")))
        {
            scheduler.scheduleAtFixedRate(new LetsEncryptJob(), 0, 1, TimeUnit.HOURS);
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event)
    {
        scheduler.shutdownNow();
    }
}

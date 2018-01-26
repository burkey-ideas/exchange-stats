package au.com.burkey.exchangestats;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class BackgroundJobManager implements ServletContextListener
{
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(final ServletContextEvent event)
    {
        System.out.println("Starting scheduler.");

        scheduler = Executors.newSingleThreadScheduledExecutor();

        if (Boolean.valueOf(PropertyUtil.getProperties().getProperty("dns.enabled")))
        {
            scheduler.scheduleAtFixedRate(new CurrencyApiJob(), 0, 1, TimeUnit.HOURS);
        }

        if (Boolean.valueOf(PropertyUtil.getProperties().getProperty("currency.enabled")))
        {
            scheduler.scheduleAtFixedRate(new DynamicDnsJob(), 0, 15, TimeUnit.MINUTES);
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event)
    {
        scheduler.shutdownNow();
    }
}

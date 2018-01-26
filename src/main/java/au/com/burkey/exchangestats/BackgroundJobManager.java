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
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(new CurrencyApiJob(), 0, 1, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(new DynamicDnsJob(), 0, 15, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event)
    {
        scheduler.shutdownNow();
    }
}

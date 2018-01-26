package au.com.burkey.exchangestats;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

public class EmbeddedWebServer
{
    public static void main(final String[] args) throws Exception
    {
        //
        // http://www.eclipse.org/jetty/documentation/9.4.x/embedded-examples.html
        //

        int port = Integer.valueOf(PropertyUtil.getProperties().getProperty("server.port"));

        Server server = new Server(port);

        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase(EmbeddedWebServer.class.getResource("/webapp").toString());

        // Add annotation scanning (for WebAppContexts)
        context.setConfigurations(new Configuration[] {
                new AnnotationConfiguration(), new WebXmlConfiguration(),
                new WebInfConfiguration(), new PlusConfiguration(),
                new MetaInfConfiguration(), new FragmentConfiguration(),
                new EnvConfiguration()
        });

        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/exchange-stats-.*\\.jar$|.*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/.*taglibs.*\\.jar$|.*/classes/.*");

        context.setParentLoaderPriority(true);

        server.setHandler(context);

        server.start();
        server.join();
    }
}

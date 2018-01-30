package au.com.burkey.exchangestats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(urlPatterns = {"/currency/*"}, initParams = {
        @WebInitParam(name = "dirAllowed", value = "true"),
        @WebInitParam(name = "pathInfoOnly", value = "true")})
public class CurrencyBrowseServlet extends DefaultServlet
{
    private static final Logger log = LoggerFactory.getLogger(CurrencyBrowseServlet.class);

    @Override
    public String getInitParameter(final String name)
    {
        try
        {
            if ("resourceBase".equals(name))
            {
                String currencyPath = PropertyUtil.getProperties().getProperty("currency.path");

                Path resourcePath = new File(currencyPath).toPath().toRealPath();

                return resourcePath.toUri().toASCIIString();
            }
        }
        catch (IOException ex)
        {
            log.error(ex.getMessage());
        }

        return super.getInitParameter(name);
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        String pathInfo = request.getPathInfo();

        if (pathInfo != null)
        {
            while (pathInfo.startsWith("/"))
            {
                pathInfo = pathInfo.substring(1);
            }

            if (pathInfo.startsWith("currency") && pathInfo.endsWith(".csv"))
            {
                response.setContentType("text/csv");
                response.addHeader("Content-Type", "application/octet-stream");
                response.addHeader("Content-Disposition", "attachment; filename=" + pathInfo);
            }
        }

        super.doGet(request, response);
    }
}

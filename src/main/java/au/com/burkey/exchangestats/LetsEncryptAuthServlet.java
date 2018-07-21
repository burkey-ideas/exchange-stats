package au.com.burkey.exchangestats;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(urlPatterns = {"/.well-known/acme-challenge/*"})
public class LetsEncryptAuthServlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger(LetsEncryptAuthServlet.class);

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        String pathInfo = request.getPathInfo();

        if (pathInfo != null)
        {
            String token = pathInfo;
            while (token.startsWith("/"))
            {
                token = token.substring(1);
            }

            String authorization = ChallengeUtil.get().getProperty(token);

            if (authorization != null)
            {
                log.info("Sending authorization for challenge token " + token);

                response.setContentType("text/plain");

                response.getWriter().print(authorization);
            }
            else
            {
                log.warn("Unable to locate authorization for challenge token " + token);

                super.doGet(request, response);
            }
        }
        else
        {
            log.warn("Unable to send authorization for challenge token " + pathInfo);

            super.doGet(request, response);
        }
    }
}

package au.com.burkey.exchangestats;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/currency"})
public class CurrencyDownload extends HttpServlet
{
    @Override
    public void init(final ServletConfig config) throws ServletException
    {
        super.init(config);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        String currencyFile = PropertyUtil.getProperties().getProperty("currency.file");

        try (InputStream inputStream = new FileInputStream(currencyFile))
        {
            response.setContentType("text/csv");
            response.addHeader("Content-Type", "application/octet-stream");
            response.addHeader("Content-Disposition", "attachment; filename=" + currencyFile);

            ServletOutputStream outputStream = response.getOutputStream();

            byte[] buf = new byte[8192];
            int len;
            while((len = inputStream.read(buf)) > 0)
            {
                outputStream.write(buf, 0, len);
            }

            outputStream.flush();
        }
    }
}
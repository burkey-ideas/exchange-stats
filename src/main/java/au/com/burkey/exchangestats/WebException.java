package au.com.burkey.exchangestats;

import java.io.IOException;

public class WebException extends IOException
{
    private int responseCode;

    private String responseText;

    public WebException(final int responseCode, final String responseText, final String message, final Throwable cause)
    {
        super(message, cause);

        this.responseCode = responseCode;
        this.responseText = responseText;
    }

    public int getResponseCode()
    {
        return responseCode;
    }

    public String getResponseText()
    {
        return responseText;
    }
}

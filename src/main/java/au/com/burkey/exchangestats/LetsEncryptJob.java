package au.com.burkey.exchangestats;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LetsEncryptJob implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(LetsEncryptJob.class);

    @Override
    public void run()
    {
        log.info("Running: " + this.getClass().getSimpleName());

        try
        {
            Properties props = PropertyUtil.get();

            // Use "acme://letsencrypt.org" for production server
            // Use "acme://letsencrypt.org/staging" for staging server

            //String sessionUrl = props.getProperty("letsencrypt.url", "https://acme-staging-v02.api.letsencrypt.org/directory");
            String sessionUrl = props.getProperty("letsencrypt.url", "acme://letsencrypt.org/staging");
            //String sessionUrl = props.getProperty("letsencrypt.url", "acme://letsencrypt.org");

            String userKey = props.getProperty("letsencrypt.userKey", "user.key");
            String domainKey = props.getProperty("letsencrypt.domainKey", "domain.key");
            String domainCsr = props.getProperty("letsencrypt.domainCsr", "domain.csr");
            String domainCrt = props.getProperty("letsencrypt.domainCrt", "domain.crt");
            String domainList = props.getProperty("letsencrypt.domainList");

            Collection<String> domains = Arrays.asList(domainList.split("[, ]"));

            //javax.swing.JOptionPane.showConfirmDialog(null, "Question?", "Hello", javax.swing.JOptionPane.YES_NO_OPTION);

            LetsEncryptUtil.ChallengeType challengeType = LetsEncryptUtil.ChallengeType.DNS;

            LetsEncryptUtil.fetchCertificate(sessionUrl, userKey, domainKey, domainCsr, domainCrt, challengeType, domains);
        }
        catch (AcmeException ex)
        {
            log.error("Unable to generate Certificate. " + ex.getMessage(), ex);
        }
        catch (IOException ex)
        {
            log.error("Unable to generate Certificate. " + ex.getMessage(), ex);
        }
    }

}

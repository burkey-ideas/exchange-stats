package au.com.burkey.exchangestats;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.security.KeyPair;
import java.security.Security;
import java.util.Collection;
import java.util.Properties;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LetsEncryptUtil
{
    private static final Logger log = LoggerFactory.getLogger(LetsEncryptUtil.class);

    /** RSA key size of generated key pairs. */
    private static final int KEY_SIZE = 2048;

    /** Challenge types. */
    public enum ChallengeType { HTTP, DNS }

    static
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Generates a certificate for the given domains. Also takes care for the registration
     * process.
     *
     * @param sessionUrl The Let's Encrypt Url.
     * @param userKey File name of the User Key Pair.
     * @param domainKey File name of the Domain Key Pair.
     * @param domainCsr File name of the CSR.
     * @param domainCrt File name of the signed certificate.
     * @param domains Domains to get a common certificate for.
     * @throws IOException Thrown if a problem occurs.
     * @throws AcmeException Thrown if a problem occurs.
     */
    public static void fetchCertificate(final String sessionUrl,
        final String userKey, final String domainKey,
        final String domainCsr, final String domainCrt,
        final ChallengeType challengeType,
        final Collection<String> domains) throws IOException, AcmeException
    {
        // Load the user key file. If there is no key file, create a new one.
        KeyPair userKeyPair = loadOrCreateKeyPair(userKey);

        // Create a session for Let's Encrypt.
        // Use "acme://letsencrypt.org" for production server
        // Use "acme://letsencrypt.org/staging" for staging server
        Session session = new Session(sessionUrl);

        // Get the Account.
        // If there is no account yet, create a new one.
        Account acct = findOrRegisterAccount(session, userKeyPair);

        // Load or create a key pair for the domains. This should not be the userKeyPair!
        KeyPair domainKeyPair = loadOrCreateKeyPair(domainKey);

        // Order the certificate
        Order order = acct.newOrder().domains(domains).create();

        // Perform all required authorizations
        for (Authorization auth : order.getAuthorizations())
        {
            authorize(challengeType, auth);
        }

        // Generate a CSR for all of the domains, and sign it with the domain key pair.
        CSRBuilder csrb = new CSRBuilder();
        csrb.addDomains(domains);
        csrb.sign(domainKeyPair);

        // Write the CSR to a file, for later use.
        try (Writer out = new FileWriter(new File(domainCsr)))
        {
            csrb.write(out);
        }

        // Order the certificate
        order.execute(csrb.getEncoded());

        // Wait for the order to complete
        try
        {
            int attempts = 10;
            while (order.getStatus() != Status.VALID && attempts-- > 0)
            {
                // Did the order fail?
                if (order.getStatus() == Status.INVALID)
                {
                    throw new AcmeException("Order failed, Status " + Status.INVALID + ", Error: " + order.getError());
                }

                // Wait for a few seconds
                Thread.sleep(3000L);

                // Then update the status
                order.update();
            }
        }
        catch (InterruptedException ex)
        {
            log.error("interrupted", ex);
            Thread.currentThread().interrupt();
        }

        // Get the certificate
        Certificate certificate = order.getCertificate();

        log.info("Success! The certificate for domains " + domains + " has been generated!");
        log.info("Certificate URL: " + certificate.getLocation());

        // Write a combined file containing the certificate and chain.
        try (FileWriter fw = new FileWriter(new File(domainCrt)))
        {
            certificate.writeCertificate(fw);
        }

        // That's all! Configure your web server to use the DOMAIN_KEY_FILE and
        // DOMAIN_CHAIN_FILE for the requested domains.
    }

    /**
     * Loads a key pair from {@value #key}. If the file does not exist,
     * a new key pair is generated and saved.
     * <p>
     * Keep this key pair in a safe place! In a production environment, you will not be
     * able to access your account again if you should lose the key pair.
     *
     * @return The {@link KeyPair}.
     */
    private static KeyPair loadOrCreateKeyPair(final String key) throws IOException
    {
        File keyFile = new File(key);

        if (keyFile.exists())
        {
            // If there is a key file, read it
            try (FileReader fr = new FileReader(keyFile))
            {
                return KeyPairUtils.readKeyPair(fr);
            }
        }
        else
        {
            // If there is none, create a new key pair and save it
            KeyPair keyPair = KeyPairUtils.createKeyPair(KEY_SIZE);

            try (FileWriter fw = new FileWriter(keyFile))
            {
                KeyPairUtils.writeKeyPair(keyPair, fw);
            }

            return keyPair;
        }
    }

    /**
     * Finds your {@link Account} at the ACME server. It will be found by your user's
     * public key. If your key is not known to the server yet, a new account will be
     * created.
     * <p>
     * This is a simple way of finding your {@link Account}. A better way is to get the
     * URL and KeyIdentifier of your new account with {@link Account#getLocation()}
     * {@link Session#getKeyIdentifier()} and store it somewhere. If you need to get
     * access to your account later, reconnect to it via
     * {@link Account#bind(Session, URI)} by using the stored location.
     *
     * @param session {@link Session} to bind with.
     * @return {@link Login} that is connected to your account.
     */
    private static Account findOrRegisterAccount(final Session session, final KeyPair accountKey) throws AcmeException
    {
        // Ask the user to accept the TOS, if server provides us with a link.
        URI tos = session.getMetadata().getTermsOfService();

        if (tos != null)
        {
            log.info("Do you accept the Terms of Service?");
            log.info(tos.toString());

//            acceptAgreement(tos);
        }

        Account account = new AccountBuilder()
                        .agreeToTermsOfService()
                        .useKeyPair(accountKey)
                        .create(session);

        log.info("Registered a new user, URL: " + account.getLocation());


        // TODO https://shredzone.org/maven/acme4j/usage/account.html
        String keyId = "TODO";

        //Properties challenges = ChallengeUtil.get();
        //challenges.setProperty(keyId, account.getLocation().toString());
        //ChallengeUtil.persist(challenges);

        return account;
    }

    /**
     * Authorize a domain. It will be associated with your account, so you will be able to
     * retrieve a signed certificate for the domain later.
     *
     * @param challengeType {@link ChallengeType} to handle.
     * @param auth {@link Authorization} to perform.
     */
    private static void authorize(final ChallengeType challengeType, final Authorization auth) throws AcmeException
    {
        log.info("Authorization for domain " + auth.getDomain());

        // The authorization is already valid. No need to process a challenge.
        if (auth.getStatus() == Status.VALID)
        {
            return;
        }

        // Find the desired challenge and prepare it.
        Challenge challenge = null;
        switch (challengeType)
        {
            case HTTP:
                challenge = httpChallenge(auth);
                break;

            case DNS:
                challenge = dnsChallenge(auth);
                break;
        }

        if (challenge == null)
        {
            throw new AcmeException("No challenge found.");
        }

        // If the challenge is already verified, there's no need to execute it again.
        if (challenge.getStatus() == Status.VALID)
        {
            return;
        }

        // Now trigger the challenge.
        challenge.trigger();

        // Poll for the challenge to complete.
        try
        {
            int attempts = 10;
            while (challenge.getStatus() != Status.VALID && attempts-- > 0)
            {
                // Did the authorization fail?
                if (challenge.getStatus() == Status.INVALID)
                {
                    throw new AcmeException("Challenge failed, Status " + Status.INVALID + ", Error: " + challenge.getError());
                }

                // Wait for a few seconds
                Thread.sleep(3000L);

                // Then update the status
                challenge.update();
            }
        }
        catch (InterruptedException ex)
        {
            log.error("interrupted", ex);
            Thread.currentThread().interrupt();
        }

        // All reattempts are used up and there is still no valid authorization?
        if (challenge.getStatus() != Status.VALID)
        {
            throw new AcmeException("Challenge failed, Domain " + auth.getDomain() + ", Status " + challenge.getStatus() + ", Error: " + challenge.getError());
        }

        if (ChallengeType.DNS.equals(challengeType))
        {
            // Clear the DNS.
            updateDns(auth, null);
        }
    }

    /**
     * Prepares a HTTP challenge.
     * <p>
     * The verification of this challenge expects a file with a certain content to be
     * reachable at a given path under the domain to be tested.
     * <p>
     * This example outputs instructions that need to be executed manually. In a
     * production environment, you would rather generate this file automatically, or maybe
     * use a servlet that returns {@link Http01Challenge#getAuthorization()}.
     *
     * @param auth {@link Authorization} to find the challenge in.
     * @return {@link Challenge} to verify.
     */
    public static Challenge httpChallenge(final Authorization auth) throws AcmeException
    {
        // Find a single http-01 challenge
        Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);

        if (challenge == null)
        {
            throw new AcmeException("Unable to find challenge of type " + Http01Challenge.TYPE + ".");
        }

        Properties challenges = ChallengeUtil.get();
        challenges.setProperty(challenge.getToken(), challenge.getAuthorization());
        ChallengeUtil.persist(challenges);

        // Output the challenge, wait for acknowledge...
        log.info("Please create a file in your web server's base directory.");
        log.info("It must be reachable at: http://" + auth.getDomain() + "/.well-known/acme-challenge/" + challenge.getToken());
        log.info("File name: " + challenge.getToken());
        log.info("Content: " + challenge.getAuthorization());
        log.info("The file must not contain any leading or trailing whitespaces or line breaks!");
        log.info("If you're ready, dismiss the dialog...");

//        StringBuilder message = new StringBuilder();
//        message.append("Please create a file in your web server's base directory.\n\n");
//        message.append("http://").append(auth.getDomain()).append("/.well-known/acme-challenge/").append(challenge.getToken()).append("\n\n");
//        message.append("Content:\n\n");
//        message.append(challenge.getAuthorization());
//        acceptChallenge(message.toString());

        return challenge;
    }

    /**
     * Prepares a DNS challenge.
     * <p>
     * The verification of this challenge expects a TXT record with a certain content.
     * <p>
     * This example outputs instructions that need to be executed manually. In a
     * production environment, you would rather configure your DNS automatically.
     *
     * @param auth {@link Authorization} to find the challenge in.
     * @return {@link Challenge} to verify.
     */
    public static Challenge dnsChallenge(final Authorization auth) throws AcmeException
    {
        // Find a single dns-01 challenge
        Dns01Challenge challenge = auth.findChallenge(Dns01Challenge.TYPE);

        if (challenge == null)
        {
            throw new AcmeException("Unable to find challenge of type " + Dns01Challenge.TYPE + ".");
        }

        Properties challenges = ChallengeUtil.get();
        challenges.setProperty("_acme-challenge." + auth.getDomain(), challenge.getDigest());
        ChallengeUtil.persist(challenges);

        updateDns(auth, challenge);

        // Output the challenge, wait for acknowledge...
        log.info("Please create a TXT record:");
        log.info("_acme-challenge." + auth.getDomain() + ". IN TXT " + challenge.getDigest());
        log.info("If you're ready, dismiss the dialog...");

//        StringBuilder message = new StringBuilder();
//        message.append("Please create a TXT record:\n\n");
//        message.append("_acme-challenge." + auth.getDomain() + ". IN TXT " + challenge.getDigest());
//        acceptChallenge(message.toString());

        return challenge;
    }

    public static void updateDns(final Authorization auth, final Dns01Challenge challenge) throws AcmeException
    {
        Properties props = PropertyUtil.get();
        String dnsUrl = props.getProperty("letsencrypt.dnsUrl");
        String apiKey = props.getProperty("letsencrypt.dnsApiKey");

        boolean clear = challenge == null;

        try
        {
            String dnsResult = UrlUtil.getTextUrl(dnsUrl, null, null, auth.getDomain(), apiKey, null, clear ? null : challenge.getDigest(), true, clear);
            log.info(dnsResult);
        }
        catch (IOException ex)
        {
            throw new AcmeException("Unable to update dns record " + auth.getDomain() + ".", ex);
        }
    }

//    /**
//     * Presents the instructions for preparing the challenge validation, and waits for
//     * dismissal. If the user cancelled the dialog, an exception is thrown.
//     *
//     * @param message Instructions to be shown in the dialog.
//     */
//    public static void acceptChallenge(final String message) throws AcmeException
//    {
//        int option = JOptionPane.showConfirmDialog(null,
//                        message,
//                        "Prepare Challenge",
//                        JOptionPane.OK_CANCEL_OPTION);
//
//        if (option == JOptionPane.CANCEL_OPTION)
//        {
//            throw new AcmeException("User cancelled the challenge.");
//        }
//    }

//    /**
//     * Presents the user a link to the Terms of Service, and asks for confirmation. If the
//     * user denies confirmation, an exception is thrown.
//     *
//     * @param agreement {@link URI} of the Terms of Service.
//     */
//    public static void acceptAgreement(final URI agreement) throws AcmeException
//    {
//        int option = JOptionPane.showConfirmDialog(null,
//                        "Do you accept the Terms of Service?\n\n" + agreement,
//                        "Accept ToS",
//                        JOptionPane.YES_NO_OPTION);
//
//        if (option == JOptionPane.NO_OPTION)
//        {
//            throw new AcmeException("User did not accept Terms of Service.");
//        }
//    }
}

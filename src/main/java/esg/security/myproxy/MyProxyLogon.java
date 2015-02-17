/**
 * Copyright 2015 University of Chicago
 * All Rights Reserved.
 */

package esg.security.myproxy;

import java.io.Console;
import java.net.URL;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;


public class MyProxyLogon {
    private static CommandLine line;
    private static String host;
    private static int port = 7512;
    private static String username;
    private static String password;

    public static void main(String[] args) {


        Options options = new Options();
        options.addOption("i", "oid", true, "OpenID endpoint from where myproxy "
                + "information (hostname, port, username) can be gathered.");
        options.addOption("l", "username", true, "Specifies the MyProxy "
                + "account under which the credential to retrieve is stored.");
        options.addOption("P", "password", true, "MyProxy password");
        options.addOption("s", "pshost", true, "Specifies the hostname(s) "
                + "of the myproxy-server(s). This option is required if "
                + "the MYPROXY_SERVER environment variable is not defined. "
                + "If specified, this option overrides the MYPROXY_SERVER "
                + "environment variable.");
        options.addOption("p", "psport", true, "Specifies the TCP port "
                + "number of the myproxy-server (Default: 7512).");
        options.addOption("o", "output", true, "File for storing the retrieved "
                + "myproxy certificate. If defined, this setting overrides the "
                + "X509_USER_PROXY environment variable.");
        options.addOption(null, "ca-directory", true, "Directory with "
                + "certificates and signing policies of trusted CAs used for "
                + "connection validation. If defined, this setting overrides "
                + "the ESG_CERT_DIR and X509_CERT_DIR environment variables.");
        options.addOption("h", "help", false, "Print this help.");

        CommandLineParser parser = new GnuParser();
        try {
            line = parser.parse(options, args);
        } catch(ParseException e) {
            System.err.println("Parsing failed. Reason: " + e.getMessage());
        }

        HelpFormatter formatter = new HelpFormatter();

        if (line.hasOption("help")) {
            formatter.printHelp("java -jar esgf-getcert.jar <options>", options);
            System.exit(0);
        }

        // Set X509_CERT_DIR property based on ESG_CERT_DIR environment variable
        String x509CertDir = null;
        if (line.hasOption("ca-directory"))
            x509CertDir = line.getOptionValue("ca-directory");
        else
            x509CertDir = System.getenv("ESG_CERT_DIR");
        if (x509CertDir != null)
                System.setProperty("X509_CERT_DIR", x509CertDir);

        // Extract hostname and port from MYPROXY_SERVER if set
        String myproxyServer = System.getenv("MYPROXY_SERVER");
        if (myproxyServer != null) {
            String[] stringArray = myproxyServer.split(":");
            host = stringArray[0];
            if (stringArray.length > 1)
                port = Integer.parseInt(stringArray[1]);
        }

        // Extract hostname, port, and username from OpenID if set
        if (line.hasOption("oid")) {
            try {
                URL url = new URL(line.getOptionValue("oid"));
                host = url.getHost();
                port = url.getPort();
                if (port == -1)
                    port = 7512;
                String path = url.getPath();
                username = FilenameUtils.getName(path);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }

        // Extract hostname, port, and username if set
        if (line.hasOption("pshost"))
            host = line.getOptionValue("pshost");

        if (line.hasOption("psport"))
            port = Integer.parseInt(line.getOptionValue("psport"));

        if (line.hasOption("username"))
            username = line.getOptionValue("username");

        // Exit if hostname, port or username is not set
        if (host == null || username == null) {
            formatter.printHelp("java -jar esgf-getcert.jar <options>", options);
            System.exit(1);
        }

        if (line.hasOption("password"))
            password = line.getOptionValue("password");
        else {
            Console console = System.console();
            char passwordArray[] = console.readPassword("Enter password: ");
            password = new String(passwordArray);
        }

        // Get an X.509 user certificate
        edu.uiuc.ncsa.myproxy.MyProxyLogon m = new edu.uiuc.ncsa.myproxy.MyProxyLogon();
        m.setHost(host);
        m.setPort(port);
        m.setUsername(username);
        m.setPassphrase(password);
        try {
            m.getCredentials();
            if (line.hasOption("output"))
                m.saveCredentialsToFile(line.getOptionValue("output"));
            else
                m.writeProxyFile();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}

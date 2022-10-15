package navicore.demo;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.concurrent.Executors;

@Command(name = "Server", version = "Server 1.0", mixinStandardHelpOptions = true)
public class Server implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Server.class);

    @CommandLine.Option(names = {"-p", "--port"},
            description = "Server port")
    int port = 8443;
    @CommandLine.Option(names = {"-h", "--hostname"},
            description = "Hostname of listener interface")
    String hostname = "0.0.0.0";
    @CommandLine.Option(names = {"-k", "--keystore"},
            description = "Full path to the keystore (jks file)")
    String keystorePath = null;
    @CommandLine.Option(names = {"-P", "--Password"},
            description = "Keystore password")
    String keystorePassword = null;
    @CommandLine.Option(names = {"-w", "--webhook"},
            description = "URL to GET when processing webhook")
    String webhookUrl = null;
    @CommandLine.Option(names = {"-a", "--require-client-cert"},
            description = "Require connecting clients present client certificates")
    boolean requireClientAuth = false;

    @CommandLine.Option(names = {"-c", "--present-client-cert"},
            description = "Present client certificate when fireing webhook")
    boolean presentClientAuth = false;
    private KeyStore createKeyStore() throws Exception {
        //KeyStore keyStore = KeyStore.getInstance("JKS");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        FileInputStream fis = new FileInputStream(keystorePath);
        keyStore.load(fis, keystorePassword.toCharArray());
        return keyStore;
    }

    private SSLContext getSslContext() throws Exception {
        KeyStore keyStore = createKeyStore();
        SSLContext sslContext = SSLContext.getInstance("TLS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, keystorePassword.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(keyStore);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    @Override
    public void run() {

        log.info("Keystore: {}", keystorePath);
        log.info("Webhook: {}", webhookUrl);
        log.info("Present Client Cert: {} Require Client Cert: {}", presentClientAuth, requireClientAuth);

        HttpsServer server;
        try {

            server = HttpsServer.create(
                    new InetSocketAddress(hostname, port), 0);

            SSLContext sslContext = getSslContext();

            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        SSLContext context = getSSLContext();
                        SSLEngine engine = context.createSSLEngine();
                        params.setNeedClientAuth(requireClientAuth);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());
                        SSLParameters sslParameters = context.getSupportedSSLParameters();
                        params.setSSLParameters(sslParameters);
                    } catch (Exception ex) {
                        log.error("Failed to create HTTPS port", ex);
                    }
                }
            });

            server.createContext("/webhook", new MyWebhookHandler());
            server.createContext("/test", new MyServiceHandler(presentClientAuth, webhookUrl, sslContext));
            server.setExecutor(Executors.newCachedThreadPool());
            log.info("Starting server {} on port {}", hostname, port);
            server.start();
            log.debug("server started");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Server()).execute(args);
        if (exitCode > 0) {
            System.exit(exitCode);
        }
    }
}

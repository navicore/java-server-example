package navicore.demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

@Command(name = "Server", version = "Server 1.0", mixinStandardHelpOptions = true)
public class Server implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Server.class);

    @CommandLine.Option(names = {"-p", "--port"},
            description = "Server port.")
    int port = 8443;

    @CommandLine.Option(names = {"-h", "--hostname"},
            description = "Hostname of listener interface.")
    String hostname = "0.0.0.0";

    @CommandLine.Option(names = {"-k", "--keystore"},
            description = "Full path to the keystore (jks file).")
    String keystorePath = null;

    @CommandLine.Option(names = {"-w", "--webhook"},
            description = "URL to GET when processing webhook.")
    String webhookUrl = null;

    @Override
    public void run() {
        if (keystorePath != null) {
            log.info("Keystore: {}", keystorePath);
        }
        if (webhookUrl != null) {
            log.info("Webhook: {}", webhookUrl);
        }
        HttpServer server;
        try {
            log.info("Starting server {} on port {}", hostname, port);
            server = HttpServer.create(
                    new InetSocketAddress(hostname, port), 0);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        server.createContext("/webhook", new MyWebhookHandler());
        server.createContext("/test", new MyServiceHandler(webhookUrl));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    private static class MyWebhookHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            OutputStream outputStream = httpExchange.getResponseBody();
            String msg = "The buck stops here.";
            httpExchange.sendResponseHeaders(200, msg.length());
            outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            log.info(msg);
        }
    }

    private static class MyServiceHandler implements HttpHandler {

        final String webhookUrl;

        MyServiceHandler(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(webhookUrl)).GET().build();
                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                OutputStream outputStream = httpExchange.getResponseBody();
                String msg = response.body();
                httpExchange.sendResponseHeaders(200, msg.length());
                outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();
                log.info(msg);

            } catch (URISyntaxException | InterruptedException e) {
                log.error(e.getMessage(), e);
                throw new IOException(e);
            }

        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Server()).execute(args);
        if (exitCode > 0) {
            System.exit(exitCode);
        }
    }
}

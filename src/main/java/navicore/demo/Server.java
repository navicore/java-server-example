package navicore.demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Command(name = "Server", version = "Server 1.0", mixinStandardHelpOptions = true)
public class Server implements Runnable {

    @CommandLine.Option(names = {"-k", "--keystore"},
               description = "Full path to the keystore (jks file).")
    String keystorePath = null;

    @CommandLine.Option(names = {"-w", "--webhook"},
               description = "URL to GET when processing webhook.")
    String webhookUrl = null;

    @Override
    public void run() {
        if (keystorePath != null) {
            System.out.println("Keystore: " + keystorePath);
        }
        if (webhookUrl != null) {
            System.out.println("Webhook: " + webhookUrl);
        }
        HttpServer server;
        try {
            server = HttpServer.create(
                new InetSocketAddress("localhost", 8001), 0);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        server.createContext("/test", new MyServiceHandler());
        server.createContext("/webhook", new MyWebhookHandler());
        server.start();
    }

    private static class MyWebhookHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            OutputStream outputStream = httpExchange.getResponseBody();
            String msg = "The buck stops here.\n";
            httpExchange.sendResponseHeaders(200, msg.length());
            outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            System.out.print(msg);
        }
    }

    private static class MyServiceHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            OutputStream outputStream = httpExchange.getResponseBody();
            String msg = "Taking action via webhook ...\n";
            httpExchange.sendResponseHeaders(200, msg.length());
            outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            System.out.print( msg);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Server()).execute(args);
        if (exitCode > 0) {
            System.exit(exitCode);
        }
    }
}

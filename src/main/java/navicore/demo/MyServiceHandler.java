package navicore.demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

class MyServiceHandler implements HttpHandler {

    private final Logger log = LoggerFactory.getLogger(MyWebhookHandler.class);

    private final SSLContext sslContext;
    final String webhookUrl;
    private final boolean presentClientAuth;

    MyServiceHandler(boolean presentClientAuth, String webhookUrl, SSLContext sslContext) {
        this.webhookUrl = webhookUrl;
        this.presentClientAuth = presentClientAuth;
        this.sslContext = sslContext;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        try {
            SSLParameters sslParam = new SSLParameters();
            sslParam.setNeedClientAuth(presentClientAuth);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(webhookUrl)).GET().build();

            HttpClient client = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .sslParameters(sslParam).build();

            log.debug("sending request: {}", request);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("resultCode: {}", response.statusCode());

            OutputStream outputStream = httpExchange.getResponseBody();
            String msg = response.body();
            httpExchange.sendResponseHeaders(200, msg.length());
            outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            log.info(msg);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IOException(e);
        }

    }
}

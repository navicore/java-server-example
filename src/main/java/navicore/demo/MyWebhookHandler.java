package navicore.demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class MyWebhookHandler implements HttpHandler {

    static final Logger log = LoggerFactory.getLogger(MyWebhookHandler.class);

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.debug("process incoming webhook");
        OutputStream outputStream = httpExchange.getResponseBody();
        String msg = "The buck stops here.";
        httpExchange.sendResponseHeaders(200, msg.length());
        outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
        log.info(msg);
    }
}

package io.advantageous.qbit.http.impl;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.concurrent.ExecutorContext;
import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.http.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.advantageous.qbit.concurrent.ScheduledExecutorBuilder.scheduledExecutorBuilder;
import static org.boon.Boon.puts;

/**
 * Created by rhightower on 2/12/15.
 */
public class SimpleHttpServer implements HttpServer {


    private final Logger logger = LoggerFactory.getLogger(SimpleHttpServer.class);
    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();

    private Consumer<WebSocketMessage> webSocketMessageConsumer = webSocketMessage -> {};
    private Consumer<WebSocketMessage> webSocketCloseMessageConsumer = webSocketMessage -> {};
    private Consumer<HttpRequest> httpRequestConsumer = request -> {};
    private Consumer<Void> requestIdleConsumer = aVoid -> {};
    private Consumer<Void> webSocketIdleConsumer = aVoid -> {};
    private Predicate<HttpRequest> shouldContinueHttpRequest = request -> true;
    private ExecutorContext executorContext;


    /**
     * Main entry point.
     * @param request request to handle
     */
    public void handleRequest(final HttpRequest request) {

        if (debug) {
            puts("HttpServer::handleRequest", request);
            logger.debug("HttpServer::handleRequest" + request);
        }
        if (shouldContinueHttpRequest.test(request)) {
            httpRequestConsumer.accept(request);
        }
    }

    @Override
    public void setShouldContinueHttpRequest(Predicate<HttpRequest> predicate) {
        this.shouldContinueHttpRequest = predicate;
    }

    @Override
    public void setWebSocketMessageConsumer(final Consumer<WebSocketMessage> webSocketMessageConsumer) {
        this.webSocketMessageConsumer = webSocketMessageConsumer;
    }

    @Override
    public void setWebSocketCloseConsumer(Consumer<WebSocketMessage> webSocketCloseMessageConsumer) {
        this.webSocketCloseMessageConsumer = webSocketCloseMessageConsumer;
    }

    @Override
    public void setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer) {
        this.httpRequestConsumer = httpRequestConsumer;
    }

    @Override
    public void setHttpRequestsIdleConsumer(Consumer<Void> idleConsumer) {
        this.requestIdleConsumer = idleConsumer;
    }

    @Override
    public void setWebSocketIdleConsume(Consumer<Void> idleConsumer) {
        this.webSocketIdleConsumer = idleConsumer;
    }


    @Override
    public void start() {
        if (executorContext!=null) {
            throw new IllegalStateException("Can't call start twice");
        }

        if (debug) {
            puts("HttpServer Started");
            logger.debug("HttpServer Started");
        }
        executorContext = scheduledExecutorBuilder()
                .setThreadName("HttpServer").setInitialDelay(50)
                .setPeriod(50)
                .setDescription("HttpServer Periodic Flush")
                .setRunnable(() -> {
                    requestIdleConsumer.accept(null);
                    webSocketIdleConsumer.accept(null);
                })
                .build();

        executorContext.start();
    }

    @Override
    public void stop() {

        if (debug) {
            puts("HttpServer Stopped");
            logger.debug("HttpServer Stopped");
        }

        if (executorContext!=null) {
            executorContext.stop();
        }
    }
}
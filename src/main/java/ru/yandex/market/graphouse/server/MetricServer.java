package ru.yandex.market.graphouse.server;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import ru.yandex.market.graphouse.Metric;
import ru.yandex.market.graphouse.cacher.MetricCacher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Andreev <a href="mailto:AndreevDm@yandex-team.ru"/>
 * @date 02/04/15
 */
public class MetricServer implements InitializingBean {

    private static final Logger log = LogManager.getLogger();

    private MetricCacher metricCacher;
    private MetricFactory metricFactory;

    private int port = 2024;
    private int socketTimeoutMillis = 50_000;
    private int threadCount = 20;

    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Starting metric server on port: " + port);
        serverSocket = new ServerSocket(port);

        log.info("Starting " + threadCount + " metric server threads");
        executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(new MetricServerWorker());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("Shutting down metric server");
                executorService.shutdownNow();
                IOUtils.closeQuietly(serverSocket);
                log.info("Metric server stopped");
            }
        }));
        log.info("Metric server started");
    }

    private class MetricServerWorker implements Runnable {

        @Override
        public void run() {
            while (!Thread.interrupted() && !serverSocket.isClosed()) {
                try {
                    read();
                } catch (Exception e) {
                    log.warn("Failed to read data", e);
                }
            }
            log.info("MetricServerWorker stopped");
        }

        private void read() throws IOException {
            Socket socket = serverSocket.accept();
            try {
                socket.setSoTimeout(socketTimeoutMillis);
                socket.setKeepAlive(false);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    int updated = (int) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                    Metric metric = metricFactory.createMetric(line, updated);
                    if (metric != null) {
                        metricCacher.submitMetric(metric);
                    }
                }
            } catch (SocketTimeoutException e) {
                log.warn("Socket timeout from " + socket.getRemoteSocketAddress().toString());
            } finally {
                socket.close();
            }
        }
    }

    @Required
    public void setMetricCacher(MetricCacher metricCacher) {
        this.metricCacher = metricCacher;
    }

    @Required
    public void setMetricFactory(MetricFactory metricFactory) {
        this.metricFactory = metricFactory;
    }

    public void setSocketTimeoutMillis(int socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}
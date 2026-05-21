package com.cyrenaica.cyrenaicaserver.tools.http_server;

import static com.google.common.io.ByteStreams.readFully;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.os.Build;
import android.util.Log;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class HTTPServer {
    private static final String TAG = "HTTP_SERVER";
    private static HttpServer mHttpServer = null;

    static InputStream input;

    static class RootHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("GET")) {

                sendResponse(exchange);

            }
        }
    }

    static class SourceHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            Log.d(TAG, "FFMPEG METHOD: " + exchange.getRequestMethod());
            Log.d(TAG, "FFMPEG HEADERS: " + exchange.getRequestHeaders().values());
            input           = exchange.getRequestBody();

            OutputStream os = exchange.getResponseBody();
            exchange.sendResponseHeaders(200, "OK\r\n".length());
            os.write("OK\n".getBytes());



        }
    }

    static class LiveStreamHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("GET")) {


                Log.d(TAG, "HEADERS: " + exchange.getRequestHeaders().values());

                OutputStream os = exchange.getResponseBody();
                exchange.getResponseHeaders().put("Host", Collections.singletonList(exchange.getRemoteAddress().getHostName()+"\r\n"));
                exchange.getResponseHeaders().put("Connection", Collections.singletonList("keep-alive\r\n"));
                exchange.getResponseHeaders().put("Content-Type", Collections.singletonList("video/mp4\r\n"));

                long count = -1;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    count = input.transferTo(os);
                }

                final byte[] buffer = new byte[1024*5];

                while (input.read(buffer) >= 0) {
                    exchange.sendResponseHeaders(200, buffer.length);
                    Log.d(TAG, "BUFFER: " + buffer.length);
                    InputStream input = new ByteArrayInputStream(buffer);
                    os.write((int) count);

                }

                //os.flush();
                //os.close();


            }
        }
    }
    public static void startServer() {
        try {
            mHttpServer = HttpServer.create(new InetSocketAddress(8000), 0);
            mHttpServer.setExecutor(null);
            mHttpServer.createContext("/", new RootHandler());
            mHttpServer.createContext("/source", new SourceHandler());
            mHttpServer.createContext("/live", new LiveStreamHandler());

            mHttpServer.start();

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static HttpHandler load(final String fileName) {
        return new HttpHandler() {
            @Override
            public void handle(HttpExchange t) throws IOException {
                File file = new File(fileName);
                FileInputStream fin = new FileInputStream(file);
                InputStream input     = t.getRequestBody();

                OutputStream os = t.getResponseBody();

                final byte[] buffer = new byte[0x10000];
                int count = 0;
                while ((count = input.read(buffer)) >= 0) {
                    t.sendResponseHeaders(200, count);
                    os.write(buffer, 0, count);
                }

                os.flush();
                os.close();
                fin.close();
            }
        };
    }

    private static void sendResponse(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(200, "Hello World".length());

        OutputStream os = httpExchange.getResponseBody();

        os.write("Hello World".getBytes());
        os.close();
    }

}

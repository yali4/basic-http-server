package http;

import http.Parser.RequestParser;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HttpRequestHandler extends Thread {

    private Socket clientSocket;

    private String defaultPage = "index.html";

    private String errorPage = "error.html";

    public HttpRequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public String readFile(String fileName) throws IOException {

        StringBuilder builder = new StringBuilder();
        FileReader reader = new FileReader(fileName);

        int c;
        while ((c = reader.read()) != -1) {
            builder.append((char)c);
        }

        return builder.toString();
    }

    public void run() {

        try {

            OutputStream response = clientSocket.getOutputStream();

            List<String> responseHeader = new ArrayList<String>();

            byte[] responseBodyBytes;

            try {

                RequestParser parser = new RequestParser(this.clientSocket);

                parser.parseRequest();

                List<HttpHeader> headers = parser.getHeaders();

                System.out.println("=====================");
                System.out.println("Request was received:");
                System.out.println("=====================");
                for (HttpHeader header : headers) {
                    System.out.println(String.format("%s: %s", header.getKey(), header.getValue()));
                }

                String requestFile = parser.getRequestFile();
                if (requestFile.isEmpty()) {
                    requestFile = this.defaultPage;
                }

                URL indexFile = getClass().getResource("/" + requestFile);
                Path indexFilePath = new File(indexFile.getFile()).toPath();

                String responseBody = readFile(indexFile.getFile());

                responseBodyBytes = responseBody.getBytes();

                responseHeader.add("HTTP/1.0 200 OK");
                responseHeader.add("Server: Java/SocketServer");
                responseHeader.add(String.format("Content-Type: %s", Files.probeContentType(indexFilePath)));
                responseHeader.add(String.format("Content-Length: %d", responseBodyBytes.length));

            } catch (Exception e) {

                URL errorFile = getClass().getResource("/" + this.errorPage);
                Path errorFilePath = new File(errorFile.getFile()).toPath();

                String responseBody = readFile(errorFile.getFile());

                responseBodyBytes = responseBody.getBytes();

                responseHeader.add("HTTP/1.0 400 Bad Request");
                responseHeader.add("Server: Java/SocketServer");
                responseHeader.add(String.format("Content-Type: %s", Files.probeContentType(errorFilePath)));
                responseHeader.add(String.format("Content-Length: %d", responseBodyBytes.length));

            }

            for (String headerLine : responseHeader) {
                response.write(headerLine.concat("\r\n").getBytes());
            }
            response.write("\r\n".getBytes());
            response.write(responseBodyBytes);

            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

package http;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HttpRequestHandler extends Thread {

    private Socket clientSocket;

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

            InputStream request = clientSocket.getInputStream();

            StringBuilder requestHeader = new StringBuilder();

            int charInt;
            while ((charInt = request.read()) != -1) {
                requestHeader.append((char) charInt);
                if (!(request.available() > 0)) {
                    break;
                }
            }

            System.out.println("Request was received:");
            System.out.print(requestHeader.toString());

            OutputStream response = clientSocket.getOutputStream();

            URL indexFile = getClass().getResource("/index.html");
            Path indexFilePath = new File(indexFile.getFile()).toPath();

            String responseBody = readFile(indexFile.getFile());

            byte[] responseBodyBytes = responseBody.getBytes();

            List<String> responseHeader = new ArrayList<String>();
            responseHeader.add("HTTP/1.0 200 OK");
            responseHeader.add("Server: Java/SocketServer");
            responseHeader.add(String.format("Content-Type: %s", Files.probeContentType(indexFilePath)));
            responseHeader.add(String.format("Content-Length: %d", responseBodyBytes.length));

            for (String headerLine : responseHeader) {
                response.write(headerLine.concat("\r\n").getBytes());
            }
            response.write("\r\n".getBytes());
            response.write(responseBodyBytes);

            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

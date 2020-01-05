package http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Application {

    public static void main(String[] args) {

        try {

            ServerSocket server = new ServerSocket(8080);

            while ( true ) {

                Socket request = server.accept();
                new HttpRequestHandler(request).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
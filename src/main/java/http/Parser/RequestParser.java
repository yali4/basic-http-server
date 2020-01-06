package http.Parser;

import http.HttpHeader;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestParser {

    Socket clientSocket;

    List<HttpHeader> headers = new ArrayList<HttpHeader>();

    String requestMethod = "";

    String protocolVersion = "";

    String requestFile = "";

    String queryString = "";

    public RequestParser(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void parseRequest() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        List<String> headerList = new ArrayList<String>();
        String headerLine;
        while ( (headerLine = reader.readLine()) != null ) {
            if (headerLine.isEmpty()) {
                break;
            }
            headerList.add(headerLine);
        }

        boolean first = false;
        for (String currentHeader : headerList) {
            if (!first) {
                Pattern pattern = Pattern.compile("^(GET|HEAD|POST|PUT|DELETE|OPTIONS|PATCH) /(.*) (HTTP/1.1)$");
                Matcher matcher = pattern.matcher(currentHeader);
                if (matcher.find()) {
                    requestMethod = matcher.group(1).trim();
                    protocolVersion = matcher.group(3).trim();
                    String[] splitFile = matcher.group(2).trim().split("\\?", 2);
                    if (splitFile.length == 2) {
                        requestFile = splitFile[0].trim();
                        queryString = splitFile[1].trim();
                    } else {
                        requestFile = matcher.group(2).trim();
                    }
                    first = true;
                    continue;
                } else {
                    throw new Exception("Bad Request.");
                }
            }
            String[] splitHeader = currentHeader.split(":", 2);
            if (splitHeader.length == 2) {
                headers.add(new HttpHeader(splitHeader[0].trim(), splitHeader[1].trim()));
            }
        }
    }

    public List<HttpHeader> getHeaders() {
        return headers;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getRequestFile() {
        return requestFile;
    }

    public String getQueryString() {
        return queryString;
    }
}

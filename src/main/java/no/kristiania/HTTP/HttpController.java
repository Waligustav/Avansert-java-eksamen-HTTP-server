package no.kristiania.HTTP;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public interface HttpController {
    HttpMessage handle(HttpMessage request, Socket clientSocket) throws IOException, SQLException;
}

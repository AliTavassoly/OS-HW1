package os.hw1.server;

import java.io.IOException;
import java.net.ServerSocket;

public class Cache {
    private int mainPort;

    private ServerSocket server;

    public Cache(int port){
        this.mainPort = port;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

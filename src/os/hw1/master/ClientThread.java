package os.hw1.master;

import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread{
    private Socket socket;

    public ClientThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {
        InputStream input = null;
        BufferedReader brinp = null;
        DataOutputStream output = null;

        try {
            input = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(input));
            output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }

        String line;
        while (true) {
            try {
                line = brinp.readLine();
                if (line == null || line.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    return;
                } else {
                    output.writeBytes(line + "\n\r");
                    output.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}

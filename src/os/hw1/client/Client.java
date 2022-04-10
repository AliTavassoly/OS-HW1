package os.hw1.client;

import os.hw1.master.MasterMain;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class Client {
    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;

    public Client(String address, int port) {
        makeConnection(address, port);

        readFromConsole();

        closeConnection();
    }

    private void makeConnection(String address, int port) {
        try {
            socket = new Socket(address, port);
            System.out.println("Client Connected");

            input = new DataInputStream(System.in);

            out = new DataOutputStream(socket.getOutputStream());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void readFromConsole(){
        String line = "";

        while (!line.equals("Over")) {
            try {
                line = input.readLine();
                out.writeUTF(line);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection(){
        try {
            input.close();
            out.close();
            socket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
         // System.out.println(InetAddress.getLocalHost());
         Client client = new Client("127.0.0.1", MasterMain.portNumber);
    }
}

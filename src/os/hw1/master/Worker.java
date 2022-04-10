package os.hw1.master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Worker {
    private int id;

    private Socket socket;
    private Scanner scanner;
    private PrintStream printStream;

    private List<Executable> requests;

    public Worker(int port, Socket socket) {
        this.socket = socket;

        try {
            printStream = new PrintStream(socket.getOutputStream());
            scanner = new Scanner(socket.getInputStream());
        } catch (IOException e){
            e.printStackTrace();
        }

        requests = new LinkedList<>();
    }

//    public String request() throws IOException{
//        return inputStream.readUTF();
//    }
//
//    public void response(String message) throws IOException {
//        outStream.writeUTF(message);
//    }
//
//    public void start(){
//
//    }
//
//    public String getStatus(){
//        return status;
//    }

    public int getId(){
        return id;
    }
}

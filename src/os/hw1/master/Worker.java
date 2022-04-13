package os.hw1.master;

import os.hw1.util.ErrorLogger;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

public class Worker {
    static Scanner scanner;
    static PrintStream printStream;

    private static void newRequest(String request){
        String[] parts = request.split(" ");
        String [] commonArgs = new String[3];

        commonArgs[0] = parts[3];
        commonArgs[1] = parts[4];
        commonArgs[2] = parts[5];

        runProgram(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), parts[2], commonArgs);
    }

    private static void runProgram(int programId, int input, String className, String[] commonArgs){
        try {
            Process process = new ProcessBuilder(
                    commonArgs[0], commonArgs[1], commonArgs[2], className
            ).start();

            PrintStream printStream = new PrintStream(process.getOutputStream());
            Scanner scanner = new Scanner(process.getInputStream());

            printStream.println(input);
            printStream.flush();

            ErrorLogger.getInstance().log("in worker: sent request to program" + new Date().getTime());

            Thread listeningThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    listenToProgram(programId, input, scanner);
                }
            });
            listeningThread.start();

            ErrorLogger.getInstance().log("in worker: received request from program" + new Date().getTime());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void listenToProgram(int programId, int input, Scanner scanner){
        while (true) {
            String programOutput = scanner.nextLine();
            String response = programId + " " + input + " " + programOutput;
            printStream.println(response);
            printStream.flush();
            return;
        }
    }

    public static void main(String[] args) {
        Socket socket;
        try {
            socket = new Socket(InetAddress.getLocalHost(), MasterMain.workersPort);

            scanner = new Scanner(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());

            while(true){
                String request = scanner.nextLine();

                newRequest(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
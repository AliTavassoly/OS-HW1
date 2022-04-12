package os.hw1.master;

import os.hw1.util.ErrorLogger;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Worker {
    private static String newRequest(String request){
        String[] parts = request.split(" ");
        String [] commonArgs = new String[3];

        commonArgs[0] = parts[3];
        commonArgs[1] = parts[4];
        commonArgs[2] = parts[5];

        return runProgram(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), parts[2], commonArgs);
    }

    private static String runProgram(int programId, int input, String className, String[] commonArgs){
        try {
            Process process = new ProcessBuilder(
                    commonArgs[0], commonArgs[1], commonArgs[2], className
            ).start();

            PrintStream printStream = new PrintStream(process.getOutputStream());
            Scanner scanner = new Scanner(process.getInputStream());

            printStream.println(input);
            printStream.flush();

            int programOutput = scanner.nextInt();

            String response = programId + " " + input + " " + programOutput;

            return response;
        } catch (IOException e){
            e.printStackTrace();
        }

        return "InvalidResponseFromWorker!";
    }

    public static void main(String[] args) {
        Socket socket;
        try {
            socket = new Socket(InetAddress.getLocalHost(), MasterMain.workersPort);

            Scanner scanner = new Scanner(socket.getInputStream());
            PrintStream printStream = new PrintStream(socket.getOutputStream());

            while(true){
                String request = scanner.nextLine();

                ErrorLogger.getInstance().log("Error logger in worker: new request: " + request);

                String response = newRequest(request);

                printStream.println(response);
                printStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

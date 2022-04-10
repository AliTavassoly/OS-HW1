package os.hw1.master;

import os.hw1.util.Logger;
import os.hw1.util.Logger2;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Worker {

    private static String newRequest(String request){
        String[] parts = request.split(" ");
        return runProgram(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), parts[2]);
    }

    private static String runProgram(int programId, int input, String className){
//        Logger.getInstance().log("runProgram in worker: " + programId + " " + input + " " + className);

        String[] commonArgs = {
                "C:\\Users\\Alico\\.jdks\\corretto-11.0.14.1\\bin\\java.exe",
                "-classpath",
                "out/production/OS-HW1/"
        };

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
//            Logger2.getInstance().log("Port in worker side: " + MasterMain.workersPort);

            Scanner scanner = new Scanner(socket.getInputStream());
            PrintStream printStream = new PrintStream(socket.getOutputStream());

            while(true){
                String request = scanner.nextLine();

                printStream = new PrintStream(socket.getOutputStream());
                printStream.println(newRequest(request));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

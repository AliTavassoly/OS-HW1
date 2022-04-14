package os.hw1.master;

import os.hw1.util.ErrorLogger;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Worker {
    static Scanner scanner;
    static PrintStream printStream;

    static Socket socket;

    static List<Process> processList;

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
            processList.add(process);
            ErrorLogger.getInstance().log("New program creating...: " + ProcessHandle.current().pid());

            PrintStream printStream = new PrintStream(process.getOutputStream());
            Scanner scanner = new Scanner(process.getInputStream());

            printStream.println(input);
            printStream.flush();

            Thread listeningThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    listenToProgram(programId, input, scanner);
                }
            });
            listeningThread.start();
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
        processList = new LinkedList<>();

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            ErrorLogger.getInstance().log("worker destroyed...");
            for(Process process: processList){
                process.destroy();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        try {
            socket = new Socket(InetAddress.getLocalHost(), MasterMain.workersPort);

            scanner = new Scanner(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());

            while(true){
                String request = scanner.nextLine();

                newRequest(request);

                ErrorLogger.getInstance().log("Request in worker: process id: " + ProcessHandle.current().pid() + " size of process: " + processList.size());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
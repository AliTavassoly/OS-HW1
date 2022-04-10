package os.hw1.master;

import os.hw1.util.Logger;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class WorkerHandler {
    int currentW;

    private Scanner scanner;
    private PrintStream printStream;
    private Process process;

    public WorkerHandler(){

    }

    public int getCurrentW(){
        return currentW;
    }

    public void start(){
        String[] commonArgs = {
                "C:\\Users\\Alico\\.jdks\\corretto-11.0.14.1\\bin\\java.exe",
                "-classpath",
                "out/production/OS-HW1/"
        };

        try {
            process = new ProcessBuilder(
                    commonArgs[0], commonArgs[1], commonArgs[2], "os.hw1.master.Worker"
            ).start();

            printStream = new PrintStream(process.getOutputStream());
            scanner = new Scanner(process.getInputStream());

            listenToWorker();
        } catch (IOException e) {
            Logger.getInstance().log("Unable to create process for a worker!");
            e.printStackTrace();
        }
    }

    private void listenToWorker(){
        while (true){
            String message = scanner.nextLine();
            String logMessage = "A message from worker!!!" + message;
            Logger.getInstance().log(logMessage);
        }
    }

    public void requestFromServer(Executable executable){
        String request = String.valueOf(executable.getProgramId());
        request += " ";
        request += String.valueOf(executable.getInput());

        printStream.println(executable);
    }

    private void response(){

    }
}

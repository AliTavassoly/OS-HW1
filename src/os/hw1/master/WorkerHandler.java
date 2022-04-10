package os.hw1.master;

import os.hw1.server.Server;
import os.hw1.util.Logger;
import os.hw1.util.Logger2;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class WorkerHandler {
    int currentW;

    private Scanner scanner;
    private PrintStream printStream;
    private Process process;

    private Server server;

    public WorkerHandler(Server server){
        this.server = server;
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
            Logger2.getInstance().log("Waiting for worker answer...");
            String message = scanner.nextLine();
            Logger2.getInstance().log("Got a worker answer... " + message);

            responseFromWorker(message);
        }
    }

    public void requestFromServer(Executable executable){
        Logger.getInstance().log("Task assigned to handler " + executable.getProgramId() + " " + executable.getInput());

        String request = String.valueOf(executable.getProgramId());
        request += " ";
        request += String.valueOf(executable.getInput());
        request += " ";
        request += MasterMain.getClassNameOfProgram(executable.getProgramId());

        Logger.getInstance().log(request);

        printStream.println(request);
        printStream.flush();

        currentW += MasterMain.getWeightOfProgram(executable.getProgramId());
    }

    private void responseFromWorker(String response){
        String[] parts = response.split(" ");

        Executable executable = new Executable(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

        Logger2.getInstance().log("Exe: " + executable.getProgramId() + " " + executable.getInput() + " " + executable.getAnswer());

        currentW -= MasterMain.getWeightOfProgram(executable.getProgramId());

        server.response(executable);
    }
}

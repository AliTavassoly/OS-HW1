package os.hw1.master;

import os.hw1.server.Server;
import os.hw1.util.Logger;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class WorkerHandler {
    int currentW, workerId, currentPort;
    long processId;

    private Scanner scanner;
    private PrintStream printStream;
    private Process process;

    private Server server;

    private static ServerSocket serverSocket;

    public WorkerHandler(int id, Server server){
        this.workerId = id;
        this.server = server;

        try {
            if(serverSocket == null)
                serverSocket = new ServerSocket(MasterMain.workersPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            processId = process.pid();

            Socket workerSocket = serverSocket.accept();

            currentPort = workerSocket.getPort();

            Logger.getInstance().log("worker " + workerId + " start " + processId + " " + currentPort);

            printStream = new PrintStream(workerSocket.getOutputStream());
            scanner = new Scanner(workerSocket.getInputStream());

            listenToWorker();
        } catch (IOException e) {
            Logger.getInstance().log("worker " + workerId + " stop " + processId + " " + currentPort);
            e.printStackTrace();
        }
    }

    private void listenToWorker(){
        Thread listenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    String message = scanner.nextLine();

                    responseFromWorker(message);
                }
            }
        });
        listenerThread.start();
    }

    public void requestFromServer(Executable executable){
        String request = String.valueOf(executable.getProgramId());
        request += " ";
        request += String.valueOf(executable.getInput());
        request += " ";
        request += MasterMain.getClassNameOfProgram(executable.getProgramId());

        printStream.println(request);
        printStream.flush();

        currentW += MasterMain.getWeightOfProgram(executable.getProgramId());
    }

    private void responseFromWorker(String response){
        String[] parts = response.split(" ");

        Executable executable = new Executable(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

        currentW -= MasterMain.getWeightOfProgram(executable.getProgramId());

        server.responseFromWorker(executable);
    }

    public long getProcessId(){
        return processId;
    }
}

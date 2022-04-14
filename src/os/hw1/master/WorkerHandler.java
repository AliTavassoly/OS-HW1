package os.hw1.master;

import os.hw1.server.Server;
import os.hw1.util.ErrorLogger;
import os.hw1.util.Logger;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;

public class WorkerHandler {
    int currentW, workerId, currentPort;
    long processId;

    private Scanner scanner;
    private PrintStream printStream;
    private Process process;

    private Server server;

    private Socket workerSocket;

    private List<Executable> executing;

    private static ServerSocket serverSocket;

    public WorkerHandler(int id, Server server){
        this.workerId = id;
        this.server = server;

        executing = new LinkedList<>();

        try {
            if(serverSocket == null)
                serverSocket = new ServerSocket(MasterMain.workersPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getWorkerId(){
        return workerId;
    }

    public int getCurrentW(){
        return currentW;
    }

    public void start(){
        ErrorLogger.getInstance().log("worker start starting again " + workerId + " " + executing.size());

        String[] commonArgs = MasterMain.getCommonArgs();
        executing.clear();
        currentW = 0;

        try {
            process = new ProcessBuilder(
                    commonArgs[0], commonArgs[1], commonArgs[2], "os.hw1.master.Worker"
            ).start();

            processId = process.pid();

            workerSocket = serverSocket.accept();

            currentPort = workerSocket.getPort();

            Logger.getInstance().log("worker " + workerId + " start " + processId + " " + currentPort);

            printStream = new PrintStream(workerSocket.getOutputStream());
            scanner = new Scanner(workerSocket.getInputStream());

            listenToWorker(workerSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenToWorker(Socket socket){
        Thread listenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String message = scanner.nextLine();

                        responseFromWorker(message);
                    }
                } catch (NoSuchElementException e){
                    Logger.getInstance().log("worker " + workerId + " stop " + processId + " " + currentPort);
                    WorkerHandler.this.stop();
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
        request += " ";
        request += MasterMain.getCommonArgs()[0];
        request += " ";
        request += MasterMain.getCommonArgs()[1];
        request += " ";
        request += MasterMain.getCommonArgs()[2];

        try {
            ErrorLogger.getInstance().log("request from server in worker with id: " + request.substring(0, 10)); // TODO: print request has bug
        } catch (Exception e){
            ErrorLogger.getInstance().log(e.getMessage());
        }

        executing.add(executable);

        printStream.println(request);
        printStream.flush();

        currentW += MasterMain.getWeightOfProgram(executable.getProgramId());
    }

    private void responseFromWorker(String response){
        String[] parts = response.split(" ");

        Executable executable = new Executable(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

        removeFromList(executable);

        server.responseFromWorker(this, executable, workerId);
    }

    private void removeFromList(Executable toRemove){
        for(Executable executable: executing){
            if(Executable.areEqual(executable, toRemove)){
                executing.remove(executable);
                return;
            }
        }
    }

    public void updateWeight(int programId){
        currentW -= MasterMain.getWeightOfProgram(programId);
    }

    public boolean isInProcessing(Executable executable){
        for(Executable processing: executing){
            if(Executable.areEqual(executable, processing))
                return true;
        }
        return false;
    }

    private void stop(){
        try {
            workerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.workerStopped(workerId);
    }

    public long getProcessId(){
        return processId;
    }

    public int numberOfProcess(){
        return executing.size();
    }

    public void shutDownHook() {
        process.destroy();
        try {
            if(workerSocket != null) {
                workerSocket.close();
                workerSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

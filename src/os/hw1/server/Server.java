package os.hw1.server;

import os.hw1.master.Executable;
import os.hw1.master.ExecuteChain;
import os.hw1.master.Program;
import os.hw1.master.Worker;
import os.hw1.util.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private int portNumber, numberOfWorkers, w, numberOfArgs, numberOfPrograms;
    private List<String> commonArgs;
    private List<Program> programs;

    private ServerSocket server;

    private List<Worker> workers;

    private List<ExecuteChain> requests;
    private List<Executable> processing;

    public Server(int portNumber, int numberOfWorkers, int w, int numberOfArgs, int numberOfPrograms,
                  List<String> commonArgs, List<Program> programs) {
        this.portNumber = portNumber;
        this.numberOfWorkers = numberOfWorkers;
        this.w = w;
        this.numberOfArgs = numberOfArgs;
        this.numberOfPrograms = numberOfPrograms;
        this.commonArgs = commonArgs;
        this.programs = programs;

        workers = new LinkedList<>();
        requests = new LinkedList<>();
        processing = new LinkedList<>();
    }

    private void newQuery(Socket socket, String query){
        requests.add(new ExecuteChain(getInputOfQuery(query), getQueueOfQuery(query), socket));
    }

    private Queue<Integer> getQueueOfQuery(String query){
        String[] parts = query.split(" ");
        String chain = parts[0];

        String[] ids = chain.split("\\|");
        Queue<Integer> queue = new LinkedList<>();

        for(int i = ids.length - 1; i >= 0; i--)
            queue.add(Integer.parseInt(ids[i]));
        return queue;
    }

    private int getInputOfQuery(String query){
        String[] parts = query.split(" ");
        String input = parts[1];

        return Integer.parseInt(input);
    }

    public void start(int port){
        try {
            server = new ServerSocket(port);

            Logger.getInstance().log("Server started");

            System.out.println("Server Started (this message is for tester)");

            listenForNewConnections();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForNewConnections() throws IOException {
        while (true) {
            Logger.getInstance().log("Waiting for a client ...");

            Socket clientSocket = null;

            try {
                clientSocket = server.accept();
                Logger.getInstance().log("Client accepted");

//                Worker worker = new Worker(portNumber, clientSocket);

                listenToClient(clientSocket);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void listenToClient(Socket socket) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Scanner inputStream = new Scanner(socket.getInputStream());

                    Logger.getInstance().log("Start listening to client...:");

                    String line = inputStream.nextLine();

                    newQuery(socket, line);
                } catch (IOException e){
                    try {
                        socket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                    Logger.getInstance().log("Client lost!");
                }
            }
        });
        thread.start();
    }

    public void stop(){
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private int createProcess(int w){
//        Process process = new ProcessBuilder().start();
//
//    }
//
//    private void createWorker(int w, int pid){
//        Worker worker = new Worker(w);
//    }
//
//    public void start(){
//        for(int i = 0; i < numberOfWorkers; i++){
//            int pid = createProcess(w);
//            createWorker(w, pid);
//        }
//    }
}

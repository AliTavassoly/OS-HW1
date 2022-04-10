package os.hw1.server;

import os.hw1.master.*;
import os.hw1.util.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private int mainPort, numberOfWorkers, maxW, numberOfArgs, numberOfPrograms;
    private List<String> commonArgs;
    private List<Program> programs;

    private ServerSocket server;

    private List<WorkerHandler> workers; // TODO: ???

    private List<ExecuteChain> requests;
    private List<ExecuteChain> processing;

    public Server(int mainPort, int numberOfWorkers, int maxW, int numberOfArgs, int numberOfPrograms,
                  List<String> commonArgs, List<Program> programs) {
        this.mainPort = mainPort;
        this.numberOfWorkers = numberOfWorkers;
        this.maxW = maxW;
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

            System.out.println("Server Started (this message is for tester)");

            Logger.getInstance().log("Server started");

            Logger.getInstance().log("Size of programs: " + programs.size());

            createInitialWorkers();
            startInitialWorkers();

            listenForNewConnections();

            startHandlingRequests();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForNewConnections() throws IOException {
        Thread listeningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Logger.getInstance().log("Waiting for a client ...");

                    Socket clientSocket = null;

                    try {
                        clientSocket = server.accept();
                        Logger.getInstance().log("Client accepted");

                        Scanner inputStream = new Scanner(clientSocket.getInputStream());

                        Logger.getInstance().log("Start listening to client...:");

                        String line = inputStream.nextLine();

                        newQuery(clientSocket, line);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        listeningThread.start();
    }

    private void createInitialWorkers(){
        for(int i = 0; i < numberOfWorkers; i++){
            workers.add(new WorkerHandler(this));
        }
    }

    private void startInitialWorkers(){
        for(WorkerHandler workerHandler: workers){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    workerHandler.start();
                }
            });
            thread.start();
        }
    }

    private void startHandlingRequests(){
        while(true) {
            handleRequest();

            try {
                Thread.sleep(50); // TODO: is it correct?!
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRequest(){
        if(requests.size() > 0){
            ExecuteChain chain = requests.get(0);
            int programId = chain.getCurrentExecutable().getProgramId();

            if(canAssign(MasterMain.getWeightOfProgram(programId))){
                requests.remove(0);
                processing.add(chain);
                process(chain);
            }
        }
    }

    private boolean canAssign(int w){
        for(WorkerHandler workerHandler: workers){
            if(workerHandler.getCurrentW() + w <= maxW){
                return true;
            }
        }
        return false;
    }

    private void process(ExecuteChain chain){
        WorkerHandler chosenWorker = null;
        for(WorkerHandler workerHandler: workers){
            if(chosenWorker == null || workerHandler.getCurrentW() < chosenWorker.getCurrentW()){
                chosenWorker = workerHandler;
            }
        }
        chosenWorker.requestFromServer(chain.getCurrentExecutable());
    }

    public void response(Executable response){
        for(int i = 0; i < processing.size(); i++){
            ExecuteChain chain = processing.get(i);
            if(chain.getCurrentExecutable().getProgramId() == response.getProgramId() &&
                chain.getCurrentExecutable().getInput() == response.getInput()){
                chain.programAnswered(response.getAnswer());
                processing.remove(i);

                if(chain.isAlive()){
                    requests.add(chain);
                } else {
                    chain.sendResponseToClient(response.getAnswer());
                }

                return;
            }
        }
    }

    public void stop(){
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

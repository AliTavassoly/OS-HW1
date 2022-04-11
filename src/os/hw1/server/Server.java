package os.hw1.server;

import os.hw1.master.*;
import os.hw1.util.ChainComparator;
import os.hw1.util.Logger2;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private int mainPort, numberOfWorkers, maxW, numberOfArgs, numberOfPrograms;
    private List<String> commonArgs;
    private List<Program> programs;

    private ServerSocket server;

    private List<WorkerHandler> workers;

    private TreeSet<ExecuteChain> requests;
    private TreeSet<ExecuteChain> processing;

    private Object chainsLock;

    private PrintStream cachePrintStream;
    private Scanner cacheScanner;

    private int priority = 0;

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
        requests = new TreeSet<ExecuteChain>(new ChainComparator());
        processing = new TreeSet<ExecuteChain>(new ChainComparator());

        chainsLock = new Object();
    }

    private void newQuery(Socket socket, String query) {
        synchronized (chainsLock) {
            requests.add(new ExecuteChain(priority++, getInputOfQuery(query), getQueueOfQuery(query), socket));
        }
    }

    private Queue<Integer> getQueueOfQuery(String query) {
        String[] parts = query.split(" ");
        String chain = parts[0];

        String[] ids = chain.split("\\|");
        Queue<Integer> queue = new LinkedList<>();

        for (int i = ids.length - 1; i >= 0; i--)
            queue.add(Integer.parseInt(ids[i]));
        return queue;
    }

    private int getInputOfQuery(String query) {
        String[] parts = query.split(" ");
        String input = parts[1];

        return Integer.parseInt(input);
    }

    public void start(int port) {

        try {
            server = new ServerSocket(port);

            System.out.println("Server Started (this message is for tester)");

            connectToCache();

            createInitialWorkers();
            startInitialWorkers();

            Thread.sleep(100);

            listenForNewClients();

            Thread.sleep(100);

            handleRequests();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void listenForNewClients() throws IOException {
        Thread listeningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
//                    Logger.getInstance().log("Waiting for a client ...");

                    Socket clientSocket = null;

                    try {
                        clientSocket = server.accept();

                        Scanner inputStream = new Scanner(clientSocket.getInputStream());

                        String line = inputStream.nextLine();

                        newQuery(clientSocket, line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        listeningThread.start();
    }

    private void createInitialWorkers() {
        for (int i = 0; i < numberOfWorkers; i++) {
//            int workerPort = mainPort + 1 + i;
            WorkerHandler workerHandler = new WorkerHandler(i, this);
            workers.add(workerHandler);
        }
    }

    private void startInitialWorkers() {
        for (WorkerHandler workerHandler : workers) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    workerHandler.start();
                }
            });
            thread.start();
        }
    }

    private void handleRequests() {
        while (true) {
            handleRequest();

            try {
                Thread.sleep(100); // TODO: is it correct?!
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRequest() {
        synchronized (chainsLock) {
            if (requests.size() > 0) {
                ExecuteChain chain = requests.first();
                int programId = chain.getCurrentExecutable().getProgramId();

                if (canAssign(MasterMain.getWeightOfProgram(programId))) {
                    requests.pollFirst();
                    boolean shouldAssign = true;

                    if (existInCache(chain.getCurrentExecutable())) {
                        int answer = getFromCache(chain.getCurrentExecutable().getProgramId(), chain.getCurrentExecutable().getInput());
                        shouldAssign = false;
                        // TODO: send response
                    } else { // check if task is already in processing list
                        for (ExecuteChain executeChain : processing) {
                            if (Executable.areEqual(executeChain.getCurrentExecutable(), chain.getCurrentExecutable())) {
                                processing.add(chain);
                                shouldAssign = false;
                                break;
                            }
                        }
                    }

                    if (shouldAssign) {
                        processing.add(chain);
                        assignToWorker(chain);
                    }
                }
            }
        }
    }

    private boolean existInCache(Executable executable) {
        int answer = getFromCache(executable.getProgramId(), executable.getInput());
        return answer != -1;
    }

    private boolean canAssign(int w) {
        for (WorkerHandler workerHandler : workers) {
            if (workerHandler.getCurrentW() + w <= maxW) {
                return true;
            }
        }
        return false;
    }

    private void assignToWorker(ExecuteChain chain) {
        WorkerHandler chosenWorker = null;
        for (WorkerHandler workerHandler : workers) {
            if (chosenWorker == null || workerHandler.getCurrentW() < chosenWorker.getCurrentW()) {
                chosenWorker = workerHandler;
            }
        }
        chosenWorker.requestFromServer(chain.getCurrentExecutable());
    }

    public void responseToProgram(Executable response) {
        List<ExecuteChain> chainsToRemove = new LinkedList<>();
        pushToCache(response.getProgramId(), response.getInput(), response.getAnswer());

        synchronized (chainsLock) {
            for (ExecuteChain chain : processing) {
                if (Executable.areEqual(chain.getCurrentExecutable(), response)) {
                    chain.programAnswered(response.getAnswer());
                    chainsToRemove.add(chain);
                }
            }
            for (ExecuteChain chain: chainsToRemove){
                processing.remove(chain);

                if (chain.isAlive()) {
                    requests.add(chain);
                } else {
                    chain.sendResponseToClient(response.getAnswer());
                }
            }
        }
    }

    private void createCacheProcess(){
        String[] commonArgs = {
                "C:\\Users\\Alico\\.jdks\\corretto-11.0.14.1\\bin\\java.exe",
                "-classpath",
                "out/production/OS-HW1/"
        };

        try {
            Process process = new ProcessBuilder(
                    commonArgs[0], commonArgs[1], commonArgs[2], "os.hw1.server.Cache"
            ).start();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void connectToCache() {
        Logger2.getInstance().log("Cache going to be called");

        createCacheProcess();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Socket clientSocket = new Socket(InetAddress.getLocalHost(), MasterMain.cachePort);
            cachePrintStream = new PrintStream(clientSocket.getOutputStream());
            cacheScanner = new Scanner(clientSocket.getInputStream());

             cacheScanner.nextLine(); // read something to ensure it connected
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pushToCache(int programId, int input, int answer) {
        String request = "GET " + programId + " " + input + " " + answer; // TODO: edit
        cachePrintStream.println(request);
        cachePrintStream.flush();
    }

    private int getFromCache(int programId, int input) {
        String request = "GET " + programId + " " + input;
        cachePrintStream.println(request);
        cachePrintStream.flush();

        return cacheScanner.nextInt();
    }

    public void stop() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


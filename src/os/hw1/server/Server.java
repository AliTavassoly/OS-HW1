package os.hw1.server;

import os.hw1.master.*;
import os.hw1.util.ChainComparator;
import os.hw1.util.Logger;
import os.hw1.util.ErrorLogger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private int mainPort, numberOfWorkers, maxW, numberOfArgs, numberOfPrograms;
    private List<String> commonArgs;
    private List<Program> programs;

    private ServerSocket server;

    private ServerSocket cacheServer;
    private Process cacheProcess;

    private List<WorkerHandler> workers;

    private TreeSet<ExecuteChain> requests;
    private TreeSet<ExecuteChain> processing;

    private Object chainsLock;

    private List<WorkerHandler> workersToWakeUp;

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

        workersToWakeUp = new LinkedList<>();

        chainsLock = new Object();
    }

    private void newQuery(Socket socket, String query) {
        synchronized (chainsLock) {
            ErrorLogger.getInstance().log("New request: " + query + " " + getWorkersWeights());

            requests.add(new ExecuteChain(priority++, getInputOfQuery(query), getQueueOfQuery(query), socket));

            chainsLock.notifyAll();
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

            connectToCache();

            createInitialWorkers();
            startInitialWorkers();

            listenForNewClients();

            handleRequests();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForNewClients() throws IOException {
        Thread listeningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
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
            WorkerHandler workerHandler = new WorkerHandler(i, this);
            workers.add(workerHandler);
        }
    }

    private void startInitialWorkers() {
        for (WorkerHandler workerHandler : workers) {
            workerHandler.start();
        }
    }

    private void handleRequests() {
        while (true) {
            handleRequest();
        }
    }

    private void handleRequest() {
        ErrorLogger.getInstance().log("start0 handling requests: " + requests + " is in cache: " + workersToWakeUp);

        synchronized (chainsLock) {
            boolean shouldAssign = true;
            boolean oneRequestHandled = false;
            ErrorLogger.getInstance().log("start1 handling requests: " + requests + " is in cache: " + workersToWakeUp);

            if (requests.size() > 0) {
                ErrorLogger.getInstance().log("start2 handling requests: " + requests + " is in cache: " + workersToWakeUp);

                ExecuteChain chain = requests.first();
                int programId = chain.getCurrentExecutable().getProgramId();

                if (existInCache(chain.getCurrentExecutable())) {
                    requests.pollFirst();
                    oneRequestHandled = true;

                    int answer = getFromCache(chain.getCurrentExecutable().getProgramId(), chain.getCurrentExecutable().getInput());
                    shouldAssign = false;
                    chain.setLastAnswer(answer);
                    chain.programAnswered(chain.getLastAnswer());

                    if (chain.isAlive()) {
                        requests.add(chain);
                    } else {
                        chain.sendResponseToClient(chain.getLastAnswer());
                    }
                } else { // check if task is already in processing list
                    for (ExecuteChain executeChain : processing) {
                        if (Executable.areEqual(executeChain.getCurrentExecutable(), chain.getCurrentExecutable())) {
                            requests.pollFirst();
                            oneRequestHandled = true;

                            processing.add(chain);
                            shouldAssign = false;
                            break;
                        }
                    }
                }

                if (shouldAssign && canAssign(MasterMain.getWeightOfProgram(programId))) {
                    requests.pollFirst();
                    oneRequestHandled = true;

                    processing.add(chain);
                    assignToWorker(chain);
                }
            }

            if (!oneRequestHandled) {
                try {
                    if(workersToWakeUp.size() > 0)
                        wakeWorkerUp();
                    chainsLock.wait();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean existInCache(Executable executable) {
        int answer = getFromCache(executable.getProgramId(), executable.getInput());
        return answer != -1;
    }

    private boolean canAssign(int w) {
        ErrorLogger.getInstance().log("Entered canAssign");

        for (WorkerHandler workerHandler : workers) {
            ErrorLogger.getInstance().log("Assigning function: " + w + " " + workersToWakeUp + " " + workersToWakeUp.contains(workerHandler) + " " + workerHandler);

            if (!workersToWakeUp.contains(workerHandler) && workerHandler.getCurrentW() + w <= maxW) {
                ErrorLogger.getInstance().log("Assigning function: " + w + " " + workersToWakeUp + " " + workersToWakeUp.contains(workerHandler) + " " + workerHandler);

                return true;
            }
        }
        return false;
    }

    private void assignToWorker(ExecuteChain chain) {
        WorkerHandler chosenWorker = null;
        for (WorkerHandler workerHandler : workers) {
            if (!workersToWakeUp.contains(workerHandler) && (chosenWorker == null || workerHandler.getCurrentW() < chosenWorker.getCurrentW())) {
                chosenWorker = workerHandler;
            }
        }

        chosenWorker.requestFromServer(chain.getCurrentExecutable());

        ErrorLogger.getInstance().log("Assigned to: " + chosenWorker.getWorkerId() + " Executable: " + chain.getCurrentExecutable().toString() + " others weight: " + getWorkersWeights());
    }

    public void responseFromWorker(WorkerHandler workerHandler, Executable response, int workerId) {
        ErrorLogger.getInstance().log("Response from worker: ProgramId: " + response.getProgramId() +
                " Input: " + response.getInput() + " answer: " + response.getAnswer() + " workerId: " + workerId);

        List<ExecuteChain> chainsToRemove = new LinkedList<>();
        pushToCache(response.getProgramId(), response.getInput(), response.getAnswer());

        synchronized (chainsLock) {
            for (ExecuteChain chain : processing) {
                if (Executable.areEqual(chain.getCurrentExecutable(), response)) {
                    chain.programAnswered(response.getAnswer());
                    chainsToRemove.add(chain);
                }
            }
            for (ExecuteChain chain : chainsToRemove) {
                processing.remove(chain);

                if (chain.isAlive()) {
                    requests.add(chain);
                } else {
                    chain.sendResponseToClient(response.getAnswer());
                }
            }

            workerHandler.updateWeight(response.getProgramId());

            chainsLock.notifyAll();
        }
    }

    private void createCacheProcess() {
        String[] commonArgs = MasterMain.getCommonArgs();

        try {
            Process process = new ProcessBuilder(
                    commonArgs[0], commonArgs[1], commonArgs[2], "os.hw1.server.Cache"
            ).start();

            cacheProcess = process;

            Logger.getInstance().log("cache start " + process.pid() + " " + MasterMain.cachePort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectToCache() {
        try {
            cacheServer = new ServerSocket(MasterMain.cachePort);

            createCacheProcess();

            Socket cacheSocket = cacheServer.accept();
            cachePrintStream = new PrintStream(cacheSocket.getOutputStream());
            cacheScanner = new Scanner(cacheSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pushToCache(int programId, int input, int answer) {
        String request = "PUSH " + programId + " " + input + " " + answer;
        cachePrintStream.println(request);
        cachePrintStream.flush();
    }

    private int getFromCache(int programId, int input) {
        String request = "GET " + programId + " " + input;
        cachePrintStream.println(request);
        cachePrintStream.flush();

        return cacheScanner.nextInt();
    }

    private String getWorkersWeights() {
        String s = "";
        for (WorkerHandler workerHandler : workers) {
            s += workerHandler.getCurrentW() + " ";
        }
        return s;
    }

    public void shutdownHook() {
        for (WorkerHandler workerHandler : workers) {
            workerHandler.shutDownHook();
        }

        try {
            Logger.getInstance().log("cache stop " + cacheProcess.pid() + " " + MasterMain.cachePort);

            server.close();
            cacheServer.close();
            cacheProcess.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void wakeWorkerUp(){
        ErrorLogger.getInstance().log("Workers to wakeup size: " + workersToWakeUp.size() + " request size: " + requests.size());
        synchronized (chainsLock) {
            workersToWakeUp.get(0).start();
            workersToWakeUp.remove(0);
            chainsLock.notifyAll();
        }
    }

    public void workerStopped(int workerId) {
        for (WorkerHandler workerHandler : workers) {
            if (workerHandler.getWorkerId() == workerId) {
                ErrorLogger.getInstance().log("Worker process size when died: " + workerHandler.numberOfProcess() + " Process size: " + processing.size() + " " + " Request size: " + requests.size());
                synchronized (chainsLock) {
                    List<ExecuteChain> removeFromProcessing = new LinkedList<>();
                    for (ExecuteChain executeChain : processing) {
                        if (workerHandler.isInProcessing(executeChain.getCurrentExecutable())) {
                            removeFromProcessing.add(executeChain);
                        }
                    }

                    for (ExecuteChain executeChain : removeFromProcessing) {
                        processing.remove(executeChain);
                        requests.add(executeChain);
                    }
                    chainsLock.notifyAll(); // TODO: ??

                    workersToWakeUp.add(workerHandler);

                    break;
                }
            }
        }
    }
}



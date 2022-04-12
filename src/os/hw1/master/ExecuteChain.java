package os.hw1.master;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Queue;

public class ExecuteChain {
    private Queue<Integer> programIds;
    private Socket returnSocket;
    private int currentInput;

    private int lastAnswer;

    private int priority;

    private PrintStream printStream;

    public ExecuteChain(int priority, int input, Queue<Integer> idQueue, Socket socket){
        this.priority = priority;

        returnSocket = socket;
        currentInput = input;
        programIds = idQueue;

        try {
            printStream = new PrintStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Executable getCurrentExecutable(){
        if(programIds.isEmpty())
            return new Executable(-1, -1);
        return new Executable(programIds.peek(), currentInput);
    }

    public int getW(){
        return MasterMain.getWeightOfProgram(programIds.peek());
    }

    public void programAnswered(int answer){
        currentInput = answer;
        programIds.remove();
    }

    public void sendResponseToClient(int answer){
        printStream.println(answer);
        printStream.flush();
    }

    public boolean isAlive(){
        return programIds.size() > 0;
    }

    public int getPriority(){
        return priority;
    }

    public int getLastAnswer(){
        return lastAnswer;
    }

    public void setLastAnswer(int answer){
        lastAnswer = answer;
    }
}

package os.hw1.master;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Queue;

public class ExecuteChain {
    private Queue<Integer> programIds;
    private Socket returnSocket;
    private int currentInput;

    private PrintStream printStream;

    public ExecuteChain(int input, Queue<Integer> idQueue, Socket socket){
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
    }

    public boolean isAlive(){
        return programIds.size() > 0;
    }
}

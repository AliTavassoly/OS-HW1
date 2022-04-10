package os.hw1.master;

import java.net.Socket;
import java.util.Queue;

public class ExecuteChain {
    private Queue<Integer> programIds;
    private Socket returnSocket;
    private int currentInput;

    public ExecuteChain(int input, Queue<Integer> idQueue, Socket socket){
        returnSocket = socket;
        currentInput = input;
        programIds = idQueue;
    }

    public Executable getCurrentExecutable(){
        if(programIds.isEmpty())
            return new Executable(-1, -1);
        return new Executable(programIds.peek(), currentInput);
    }

    public void programAnswered(int answer){
        currentInput = answer;
        programIds.remove();
    }
}

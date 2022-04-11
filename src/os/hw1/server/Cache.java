package os.hw1.server;

import os.hw1.master.MasterMain;
import os.hw1.util.Logger2;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Cache {
    private static PrintStream printStream;
    private static Scanner scanner;

    private static class CacheExecutable{
        int programId, input, answer;

        public CacheExecutable(int programId, int input, int answer) {
            this.programId = programId;
            this.input = input;
            this.answer = answer;
        }

        public int getProgramId() {
            return programId;
        }
        public void setProgramId(int programId) {
            this.programId = programId;
        }

        public int getInput() {
            return input;
        }
        public void setInput(int input) {
            this.input = input;
        }

        public int getAnswer() {
            return answer;
        }
        public void setAnswer(int answer) {
            this.answer = answer;
        }
    }

    private static ArrayList<CacheExecutable> list;

    public static int find(int programId, int input){
        for(CacheExecutable executable: list){
            if(executable.getInput() == input && executable.getProgramId() == programId){
                return executable.getAnswer();
            }
        }
        return -1;
    }

    public static void push(int programId, int input, int answer){
        list.add(new CacheExecutable(programId, input, answer));
    }

    public static void newRequest(String request){
        String[] parts = request.split(" ");
        Logger2.getInstance().log(request);

        String type = parts[0];
        int programId = Integer.parseInt(parts[1]);
        int input = Integer.parseInt(parts[2]);

        if(type.equals("GET")){
            int answer = find(programId, input);
            printStream.println(answer);
            printStream.flush();
        } else if (type.equals("PUSH")){
            int answer = Integer.parseInt(parts[3]);
            push(programId, input, answer);
        } else {
            // INVALID QUERY
        }
    }

    public static void main(String[] args) {
        Logger2.getInstance().log("Cache started");

        list = new ArrayList<>();

        try {
            ServerSocket serverSocket;

            serverSocket = new ServerSocket(MasterMain.cachePort);

            Socket clientSocket = serverSocket.accept();
            printStream = new PrintStream(clientSocket.getOutputStream());
            scanner = new Scanner(clientSocket.getInputStream());

            printStream.println("Connected to cache");
            printStream.flush();

            while (true){
                String request = scanner.nextLine();
                newRequest(request);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

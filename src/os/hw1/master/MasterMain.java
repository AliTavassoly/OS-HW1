package os.hw1.master;

import os.hw1.server.Server;
import os.hw1.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MasterMain {
    public static int portNumber;
    private static int numberOfWorkers, w, numberOfArgs, numberOfPrograms;
    private static List<String> commonArgs = new ArrayList<>();

    private static List<Program> programs = new ArrayList<>();

    private static void input(){
        Scanner scanner = new Scanner(System.in);

        portNumber = scanner.nextInt();
        numberOfWorkers = scanner.nextInt();
        w = scanner.nextInt();

        numberOfArgs = scanner.nextInt();;
        for(int i = 0; i < numberOfArgs; i++){
            String arg = scanner.next();
            commonArgs.add(arg);
        }

        numberOfPrograms = scanner.nextInt();
        for(int i = 0; i < numberOfPrograms; i++){
            String className = scanner.next();
            int weight = scanner.nextInt();

            Program program = new Program(className, weight, i + 1);
            programs.add(program);
        }
    }

    public static int getWeightOfProgram(int programId){
        for(Program program: programs){
            if(program.getId() == programId)
                return program.getW();
        }
        return -1;
    }

    public static String getClassNameOfProgram(int programId){
        for(Program program: programs){
            if(program.getId() == programId)
                return program.getClassName();
        }
        return "";
    }

    public static void main(String[] args) {
        input();
        Server server = new Server(portNumber, numberOfWorkers, w, numberOfArgs,
                numberOfPrograms, commonArgs, programs);

        server.start(portNumber);

        // TODO: shutdown
    }
}

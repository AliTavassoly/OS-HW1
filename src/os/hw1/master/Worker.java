package os.hw1.master;

import os.hw1.util.Logger;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class Worker {

    private static int newRequest(String request){
        String[] parts = request.split(" ");
        return runProgram(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    private static int runProgram(int programId, int input){
        Logger.getInstance().log("runProgram in worker: " + programId + " " + input);

        String[] commonArgs = {
                "C:\\Users\\Alico\\.jdks\\corretto-11.0.14.1\\bin\\java.exe",
                "-classpath",
                "out/production/OS-HW1/"
        };

        try {
            Process process = new ProcessBuilder(
                    commonArgs[0], commonArgs[1], commonArgs[2], "os.hw1.programs.Program" + programId
            ).start();

            PrintStream printStream = new PrintStream(process.getOutputStream());
            Scanner scanner = new Scanner(process.getInputStream());

            printStream.println(input);

            return scanner.nextInt();
        } catch (IOException e){
            e.printStackTrace();
        }

        return -1;
    }

    public static void main(String[] args) {
        System.out.println("Hello from worker 17!!!!!!!!!");
        while(true){
            Scanner scanner = new Scanner(System.in);
            System.out.println(newRequest(scanner.nextLine()));
        }
    }
}

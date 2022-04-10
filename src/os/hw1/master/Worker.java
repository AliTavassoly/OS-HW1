package os.hw1.master;

import os.hw1.util.Logger;
import os.hw1.util.Logger2;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class Worker {

    private static String newRequest(String request){
        String[] parts = request.split(" ");
        return runProgram(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), parts[2]);
    }

    private static String runProgram(int programId, int input, String className){
        Logger.getInstance().log("runProgram in worker: " + programId + " " + input + " " + className);

        String[] commonArgs = {
                "C:\\Users\\Alico\\.jdks\\corretto-11.0.14.1\\bin\\java.exe",
                "-classpath",
                "out/production/OS-HW1/"
        };

        try {
            Process process = new ProcessBuilder(
                    commonArgs[0], commonArgs[1], commonArgs[2], className
            ).start();

            PrintStream printStream = new PrintStream(process.getOutputStream());
            Scanner scanner = new Scanner(process.getInputStream());

            Logger2.getInstance().log("This input: " + input + " is going into " + className);
            Logger.getInstance().log("This input: " + input + " is going into " + className);
            printStream.println(input);
            printStream.flush();
            Logger2.getInstance().log("This input: " + input + " went into " + className);
            Logger.getInstance().log("This input: " + input + " is going into " + className);

            int programOutput = scanner.nextInt();
            Logger2.getInstance().log("This input: " + programOutput + " came from " + className);

            Logger.getInstance().log("Program answered: " + programOutput);

            String response = programId + " " + input + " " + programOutput;
            Logger2.getInstance().log(response);

            return response;
        } catch (IOException e){
            e.printStackTrace();
        }

        return "InvalidResponseFromWorker!";
    }

    public static void main(String[] args) {
        while(true){
            Scanner scanner = new Scanner(System.in);
            Logger.getInstance().log("Worker is sad:( ");
            String request = scanner.nextLine();
            Logger.getInstance().log("Worker got: " + request);
            System.out.println(newRequest(request));
        }
    }
}

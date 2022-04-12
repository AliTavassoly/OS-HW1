package os.hw1.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ErrorLogger {
    private static ErrorLogger instance;
    private PrintWriter pw;

    public static ErrorLogger getInstance(){
        if(instance == null)
            return instance = new ErrorLogger();
        return instance;
    }

    // Just for testing
    private void createFile(){
        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            fw = new FileWriter("C:\\Users\\Alico\\Desktop\\log2.txt", true);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ErrorLogger(){
        createFile();
    }

    public void log(String message){
//        pw.println(message);
//        pw.flush();
        System.err.println(message);
    }
}

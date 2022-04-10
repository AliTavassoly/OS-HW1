package os.hw1.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    private static Logger instance;
    private PrintWriter pw;

    public static Logger getInstance(){
        if(instance == null)
            return instance = new Logger();
        return instance;
    }

    // Just for testing
    private void createFile(){
        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            fw = new FileWriter("C:\\Users\\Alico\\Desktop\\logs.txt");
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Logger(){
        createFile();
    }

    public void log(String message){
        pw.println(message);
        pw.flush();
    }
}

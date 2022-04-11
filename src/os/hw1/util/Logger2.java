package os.hw1.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger2 {
    private static Logger2 instance;
    private PrintWriter pw;

    public static Logger2 getInstance(){
        if(instance == null)
            return instance = new Logger2();
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

    private Logger2(){
        createFile();
    }

//    public void log(String message){
//        pw.println(message);
//        pw.flush();
//    }
}

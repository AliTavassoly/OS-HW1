package os.hw1.util;

public class Logger {
    private static Logger instance;

    public static Logger getInstance(){
        if(instance == null)
            return instance = new Logger();
        return instance;
    }

    private Logger(){ }

    public void log(String message){
        System.out.println(message);
//        Logger2.getInstance().log(message);
    }
}

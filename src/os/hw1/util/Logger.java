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
//        if(ErrorLogger.isDebug == 1) {
//            ErrorLogger.getInstance().log(message);
//        }
    }
}

package os.hw1.master;

public class Program {
    private String className;
    private int w;

    public Program(String className, int w) {
        this.className = className;
        this.w = w;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }
}

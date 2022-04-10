package os.hw1.master;

public class Program {
    private String className;
    private int w;
    private int id;

    public Program(String className, int w, int id) {
        this.className = className;
        this.w = w;
        this.id = id;
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

    public int getId(){ return id;}
}

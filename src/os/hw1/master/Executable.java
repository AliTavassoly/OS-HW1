package os.hw1.master;

public class Executable {
    private int programId;
    private int input;

    public Executable(int programId, int input) {
        this.programId = programId;
        this.input = input;
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public void setInput(int input) {
        this.input = input;
    }

    public int getInput() {
        return input;
    }

    public boolean isValid(){
        return programId >= 0;
    }
}

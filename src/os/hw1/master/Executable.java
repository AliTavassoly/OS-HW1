package os.hw1.master;

public class Executable {
    private int programId;
    private int input;

    private int answer;

    public Executable(int programId, int input) {
        this.programId = programId;
        this.input = input;
    }

    public Executable(int programId, int input, int answer){
        this.programId = programId;
        this.input = input;
        this.answer = answer;
    }

    public int getAnswer(){
        return answer;
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

    public static boolean areEqual(Executable a, Executable b){
        return a.getInput() == b.getInput() && a.getProgramId() == b.getProgramId();
    }
}

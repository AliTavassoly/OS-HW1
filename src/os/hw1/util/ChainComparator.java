package os.hw1.util;

import os.hw1.master.ExecuteChain;

import java.util.Comparator;

public class ChainComparator implements Comparator<ExecuteChain>{
    public int compare(ExecuteChain c1, ExecuteChain c2)
    {
        return c1.getPriority() - c2.getPriority();
    }
}

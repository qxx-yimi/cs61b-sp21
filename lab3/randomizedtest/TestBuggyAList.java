package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> trueList = new AListNoResizing<>();
        BuggyAList<Integer> bugList = new BuggyAList<>();
        for(int i = 4; i <= 6; i++) {
            trueList.addLast(i);
            bugList.addLast(i);
        }
        for(int i = 0; i < 3; i++) {
            int item1 = trueList.removeLast();
            int item2 = bugList.removeLast();
            assertEquals(item1, item2);
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> bugL = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                bugL.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                int bugSize = bugL.size();
                assertEquals(size, bugSize);
            } else if (operationNumber == 2) {
                // getLast
                if(L.size() == 0) continue;
                int last = L.getLast();
                int bugLast = bugL.getLast();
                assertEquals(last, bugLast);
            } else {
                // removeLast
                if(L.size() == 0) continue;
                int last = L.removeLast();
                int bugLast = bugL.removeLast();
                assertEquals(last, bugLast);
            }
        }
    }
}

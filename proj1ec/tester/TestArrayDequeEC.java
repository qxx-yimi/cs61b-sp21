package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {

    @Test
    public void randomTest() {
        StudentArrayDeque<Integer> bugDeque = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> trueDeque = new ArrayDequeSolution<>();
        String ops = "";
        while (true) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addFirst
                int randVal = StdRandom.uniform(0, 100);
                bugDeque.addFirst(randVal);
                trueDeque.addFirst(randVal);
                ops += "addFirst(" + randVal + ")\n";
            } else if (operationNumber == 1) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                bugDeque.addLast(randVal);
                trueDeque.addLast(randVal);
                ops += "addLast(" + randVal + ")\n";
            } else if (operationNumber == 2) {
                // removeFirst
                Integer item1 = null;
                if (!bugDeque.isEmpty()) {
                    item1 = bugDeque.removeFirst();
                }
                Integer item2 = null;
                if (!trueDeque.isEmpty()) {
                    item2 = trueDeque.removeFirst();
                }
                ops += "removeFirst()\n";
                assertEquals(ops, item2, item1);
            } else {
                // removeLast
                Integer item1 = null;
                if (!bugDeque.isEmpty()) {
                    item1 = bugDeque.removeLast();
                }
                Integer item2 = null;
                if (!trueDeque.isEmpty()) {
                    item2 = trueDeque.removeLast();
                }
                ops += "removeLast()\n";
                assertEquals(ops, item2, item1);
            }
        }
    }
}

package IntList;

import static org.junit.Assert.*;
import org.junit.Test;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSimple() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }

    /**
     * 没有质数，changed应该为false
     */
    @Test
    public void testSquarePrimesComplex1() {
        IntList lst = IntList.of(14, 15, 16, 16, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 16 -> 18", lst.toString());
        assertFalse(changed);
    }

    /**
     * 有多个质数，changed应该为true
     */
    @Test
    public void testSquarePrimesComplex2() {
        IntList lst = IntList.of(2, 3, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("4 -> 9 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }
}

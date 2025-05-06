/** Class that prints the Collatz sequence starting from a given number.
 *  @author one meter
 */
public class Collatz {

    /** return next collatz number
     * if n is even return n / 2
     * if n is odd return 3 * n + 1
     */
    public static int nextNumber(int n) {
        if(n % 2 == 1) {
            return 3 * n + 1;
        }else {
            return n / 2;
        }
    }

    public static void main(String[] args) {
        int n = 5;
        System.out.print(n + " ");
        while (n != 1) {
            n = nextNumber(n);
            System.out.print(n + " ");
        }
        System.out.println();
    }
}


package deque;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;

public class MaxArrayDequeTest {

    private static class Person {
        private String name;
        private int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    private static class NameComparator implements Comparator<Person> {
        public int compare(Person o1, Person o2) {
            return o1.name.compareTo(o2.name);
        }
    }

    private static class AgeComparator implements Comparator<Person> {
        public int compare(Person o1, Person o2) {
            return o1.age - o2.age;
        }
    }

    @Test
    public void nameComparatorTest() {
        Person p1 = new Person("abc", 30);
        Person p2 = new Person("bcd", 20);
        Person p3 = new Person("cde", 10);
        MaxArrayDeque<Person> q = new MaxArrayDeque<>(new NameComparator());
        q.addLast(p1);
        q.addLast(p2);
        q.addLast(p3);
        assertEquals(p3, q.max());
    }

    @Test
    public void ageComparatorTest() {
        Person p1 = new Person("abc", 40);
        Person p2 = new Person("bcd", 30);
        Person p3 = new Person("cde", 20);
        MaxArrayDeque<Person> q = new MaxArrayDeque<>(new NameComparator());
        q.addLast(p1);
        q.addLast(p2);
        q.addLast(p3);
        assertEquals(p1, q.max(new AgeComparator()));
    }
}

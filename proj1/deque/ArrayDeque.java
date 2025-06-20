package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {

    private T[] items;
    private int nextFirst;
    private int nextLast;
    private int size;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        nextFirst = 0;
        nextLast = 1;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[nextFirst] = item;
        nextFirst = (nextFirst - 1 + items.length) % items.length;
        size++;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[nextLast] = item;
        nextLast = (nextLast + 1) % items.length;
        size++;
    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        if (size < items.length / 4 && size > 8) {
            resize(items.length / 2);
        }
        nextFirst = (nextFirst + 1) % items.length;
        T item = items[nextFirst];
        items[nextFirst] = null;
        size--;
        return item;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        if (size < items.length / 4 && size > 8) {
            resize(items.length / 2);
        }
        nextLast = (nextLast - 1 + items.length) % items.length;
        T item = items[nextLast];
        items[nextLast] = null;
        size--;
        return item;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        int current = (nextFirst + 1) % items.length;
        for (int i = 0; i < index; i++) {
            current = (current + 1) % items.length;
        }
        return items[current];
    }

    @Override
    public void printDeque() {
        int current = (nextFirst + 1) % items.length;
        for (int i = 0; i < size; i++) {
            System.out.print(items[current] + " ");
            current = (current + 1) % items.length;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {

        private int currentIndex;
        private int cnt;

        ArrayDequeIterator() {
            currentIndex = (nextFirst + 1) % items.length;
            cnt = 0;
        }

        public boolean hasNext() {
            return cnt < size;
        }

        public T next() {
            T item = items[currentIndex];
            currentIndex = (currentIndex + 1) % items.length;
            cnt++;
            return item;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Deque)) {
            return false;
        }
        Deque<T> o = (Deque<T>) other;
        if (size() != o.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!(get(i).equals(o.get(i)))) {
                return false;
            }
        }
        return true;
    }

    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        int currentIndex = (nextFirst + 1) % items.length;
        for (int i = 0; i < size; i++) {
            a[i] = items[currentIndex];
            currentIndex = (currentIndex + 1) % items.length;
        }
        nextFirst = capacity - 1;
        nextLast = size;
        items = a;
    }
}

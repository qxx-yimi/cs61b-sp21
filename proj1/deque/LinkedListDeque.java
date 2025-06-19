package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {

    public class Node {

        private Node prev;
        private T item;
        private Node next;

        public Node(Node prev, T item, Node next) {
            this.prev = prev;
            this.item = item;
            this.next = next;
        }
    }

    private Node sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        sentinel.next = new Node(sentinel, item, sentinel.next);
        sentinel.next.next.prev = sentinel.next;
        size++;
    }

    @Override
    public void addLast(T item) {
        sentinel.prev = new Node(sentinel.prev, item, sentinel);
        sentinel.prev.prev.next = sentinel.prev;
        size++;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
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
        T item = sentinel.next.item;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel;
        size--;
        return item;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        T item = sentinel.prev.item;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel;
        size--;
        return item;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        Node current = sentinel.next;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.item;
    }

    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return getRecursive(index, sentinel.next);
    }

    private T getRecursive(int index, Node current) {
        if (index == 0) {
            return current.item;
        }
        return getRecursive(index - 1, current.next);
    }

    @Override
    public void printDeque() {
        Node current = sentinel.next;
        for (int i = 0; i < size; i++) {
            System.out.print(current.item + " ");
            current = current.next;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {

        private int currentIndex;
        private Node currentNode;

        LinkedListDequeIterator() {
            currentIndex = 0;
            currentNode = sentinel.next;
        }

        public boolean hasNext() {
            return currentIndex < size;
        }

        public T next() {
            T item = currentNode.item;
            currentIndex++;
            currentNode = currentNode.next;
            return item;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LinkedListDeque)) {
            return false;
        }
        LinkedListDeque<T> o = (LinkedListDeque<T>) other;
        if (size != o.size) {
            return false;
        }
        Node thisNode = sentinel.next;
        Node otherNode = o.sentinel.next;
        for (int i = 0; i < size; i++) {
            if (!(thisNode.item.equals(otherNode.item))) {
                return false;
            }
        }
        return true;
    }
}

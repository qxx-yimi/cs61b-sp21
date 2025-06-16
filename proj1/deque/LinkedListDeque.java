package deque;

public class LinkedListDeque<T> {

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
        this.sentinel = new Node(null, null, null);
        this.sentinel.prev = sentinel;
        this.sentinel.next = sentinel;
        this.size = 0;
    }

    public void addFirst(T item) {
        this.sentinel.next = new Node(this.sentinel, item, this.sentinel.next);
        this.sentinel.next.next.prev = this.sentinel.next;
        this.size++;
    }

    public void addLast(T item) {
        this.sentinel.prev = new Node(this.sentinel.prev, item, this.sentinel);
        this.sentinel.prev.prev.next = this.sentinel.prev;
        this.size++;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public int size() {
        return this.size;
    }

    public T removeFirst() {
        if (this.isEmpty()) {
            return null;
        }
        T item = this.sentinel.next.item;
        this.sentinel.next = this.sentinel.next.next;
        this.sentinel.next.prev = this.sentinel;
        this.size--;
        return item;
    }

    public T removeLast() {
        if (this.isEmpty()) {
            return null;
        }
        T item = this.sentinel.prev.item;
        this.sentinel.prev = this.sentinel.prev.prev;
        this.sentinel.prev.next = this.sentinel;
        this.size--;
        return item;
    }

    public T get(int index) {
        if (index < 0 || index >= this.size) {
            return null;
        }
        Node current = this.sentinel.next;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.item;
    }

    public void printDeque() {
        Node current = this.sentinel.next;
        for (int i = 0; i < this.size; i++) {
            System.out.print(current.item + " ");
        }
    }
}

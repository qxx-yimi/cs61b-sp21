package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private int size;
    private BSTNode root;

    public BSTMap() {
        size = 0;
        root = null;
    }

    private class BSTNode {
        private K key;
        private V value;
        private BSTNode left;
        private BSTNode right;

        public BSTNode(K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }

    @Override
    public void clear() {
        size = 0;
        root = null;
    }

    @Override
    public boolean containsKey(K key) {
        BSTNode current = root;
        while (current != null) {
            int cmp = key.compareTo(current.key);
            if (cmp == 0) {
                return true;
            } else if (cmp < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        BSTNode current = root;
        while (current != null) {
            int cmp = key.compareTo(current.key);
            if (cmp == 0) {
                return current.value;
            } else if (cmp < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
    }

    private BSTNode put(BSTNode root, K key, V value) {
        if (root == null) {
            size++;
            return new BSTNode(key, value);
        }
        int cmp = key.compareTo(root.key);
        if (cmp == 0) {
            root.value = value;
        } else if (cmp < 0) {
            root.left = put(root.left, key, value);
        } else {
            root.right = put(root.right, key, value);
        }
        return root;
    }

    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(BSTNode root) {
        if (root != null) {
            printInOrder(root.left);
            System.out.println(root.key + " : " + root.value);
            printInOrder(root.right);
        }
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException("Not implemented");
    }
}


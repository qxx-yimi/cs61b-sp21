package bstmap;

import java.util.Collection;
import java.util.HashSet;
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
        return keySet(root);
    }

    private Set<K> keySet(BSTNode root) {
        if (root == null) {
            return new HashSet<>();
        }
        Set<K> keys = new HashSet<>();
        keys.addAll(keySet(root.left));
        keys.add(root.key);
        keys.addAll(keySet(root.right));
        return keys;
    }

    @Override
    public V remove(K key) {
        V value = get(key);
        root = remove(root, key);
        return value;
    }

    private BSTNode remove(BSTNode root, K key) {
        if (root == null) {
            return null;
        }
        int cmp = key.compareTo(root.key);
        if (cmp == 0) {
            if (root.left == null) {
                size--;
                return root.right;
            }
            if (root.right == null) {
                size--;
                return root.left;
            }
            BSTNode t = getMin(root.right);
            root.key = t.key;
            root.value = t.value;
            root.right = deleteMin(root.right);
        } else if (cmp < 0) {
            root.left = remove(root.left, key);
        } else {
            root.right = remove(root.right, key);
        }
        return root;
    }

    private BSTNode getMin(BSTNode root) {
        if (root.left == null) {
            return root;
        }
        return getMin(root.left);
    }

    private BSTNode deleteMin(BSTNode root) {
        if (root.left == null) {
            size--;
            return root.right;
        }
        root.left = deleteMin(root.left);
        return root;
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


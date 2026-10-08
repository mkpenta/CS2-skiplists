import java.util.Random;

public class SkiplistSSet<T extends Comparable<? super T>> {

    private static final int MAX_LEVEL = 31;

    private static class Node<E> {
        final E value;
        final Node<E>[] next;

        @SuppressWarnings("unchecked")
        Node(E value, int highestLevel) {
            this.value = value;
            // One forward reference for each level, including level 0.
            next = (Node<E>[]) new Node[highestLevel + 1];
        }

        int highestLevel() {
            return next.length - 1;
        }
    }

    // The head provides a starting point at every level.
    private final Node<T> head = new Node<>(null, MAX_LEVEL);
    private final Random random = new Random();
    private int highestActiveLevel = 0;
    private int elementCount = 0;

    public int size() {
        return elementCount;
    }

    public boolean isEmpty() {
        return elementCount == 0;
    }

    private void requireValue(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Null values are not supported.");
        }
    }

    private int chooseNodeLevel() {
        int nodeLevel = 0;
        // Each heads adds a level; the first tails stops promotion.
        while (nodeLevel < MAX_LEVEL && random.nextBoolean()) {
            nodeLevel++;
        }
        return nodeLevel;
    }

    private Node<T> findPredecessor(T target) {
        Node<T> current = head;
        for (int level = highestActiveLevel; level >= 0; level--) {
            Node<T> nextNode = current.next[level];
            // Move right without reaching or passing the target.
            while (nextNode != null
                    && nextNode.value.compareTo(target) < 0) {
                current = nextNode;
                nextNode = current.next[level];
            }
            // Continue one level lower, staying at the same node.
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private Node<T>[] findPredecessors(T target) {
        Node<T>[] predecessors = (Node<T>[]) new Node[MAX_LEVEL + 1];
        Node<T> current = head;
        for (int level = highestActiveLevel; level >= 0; level--) {
            Node<T> nextNode = current.next[level];
            while (nextNode != null
                    && nextNode.value.compareTo(target) < 0) {
                current = nextNode;
                nextNode = current.next[level];
            }
            // Remember where links may need to change at this level.
            predecessors[level] = current;
        }
        return predecessors;
    }

    // Return the smallest value >= target, or null if none exists.
    public T find(T target) {
        requireValue(target);
        Node<T> predecessor = findPredecessor(target);
        Node<T> candidate = predecessor.next[0];
        if (candidate == null) {
            return null;
        }
        return candidate.value;
    }

    public boolean contains(T target) {
        T candidate = find(target);
        if (candidate == null) {
            return false;
        }
        return candidate.compareTo(target) == 0;
    }

    public boolean add(T value) {
        requireValue(value);
        Node<T>[] predecessors = findPredecessors(value);
        Node<T> candidate = predecessors[0].next[0];
        // Reject duplicates before creating a node.
        if (candidate != null && candidate.value.compareTo(value) == 0) {
            return false;
        }

        int newNodeLevel = chooseNodeLevel();
        Node<T> newNode = new Node<>(value, newNodeLevel);
        // Newly activated levels begin at the head.
        if (newNodeLevel > highestActiveLevel) {
            for (int level = highestActiveLevel + 1;
                    level <= newNodeLevel; level++) {
                predecessors[level] = head;
            }
            highestActiveLevel = newNodeLevel;
        }

        // Splice the node between its predecessor and successor.
        for (int level = 0; level <= newNodeLevel; level++) {
            Node<T> predecessor = predecessors[level];
            Node<T> successor = predecessor.next[level];
            newNode.next[level] = successor;
            predecessor.next[level] = newNode;
        }
        elementCount++;
        return true;
    }

    public boolean remove(T value) {
        requireValue(value);
        Node<T>[] predecessors = findPredecessors(value);
        Node<T> nodeToRemove = predecessors[0].next[0];
        if (nodeToRemove == null) {
            return false;
        }
        if (nodeToRemove.value.compareTo(value) != 0) {
            return false;
        }

        // Bypass the node at every level it occupies.
        for (int level = 0; level <= nodeToRemove.highestLevel(); level++) {
            Node<T> predecessor = predecessors[level];
            Node<T> successor = nodeToRemove.next[level];
            predecessor.next[level] = successor;
        }
        // Lower the starting level if upper levels became empty.
        while (highestActiveLevel > 0
                && head.next[highestActiveLevel] == null) {
            highestActiveLevel--;
        }
        elementCount--;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("[");
        Node<T> current = head.next[0];
        // Level 0 contains every element in sorted order.
        while (current != null) {
            text.append(current.value);
            current = current.next[0];
            if (current != null) {
                text.append(", ");
            }
        }
        text.append("]");
        return text.toString();
    }
}

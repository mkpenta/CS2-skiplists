import java.util.Random;

public class SkiplistList<T> {

    private static final int MAX_LEVEL = 31;

    private static class Node<E> {
        E value;
        final Node<E>[] next;
        final int[] span;

        @SuppressWarnings("unchecked")
        Node(E value, int highestLevel) {
            this.value = value;
            next = (Node<E>[]) new Node[highestLevel + 1];
            span = new int[highestLevel + 1];
        }

        int highestLevel() {
            return next.length - 1;
        }
    }

    private final Node<T> head = new Node<>(null, MAX_LEVEL);
    private final Random random = new Random();
    private int highestActiveLevel = 0;
    private int elementCount = 0;

    public SkiplistList() {
        // Treat head as position -1 and null as the position after the list.
        head.span[0] = 1;
    }

    public int size() {
        return elementCount;
    }

    public boolean isEmpty() {
        return elementCount == 0;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= elementCount) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    private void checkInsertionIndex(int index) {
        // Insertion at size() appends to the list.
        if (index < 0 || index > elementCount) {
            throw new IndexOutOfBoundsException("Index: " + index);
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

    private Node<T> findPredecessor(int index) {
        Node<T> current = head;
        int currentIndex = -1;
        for (int level = highestActiveLevel; level >= 0; level--) {
            // Move right only if the next node is before the target index.
            while (current.next[level] != null
                    && currentIndex + current.span[level] < index) {
                currentIndex += current.span[level];
                current = current.next[level];
            }
        }
        return current;
    }

    public T get(int index) {
        checkElementIndex(index);
        Node<T> predecessor = findPredecessor(index);
        return predecessor.next[0].value;
    }

    public T set(int index, T value) {
        checkElementIndex(index);
        Node<T> predecessor = findPredecessor(index);
        Node<T> target = predecessor.next[0];
        T previousValue = target.value;
        target.value = value;
        return previousValue;
    }

    public void add(T value) {
        add(elementCount, value);
    }

    public void add(int index, T value) {
        checkInsertionIndex(index);
        Node<T> newNode = new Node<>(value, chooseNodeLevel());
        // New upper levels initially span the entire existing list.
        if (newNode.highestLevel() > highestActiveLevel) {
            for (int level = highestActiveLevel + 1;
                    level <= newNode.highestLevel(); level++) {
                head.span[level] = elementCount + 1;
            }
            highestActiveLevel = newNode.highestLevel();
        }

        Node<T> current = head;
        int currentIndex = -1;
        for (int level = highestActiveLevel; level >= 0; level--) {
            while (current.next[level] != null
                    && currentIndex + current.span[level] < index) {
                currentIndex += current.span[level];
                current = current.next[level];
            }
            // This link crosses the insertion position, so it grows by one.
            current.span[level]++;
            if (level <= newNode.highestLevel()) {
                Node<T> successor = current.next[level];
                int distanceToNewNode = index - currentIndex;
                // Split the existing link and its span around the new node.
                newNode.next[level] = successor;
                newNode.span[level] =
                        current.span[level] - distanceToNewNode;
                current.next[level] = newNode;
                current.span[level] = distanceToNewNode;
            }
        }
        elementCount++;
    }

    public T remove(int index) {
        checkElementIndex(index);
        Node<T> current = head;
        int currentIndex = -1;
        T removedValue = null;
        for (int level = highestActiveLevel; level >= 0; level--) {
            while (current.next[level] != null
                    && currentIndex + current.span[level] < index) {
                currentIndex += current.span[level];
                current = current.next[level];
            }
            Node<T> nextNode = current.next[level];
            int nextIndex = currentIndex + current.span[level];
            if (nextNode != null && nextIndex == index) {
                removedValue = nextNode.value;
                // Bypass the removed node and combine the two spans.
                current.next[level] = nextNode.next[level];
                current.span[level] += nextNode.span[level];
            }
            // Every link crossing the removed position shrinks by one.
            current.span[level]--;
        }
        elementCount--;
        while (highestActiveLevel > 0
                && head.next[highestActiveLevel] == null) {
            highestActiveLevel--;
        }
        return removedValue;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("[");
        Node<T> current = head.next[0];
        // Level 0 contains every element in list order.
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

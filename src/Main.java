public class Main {

    public static void main(String[] args) {
        testSkiplistSSet();
        testSkiplistList();
        System.out.println("\nAll tests passed.");
    }

    private static void testSkiplistSSet() {
        System.out.println("Testing SkiplistSSet");
        SkiplistSSet<Integer> numbers = new SkiplistSSet<>();

        check("Starts empty", true, numbers.isEmpty());
        numbers.add(30);
        numbers.add(10);
        numbers.add(20);
        check("Sorted order", "[10, 20, 30]", numbers.toString());
        check("Reject duplicate", false, numbers.add(20));
        check("Size", 3, numbers.size());

        check("Contains 20", true, numbers.contains(20));
        check("Does not contain 15", false, numbers.contains(15));
        check("Find exact value", 20, numbers.find(20));
        // find returns the smallest value greater than or equal to the target.
        check("Find next larger value", 20, numbers.find(15));
        check("Find above maximum", null, numbers.find(40));

        check("Remove 20", true, numbers.remove(20));
        check("Order after removal", "[10, 30]", numbers.toString());
        check("Remove missing value", false, numbers.remove(20));
        numbers.remove(10);
        numbers.remove(30);
        check("Empty after removals", true, numbers.isEmpty());
        check("Size after removals", 0, numbers.size());
        numbers.add(25);
        check("Add after emptying", "[25]", numbers.toString());
    }

    private static void testSkiplistList() {
        System.out.println("\nTesting SkiplistList");
        SkiplistList<String> names = new SkiplistList<>();

        check("Starts empty", true, names.isEmpty());
        names.add("Charlie");
        names.add("Alice");
        names.add(1, "Bob");
        check("Insert in middle", "[Charlie, Bob, Alice]", names.toString());
        check("Size", 3, names.size());
        check("Get first", "Charlie", names.get(0));
        check("Get middle", "Bob", names.get(1));
        check("Get last", "Alice", names.get(2));

        check("Set returns old value", "Bob", names.set(1, "Dana"));
        check("Set changes value", "Dana", names.get(1));
        names.add(0, "Zoe");
        names.add(names.size(), "Eve");
        check("Insert at both ends", "[Zoe, Charlie, Dana, Alice, Eve]",
                names.toString());

        check("Remove middle", "Dana", names.remove(2));
        check("Remove first", "Zoe", names.remove(0));
        check("Remove last", "Eve", names.remove(names.size() - 1));
        check("Remaining order", "[Charlie, Alice]", names.toString());
        check("Remaining size", 2, names.size());

        names.remove(0);
        names.remove(0);
        check("Empty after removals", true, names.isEmpty());
        names.add("Bob");
        names.add("Bob");
        check("Duplicates allowed", "[Bob, Bob]", names.toString());

        try {
            names.get(names.size());
            throw new AssertionError("Expected an invalid-index exception");
        } catch (IndexOutOfBoundsException expected) {
            System.out.println("PASS: Reject invalid index");
        }
    }

    // Print each success and stop with details if a result is incorrect.
    private static void check(String test, Object expected, Object actual) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(test + ": expected " + expected
                    + ", got " + actual);
        }
        System.out.println("PASS: " + test);
    }
}

package com.sap.jam.api.sample;

/**
 * A class that holds a pair of objects.
 */
public final class Pair<A, B> {

    /** The first object of the pair. */
    private final A object1;

    /** The second object of the pair. */
    private final B object2;

    /**
     * Constructor for a Pair object.
     * @param object1 the first object of the pair.
     * @param object2 the second object of the pair.
     */
    public Pair(A object1, B object2) {
        this.object1 = object1;
        this.object2 = object2;
    }

    public static <A, B> Pair<A, B> make(A object1, B object2) {
        return new Pair<A, B>(object1, object2);
    }

    /**
     * Get the first object of the pair.
     * @return the first object of the pair.
     */
    public A fst() {
        return object1;
    }

    /**
     * Get the second object of the pair.
     * @return the second object of the pair.
     */
    public B snd() {
        return object2;
    }

    /**
     * Return whether this object is "equal" to another.
     * This is true if the corresponding elements of the pairs are equals().
     * @param obj Object the other object.
     * @return boolean true if "equal"
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Pair)) {
            return false;
        }

        // true if the corresponding objects in each pair match.
        Pair<?, ?> otherPair = (Pair<?, ?>)obj;
        return (object1 == null ? otherPair.object1 == null : object1.equals(otherPair.object1)) &&
               (object2 == null ? otherPair.object2 == null : object2.equals(otherPair.object2));
    }

    /**
     * Return a hashcode for this object.
     * @return int the pair's hash code
     */
    @Override
    public int hashCode() {
        // make sure that two objects that are equals() have the same hashCode (for use in collections)
        int object1HashCode = object1 == null ? 0 : object1.hashCode();
        int object2HashCode = object2 == null ? 0 : object2.hashCode();
        return 37 * (17 + object1HashCode) + object2HashCode;
    }

    /**
     * A reasonable string representation for this class.
     * @return the string representation for this class.
     */
    @Override
    public String toString() {
        return "Pair: (" + object1 + ", " + object2 + ")";
    }

}
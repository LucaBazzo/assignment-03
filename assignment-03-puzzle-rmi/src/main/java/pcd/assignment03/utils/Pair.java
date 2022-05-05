package pcd.assignment03.utils;

import java.io.Serializable;

/**
 *
 * Defines a pair of two objects
 *
 */
public class Pair<X,Y> implements Serializable {

    private final X first;
    private final Y second;

    /**
     * constructor
     * @param first the first element of the pair
     * @param second the second element of the pair
     */
    public Pair(X first, Y second) {
        this.first = first;
        this.second = second;
    }

    /**
     * get the first element of the pair
     * @return the first element
     */
    public X getFirst() {
        return first;
    }

    /**
     * get the second element of the pair
     * @return the second element
     */
    public Y getSecond() {
        return second;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pair other = (Pair) obj;
        if (first == null) {
            if (other.first != null)
                return false;
        } else if (!first.equals(other.first))
            return false;
        if (second == null) {
            return other.second == null;
        } else return second.equals(other.second);
    }

    @Override
    public String toString() {
        return first + "  -  " + second;
    }

}


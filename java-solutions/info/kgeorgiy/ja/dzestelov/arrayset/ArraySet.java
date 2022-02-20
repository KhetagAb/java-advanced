package info.kgeorgiy.ja.dzestelov.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {

    private final List<E> elements;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this(new Object[0], null);
    }

    public ArraySet(final Collection<? extends Comparable<? super E>> collection) {
        this(toArray(collection, null), null);
    }

    public ArraySet(final Collection<? extends E> collection, final Comparator<? super E> comparator) {
        this(toArray(collection, comparator), comparator);
    }

    public ArraySet(final SortedSet<E> sortedSet) {
        this(sortedSet.toArray(), sortedSet.comparator());
    }

    private ArraySet(final List<E> elements, Comparator<? super E> comparator) {
        this.elements = elements;
        this.comparator = comparator;
    }

    private ArraySet(final Object[] sortedArray, Comparator<? super E> comparator) {
        elements = (List<E>) List.of(sortedArray);
        this.comparator = comparator;
    }

    private static <E> Object[] toArray(final Collection<? extends E> collection, final Comparator<E> comparator) {
        SortedSet<E> es = new TreeSet<>(comparator);
        es.addAll(collection);
        return es.toArray();
    }

    private int lowerIndex(E t) {
        return getIndex(t, -1, -1);
    }

    private int floorIndex(E t) {
        return getIndex(t, 0, -1);
    }

    private int ceilingIndex(E t) {
        return getIndex(t, 0, 0);
    }

    private int higherIndex(E t) {
        return getIndex(t, 1, 0);
    }

    @Override
    public E lower(E t) {
        return getOrNull(lowerIndex(t));
    }

    @Override
    public E floor(E t) {
        return getOrNull(floorIndex(t));
    }

    @Override
    public E ceiling(E t) {
        return getOrNull(ceilingIndex(t));
    }

    @Override
    public E higher(E t) {
        return getOrNull(higherIndex(t));
    }

    private int getIndex(E t, int inRangeDelta, int outOfRangeDelta) {
        int i = Collections.binarySearch(elements, Objects.requireNonNull(t), comparator);
        return i >= 0 ? i + inRangeDelta : -i - 1 + outOfRangeDelta;
    }

    private E getOrNull(int i) {
        return 0 <= i && i < elements.size() ? elements.get(i) : null;
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(elements, (E) Objects.requireNonNull(o), comparator) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return null;
    }

    @Override
    public Iterator<E> descendingIterator() {
        return null;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int from = fromInclusive ? ceilingIndex(fromElement) : higherIndex(fromElement);
        int to = toInclusive ? floorIndex(toElement) : lowerIndex(toElement);

        if (to < from) {
            throw new IllegalArgumentException("fromElement > toElement: " + elements.toString() + ", " + comparator.toString() + ": subSet(" + fromElement + ", " + fromInclusive + ", " + toElement + ", " + toInclusive + ")");
        }
        return new ArraySet<>(elements.subList(from, to + 1), comparator);
    }
//    private boolean inRange(E element, boolean inclusive) {
//        if (isEmpty()) {
//            return false;
//        } else {
//            if (inclusive) {
//                if (comparator == null) {
//                    return first() < element;
//                }
//                return comparator.compare(first(), element) <= 0 && comparator.compare(element, last()) <= 0;
//            } else {
//                return comparator.compare(first(), element) < 0 && comparator.compare(element, last()) < 0;
//            }
//        }

//    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (isEmpty()) {
            return new ArraySet<>();
        }
        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (isEmpty()) {
            return new ArraySet<>();
        }
        return subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        return elements.get(0);
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        return elements.get(elements.size() - 1);
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Unable to poll first element from ArraySet");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Unable to poll last element from ArraySet");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Unable to remove element from ArraySet");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Unable to retain elements from ArraySet");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Unable to remove elements from ArraySet");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unable to clear ArraySet");
    }
}

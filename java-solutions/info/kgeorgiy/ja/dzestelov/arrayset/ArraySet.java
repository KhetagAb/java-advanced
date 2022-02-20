package info.kgeorgiy.ja.dzestelov.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {

    private final List<E> elements;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this(new Object[0], null);
    }

    public ArraySet(final Collection<? extends E> collection) {
        this(toArray(collection, null), null);
    }

    public ArraySet(final Collection<? extends E> collection, final Comparator<? super E> comparator) {
        this(toArray(collection, comparator), comparator);
    }

    public ArraySet(final SortedSet<E> sortedSet) {
        this(sortedSet.toArray(), sortedSet.comparator());
    }

    @SuppressWarnings("unchecked")
    private ArraySet(final Object[] sortedArray, Comparator<? super E> comparator) {
        elements = (List<E>) List.of(sortedArray);
        this.comparator = comparator;
    }

    private static <E> Object[] toArray(final Collection<? extends E> collection, final Comparator<E> comparator) {
        SortedSet<E> es = new TreeSet<>(comparator);
        es.addAll(collection);
        return es.toArray();
    }

    @Override
    public E lower(E t) {
        return null;
    }

    @Override
    public E floor(E t) {
        return null;
    }

    @Override
    public E ceiling(E t) {
        return null;
    }

    @Override
    public E higher(E t) {
        return null;
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        } else return Collections.binarySearch(elements, (E) o, comparator) > 0;
    }

    @Override
    public Iterator<E> iterator() {
        return null;
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
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return null;
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return null;
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return null;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return null;
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return null;
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return null;
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public E first() {
        return elements.get(0);
    }

    @Override
    public E last() {
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
        throw new UnsupportedOperationException("Unable to clear  ArraySet");
    }
}

package info.kgeorgiy.ja.dzestelov.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {

    private final ViewList<E> elements;
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

    private NavigableSet<E> descendingView = null;

    private ArraySet(final ViewList<E> view, final Comparator<? super E> comparator) {
        this.elements = view;
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
        int i = Collections.binarySearch(elements, t, comparator);
        return i >= 0 ? i + inRangeDelta : -i - 1 + outOfRangeDelta;
    }

    private E getOrNull(int i) {
        return 0 <= i && i < elements.size() ? elements.get(i) : null;
    }

    @Override
    public int size() {
        return elements.size();
    }

    @SuppressWarnings("unchecked")
    private ArraySet(final Object[] sortedArray, final Comparator<? super E> comparator) {
        this.elements = new ViewList<>((E[]) sortedArray);
        this.comparator = comparator;
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(elements, (E) o, comparator) >= 0;
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (isInvalidRange(fromElement, toElement)) {
            throw new IllegalArgumentException("fromElement > toElement");
        }

        int from = fromInclusive ? ceilingIndex(fromElement) : higherIndex(fromElement);
        int to = toInclusive ? floorIndex(toElement) : lowerIndex(toElement);

        if (from > to) {
            return emptyList();
        } else {
            return new ArraySet<>(new ViewList<>(elements, from, to + 1), comparator);
        }
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (isEmpty() || isInvalidRange(first(), toElement)) {
            return emptyList();
        }
        return subSet(first(), true, toElement, inclusive);
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
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (isEmpty() || isInvalidRange(fromElement, last())) {
            return emptyList();
        }
        return subSet(fromElement, inclusive, last(), true);
    }

    private ArraySet<E> emptyList() {
        return new ArraySet<>(new Object[0], comparator);
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

    @SuppressWarnings("unchecked")
    private boolean isInvalidRange(E from, E to) {
        if (comparator != null) {
            return comparator.compare(from, to) > 0;
        } else {
            return ((Comparable<? super E>) from).compareTo(to) > 0;
        }
    }

    @Override
    public NavigableSet<E> descendingSet() {
        if (descendingView == null) {
            descendingView = new ArraySet<>(new ViewList<>(elements, true), Collections.reverseOrder(this.comparator));
        }
        return descendingView;
    }

    @Override
    public Iterator<E> descendingIterator() {
        return (descendingView != null ? descendingView.iterator() : descendingSet().iterator());
    }


    static class ViewList<E> extends AbstractList<E> implements RandomAccess {

        private final E[] elements;
        private final int from, to;
        private final boolean isReversed;

        private ViewList(E[] elements) {
            this.elements = elements;
            this.from = 0;
            this.to = elements.length;
            this.isReversed = false;
        }

        private ViewList(ViewList<E> elements, boolean isReversed) {
            this.elements = elements.elements;
            this.from = elements.from;
            this.to = elements.to;
            this.isReversed = elements.isReversed ^ isReversed;
        }

        private ViewList(ViewList<E> elements, int from, int to) {
            this.elements = elements.elements;
            if (elements.isReversed) {
                this.from = elements.from + elements.size() - to;
                this.to = elements.to - from;
            } else {
                this.from = elements.from + from;
                this.to = elements.from + to;
            }
            this.isReversed = elements.isReversed;
        }

        @Override
        public E get(int index) {
            if (isReversed) {
                return elements[to - 1 - index];
            } else {
                return elements[from + index];
            }
        }

        @Override
        public int size() {
            return to - from;
        }
    }
}

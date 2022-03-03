package info.kgeorgiy.ja.dzestelov.student;

import info.kgeorgiy.java.advanced.student.AdvancedQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class StudentDB implements AdvancedQuery {

    private final Comparator<Student> BY_ID = Comparator.comparingInt(Student::getId);

    private final Comparator<Student> BY_NAME = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName).reversed()
            .thenComparing(Student::getId);

    private final Function<Student, String> FULL_NAME = student -> student.getFirstName() + " " + student.getLastName();

    private <K> Stream<Map.Entry<K, List<Student>>> getCollectedStream(Collection<Student> students, Collector<Student, ?, Map<K, List<Student>>> collector) {
        return students.stream()
                .collect(collector)
                .entrySet().stream();
    }

    private List<Group> getGroupsBy(Collection<Student> students, Comparator<Student> comparator) {
        return getCollectedStream(students, Collectors.groupingBy(Student::getGroup))
                .map(group -> new Group(group.getKey(), sortedBy(group.getValue(), comparator)))
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsBy(students, BY_NAME);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsBy(students, BY_ID);

    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getLargestGroupBy(students,
                Map.Entry.<GroupName, List<Student>>comparingByValue(Comparator.comparing(List::size))
                        .thenComparing(Map.Entry::getKey));
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupBy(students,
                Map.Entry.<GroupName, List<Student>>comparingByValue(Comparator.comparing(list -> getDistinctFirstNames(list).size()))
                        .reversed()
                        .thenComparing(Map.Entry::getKey)
                        .reversed());
    }

    private <T, C extends Collection<T>> C mapByToCollection(List<Student> students, Function<? super Student, ? extends T> mapped, Supplier<C> collectionFactory) {
        return students.stream()
                .map(mapped)
                .collect(Collectors.toCollection(collectionFactory));
    }

    private <T> List<T> mapByToList(List<Student> students, Function<? super Student, ? extends T> mapped) {
        return mapByToCollection(students, mapped, ArrayList::new);
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapByToList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapByToList(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return mapByToList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapByToList(students, FULL_NAME);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return mapByToCollection(students, Student::getFirstName, TreeSet::new);
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Comparator.comparingInt(Student::getId))
                .map(Student::getFirstName)
                .orElse("");
    }

    private List<Student> sortedBy(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortedBy(students, BY_ID);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortedBy(students, BY_NAME);
    }

    private <T> Stream<Student> filteredStreamByField(Collection<Student> students, Function<? super Student, ? extends T> mapped, T value) {
        return students.stream()
                .filter(student -> mapped.apply(student).equals(value));
    }

    private <T> List<Student> filterByField(Collection<Student> students, Function<? super Student, ? extends T> mapped, T value) {
        return filteredStreamByField(students, mapped, value)
                .sorted(BY_NAME)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return filterByField(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return filterByField(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return filterByField(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return filteredStreamByField(students, Student::getGroup, group)
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)));
    }

    public <T, C> Optional<T> getByComparatorWithCollector(Collection<Student> students,
                                                           Collector<Student, ?, Map<T, C>> collector,
                                                           Comparator<Map.Entry<T, C>> comparator) {
        return students.stream()
                .collect(collector)
                .entrySet()
                .stream()
                .max(comparator)
                .map(Map.Entry::getKey);
    }


    private GroupName getLargestGroupBy(Collection<Student> students, Comparator<Map.Entry<GroupName, List<Student>>> comparator) {
        return getByComparatorWithCollector(students, Collectors.groupingBy(Student::getGroup), comparator).orElse(null);
    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return getByComparatorWithCollector(students,
                Collectors.groupingBy(Student::getFirstName, Collectors.groupingBy(Student::getGroup)),
                Map.Entry.<String, Map<GroupName, List<Student>>>comparingByValue(Comparator.comparing(Map::size))
                        .thenComparing(Map.Entry::getKey)).orElse("");
    }

    private <T> List<T> getByIndices(List<Student> students, Function<? super Student, ? extends T> mapper, int[] indices) {
        return Arrays.stream(indices)
                .mapToObj(students::get)
                .map(mapper)
                .collect(Collectors.toList());
    }

    private <T> List<T> getByIndices(Collection<Student> students, Function<? super Student, ? extends T> mapper, int[] indices) {
        return getByIndices(students.stream().limit(Arrays.stream(indices).max().orElse(-1) + 1).toList(), mapper, indices);
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, Student::getFirstName, indices);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, Student::getLastName, indices);
    }

    @Override
    public List<GroupName> getGroups(Collection<Student> students, int[] indices) {
        return getByIndices(students, Student::getGroup, indices);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, FULL_NAME, indices);
    }
}

package info.kgeorgiy.ja.dzestelov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.GroupQuery;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class StudentDB implements GroupQuery {

    private final Comparator<Student> BY_ID = Comparator.comparingInt(Student::getId);

    private final Comparator<Student> BY_NAME = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName).reversed()
            .thenComparing(Student::getId);

    private <T> Stream<Map.Entry<GroupName, List<Student>>> getGroupsStream(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup))
                .entrySet().stream();
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsStream(students)
                .map(group -> new Group(group.getKey(), sortedBy(group.getValue(), BY_NAME)))
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsStream(students)
                .map(group -> new Group(group.getKey(), sortedBy(group.getValue(), BY_ID)))
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getGroupsStream(students)
                .max(Map.Entry.<GroupName, List<Student>>comparingByValue(Comparator.comparing(List::size))
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getGroupsStream(students)
                .min(Map.Entry.<GroupName, List<Student>>comparingByValue(
                                Comparator.comparing(list -> list.stream().map(Student::getFirstName).distinct().count())
                        ).reversed()
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .orElse(null);
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
        return mapByToList(students, student -> student.getFirstName() + " " + student.getLastName());
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
                .collect(
                        Collectors.toMap(
                                Student::getLastName,
                                Student::getFirstName,
                                BinaryOperator.minBy(String::compareTo)
                        )
                );
    }
}

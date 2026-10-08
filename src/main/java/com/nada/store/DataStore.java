package com.nada.store;

import com.nada.models.Person;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DataStore {
    private final Map<Integer, Person> people = new ConcurrentHashMap<>();
    private final Map<Integer, Set<Integer>> connections = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    public void clear() {
        people.clear();
        connections.clear();
        idGenerator.set(1);
    }

    public Person createPerson(String name) {
        int id = idGenerator.getAndIncrement();
        Person p = new Person(id, name);
        connections.put(id, ConcurrentHashMap.newKeySet()); // Put connections first to avoid race conditions
        people.put(id, p);
        return p;
    }

    public Person getPerson(int id) {
        return people.get(id);
    }

    public boolean addConnection(int idA, int idB) {
        if (!people.containsKey(idA) || !people.containsKey(idB)) {
            return false;
        }
        if (idA == idB) {
            return true;
        }
        connections.get(idA).add(idB);
        connections.get(idB).add(idA);
        return true;
    }

    public List<Person> getContacts(int id) {
        if (!people.containsKey(id)) {
            return null;
        }
        List<Person> contacts = new ArrayList<>();
        for (int contactId : connections.get(id)) {
            contacts.add(people.get(contactId));
        }
        contacts.sort(Comparator.comparingInt(Person::getId)); // return consistently ordered
        return contacts;
    }

    public List<Person> findShortestPath(int from, int to, int maxHops) {
        if (!people.containsKey(from) || !people.containsKey(to)) {
            return null;
        }
        if (from == to) {
            return List.of(people.get(from));
        }

        Queue<List<Integer>> queue = new LinkedList<>();
        queue.add(List.of(from));
        
        Set<Integer> visited = new HashSet<>();
        visited.add(from);

        while (!queue.isEmpty()) {
            List<Integer> path = queue.poll();
            int currentHops = path.size() - 1;
            int current = path.get(path.size() - 1);
            
            if (current == to) {
                List<Person> result = new ArrayList<>();
                for (int id : path) {
                    result.add(people.get(id));
                }
                return result;
            }

            if (currentHops < maxHops) {
                for (int neighbor : connections.get(current)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        List<Integer> newPath = new ArrayList<>(path);
                        newPath.add(neighbor);
                        queue.add(newPath);
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}

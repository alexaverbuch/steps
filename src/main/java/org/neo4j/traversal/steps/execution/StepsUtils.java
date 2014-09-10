package org.neo4j.traversal.steps.execution;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.*;

// TODO test
public class StepsUtils {
    // TODO generalize to "aggregate" where input function defined what happens:
    // TODO count, group, distinct
    // TODO current impl -> Function<GROUP_TYPE, Map<GROUP_TYPE,INTEGER>>
    // overwrites aggregate
    // TODO then build helpers on top of "aggregate" -> count, group

    public static class Pair<TUPLE_1_TYPE,TUPLE_2_TYPE> {
        private final TUPLE_1_TYPE val1;
        private final TUPLE_2_TYPE val2;

        public Pair(TUPLE_1_TYPE val1, TUPLE_2_TYPE val2) {
            this.val1 = val1;
            this.val2 = val2;
        }

        public TUPLE_1_TYPE _1() {
            return val1;
        }

        public TUPLE_2_TYPE _2() {
            return val2;
        }
    }

    public static <GROUP_KEY, GROUPED_VALUE, ORIGINAL> Map<GROUP_KEY, Collection<GROUPED_VALUE>> groupBy(
            Iterator<ORIGINAL> originalValues,
            Function<ORIGINAL, Pair<GROUP_KEY, GROUPED_VALUE>> extractFun,
            final boolean distinct) {
        Function<Object, Collection<GROUPED_VALUE>> newGroupFun = new Function<Object, Collection<GROUPED_VALUE>>() {
            @Override
            public Collection<GROUPED_VALUE> apply(Object nothing) {
                if (distinct) return new HashSet<>();
                else return new ArrayList<>();
            }
        };
        Map<GROUP_KEY, Collection<GROUPED_VALUE>> groups = new HashMap<>();
        while (originalValues.hasNext()) {
            ORIGINAL originalValue = originalValues.next();
            Pair<GROUP_KEY, GROUPED_VALUE> pathValues = extractFun.apply(originalValue);
            GROUP_KEY groupKey = pathValues._1();
            GROUPED_VALUE groupValue = pathValues._2();
            Collection<GROUPED_VALUE> group = groups.get(groupKey);
            if (null == group) group = newGroupFun.apply(null);
            group.add(groupValue);
            groups.put(groupKey, group);
        }
        return groups;
    }

    public static <THING> Map<THING, Integer> count(Iterator<THING> thingsToCount) {
        Map<THING, Integer> countedThings = new HashMap<>();
        while (thingsToCount.hasNext()) {
            THING thing = thingsToCount.next();
            if (countedThings.containsKey(thing)) {
                int count = countedThings.get(thing);
                countedThings.put(thing, count + 1);
            } else {
                countedThings.put(thing, 1);
            }
        }
        return countedThings;
    }

    public static Iterator<Node> projectNodesFromPath(Iterable<Path> paths, int indexInPath) {
        return projectNodesFromPath(paths.iterator(), indexInPath);
    }

    public static Iterator<Node> projectNodesFromPath(Iterator<Path> paths, final int indexInPath) {
        return Iterators.transform(paths, new Function<Path, Node>() {
            @Override
            public Node apply(Path path) {
                return Iterables.get(path.nodes(), indexInPath);
            }
        });
    }

    public static <THING> Iterator<THING> distinct(Iterable<THING> withDuplicates) {
        return distinct(withDuplicates.iterator());
    }

    public static <THING> Iterator<THING> distinct(Iterator<THING> withDuplicates) {
        Predicate<THING> distinctFun = new Predicate<THING>() {
            Set<THING> alreadySeen = new HashSet<>();

            @Override
            public boolean apply(THING input) {
                return alreadySeen.add(input);
            }
        };
        return Iterators.filter(withDuplicates, distinctFun);
    }

    public static <THING> Iterator<THING> excluding(Iterable<THING> allThings, THING... thingsToExclude) {
        return excluding(allThings.iterator(), thingsToExclude);
    }

    public static <THING> Iterator<THING> excluding(Iterator<THING> allThings, THING... thingsToExclude) {
        final Set<THING> excludeSet = Sets.newHashSet(thingsToExclude);
        Predicate<THING> excludeFun = new Predicate<THING>() {
            @Override
            public boolean apply(THING thing) {
                return false == excludeSet.contains(thing);
            }
        };
        return Iterators.filter(allThings, excludeFun);
    }

    public static String pathString(Path path) {
        StringBuilder sb = new StringBuilder();
        Node lastNode = null;
        for (PropertyContainer thing : path) {
            if (thing instanceof Node) {
                lastNode = (Node) thing;
                sb.append("(").append(
                        Sets.newHashSet(lastNode.getLabels()).toString().replace("[", "").replace("]", "")).append(
                        ")");
            }
            if (thing instanceof Relationship) {
                Relationship relationship = (Relationship) thing;
                if (relationship.getStartNode().equals(lastNode)) {
                    sb.append("-[").append(relationship.getType().name()).append("]->");
                } else {
                    sb.append("<-[").append(relationship.getType().name()).append("]-");
                }
            }
        }
        return sb.toString();
    }
}

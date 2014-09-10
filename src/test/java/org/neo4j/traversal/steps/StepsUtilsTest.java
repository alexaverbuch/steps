package org.neo4j.traversal.steps;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.neo4j.traversal.steps.execution.StepsUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StepsUtilsTest {
        @Test
        public void groupByNotDistinct() throws Exception {
            // Given
            List<String> original = Lists.newArrayList("1", "1", "2", "3", "11", "12", "123");
            Function<String, StepsUtils.Pair<Integer, String>> extractFun = new Function<String, StepsUtils.Pair<Integer, String>>() {
                @Override
                public StepsUtils.Pair<Integer, String> apply(String s) {
                    return new StepsUtils.Pair(s.length(), s);
                }
            };
            boolean distinct = false;

            // When
            Map<Integer, Collection<String>> groups = StepsUtils.groupBy(original.iterator(), extractFun, distinct);

            // Then
            assertThat(groups.size(), is(3));
            assertThat(Lists.newArrayList(groups.get(1)), equalTo(Lists.newArrayList("1", "1", "2", "3")));
            assertThat(Lists.newArrayList(groups.get(2)), equalTo(Lists.newArrayList("11", "12")));
            assertThat(Lists.newArrayList(groups.get(3)), equalTo(Lists.newArrayList("123")));
        }

        @Test
        public void groupByDistinct() throws Exception {
            // Given
            List<String> original = Lists.newArrayList("1", "1", "2", "3", "11", "12", "123");
            Function<String, StepsUtils.Pair<Integer, String>> extractFun = new Function<String, StepsUtils.Pair<Integer, String>>() {
                @Override
                public StepsUtils.Pair<Integer, String> apply(String s) {
                    return new StepsUtils.Pair(s.length(), s);
                }
            };
            boolean distinct = true;

            // When
            Map<Integer, Collection<String>> groups = StepsUtils.groupBy(original.iterator(), extractFun, distinct);

            // Then
            assertThat(groups.size(), is(3));
            assertThat(Sets.newHashSet(groups.get(1)), equalTo(Sets.newHashSet("1", "2", "3")));
            assertThat(Sets.newHashSet(groups.get(2)), equalTo(Sets.newHashSet("11", "12")));
            assertThat(Sets.newHashSet(groups.get(3)), equalTo(Sets.newHashSet("123")));
        }
}

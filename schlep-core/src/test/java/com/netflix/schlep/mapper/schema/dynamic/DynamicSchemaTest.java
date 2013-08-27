package com.netflix.schlep.mapper.schema.dynamic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class DynamicSchemaTest {
    @Test
    public void test() {
        Multimap<String, Object> data = ImmutableMultimap.<String, Object>builder()
                .putAll("a", "str", 123)
                .putAll("b", "str", true)
                .putAll("c", "str")
                .putAll("d", "str")
                .putAll("e", 123)
                .putAll("f", "str")
                .putAll("g", "str")
                .putAll("h", "str")
                .putAll("i", "str")
                .putAll("j", "str")
                .putAll("k", "str")
                .build();
        
//        DynamicSchema schema = new DynamicSchema();
//        Random r = new Random();
//        List<Entry<String, Object>> entries = Lists.newArrayList(data.entries());
//        for (int i = 0 ; i < 1000; i++) {
//            Entry<String, Object> entry = entries.get(r.nextInt(entries.size()));
//            schema.readAccess(entry.getKey(), entry.getValue().getClass());
//        }
    }
}

package com;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
class RecursiveJsonUnflattener {
    private final Map<String, Map<String, Object>> objectMap = new HashMap<>();
    private final Set<String> rootKeys = new HashSet<>();
    private final Object[] jsonData;
    private final Gson serializer = new Gson();

    public RecursiveJsonUnflattener(String jsonData) {
        this.jsonData = serializer.fromJson(jsonData, Object[].class);
    }

    public String getNested() {
        for (var obj : jsonData) {
            if (obj instanceof Map) {
                addToKeys((Map<String, Object>) obj);
            }
        }
        rootKeys.addAll(objectMap.keySet());

        for (var map : objectMap.values()) {
            substituteStringValues(map);
        }

        var resultingMap = rootKeys.stream().collect(Collectors.toMap(key -> key, objectMap::get));
        return serializer.toJson(resultingMap);
    }

    private void addToKeys(Map<String, Object> map) {
        for (var entry : map.entrySet()) {
            if (!(entry.getValue() instanceof String)) {
                objectMap.put(entry.getKey(), (Map<String, Object>) entry.getValue());
                addToKeys((Map<String, Object>) entry.getValue());
            }
        }
    }

    private void substituteStringValues(Map<String, Object> map) {
        for (var entry : map.entrySet()) {
            var value = entry.getValue();
            if (value instanceof String && !value.equals("")) {
                map.put(entry.getKey(), objectMap.get(value));
                rootKeys.remove(value);
            } else if (entry.getValue() instanceof Map) {
                substituteStringValues((Map<String, Object>) value);
            }
        }
    }
}

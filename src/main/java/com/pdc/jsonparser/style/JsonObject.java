package com.pdc.jsonparser.style;

import com.pdc.jsonparser.exception.JsonTypeException;
import com.pdc.jsonparser.util.FormatUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON的对象形式
 * 对象是一个无序的“‘键/值’对”集合。
 * 一个对象以“{”（左括号）开始，“}”（右括号）结束。
 * 每个“名称”后跟一个“:”（冒号）；“‘键/值’ 对”之间使用“,”（逗号）分隔。
 * 例：{"姓名":"彭德崇","年龄":"20"}
 * 可以想到，我们可以用map来存储键值对
 * author PDC
 */
public class JsonObject {
    private Map<String, Object> map = new HashMap<>();

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public List<Map.Entry<String, Object>> getAllKeyValue() {
        return new ArrayList<>(map.entrySet());
    }

    public JsonObject getJsonObject(String key) {
        if (!map.containsKey(key)) {
            throw new IllegalArgumentException("Invalid key");
        }

        Object obj = map.get(key);
        if (!(obj instanceof JsonObject)) {
            throw new JsonTypeException("Type of value is not JsonObject");
        }

        return (JsonObject) obj;
    }

    public JsonArray getJsonArray(String key) {
        if (!map.containsKey(key))
            throw new IllegalArgumentException("Invalid key");
        Object obj = map.get(key);
        if (!(obj instanceof JsonArray))
            throw new JsonTypeException("Type of value is not JsonArray");
        return (JsonArray) obj;
    }

    @Override
    public String toString() {
        return FormatUtil.beautify(this);
    }
}

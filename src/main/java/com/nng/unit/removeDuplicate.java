package com.nng.unit;

import java.util.HashSet;
import java.util.List;

public class removeDuplicate {
    public static List remove(List list) {
        HashSet h = new HashSet(list);
        list.clear();
        list.addAll(h);
        return list;
    }
}

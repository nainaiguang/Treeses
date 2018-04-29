package com.nng.unit.sort.util;

import com.nng.unit.Util;

import java.util.Map;

/**
 * 工具类的统一外部调用接口
 * @author Administrator
 *
 * @param <T>
 */
public interface SortUtil<T> extends Util {
	Map<Integer, T> rank(Map<Integer, T> srcData, boolean preface);
}

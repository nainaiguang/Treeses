package com.nng.unit.sort.strategy;

import java.util.Map;

/**
 * 排序工具接口
 * 使用 策略模式 管理
 * @author Vijay
 *
 */
public interface SortUtil<T> {
	/**
	 * 根据 自定义比较器 比较 Map 中的 value，从而达到 排序 Map 的目的
	 * @param srcData
	 * @param preface    序向
	 * @return Map<Integer, S>
	 */
	Map<Integer, T> sort(Map<Integer, T> srcData, boolean preface);
}

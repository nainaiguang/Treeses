package com.nng.unit.sort.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 数据类型比较策略 -- 字符串比较
 * @author Vijay
 * @category SortByDataTypeStrategy<String>
 * 
 */
public class StringSort implements SortUtil<String> {
	
	/**
	 * 根据 自定义比较器 比较 Map 中的 value，从而达到 排序 Map 的目的
	 * @param srcData    数据源
	 *  @param preface	 序向 （当字符或字符串比较时，大小写字母排序拼音开头第一字母排序）
	 * @return resultMap
	 * 
	 */
	@Override
	public Map<Integer, String> sort(Map<Integer, String> srcData, boolean preface) {
		Map<Integer, String> resultMap  = new LinkedHashMap<>();		//新建 支持有序的 LinkHashMap，装载结果
		List<Entry<Integer, String>> srcDataEntryList = new ArrayList<>(srcData.entrySet());	//通过ArrayList的构造器，将 Map的EntrySet 转换成 List
		
		Collections.sort(srcDataEntryList, new Comparator<Entry<Integer, String>>() {		//通过比较器重写 Comparator，对 srcDataEntryList 进行比较排序
			@Override
			public int compare(Entry<Integer, String> entryA, Entry<Integer, String> entryB) {
				int result = entryA.getValue().compareTo(entryB.getValue());
				
				return preface ? result : -result;
			}
		});
		
		for (Entry<Integer, String> entry : srcDataEntryList) {		//装载已排序的数据
			resultMap.put(entry.getKey(), entry.getValue());
		}
		
		return resultMap;
	}
}

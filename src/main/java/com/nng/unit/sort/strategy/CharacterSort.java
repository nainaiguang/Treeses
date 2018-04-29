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
public class CharacterSort implements SortUtil<Character> {
	
	/**
	 * 根据 自定义比较器 比较 Map 中的 value，从而达到 排序 Map 的目的
	 * @param srcData		数据源
	 * @param preface		序向 （当字符或字符串比较时，大小写字母排序拼音开头第一字母排序）
	 * @return resultMap
	 * 
	 */
	@Override
	public Map<Integer, Character> sort(Map<Integer, Character> srcData, boolean preface) {
		Map<Integer, Character> resultMap = new LinkedHashMap<>();
		
		List<Entry<Integer, Character>> srcDataEntryList = new ArrayList<>(srcData.entrySet());
		
		Collections.sort(srcDataEntryList, new Comparator<Entry<Integer, Character>>() {

			@Override
			public int compare(Entry<Integer, Character> entryA, Entry<Integer, Character> entryB) {
				/*char char_a = Character.toUpperCase(entryA.getValue());		//先把字符全部变成大写
				char char_b = Character.toUpperCase(entryB.getValue());
				
				//如果大写的字符比较都相等时，可能为大小写不同的字符，如 "A" 和 "a"
				if(char_a == char_b) {
					int result = entryA.getValue().charValue() - entryB.getValue().charValue();
					
					return preface ? result : -result;		//preface 为 true 时，大写优先，否则小写优先
				} else {
					return char_a - char_b;
				}*/
				
				//默认 按char值大小排序 true --> 顺序   false --> 倒序
				int ressult = entryA.getValue().compareTo(entryB.getValue());
				return preface ? ressult : -ressult ;
			}
		});
		
		for (Entry<Integer, Character> entry : srcDataEntryList) {
			resultMap.put(entry.getKey(), entry.getValue());
		}
		
		return resultMap;
	}

}

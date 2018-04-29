package com.nng.unit.sort.util.factory;


import com.nng.unit.sort.util.*;

/**
 * 排序工具静态工厂
 * @author Vijay
 *
 */
public class SortUtilFactory {
	public static SortUtil<?> createSortUtil(String dataType) {
		if(dataType.equals("string")) {
			return new StringSortUtil();
		} else if(dataType.equals("integer") || dataType.equals("int")) {
			return new IntegerSortUtil();
		} else if(dataType.equals("char")) {
			return new CharacterSortUtil();
		} else if(dataType.equals("float")) {
			return new FloatSortUtil();
		} else if(dataType.equals("double")) {
			return new DoubleSortUtil();
		} else {
			return null;
		}
	}
}

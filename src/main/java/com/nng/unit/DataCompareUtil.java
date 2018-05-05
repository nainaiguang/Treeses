package com.nng.unit;

/**
 *数据内容比较工具
 * 
 * @author 45116
 *
 */
public class DataCompareUtil {
	public static boolean isEquals(Object a, Object b) {
		// 判断是否为空
		if (a == null || b == null) {
			if (a == null && b == null)
				return true;
			else
				return false;
		}
		// 获取本组数据类型(a与b数据类型相同)
		Class<? extends Object> type = a.getClass();
		String typename = type.getSimpleName();
		// 判断数据类型分别进行比较
		if ("Integer".equals(typename) || "Float".equals(typename) || "Double".equals(typename)
				|| "Long".equals(typename)) {
			Double aDouble = Double.parseDouble(a.toString());
			Double bDouble = Double.parseDouble(b.toString());
			if (aDouble.compareTo(bDouble) == 0) {
				return true;
			} else {
				return false;
			}
		} else if ("Character".equals(typename) || "String".equals(typename)) {
			String aString = a.toString();
			String bString = b.toString();
			if (aString.equals(bString)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
}

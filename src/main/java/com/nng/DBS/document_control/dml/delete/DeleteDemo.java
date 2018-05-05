package com.nng.DBS.document_control.dml.delete;

import com.nng.DBS.document_control.dql.select.temporarytype.columnsType;
import com.nng.unit.DataCompareUtil;

import java.util.ArrayList;
import java.util.List;

public class DeleteDemo {
	/**
	 * 从A列表中去除掉与B列表相同的部分
	 * 
	 * @param A
	 * @param B
	 * @return
	 */
	public List<columnsType> removeDcate(List<columnsType> A, List<columnsType> B) {
		boolean isDel=false;//标记是否删除，删除的不添加到结果，不删除的添加到结果。
		List<columnsType> result = new ArrayList<>();
		for (columnsType cta : A) {
			for (columnsType ctb : B) {
				//遇到有相同的，则标记为删除,并跳出循环
				if(columnsTypeCompare(cta,ctb)) {
					isDel=true;
					break;
				}
			}
			//标记为不删除的，添加到结果
			if (!isDel)
				result.add(cta);
			//恢复标记
			isDel=false;
		}
		return result;
	}
	
	/**
	 * 判断a,b是否相同
	 * 
	 * @param a
	 * @param b
	 * @return boolean
	 */
	private boolean columnsTypeCompare(columnsType a, columnsType b) {
		List<Object> aList = a.getItem();
		List<Object> bList = b.getItem();
		for (int i=0;i<aList.size();i++) {
			if(!DataCompareUtil.isEquals(aList.get(i), bList.get(i)))
				return false;
		}
		return true;
	}
}

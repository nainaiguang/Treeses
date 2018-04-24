package com.nng.DBS.document_control.dql.select.temporarytype;

import com.nng.DBS.dictionary.type.Tabletype;
import com.nng.DBS.jurisdiction.Jurisdiction;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.SelectItem;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 结果表，也可以做临时表使用，也可以做输出表输出
 */
@Getter
@Setter
public class Table_result extends Tabletype{

    //每一列的内容，即最后输出的结果的列项是什么，若如果是聚合选择项，等的一些函数的调用，明天考虑
    List<SelectItem> items;
    List<String> items_attribute;

    //最终的输出结果，全部转化为
    List<columnsType> column_content=new ArrayList<>();

    public Table_result(String tablename)
    {
        setTable_name(tablename);
        setJurisdiction(Jurisdiction.READ_WRITE);
        //设置临时表的地址
        setAddress("SYSTEM\\DATA\\TEMPORARY\\TABLE\\"+tablename+".txt");
    }

    /**
     * 设置结果列
     * @param items
     */
    public void set_items(List<SelectItem> items)
    {
            this.items=items;
    }

    /**
     * 为结果列添加属性
     * @param attribute
     */
    public void set_itemsAttribute(List<String> attribute)
    {
        this.items_attribute=attribute;
    }

    /**
     * 添加查询结果项
     * @param item
     */
    public void add_item(columnsType item)
    {
        column_content.add(item);
    }




}

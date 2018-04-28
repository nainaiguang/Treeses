package com.nng.lexical_analysis.analysis.mean_analyzer.statement.dql.select;

import com.google.common.base.Preconditions;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.OrderItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.limit.Limit;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.AggregationSelectItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem.SelectItem;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dql.DQLStatement;
import com.nng.lexical_analysis.contact.controlType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Select SQL语句对象.
 */
@Getter
@Setter
@ToString(callSuper = true)
public final class SelectStatement extends DQLStatement{
    /**
     * 是否行 DISTINCT / DISTINCTROW / UNION
     */
    private boolean distinct;
    /**
     * 是否查询所有字段，即 SELECT *
     * 单独加了这个字段的标志原因是，一些业务地方会判断是否需要的字段已经查询，例如 GROUP BY / ORDER BY
     */
    private boolean containStar;
    /**
     * TODO 待研究，表示有子查询
     */
    private boolean containSubQuery;
    /**
     * 最后一个查询项下一个 Token 的开始位置
     *
     * @see #items
     */
    private int selectListLastPosition;
    /**
     * 最后一个分组项下一个 Token 的开始位置
     */
    private int groupByLastPosition;
    /**
     * 查询项
     */
    private final List<SelectItem> items = new LinkedList<>();
    /**
     * 分组项
     */
    private final List<OrderItem> groupByItems = new LinkedList<>();
    /**
     * 排序项
     */
    private final List<OrderItem> orderByItems = new LinkedList<>();
    /**
     * 分页
     */
    private Limit limit;


    /**
     * 获取聚合选择项集合.
     *
     * @return 聚合选择项
     */
    public List<AggregationSelectItem> getAggregationSelectItems() {
        List<AggregationSelectItem> result = new LinkedList<>();
        for (SelectItem each : items) {
            if (each instanceof AggregationSelectItem) {
                AggregationSelectItem aggregationSelectItem = (AggregationSelectItem) each;
                result.add(aggregationSelectItem);
                result.addAll(aggregationSelectItem.getDerivedAggregationSelectItems());
            }
        }
        return result;
    }

    /**
     * 判断是否分组和排序项一致.
     *
     * @return 是否分组和排序项一致
     */
    public boolean isSameGroupByAndOrderByItems() {
        return !getGroupByItems().isEmpty() && getGroupByItems().equals(getOrderByItems());
    }

    /**
     * 为选择项设置索引.
     *
     * @param columnLabelIndexMap 列标签索引字典
     */
    public void setIndexForItems(final Map<String, Integer> columnLabelIndexMap) {
        setIndexForAggregationItem(columnLabelIndexMap);
        setIndexForOrderItem(columnLabelIndexMap, orderByItems);
        setIndexForOrderItem(columnLabelIndexMap, groupByItems);
    }

    private void setIndexForAggregationItem(final Map<String, Integer> columnLabelIndexMap) {
        for (AggregationSelectItem each : getAggregationSelectItems()) {
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel()), String.format("Can't find index: %s, please add alias for aggregate selections", each));
            each.setIndex(columnLabelIndexMap.get(each.getColumnLabel()));
            for (AggregationSelectItem derived : each.getDerivedAggregationSelectItems()) {
                Preconditions.checkState(columnLabelIndexMap.containsKey(derived.getColumnLabel()), String.format("Can't find index: %s", derived));
                derived.setIndex(columnLabelIndexMap.get(derived.getColumnLabel()));
            }
        }
    }

    private void setIndexForOrderItem(final Map<String, Integer> columnLabelIndexMap, final List<OrderItem> orderItems) {
        for (OrderItem each : orderItems) {
            if (-1 != each.getIndex()) {
                continue;
            }
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel()), String.format("Can't find index: %s", each));
            if (columnLabelIndexMap.containsKey(each.getColumnLabel())) {
                each.setIndex(columnLabelIndexMap.get(each.getColumnLabel()));
            }
        }
    }
}

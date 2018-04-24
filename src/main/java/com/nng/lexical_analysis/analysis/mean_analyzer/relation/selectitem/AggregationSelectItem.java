package com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem;

import com.google.common.base.Optional;
import com.nng.lexical_analysis.contact.AggregationType;
import com.nng.unit.SQLUtil;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚合选择项.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class AggregationSelectItem implements SelectItem {

    /**
     * 聚合类型
     */
    private final AggregationType type;
    /**
     * 聚合内部表达式
     */
    private final String innerExpression;
    /**
     * 别名
     */
    private final Optional<String> alias;
    /**
     * 推导字段
     * 目前只有 AVG 聚合选择项需要用到：AVG 改写成 SUM + COUNT 查询，内存计算出 AVG 结果。
     */
    private final List<AggregationSelectItem> derivedAggregationSelectItems = new ArrayList<>(2);

    @Setter
    private int index = -1;

    @Override
    public String getExpression() {
        return SQLUtil.getExactlyValue(type.name() + innerExpression);
    }

    /**
     * 获取列标签.
     *
     * @return 列标签
     */
    public String getColumnLabel() {
        return alias.isPresent() ? alias.get() : getExpression();
    }
}

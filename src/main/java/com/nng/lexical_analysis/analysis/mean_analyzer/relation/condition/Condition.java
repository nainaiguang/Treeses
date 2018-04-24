package com.nng.lexical_analysis.analysis.mean_analyzer.relation.condition;

import com.nng.lexical_analysis.analysis.word_analyzer.token.Symbol;
import com.nng.lexical_analysis.api.ShardingValue;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.Expression;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.NumberExpression;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.PlaceholderExpression;
import com.nng.lexical_analysis.analysis.mean_analyzer.expression.TextExpression;
import com.nng.lexical_analysis.contact.ShardingOperator;
import com.nng.exception.TreesesException;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 条件对象.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public final class Condition {

    /**
     * 字段
     */
    @Getter
    private final Column column;

    @Getter
    @Setter
    private  Symbol symbol=Symbol.TILDE;//当不是in，between 的时候才有用，当是的时候为“~”
    
    @Getter
    private final ShardingOperator operator;
    
    private final Map<Integer, Comparable<?>> positionValueMap = new LinkedHashMap<>();

    @Getter
    private final Map<Integer, Integer> positionIndexMap = new LinkedHashMap<>();
    
    public Condition(final Column column, final Expression sqlExpression) {
        this(column, ShardingOperator.EQUAL);
        init(sqlExpression, 0);
    }
    
    public Condition(final Column column, final Expression beginSQLExpression, final Expression endSQLExpression) {
        this(column, ShardingOperator.BETWEEN);
        init(beginSQLExpression, 0);
        init(endSQLExpression, 1);
    }
    
    public Condition(final Column column, final List<Expression> sqlExpressions) {
        this(column, ShardingOperator.IN);
        int count = 0;
        for (Expression each : sqlExpressions) {
            init(each, count);
            count++;
        }
    }
    
    private void init(final Expression sqlExpression, final int position) {
        if (sqlExpression instanceof PlaceholderExpression) {
            positionIndexMap.put(position, ((PlaceholderExpression) sqlExpression).getIndex());
        } else if (sqlExpression instanceof TextExpression) {
            positionValueMap.put(position, ((TextExpression) sqlExpression).getText());
        } else if (sqlExpression instanceof NumberExpression) {
            positionValueMap.put(position, (Comparable) ((NumberExpression) sqlExpression).getNumber());
        }
    }
    
    /**
     * 将条件对象转换为分片值.
     *
     * @param parameters 参数有什么用，占位符时才有用
     * @return 分片值
     */
    public ShardingValue<?> getShardingValue(final List<Object> parameters) {
        List<Comparable<?>> conditionValues = getValues(parameters);
        switch (operator) {
            case EQUAL:
                return new ShardingValue<Comparable<?>>(column.getTableName(), column.getName(), conditionValues.get(0));
            case IN:
                return new ShardingValue<>(column.getTableName(), column.getName(), conditionValues);
            case BETWEEN:
                return new ShardingValue<>(column.getTableName(), column.getName(), Range.range(conditionValues.get(0), BoundType.CLOSED, conditionValues.get(1), BoundType.CLOSED));
            default:
                throw new UnsupportedOperationException(operator.getExpression());
        }
    }
    
    private List<Comparable<?>> getValues(final List<Object> parameters) {
        List<Comparable<?>> result = new LinkedList<>(positionValueMap.values());
        for (Entry<Integer, Integer> entry : positionIndexMap.entrySet()) {
            Object parameter = parameters.get(entry.getValue());
            if (!(parameter instanceof Comparable<?>)) {
                throw new TreesesException("Parameter `%s` should extends Comparable for sharding value.", parameter);
            }
            if (entry.getKey() < result.size()) {
                result.add(entry.getKey(), (Comparable<?>) parameter);
            } else {
                result.add((Comparable<?>) parameter);
            }
        }
        return result;
    }
}

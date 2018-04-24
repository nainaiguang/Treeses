package com.nng.lexical_analysis.analysis.mean_analyzer.relation.limit;

import com.nng.lexical_analysis.analysis.mean_analyzer.exception.SQLParsingException;
import com.nng.unit.NumberUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
/**
 * 暂不考虑重写
 */

/**
 * 分页对象.
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public final class Limit {

    /**
     * 是否重写rowCount
     * TODO 待补充：预计和内存分页合并有关
     */
    private final boolean rowCountRewriteFlag;
    /**
     * offset
     */
    private LimitValue offset;
    /**
     * row
     */
    private LimitValue rowCount;

    /**
     * 获取分页偏移量.
     *
     * @return 分页偏移量
     */
    public int getOffsetValue() {
        return null != offset ? offset.getValue() : 0;
    }

    /**
     * 获取分页行数.
     *
     * @return 分页行数
     */
    public int getRowCountValue() {
        return null != rowCount ? rowCount.getValue() : -1;
    }

    /**
     * 填充改写分页参数.
     *
     * @param parameters 参数
     * @param isRewrite 是否重写参数
     * @param isFetchAll 是否获取所有数据
     */
    public void processParameters(final List<Object> parameters, final boolean isRewrite, final boolean isFetchAll) {
        fill(parameters);
        if (isRewrite) {
            rewrite(parameters, isFetchAll);
        }
    }

    /**
     * 将占位符参数里是分页的参数赋值给 offset 、rowCount
     * 赋值的前提条件是 offset、rowCount 是 占位符
     *
     * @param parameters 占位符参数
     */
    private void fill(final List<Object> parameters) {
        int offset = 0;
        if (null != this.offset) {
            offset = -1 == this.offset.getIndex() ? getOffsetValue() : NumberUtil.roundHalfUp(parameters.get(this.offset.getIndex()));
            this.offset.setValue(offset);
        }
        int rowCount = 0;
        if (null != this.rowCount) {
            rowCount = -1 == this.rowCount.getIndex() ? getRowCountValue() : NumberUtil.roundHalfUp(parameters.get(this.rowCount.getIndex()));
            this.rowCount.setValue(rowCount);
        }
        if (offset < 0 || rowCount < 0) {
            throw new SQLParsingException("LIMIT offset and row count can not be a negative value.");
        }
    }

    /**
     * 重写分页条件对应的参数
     *
     * @param parameters 参数
     * @param isFetchAll 是否拉取所有
     */
    private void rewrite(final List<Object> parameters, final boolean isFetchAll) {
        int rewriteOffset = 0;
        int rewriteRowCount;
        // 重写
        if (isFetchAll) {
            rewriteRowCount = Integer.MAX_VALUE;
        } else if (rowCountRewriteFlag) {
            rewriteRowCount = null == rowCount ? -1 : getOffsetValue() + rowCount.getValue();
        } else {
            rewriteRowCount = rowCount.getValue();
        }
        // 参数设置
        if (null != offset && offset.getIndex() > -1) {
            parameters.set(offset.getIndex(), rewriteOffset);
        }
        if (null != rowCount && rowCount.getIndex() > -1) {
            parameters.set(rowCount.getIndex(), rewriteRowCount);
        }
    }
}

package com.nng.lexical_analysis.analysis.mean_analyzer.relation;

import com.google.common.base.Optional;
import com.nng.lexical_analysis.contact.OrderType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 排序项.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class OrderItem {

    /**
     * 所属表别名
     */
    private final Optional<String> owner;
    /**
     * 排序字段
     */
    private final Optional<String> name;
    /**
     * 排序类型
     */
    private final OrderType type;
    /**
     * 按照第几个查询字段排序
     * ORDER BY 数字 的 数字代表的是第几个字段
     */
    @Setter
    private int index = -1;
    /**
     * 字段在查询项({@link com.nng.lexical_analysis.analysis.mean_analyzer.relation.selectitem} 的别名
     */
    @Setter
    private Optional<String> alias;
    
    public OrderItem(final String name, final OrderType type, final Optional<String> alias) {
        this.owner = Optional.absent();
        this.name = Optional.of(name);
        this.type = type;
        this.alias = alias;
    }
    
    public OrderItem(final String owner, final String name, final OrderType type, final Optional<String> alias) {
        this.owner = Optional.of(owner);
        this.name = Optional.of(name);
        this.type = type;
        this.alias = alias;
    }
    
    public OrderItem(final int index, final OrderType type) {
        owner = Optional.absent();
        name = Optional.absent();
        this.index = index;
        this.type = type;
        alias = Optional.absent();
    }
    
    /**
     * 获取列标签.
     *
     * @return 列标签
     */
    public String getColumnLabel() {
        return alias.isPresent() ? alias.get() : name.orNull();
    }
    
    /**
     * 获取列全名.
     *
     * @return 列全名
     */
    public Optional<String> getQualifiedName() {
        if (!name.isPresent()) {
            return Optional.absent();
        }
        return owner.isPresent() ? Optional.of(owner.get() + "." + name.get()) : name;
    }
}

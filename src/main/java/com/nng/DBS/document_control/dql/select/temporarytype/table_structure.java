package com.nng.DBS.document_control.dql.select.temporarytype;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 表结构
 */
@Getter
@AllArgsConstructor
public class table_structure {
    //表名
    private String table_name;
    //列名
    private List<String> columns_name;
    //列类型
    private List<String> type;
}

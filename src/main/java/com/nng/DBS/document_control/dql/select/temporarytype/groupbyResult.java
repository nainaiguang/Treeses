package com.nng.DBS.document_control.dql.select.temporarytype;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class groupbyResult {
    private String table_name;//分组属于哪一个表的
    private String column_name;//列名
    private int place;//分组再笛卡尔中的位置的位置
    private List<Object> groupbyResult;//Groupby分组结果
}

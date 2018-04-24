package com.nng.DBS.document_control;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class tableBean {
    /**
     * 表
     */
    private  String name;
    /**
     * 别名
     */
    private  String alias;
    /**
     *列名
     **/
    private List<String> columns_name;
    /**
     * 数据
     */

}

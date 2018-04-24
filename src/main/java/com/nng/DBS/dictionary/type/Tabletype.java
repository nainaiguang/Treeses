package com.nng.DBS.dictionary.type;

/**
 * 表单数据类型
 */

import com.nng.DBS.jurisdiction.Jurisdiction;
import lombok.*;

import java.util.List;


@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Tabletype {
    /**
     * 表名
     */
    private  String table_name;
    /**
     * 拥有者
     */
    private  String owner="System";

    /**
     * 权限,
     * 默认读写完全控制
     */
    private  Jurisdiction jurisdiction=Jurisdiction.READ_WRITE;
    /**
     * 文件保存地址
     */
    private  String address;
    /**
     * 别名
     */
    private  String alias="none";
    /**
     * 列名
     */
    List<String> column;
    /**
     * 列的类型
     */
    List<String> attribute;



}
package com.nng.DBS.document_control.dql.select.temporarytype;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 每一行的各个元素的，可以是不用的类型
 */
@NoArgsConstructor
@Setter
@Getter
public class columnsType {
   private List<Object> item=new ArrayList<>();

}

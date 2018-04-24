package com.nng.lexical_analysis.structure;

import java.util.*;

public class PWQueue {
   private List<DString> lex;
   public PWQueue() {
       lex = new ArrayList<DString>();
   }
   public void add_relation(String keyword,String A)
   {
       /**
        * 往动态数组里添加关系对
        * keyword一定要在KEYWORD中得到
        */
       DString one=new DString(keyword,A);
       lex.add(one);
   }
    public DString get_DString(int i)
    {
        return lex.get(i);
    }

}

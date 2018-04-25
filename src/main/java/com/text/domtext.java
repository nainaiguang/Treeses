package com.text;

import com.nng.DBS.dictionary.domParser.tableparser.TablerParser;
import com.nng.DBS.dictionary.type.Tabletype;
import com.nng.DBS.jurisdiction.Jurisdiction;
import com.nng.unit.readJson;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class domtext {
    @Setter
    @Getter
    String address;
    DocumentBuilderFactory dbf;
    DocumentBuilder dBuilder;
    Document document;
    public void DomFactory() throws Exception
    {
        /*创建DOM 解析器的工厂*/
        dbf=DocumentBuilderFactory.newInstance();
        /*得到 DOM 解析器对象*/
        dBuilder=dbf.newDocumentBuilder();
        /*得到代表整个文档的 Document 对象*/
        document=dBuilder.parse(address);
    }

    public Element getRootElement()
    {
        return document.getDocumentElement();
    }
    public Node  get_TextContent (String table_name,String temp)throws Exception{
        Element b=getRootElement();
        NodeList tables_nodeList=b.getChildNodes();
        Node final_temp_values=null;
        for(int i=0;i<tables_nodeList.getLength();i++) {
            Node tablenode = tables_nodeList.item(i);
            if (tablenode.getNodeType() == Node.ELEMENT_NODE) {
                //System.out.println(tablenode.getTextContent());
                NodeList table_item_nodeList=tablenode.getChildNodes();
                boolean temp_values=false;
                for(int j=0;j<table_item_nodeList.getLength();j++)
                {
                    Node table_item_node = table_item_nodeList.item(j);
                    if (table_item_node.getNodeType() == Node.ELEMENT_NODE){
                        Element item=(Element)table_item_node;
                        if(item.getTagName().equals("table_name")&&item.getTextContent().equals(table_name))
                        {
                            temp_values=true;
                        }
                        if(item.getTagName().equals(temp)&&temp_values==true)
                        {
                            final_temp_values=item;
                            //System.out.println(temp_values+"p"+i+"p"+j);
                        }

                    }
                }
            }
        }
        return final_temp_values;
    }
    public static void main(String[] args)throws Exception
    {
//        TablerParser a=new TablerParser();
//       //a.setAddress("E:\\LearningProject\\idea_project\\maven_workplace\\SYSTEM\\DICTIONARY\\Tabletype.xml");
//       //a.DomFactory();
//        Tabletype p=new Tabletype();
//        p.setAddress("sss");
//        p.setJurisdiction(Jurisdiction.READ_WRITE);
//        p.setTable_name("asdwqa");
//        p.setOwner("drfwq3er");
//
//        List<String> o=new ArrayList<>();
//        o.add("dawedawdxa");
//        o.add("sdawecsdfsa");
//        o.add("awsdfsa");
//        o.add("aa");
//        List<String> y=new ArrayList<>();
//        y.add("dawedawdxa");
//        y.add("sdawecsdfsa");
//        y.add("awsdfsa");
//        y.add("aa");
//        a.add_column("asdwqa",o,y);
    // String o=a.get_column_type("asdwqa","sss");
   //  System.out.println(o);

//        p.setColumn(o);
     //   a.add_table(p);
      //  a.delete_column("asdwqa","aas");
       // a.add_column("asdwqa",o);
        //a.alter_owner("asdwqa","sdwa");
      // System.out.println(a.get_tables().get(1));
        //a.drop_table("ffr");

        /**
         * 注意读取文件中的双引号与引号
         */
        //System.out.println("[1,'s',\"sss\"]");

    List<String> a=new ArrayList<>();
    List<String> b=new ArrayList<>();
    a.add("nihao");
        a.add("nihao");
        a.add("nihao");
        a.add("nihao");
        a.add("nihao");
        b.add("dajia");
        b.add("dajia");
        b.add("dajia");
        b.add("dajia");
        b.add("dajia");
        a.addAll(b);

        Object A=55;
        double B=Double.parseDouble(A.toString());
        System.out.print(B);


    }
}

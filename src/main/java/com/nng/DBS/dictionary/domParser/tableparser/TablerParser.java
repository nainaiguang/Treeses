package com.nng.DBS.dictionary.domParser.tableparser;

import com.nng.DBS.dictionary.domParser.Domparser;
import com.nng.DBS.dictionary.exception.SQLDictionaryException;
import com.nng.DBS.dictionary.type.Tabletype;

import com.nng.DBS.jurisdiction.Jurisdiction;
import com.nng.lexical_analysis.analysis.mean_analyzer.relation.table.Table;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class TablerParser extends Domparser{

    private static TablerParser tablerParser;
    private TablerParser() throws Exception {
        setAddress("SYSTEM\\DICTIONARY\\Tabletype.xml");
        DomFactory();
    }

    public static TablerParser getInstance() throws Exception
    {
        if(tablerParser==null)
            tablerParser=new TablerParser();
        return tablerParser;
    }

    /**
     * 获取某个表的拥有着
     * @param table_name
     * @return
     * @throws Exception
     */
    public String get_owner(String table_name) throws Exception {
        if(!existTable(table_name))
        {throw new SQLDictionaryException(table_name);}
        return get_TextContent(table_name,"owner").getTextContent();
    }

    /**
     * 根据表名获取表名，作用是判定表是否存在
     * @param table_name
     * @return
     */
    public String get_tablename(String table_name)
    {
        if(!existTable(table_name))
        {throw new SQLDictionaryException(table_name);}
        return table_name;
    }

    /**
     * 获取某个表的读取权限
     * @param table_name
     * @return
     * @throws Exception
     */
    public String get_jurisdiction(String table_name)throws Exception {
        if(!existTable(table_name))
        {throw new SQLDictionaryException(table_name);}
        return get_TextContent(table_name,"jurisdiction").getTextContent();
    }



    /**
     * 获取某个表的存放地址
     * @param table_name
     * @return
     * @throws Exception
     */
    public String get_address(String table_name)throws Exception {
        if(!existTable(table_name))
        {throw new SQLDictionaryException(table_name);}
        return get_TextContent(table_name,"address").getTextContent();
    }

    /**
     * 获取某个表的别名
     * @param table_name
     * @return
     * @throws Exception
     */
    public String get_alias(String table_name) throws Exception {
        if(!existTable(table_name))
        {throw new SQLDictionaryException(table_name);}
        return get_TextContent(table_name,"alias").getTextContent();
    }

    /**
     * 获取某个表的所有行
     * @param table_name
     * @return
     * @throws Exception
     */
    public List<String> get_column(String table_name) throws Exception {
        if(!existTable(table_name))
        {throw new SQLDictionaryException(table_name);}
        List<String> co=new ArrayList<>();
        Node colums=get_TextContent(table_name,"columns");
        NodeList colum=colums.getChildNodes();
        for(int i=0;i<colum.getLength();i++)
        {
            Node item=colum.item(i);
            if(item.getNodeType()==Node.ELEMENT_NODE)
            {
                Element items=(Element)item;
                if(items.getTagName().equals("column"))
                {
                co.add(items.getAttribute("name"));
                }
            }
        }
        if(co.isEmpty())
        {return null;}
        else
        {return co;}
    }

    /**
     * 获取某表某行的类型
     * @param table_name
     * @param column_name
     * @return
     * @throws Exception
     */
    public String get_column_type(String table_name,String column_name)throws Exception
    {
        if(!existTable(table_name))
        {throw new SQLDictionaryException(table_name);}
        String co=null;
        Node colums=get_TextContent(table_name,"columns");
        NodeList colum=colums.getChildNodes();
        for(int i=0;i<colum.getLength();i++)
        {
            Node item=colum.item(i);
            if(item.getNodeType()==Node.ELEMENT_NODE)
            {
                Element items=(Element)item;
                if(items.getTagName().equals("column"))
                {
                    if(items.getAttribute("name").equals(column_name))
                        co=items.getAttribute("type");
                }
            }
        }
        return co;
    }

    /**
     * 返回所有列的数据类型
     * @param table_name
     * @return
     * @throws Exception
     */
    public List<String> get_columns_type(String table_name) throws Exception {
        List<String> types=new ArrayList<>();
        List<String> columns=get_column(table_name);
        for(String column:columns)
        {
            types.add(get_column_type(table_name,column));
        }
        return types;
    }
    /**
     * 获取表名
     * @return
     */
    public List<String> get_tables(){
        List<String> tables=new ArrayList<>();
        Element b=getRootElement();
        NodeList tables_nodeList=b.getChildNodes();
        for(int i=0;i<tables_nodeList.getLength();i++) {
            Node tablenode = tables_nodeList.item(i);
            if (tablenode.getNodeType() == Node.ELEMENT_NODE) {
                //System.out.println(tablenode.getTextContent());
                NodeList table_item_nodeList=tablenode.getChildNodes();
                for(int j=0;j<table_item_nodeList.getLength();j++)
                {
                    Node table_item_node = table_item_nodeList.item(j);
                    if (table_item_node.getNodeType() == Node.ELEMENT_NODE){
                        Element item=(Element)table_item_node;
                        if(item.getTagName().equals("table_name"))
                        {
                            tables.add(item.getTextContent());
                        }
                    }
                }
            }
        }
        return tables;
    }

    /**
     * 增加表
     * @param table
     * @throws TransformerException
     */
    public void add_table(Tabletype table) throws TransformerException {

        if(existTable(table.getTable_name()))
        {
            throw new SQLDictionaryException(table.getTable_name(),1);
        }

        //添加子元素
        Element node=getDocument().createElement("table");

        Element table_name=getDocument().createElement("table_name");
        table_name.setTextContent(table.getTable_name());

        Element owner=getDocument().createElement("owner");
        owner.setTextContent(table.getOwner());

        Element jurisdiction=getDocument().createElement("jurisdiction");
        jurisdiction.setTextContent(table.getJurisdiction().toString());

        Element addresss=getDocument().createElement("address");
        addresss.setTextContent(table.getAddress());

        Element alias=getDocument().createElement("alias");
        alias.setTextContent(table.getAlias());

        Element columns=getDocument().createElement("columns");

        for(int i=0;i<table.getColumn().size();i++)
        {
            Element column=getDocument().createElement("column");
            column.setAttribute("name",table.getColumn().get(i));
            column.setAttribute("type",table.getAttribute().get(i));
            columns.appendChild(column);
        }

        node.appendChild(table_name);
        node.appendChild(owner);
        node.appendChild(jurisdiction);
        node.appendChild(addresss);
        node.appendChild(alias);
        node.appendChild(columns);

        Element root=getRootElement();
        root.appendChild(node);

        //重新提交表单
       // DOMSource domsource=new DOMSource(getDocument());
        TransformerFactory tff=TransformerFactory.newInstance();
        Transformer tf=tff.newTransformer();
        tf.transform(new DOMSource(getDocument()), new StreamResult(getAddress()));
    }

    /**
     * 为某个表增加列，一次增加多行
     * @param table_name
     * @param columns
     * @throws Exception
     */
    public void add_column(String table_name,List<String>columns,List<String> types) throws Exception {
        //如果表不在
        if(!existTable(table_name))
        {
            throw new SQLDictionaryException(table_name);
        }
        //去除重复列项
        existcolumn(table_name,columns);

        Node columnss =get_TextContent(table_name,"columns");

        for(int i=0;i<columns.size();i++)
        {
            Element column=getDocument().createElement("column");
            column.setAttribute("name",columns.get(i));
            column.setAttribute("type",types.get(i));
            columnss.appendChild(column);
        }

        //重新提交表单
        //DOMSource domsource=new DOMSource(getDocument());
        TransformerFactory tff=TransformerFactory.newInstance();
        Transformer tf=tff.newTransformer();
        tf.transform(new DOMSource(getDocument()), new StreamResult(getAddress()));
    }

    /**
     * 更改表的拥有着
     * @param table_name
     * @param owner
     * @throws Exception
     */
    public void alter_owner(String table_name,String owner) throws Exception {
        if(!existTable(table_name))
        {throw new SQLDictionaryException(table_name);}
        alter(table_name,owner,"owner");
        //重新提交表单
        //DOMSource domsource=new DOMSource(getDocument());
        TransformerFactory tff=TransformerFactory.newInstance();
        Transformer tf=tff.newTransformer();
        tf.transform(new DOMSource(getDocument()), new StreamResult(getAddress()));
    }

    /**
     * 更改权限
     * @param table_name
     * @param jurisdiction
     * @throws Exception
     */
    public void alter_jurisdiction(String table_name,Jurisdiction jurisdiction) throws Exception {
        if(!existTable(table_name))
        {throw new SQLDictionaryException(table_name);}
        alter(table_name,jurisdiction.toString(),"jurisdiction");
        //重新提交表单
        //DOMSource domsource=new DOMSource(getDocument());
        TransformerFactory tff=TransformerFactory.newInstance();
        Transformer tf=tff.newTransformer();
        tf.transform(new DOMSource(getDocument()), new StreamResult(getAddress()));
    }

    /**
     * 更改存放位置
     * @param table_name
     * @param address
     * @throws Exception
     */
    public void alter_address(String table_name,String address) throws Exception {
        if(!existTable(table_name))
        {throw new SQLDictionaryException(table_name);}
        alter(table_name,address,"address");
        //重新提交表单
        //DOMSource domsource=new DOMSource(getDocument());
        TransformerFactory tff=TransformerFactory.newInstance();
        Transformer tf=tff.newTransformer();
        tf.transform(new DOMSource(getDocument()), new StreamResult(getAddress()));
    }

    /**
     * 更改表名
     * @param table_name
     * @param new_table_name
     * @throws Exception
     */
    public void alter_table_name(String table_name,String new_table_name) throws Exception {
        if(!existTable(table_name))
        {throw new SQLDictionaryException(table_name);}
        alter(table_name,new_table_name,"table_name");
        //重新提交表单
        //DOMSource domsource=new DOMSource(getDocument());
        TransformerFactory tff=TransformerFactory.newInstance();
        Transformer tf=tff.newTransformer();
        tf.transform(new DOMSource(getDocument()), new StreamResult(getAddress()));
    }

    /**
     * 更改别名
     * @param table_name
     * @param alias
     * @throws Exception
     */
    public void alter_alias(String table_name,String alias) throws Exception {
        if(!existTable(table_name))
        {throw new SQLDictionaryException(table_name);}
        alter(table_name,alias,"alias");
        //重新提交表单
        //DOMSource domsource=new DOMSource(getDocument());
        TransformerFactory tff=TransformerFactory.newInstance();
        Transformer tf=tff.newTransformer();
        tf.transform(new DOMSource(getDocument()), new StreamResult(getAddress()));
    }

    /**
     * 删除某张表
     * @param table_name
     * @throws TransformerException
     */
    public void drop_table(String table_name) throws TransformerException {
        Element root=getRootElement();
        NodeList tableslist=root.getChildNodes();
        for(int i=0;i<tableslist.getLength();i++)
        {
            Node table=tableslist.item(i);
            if (table.getNodeType() == Node.ELEMENT_NODE) {
                NodeList tablelist = table.getChildNodes();
                for (int j = 0; j < tablelist.getLength(); j++) {
                    if(tablelist.item(j).getNodeType()==Node.ELEMENT_NODE) {
                        Element t = (Element) tablelist.item(j);
                        if (t.getTagName().equals("table_name") && tablelist.item(j).getTextContent().equals(table_name)) {
                            root.removeChild(tableslist.item(i));
                            //重新提交表单
                            //DOMSource domsource=new DOMSource(getDocument());
                            TransformerFactory tff = TransformerFactory.newInstance();
                            Transformer tf = tff.newTransformer();
                            tf.transform(new DOMSource(getDocument()), new StreamResult(getAddress()));
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * 删除某一行，如果要删除多行请一行一行来
     * @param table_name
     * @param column_name
     * @throws Exception
     */
    public void delete_column(String table_name,String column_name) throws Exception {
        if(!existTable(table_name))
        {throw new SQLDictionaryException(table_name);}
        Node columns=get_TextContent(table_name,"columns");
        NodeList columnlist=columns.getChildNodes();
        for(int i=0;i<columnlist.getLength();i++) {
            Node temp = columnlist.item(i);
            if (temp.getNodeType() == Node.ELEMENT_NODE) {
                if (((Element)temp).getAttribute("name").equals(column_name)) {
                    columns.removeChild(columnlist.item(i));
                    //重新提交表单
                    //DOMSource domsource=new DOMSource(getDocument());
                    TransformerFactory tff = TransformerFactory.newInstance();
                    Transformer tf = tff.newTransformer();
                    tf.transform(new DOMSource(getDocument()), new StreamResult(getAddress()));
                    return;
                }
            }
        }
    }

    /**
     * 得到某一标签的在表名为某一个情况下的某一个值
     * @param table_name
     * @param temp
     * @return
     * @throws Exception
     */
    public Node get_TextContent (String table_name,String temp)throws Exception{
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
                        if(item.getTagName().equals(temp)&&temp_values)
                        {
                            final_temp_values=item;
                        }
                    }
                }
            }
        }
        return final_temp_values;
    }

    /**
     * 判断表是否存在
     * @param table_name
     * @return
     */
    public boolean existTable(String table_name)
    {
        List<String> a=get_tables();
        //判断是否重复表
        for(int i=0;i<a.size();i++)
        {
            if(a.get(i).equals(table_name))
            {
               return true;
            }
        }
        return false;
    }

    /**
     * 去除重复的列项，即使全部重复也会返回一个空的
     * @param table_name
     * @param columns
     * @return
     * @throws Exception
     */
    public void existcolumn(String table_name,List<String>columns) throws Exception {
        //如果表不在
        if(!existTable(table_name))
        {
            throw new SQLDictionaryException(table_name);
        }
        /**
         * 暂留一个算法，判断一个list的数组在另外一个list里有没有，并抛出没有的数
         */
        Node columns_node=get_TextContent(table_name,"columns");
        NodeList columnList=columns_node.getChildNodes();
        for(int i=0;i<columns.size();i++)
        {
                boolean temp = true;
                for (int j = 0; j <columnList.getLength() ; j++) {
                    Node item=columnList.item(j);
                    if(item.getNodeType()==Node.ELEMENT_NODE) {
                        Element items = (Element) item;
                        if (items.getAttribute("name").equals(columns.get(i))) {
                            temp = false;
                        }
                    }
                }
                if (!temp) {
                    throw new SQLDictionaryException(columns.get(i),1);
                }

        }

    }



    /**
     * 更改某一项的值
     * @param table_name
     * @param new_owner
     * @param temp
     * @throws Exception
     */
    public void alter(String table_name,String new_owner,String temp) throws Exception {
        Node temps=get_TextContent(table_name,temp);
        temps.setTextContent(new_owner);
    }
}

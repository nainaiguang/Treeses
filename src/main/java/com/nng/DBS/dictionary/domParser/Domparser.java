package com.nng.DBS.dictionary.domParser;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@NoArgsConstructor
public abstract class Domparser {
    @Setter
    @Getter
    String address;

    DocumentBuilderFactory dbf;
    DocumentBuilder dBuilder;
    @Getter
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

}

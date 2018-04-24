package com.nng.HMI_Text;

import com.nng.DBS.document_control.dml.insert.InsertControl;
import com.nng.DBS.document_control.dql.select.selectControl;
import com.nng.lexical_analysis.analysis.SQLparsingEngine;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.SQLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dml.DMLStatement;
import com.nng.lexical_analysis.analysis.mean_analyzer.statement.dql.select.SelectStatement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class text {
    public JPanel panel1;
    private JTextArea textArea1;
    private JButton button1;
    private JPanel jframe;
    private JTextArea textArea2;

    public text()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle bounds = new Rectangle( screenSize );
        panel1.setBounds( bounds );


        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sql=textArea1.getText();

                System.out.println(sql);
               // textArea1.setText("");
                SQLparsingEngine a=new SQLparsingEngine(sql);
               SQLStatement b= a.parse();



              // textArea1.append( b.getTables().getSingleTableName());
                try {
                    selectControl selectControl =new selectControl((SelectStatement) b);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    textArea2.setText(e1.getMessage());
                }

//                try {
//                    InsertControl.getInstance().insert_table((DMLStatement) b);
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
//                try {
//                    createControl.getInstance().create_table((DDLStatement) b);
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
//                try {
//                    doucumentInsert.getInstance().insert_table((InsertStatement) b);
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
//                try {
//                    documentDrop.getInstance().drop_table(b.getTables().getSingleTableName());
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
//                try {
//                    XMLDrop.getInstance().drop_Table((DDLStatement) b);
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }


//                try {
//                    XMLCreate.getInstance().creat_Table((DDLStatement) b);
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
//                try {
//                    documentCreate.getInstance().creat_Table(b.getTables().getSingleTableName());
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }

//                for(int i=0;i<b.getSqlTokens().size();i++)
//                {
//                    if (b.getSqlTokens().get(i) instanceof ItemsToken)
//                    {
//                       System.out.println("ItemsToken");
//                    }
//                    else if(b.getSqlTokens().get(i) instanceof OffsetToken)
//                    {
//                        System.out.println("ItemsToken");
//                    }
//                    else if(b.getSqlTokens().get(i) instanceof OrderByToken)
//                    {
//                        System.out.println("ItemsToken");
//                    }
//                    else if(b.getSqlTokens().get(i) instanceof RowCountToken)
//                    {
//                        System.out.println("ItemsToken");
//                    }
//                    else if(b.getSqlTokens().get(i) instanceof GeneratedKeyToken)
//                    {
//                        System.out.println("ItemsToken");
//                    }
//                    else if(b.getSqlTokens().get(i) instanceof TableToken)
//                    {
//                          System.out.println("Tabletoken项名："+(((TableToken) b.getSqlTokens().get(i)).getOriginalLiterals()));
//                    }
//
//                }
//                Dictionary b=new Dictionary();
//              Lexer a=new Lexer(sql,b);
//              while (!a.isEnd())
//                {
//                    a.nextToken();
//                    Token currentToken=a.getCurrentToken();
//                    textArea1.append("| " + currentToken.getLiterals() + " | "
//                            +  currentToken.getType().getClass().getSimpleName() + " | " + currentToken.getType() + " | "
//                            + currentToken.getEndPosition() + " |"+"\n");
//                }
               // sql= Trim.clear_up(sql);
               // textArea1.setText(sql);
            }
        });
    }
}

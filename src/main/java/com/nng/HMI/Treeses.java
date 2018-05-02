package com.nng.HMI;

import com.nng.DBS.SQLEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Treeses {
    public JPanel jframe;
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JButton runButton;
    private boolean click=false;

    public Treeses(){
            /**
             * 设计样式
             */
            setStyle();

            runButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String sql=textArea1.getText();
                        try {
                            SQLEngine sqlEngine=new SQLEngine();
                            sqlEngine.runSQLEngine(sql);
                            textArea2.setForeground(new Color(255,255,255));
                            textArea2.setText(sqlEngine.getResult());
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            textArea2.setForeground(new Color(171,80,78));
                            textArea2.setText(e1.getMessage());
                        }



                }
            });


             textArea1.addKeyListener(new KeyListener() {
                 @Override
                 public void keyTyped(KeyEvent e) {

                 }

                 @Override
                 public void keyPressed(KeyEvent e) {

                     switch (e.getKeyCode())
                     {
                         case KeyEvent.VK_ENTER:
                             String press=textArea1.getText();
                             char last='s';
                             int place=0;
                             for(int i=press.length()-1;i>=0;i--)
                             {
                                 if(press.charAt(i)!=' '||press.charAt(i)!='\n'||press.charAt(i)!='\t')
                                 {
                                     last=press.charAt(i);
                                     place=press.length()-i;
                                     press=press.substring(0,i);
                                     break;
                                 }
                             }

                             if(last==';')
                             {
                                 String sql = press;
                                 try {
                                     SQLEngine sqlEngine=new SQLEngine();
                                     sqlEngine.runSQLEngine(sql);
                                     textArea2.setForeground(new Color(255,255,255));
                                     textArea2.setText(sqlEngine.getResult());
                                 } catch (Exception e1) {
                                     e1.printStackTrace();
                                     textArea2.setForeground(new Color(171,80,78));
                                     textArea2.setText(e1.getMessage());
                                 }
                             }

                             break;

                     }
                 }

                 @Override
                 public void keyReleased(KeyEvent e) {
                 }
             });
             textArea1.addMouseListener(new MouseListener() {
                 @Override
                 public void mouseClicked(MouseEvent e) {

                 }

                 @Override
                 public void mousePressed(MouseEvent e) {
                     if(!click) {
                         textArea1.setText("");
                         textArea1.setEditable(true);
                         click = true;
                     }
                 }

                 @Override
                 public void mouseReleased(MouseEvent e) {

                 }

                 @Override
                 public void mouseEntered(MouseEvent e) {

             }

                 @Override
                 public void mouseExited(MouseEvent e) {

                 }
             });
        }



        public void setStyle()
        {
            jframe.setBackground(new Color(60,63,65));
            textArea1.setBackground(new Color(43,43,43));
            textArea1.setFont(new Font("黑体",Font.BOLD,15));
            textArea1.setForeground(new Color(255,255,255));
            textArea1.setEditable(false);
            textArea1.setCaretColor(Color.WHITE);

            textArea2.setBackground(new Color(43,43,43));
            textArea2.setFont(new Font("黑体",Font.BOLD,15));
            textArea2.setForeground(new Color(255,255,255));
            textArea2.setEditable(false);


            List<String> sleep=new ArrayList<>();
            sleep.add("DATABASE Loading");
            sleep.add("DATABASE Loading.");
            sleep.add("DATABASE Loading..");
            sleep.add("DATABASE Loading...");
            sleep.add("DATABASE Loading");
            sleep.add("DATABASE Loading.");
            sleep.add("DATABASE Loading..");
            sleep.add("DATABASE Loading...");
            sleep.add("DATABASE Loading");
            sleep.add("DATABASE Loading.");
            sleep.add("DATABASE Loading..");
            sleep.add("DATABASE Loading...");

            List<String> loading=new ArrayList<>();
            loading.add("[Tresses] Loading configuration file: none");
            loading.add("[Tresses] Creating Instance");
            loading.add("[Tresses] Treeses Instance stared");
            loading.add("[Tresses] Config dictionary ");
            loading.add("[Tresses] Loading tables");
            loading.add("[Tresses] Loading SQL Engine");
            loading.add("[Tresses] Loading Controller");
            loading.add("[Tresses] Treeses server started");



          myThread myThreads=  new myThread(textArea1,sleep,loading);
            myThreads.start();



        }

        class myThread extends Thread
        {
            JTextArea textArea;
            List<String> mesA;
            List<String> mesB;
            public myThread(JTextArea textArea,List<String> mesA,List<String> mesB)
            {
                this.textArea=textArea;
                this.mesA=mesA;
                this.mesB=mesB;
            }
            @Override
            public void run(){
                for(int i=0;i<mesA.size();i++)
                {
                    textArea1.setText(mesA.get(i));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                textArea.setText("");
                for(int i=0;i<mesB.size();i++)
                {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                    // new Date()为获取当前系统时间
                    textArea1.append(df.format(new Date())+"\t"+mesB.get(i)+"\n");
                    try {

                        Thread.sleep(100*(int)(1+Math.random()*(10-1+1)));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                textArea.append("Welcome to the Treeses database...");

            }
        }
}


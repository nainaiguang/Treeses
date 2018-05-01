package com.nng.HMI;

import com.nng.DBS.SQLEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Treeses {
    public JPanel jframe;
    private JTextArea textArea1;
    private JTextArea textArea2;



    public Treeses(){
            /**
             * 设计样式
             */
            setStyle();



             textArea1.addKeyListener(new KeyListener() {
                 @Override
                 public void keyTyped(KeyEvent e) {

                 }

                 @Override
                 public void keyPressed(KeyEvent e) {

                     switch (e.getKeyCode())
                     {
                         case KeyEvent.VK_ENTER:
                             if(textArea1.getText().charAt(textArea1.getText().length()-1)==';')
                             {
                                 String sql = textArea1.getText().substring(0,textArea1.getText().length()-1);
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

                     }
                 }

                 @Override
                 public void keyReleased(KeyEvent e) {
                 }
             });
        }



        public void setStyle()
        {
            jframe.setBackground(new Color(60,63,65));
            textArea1.setBackground(new Color(43,43,43));
            textArea1.setFont(new Font("黑体",Font.BOLD,15));
            textArea1.setForeground(new Color(255,255,255));


            textArea2.setBackground(new Color(43,43,43));
            textArea2.setFont(new Font("黑体",Font.BOLD,15));
            textArea2.setForeground(new Color(255,255,255));
            textArea2.setEditable(false);

        }
        }


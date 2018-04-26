package com.nng.main;

import com.nng.HMI_Text.text;


import javax.swing.*;

public class main {
    public static void main(String args[])
    {
//        Scanner input=new Scanner(System.in);
//        String a=input.next();
//        System.out.print(Trim.clear_up(a));
        JFrame frame=new JFrame("text");
        frame.setContentPane(new text().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }
}

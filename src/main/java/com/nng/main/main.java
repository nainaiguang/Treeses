package com.nng.main;

import com.nng.HMI.Treeses;
import com.nng.HMI_Text.text;


import javax.swing.*;
import java.awt.*;

public class main {
    public static void main(String args[])
    {
//        Scanner input=new Scanner(System.in);
//        String a=input.next();
//        System.out.print(Trim.clear_up(a));
        JFrame frame=new JFrame("Treeses");
        frame.setBackground(new Color(60,63,65));
        frame.setContentPane(new Treeses().jframe);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }
}

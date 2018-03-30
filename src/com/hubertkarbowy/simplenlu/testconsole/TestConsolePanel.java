package com.hubertkarbowy.simplenlu.testconsole;

import javax.swing.*;
import java.awt.*;

public class TestConsolePanel extends JFrame {
    private static TestConsolePanel ourInstance = new TestConsolePanel();
    static {
        ourInstance.setSize(800,400);
        ourInstance.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ourInstance.setLocationRelativeTo(null);
        ourInstance.setLayout(null);
    }

    JPanel mainPanel = new JPanel();
    JButton submit = new JButton("Submit");
    JButton quit = new JButton("Quit");
    JTextArea nl = new JTextArea("jaka jest pogoda w suwonie jutro");
    JTextArea response = new JTextArea("<response here>");

    public static TestConsolePanel getInstance() {
        return ourInstance;
    }

    private TestConsolePanel() {
        mainPanel.setLayout(null);
        mainPanel.setBounds(0,0,800,400);
        addButtons();
    }

    private void addButtons() {
        Container pane = this.getContentPane();
        submit.setBounds(320,10,100,30);
        submit.setMnemonic('s');
        quit.setBounds(320,40,100,30);
        quit.addActionListener(e -> {System.exit(0);});
        quit.setMnemonic('q');
        nl.setBounds(10,10,300,30);
        response.setBounds(10,60,300,300);




        mainPanel.add(submit);
        mainPanel.add(quit);
        mainPanel.add(nl);
        mainPanel.add(response);



        this.getContentPane().add(mainPanel);
        submit.setVisible(true);
        mainPanel.setVisible(true);
        this.repaint();
    }
}

package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame implements ActionListener {
    private JPanel contPanel;
    private CardLayout cl;
    private ConnectPanel connectPanel = new ConnectPanel();
    private GamePanel gamePanel;
    public static String ipAddress;
    Connection connection;
    NewThread newThread;

    public MainWindow() throws HeadlessException{
        this.setTitle("Mastermind");
        this.setSize(500,800);
        setResizable(false);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        contPanel = new JPanel();
        cl = new CardLayout();
        contPanel.setLayout(cl);
        contPanel.add(connectPanel, "1");
        connectPanel.getConnectButton().addActionListener(this);
        cl.show(contPanel, "1");
        this.add(contPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == connectPanel.getConnectButton()){
            ipAddress = connectPanel.getAddress();
            System.out.println(ipAddress);
            try {
                this.connection = new Connection(ipAddress, 1234);
                String message = this.connection.readMessage();
                System.out.println(message);

                if(message.equals("111111")){
                    gamePanel = new GamePanel(1, connection);
                    newThread = new NewThread(this.connection, this.gamePanel);
                    newThread.start();
                    contPanel.add(gamePanel, "2");
                    cl.show(contPanel, "2");
                } else if (message.equals("000000")){
                    gamePanel = new GamePanel(0, connection);
                    newThread = new NewThread(this.connection, this.gamePanel);
                    newThread.start();
                    contPanel.add(gamePanel, "2");
                    cl.show(contPanel, "2");
                }

            } catch (Exception ex){
                this.connection.closeSocket();
            }

        }
    }
}

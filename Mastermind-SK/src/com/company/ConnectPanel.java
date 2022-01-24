package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectPanel extends JLayeredPane implements ActionListener {
    public JButton connectButton;
    public JTextField addressTextField;

    public ConnectPanel(){
        connectButton = new JButton("Connect");
        connectButton.setBounds(150, 300, 100,40);
        connectButton.addActionListener(this);
        this.add(connectButton);

        addressTextField = new JTextField("192.168.1.21");
        addressTextField.setBounds(125, 100, 200,40);
        this.add(addressTextField);



    }

    public JButton getConnectButton() {
        return connectButton;
    }
    public String getAddress(){
        return addressTextField.getText();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

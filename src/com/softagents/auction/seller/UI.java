package com.softagents.auction.seller;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

class UI extends JFrame {

    private Seller myAgent;
    private JTextField nameField, priceField;
    private JTable sellList;
    private JLabel error;
    private String[] sellListColumns = {"#", "Name", "Starting price", "Selling Price", "Auctioneer", "Status"};

    UI(Seller a) {
        super(a.getLocalName());
        myAgent = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 2));
        p.add(new JLabel("Coffee Name:"));
        nameField = new JTextField(15);
        p.add(nameField);
        p.add(new JLabel("Price:"));
        priceField = new JTextField(15);
        p.add(priceField);
        error = new JLabel();
        error.setForeground(Color.red);

        JButton addButton = new JButton("Sell");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String name = nameField.getText().trim();
                    int price = Integer.parseInt(priceField.getText().trim());
                    if (myAgent.find(name) != -1) {
                        error.setText("still trying to sell : " + name);
                    } else {
                        myAgent.registerCoffee(name, price);
                        error.setText("");
                    }
                    nameField.setText("");
                    priceField.setText("");
                } catch (Exception e) {
                    error.setText("Invalid values. " + e.getMessage());
                }
            }
        });
        JPanel left = new JPanel(new BorderLayout());
        left.add(error, BorderLayout.NORTH);
        left.add(p, BorderLayout.CENTER);
        left.add(addButton, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(Box.createGlue(), BorderLayout.CENTER);
        wrapper.add(left, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(wrapper, 21, 31), BorderLayout.WEST);

        sellList = new JTable() {
            public boolean isCellEditable(int nRow, int nCol) {
                return false;
            }
        };
        sellList.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus, int row, int col) {

                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                String status = (String) table.getModel().getValueAt(row, 5);

                if (status.equals("Failure to Sell")) {
                    setBackground(Color.magenta);
                } else if (status.equals("Sold")) {
                    setBackground(Color.green);
                } else if (status.equals("Selling")) {
                    setBackground(Color.cyan);
                } else {
                    setBackground(table.getBackground());
                }
                return this;
            }
        });
        DefaultTableModel ledgerModel = (DefaultTableModel) sellList.getModel();
        sellList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ledgerModel.setColumnIdentifiers(sellListColumns);
        JScrollPane SP = new JScrollPane(sellList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        getContentPane().add(SP, BorderLayout.CENTER);

        // Make the agent terminate when the user closes 
        // the GUI using the button on the upper right corner	
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        });

        setResizable(false);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int) screenSize.getWidth() / 2;
        int centerY = (int) screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }

    void updateTable(ArrayList<Coffee> coffeeList) {
        DefaultTableModel tableModel = (DefaultTableModel) sellList.getModel();
        tableModel.setRowCount(0);
        String[] data = new String[6];
        for (int i = 0; i < coffeeList.size(); i++) {
            data[0] = "" + (i + 1);
            data[1] = coffeeList.get(i).getName();
            data[2] = "" + coffeeList.get(i).getStartingPrice();
            if (coffeeList.get(i).getSellingPrice() == 0) {
                data[3] = "-";
            } else {
                data[3] = "" + coffeeList.get(i).getSellingPrice();
            }
            if (coffeeList.get(i).getTriedAuctioneers().size() == 0) {
                data[4] = "-";
            } else {
                data[4] = coffeeList.get(i).getTriedAuctioneers().get(coffeeList.get(i).getTriedAuctioneers().size() - 1).getLocalName();
            }
            data[5] = coffeeList.get(i).getStatus();
            tableModel.addRow(data);
        }
        sellList.setModel(tableModel);
        tableModel.fireTableDataChanged();
    }
}

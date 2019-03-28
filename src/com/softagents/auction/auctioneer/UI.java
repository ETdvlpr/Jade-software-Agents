package com.softagents.auction.auctioneer;

import jade.core.AID;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

class UI extends JFrame {

    private Auctioneer myAgent;
    private JTable ledgerTable, buyerTable;
    private String[] ledgerColumns = {"#", "Time", "Coffee", "Starting price", "Seller", "Selling price", "Buyer", "Status"};
    private String[] buyerColumns = {"#", "Name"};
    private JTextArea console;
    private JScrollPane consoleSP;

    UI(Auctioneer a) {
        super(a.getLocalName());
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }

        myAgent = a;

        ledgerTable = new JTable() {
            public boolean isCellEditable(int nRow, int nCol) {
                return false;
            }
        };
        buyerTable = new JTable() {
            public boolean isCellEditable(int nRow, int nCol) {
                return false;
            }
        };
        DefaultTableModel ledgerModel = (DefaultTableModel) ledgerTable.getModel();
        ledgerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ledgerModel.setColumnIdentifiers(ledgerColumns);
        ledgerModel.addRow(new String[]{"","","","","","","",""});
        DefaultTableModel buyerModel = (DefaultTableModel) buyerTable.getModel();
        buyerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buyerModel.setColumnIdentifiers(buyerColumns);
        ledgerTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus, int row, int col) {

                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                String status = (String) table.getModel().getValueAt(row, 7);

                if (status.equals("SOLD")) {
                    setBackground(Color.green);
                } else if (status.equals("IN BID")) {
                    setBackground(Color.cyan);
                } else if (status.equals("NOT SOLD")) {
                    setBackground(Color.magenta);
                } else {
                    setBackground(table.getBackground());
                    setForeground(table.getForeground());
                }
                return this;
            }
        });
        JScrollPane ledgerSP = new JScrollPane(ledgerTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane buyerSP = new JScrollPane(buyerTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel ledger = new JPanel(new BorderLayout());
        JPanel buyer = new JPanel(new BorderLayout());

        ledger.add(ledgerSP, BorderLayout.CENTER);
        ledger.add(new JLabel("Ledger"), BorderLayout.NORTH);
        buyer.add(buyerSP, BorderLayout.CENTER);
        buyer.add(new JLabel("Buyers"), BorderLayout.NORTH);
        getContentPane().add(ledger, BorderLayout.CENTER);
        getContentPane().add(buyer, BorderLayout.EAST);

        console = new JTextArea();
        console.setEditable(false);
        console.setBackground(Color.black);
        console.setForeground(Color.green);
        consoleSP = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        getContentPane().add(consoleSP, BorderLayout.SOUTH);
        // Make the agent terminate when the user closes 
        // the GUI using the button on the upper right corner	
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        });
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int) screenSize.getWidth() / 2;
        int centerY = (int) screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
        for (int i = 0; i < 10; i++) {
            updateConsole("");
        }
    }

    public void updateLedgerTable(ArrayList<Coffee> ledger) {
        DefaultTableModel tableModel = (DefaultTableModel) ledgerTable.getModel();
        tableModel.setRowCount(0);
        String[] data = new String[8];
        for (int i = 0; i < ledger.size(); i++) {
            data[0] = "" + (i + 1);
            data[1] = new Date(ledger.get(i).getRequest().getPostTimeStamp()).toString();
            data[2] = ledger.get(i).getName();
            data[3] = "" + ledger.get(i).getStartingPrice();
            data[4] = ledger.get(i).getRequest().getSender().getLocalName();
            data[7] = ledger.get(i).getStatus();
            if (ledger.get(i).getSellingPrice() == 0) {
                data[5] = "-";
                data[6] = "-";
            } else {
                data[5] = "" + ledger.get(i).getSellingPrice();
                data[6] = ledger.get(i).getBuyer().getLocalName();
            }
            tableModel.addRow(data);
        }
        ledgerTable.setModel(tableModel);
        tableModel.fireTableDataChanged();
    }

    public void updateBuyerTable(ArrayList<AID> buyers) {
        DefaultTableModel tableModel = (DefaultTableModel) buyerTable.getModel();
        tableModel.setRowCount(0);
        String[] data = new String[2];
        for (int i = 0; i < buyers.size(); i++) {
            data[0] = "" + (i + 1);
            data[1] = buyers.get(i).getLocalName();
            tableModel.addRow(data);
        }
        buyerTable.setModel(tableModel);
        tableModel.fireTableDataChanged();
    }

    public void updateConsole(String text) {
        console.setText(console.getText() + "\n" + text);
        consoleSP.getVerticalScrollBar().setValue(consoleSP.getVerticalScrollBar().getMaximum());
    }
}

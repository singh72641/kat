package com.punwire.kat.chart;

import com.punwire.kat.data.MongoDb;
import com.punwire.kat.data.Option;

import javax.swing.*;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * Created by Kanwal on 21/01/16.
 */
public class OptionTable extends JPanel {

    private MongoDb db = new MongoDb();
    public OptionTable() {
        super();
        setBackground(AppStyle.BACKGROUD);
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        final OptionTableModel tableModel = new OptionTableModel(db);

        TableColumnModel columnModel = new DefaultTableColumnModel();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            TableColumn firstColumn = new TableColumn(i);
            firstColumn.setHeaderValue(tableModel.getColumnHeader(i));
            firstColumn.setPreferredWidth(tableModel.getColumnWidth(i));
            columnModel.addColumn(firstColumn);
        }

        JPanel symbolSelect = new JPanel();
        symbolSelect.setBackground(AppStyle.BACKGROUD);
        JLabel symbolLabel = new JLabel("Symbol:");
        JTextField symbolField = new JTextField("RELIANCE");
        symbolField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.load(e.getActionCommand());
                System.out.println("COMMAND " + e.getActionCommand());
                tableModel.fireTableDataChanged();

            }
        });
        symbolField.setColumns(60);
        symbolSelect.add(symbolLabel);
        symbolSelect.add(symbolField);
        JTable optionTable = new JTable(tableModel, columnModel);
        optionTable.setBackground(AppStyle.BACKGROUD);
        optionTable.setForeground(AppStyle.TEXT);
        optionTable.setRowHeight(40);
        optionTable.setFillsViewportHeight(true);
        JScrollPane pane = new JScrollPane(optionTable);
        pane.setPreferredSize(new Dimension(3000, 2000));
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addComponent(symbolSelect)
                        .addComponent(pane));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(symbolSelect)
                        .addComponent(pane)
        );
    }

    class OptionTableModel extends AbstractTableModel {
        String col[] = {"Symbol", "   ", "type", "Bid", "Ask", "%itm", "spacer", "Strike", "spacer", "Bid", "Ask", "%itm"};
        int widths[] = {300, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50};
        Object data[][] = {{"RELIANCE16JAN", "    ", "CE", 12.2, 13.23, 12.5, "   ->   ", 1100, "    <-   ", "PE", 12.22, 23.22, 14.5},
                {"RELIANCE17JAN", "    ", "CE", 12.2, 13.23, 12.5, "   ->   ", 1100, "    <-   ", "PE", 12.22, 23.22, 14.5},
                {"RELIANCE18JAN", "    ", "CE", 12.2, 13.23, 12.5, "   ->   ", 1100, "    <-   ", "PE", 12.22, 23.22, 14.5}};

        private java.util.List<Option> options;
        private HashMap<String, Option> calls = new HashMap<>();
        private HashMap<String, Option> puts = new HashMap<>();
        private List<String> strikes;
        private MongoDb db;

        public OptionTableModel(MongoDb db)
        {
            this.db = db;
            load("RELIANCE");
        }

        public void load(String symbol)
        {
            options = db.getOptions("OPTSTK",symbol);
            strikes = new ArrayList<>();
            for(Option opt: options){
                if( ! strikes.contains( opt.getExpiryDate().getMonth().name() + Double.valueOf(opt.getStrike()))) strikes.add( opt.getExpiryDate().getMonth().name() + + Double.valueOf(opt.getStrike()));
                if( opt.getOptionType().equals("PE")) puts.put( opt.getExpiryDate().getMonth().name() + + Double.valueOf(opt.getStrike()),opt);
                else calls.put(opt.getExpiryDate().getMonth().name() + Double.valueOf(opt.getStrike()),opt);
            }
            Collections.sort(strikes);
        }

        @Override
        public int getRowCount() {
            System.out.println("Strikes Count: " + strikes.size());
            return strikes.size();
        }

        @Override
        public int getColumnCount() {
            return col.length;
        }

        public String getColumnHeader(int i) {
            return col[i];
        }


        public int getColumnWidth(int i) {
            return widths[i];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String strike = strikes.get(rowIndex);
            Option call = calls.get(strike);
            Option put = puts.get(strike);
            switch (columnIndex) {
                case 0:
                    return call != null? call.getSymbol(): put.getSymbol();
                case 1:
                    return " ";
                case 2:
                    return call != null? call.getOptionType(): "";
                case 3:
                    return call != null? call.getLastPrice(): "";
                case 4:
                    return call != null? call.getLastPrice(): "";
                case 5:
                    return "25";
                case 6:
                    return "";
                case 7:
                    return strike;
                case 8:
                    return put != null? put.getOptionType(): "";
                case 9:
                    return put != null? put.getLastPrice(): "";
                case 10:
                    return put != null? put.getLastPrice(): "";
                case 11:
                    return "25";
            }
            return "";
        }
    }
}

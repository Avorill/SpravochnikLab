

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXDatePicker;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
public class DirectoryApp {
    private final JFrame frame;
    private final JTable table;
    private  DefaultTableModel tableModel;
    private Connection connection;
    private JDialog dialog;
    JXDatePicker datePicker = new JXDatePicker();
    public DirectoryApp() {
        List<String> tables = new ArrayList<>();
        HashMap<String, List<String>> columns = new HashMap<>();
        HashMap<String, List<String>> types = new HashMap<>();
        HashMap<String, String> primaryKeys = new HashMap<>();
        HashMap<String,List<String>> connectionTable= new HashMap<>();
        frame = new JFrame("Ульнирова Полина Алексеевна, 4 группа, 4 курс");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        connectToDatabase(tables, columns, types, primaryKeys);
        StringBuilder currentTable = new StringBuilder();
        currentTable.append(addDirectory(tables));
        tableModel = new DefaultTableModel(columns.get(currentTable.toString()).toArray(), 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JButton addButton = new JButton("Добавить строку");
        addButton.addActionListener(e -> {
            dialog = null;
            addRow(columns, currentTable.toString(), tableModel, types, primaryKeys,connectionTable);
        });

        JButton deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(e -> deleteDirectory(currentTable.toString(), tableModel, columns, primaryKeys));

        JButton selectButton = new JButton("Отменить выделение");
        selectButton.addActionListener(e -> table.clearSelection());

        JButton chooseAnotherButton = new JButton("Открыть таблицу");
        chooseAnotherButton.addActionListener(e -> {

            String selectedTable = updateViewTable(tables);
            if (selectedTable != null) {
                // Обработка выбранной таблицы, например, обновление JTable

                currentTable.setLength(0);
                currentTable.append(selectedTable);
                DefaultTableModel newTableModel= new DefaultTableModel(columns.get(currentTable.toString()).toArray(), 0);
                table.repaint();
                table.setModel(newTableModel);
                sorter.setModel(newTableModel);
                table.setRowSorter(sorter);
                loadDirectory(currentTable.toString(), columns,types,primaryKeys, newTableModel,connectionTable);
                tableModel = newTableModel;
            }

        });


        JButton updateButton = new JButton("Изменить строку");
        updateButton.addActionListener(e -> {
            dialog = null;
            updateRow(columns, currentTable.toString(), tableModel, types, primaryKeys,connectionTable);

        });

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int columnIndex = table.columnAtPoint(e.getPoint());


                // Set the comparator
                Comparator<Object> comparator = setupColumnComparator(columnIndex, types,columns, primaryKeys, currentTable.toString(),connectionTable);
                sorter.setComparator(columnIndex, comparator);

                // Toggle the sort order
                sorter.toggleSortOrder(columnIndex);
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(chooseAnotherButton);
        buttonPanel.add(selectButton);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
        loadDirectory(currentTable.toString(), columns,types,primaryKeys,tableModel,connectionTable);
    }

    private void connectToDatabase(List<String> tables, HashMap<String, List<String>> columns,
                                   HashMap<String, List<String>> types, HashMap<String, String> primaryKeys) {
        try {
            String username = JOptionPane.showInputDialog("Enter database username (default: postgres):");
            if (username == null || username.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username cannot be empty. Exiting.");
                return;
            }

            String password = JOptionPane.showInputDialog("Enter database password:");

            if (password == null || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Password cannot be empty. Exiting.");
                return;
            }

            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/labs", username,
                    password);
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            Pattern pattern = Pattern.compile("(_pkey)$");

            while (rs.next()) {
                Matcher matcher = pattern.matcher(rs.getString(3));
                List<String> col = new ArrayList<>();
                List<String> colType = new ArrayList<>();
                if (matcher.find()) {
                    if (!rs.getString(3).replace("_pkey", "").equals("sinonim")) {
                        tables.add(rs.getString(3).replace("_pkey", ""));
                        ResultSet rCol = md.getColumns(null, null, tables.get(tables.size() - 1), null);
                        ResultSet rPrim = md.getPrimaryKeys(null, null, tables.get(tables.size() - 1));

                        while (rCol.next()) {
                            col.add(rCol.getString("COLUMN_NAME"));
                            colType.add(rCol.getString("TYPE_NAME"));
                        }
                        while ((rPrim.next())) {

                            primaryKeys.put(tables.get(tables.size() - 1), rPrim.getString("COLUMN_NAME"));
                        }
                        columns.put(tables.get(tables.size() - 1), col);
                        types.put(tables.get(tables.size() - 1), colType);

//
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDirectory(String currentTable, HashMap<String,
            List<String>> columns, HashMap<String, List<String>> types,
                               HashMap<String, String> primaryKeys, DefaultTableModel tableModel,
                               HashMap<String,List<String>> connectionTable ) {
        try {
            List<String> primList = new ArrayList<>();
            List<String> columnNames = new ArrayList<>();
            for (Map.Entry<String, String> entry : primaryKeys.entrySet()) {

                String value = entry.getValue();
                if (!value.equals(primaryKeys.get(currentTable))) {
                    primList.add(value);
                }

            }
            primList.retainAll(columns.get(currentTable));
            if(!primList.isEmpty()) {
                StringBuilder sqlQuaryZero = new StringBuilder("select id_foreign, table_name, id_column_foreign from sinonim where id_foreign = '");

                for (String el : primList) {
                    sqlQuaryZero.append(el).append("'");
                    if (primList.indexOf(el) < primList.size()-1) {
                        sqlQuaryZero.append(" and id_foreign = ");
                    }
                }
                PreparedStatement statementZero = connection.prepareStatement(String.valueOf(sqlQuaryZero));
                ResultSet resultZero = statementZero.executeQuery();
                int i = 0;
                while (resultZero.next()) {
                    List<String> el= new ArrayList<>();

                    el.add(primList.get(i));
                    i++;
                    el.add(resultZero.getString("table_name"));
                    el.add(resultZero.getString("id_column_foreign"));


                    List<String> enter = new ArrayList<>();
                    enter.add(el.get(1));
                    enter.add(el.get(2));
                    connectionTable.put( el.get(0),enter);
                }
                resultZero.close();
                statementZero.close();
            }


            StringBuilder sqlQuery = new StringBuilder();

            if(primList.isEmpty()) {


                sqlQuery.append("SELECT * FROM ").append(currentTable);
                PreparedStatement statement = connection.prepareStatement(String.valueOf(sqlQuery));
                ResultSet result = statement.executeQuery();

                while (result.next()) {
                    List<Object> elements = new ArrayList<>();
                    for (int i = 0; i < columns.get(currentTable).size(); i++) {
                        if(!types.get(currentTable).get(i).equals("date")) {
                            elements.add(result.getString(columns.get(currentTable).get(i)));
                        }
                        else{
                            Date date = result.getDate(columns.get(currentTable).get(i));
                            if (date != null) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                                elements.add(dateFormat.format(date));
                            } else {
                                elements.add(null); // Handle null date values as needed
                            }
                        }

                    }
                    tableModel.addRow(elements.toArray());
                }
                result.close();
                statement.close();
            }
            else {
                sqlQuery.append("SELECT ");
                for(String col : columns.get(currentTable)){

                    if(!primList.contains(col)){
                        sqlQuery.append(currentTable).append(".").append(col);
                    }
                    else {
                        String tableName = connectionTable.get(col).get(0);
                        String columnName = connectionTable.get(col).get(1);
                        sqlQuery.append(tableName).append(".").append(columnName);

                    }
                    if (columns.get(currentTable).indexOf(col) <columns.get(currentTable).size()-1) {
                        sqlQuery.append(", ");
                    }
                }
                sqlQuery.append(" from ").append(currentTable);
                sqlQuery.append(" left join ");
                for(String prim :primList){
                    sqlQuery.append(connectionTable.get(prim).get(0)).append(" ON ").append(currentTable)
                            .append(".").append(prim).append(" = ").append(connectionTable.get(prim)
                                    .get(0)).append(".").append(prim);
                }
                PreparedStatement statement = connection.prepareStatement(String.valueOf(sqlQuery));
                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    List<Object> elements = new ArrayList<>();

                    for (int i = 0; i < columns.get(currentTable).size(); i++) {
                        boolean isNotForeign = true;
                        for(String prim : primList){
                            if(columns.get(currentTable).get(i).equals(prim)){
                                isNotForeign = false;
                            }
                        }
                        if(isNotForeign){
                            if(!types.get(currentTable).get(i).equals("date")){
                                elements.add(result.getString(columns.get(currentTable).get(i)));
                            }
                            else {
                                Date date = result.getDate(columns.get(currentTable).get(i));
                                if (date != null) {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                                    elements.add(dateFormat.format(date));
                                } else {
                                    elements.add(null); // Handle null date values as needed
                                }
                            }
//
                        }
                        else {
                            elements.add(result.getString(connectionTable.get(columns.get(currentTable).get(i)).get(1)));
//
                        }

                    }
                    tableModel.addRow(elements.toArray());
                }
                for(String col : columns.get(currentTable)){

                    if (primList.contains(col)) {
                        columnNames.add(connectionTable.get(col).get(1));
                    } else {
                        columnNames.add(col);
                    }

                }
                result.close();
                statement.close();
                tableModel.setColumnIdentifiers(columnNames.toArray());

            }
            TableColumn column = table.getColumnModel().getColumn(table.getColumnModel()
                    .getColumnIndex(primaryKeys.get(currentTable)));
            column.setMinWidth(0);
            column.setMaxWidth(0);
            column.setPreferredWidth(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String addDirectory(List<String> tables) {
        String name = String.valueOf(JOptionPane.showOptionDialog(frame, "Введите название справочника:", "",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, tables.toArray(), tables.toArray()[0]));
        if (!name.isEmpty()) {
            return tables.get(Integer.parseInt(name));
        }
        return null;
    }
    private String updateViewTable(@NotNull List<String> tables) {

        JComboBox<String> comboBox = new JComboBox<>(tables.toArray(new String[0]));
        int result = JOptionPane.showOptionDialog(
                null,                           // Родительское окно
                comboBox,                       // Компонент внутри окна
                "Выбрать таблицу",              // Заголовок окна
                JOptionPane.OK_CANCEL_OPTION,   // Опции
                JOptionPane.QUESTION_MESSAGE,    // Тип сообщения
                null,                           // Иконка
                null,                           // Кнопки
                null                            // Кнопка по умолчанию
        );
        if (result == JOptionPane.OK_OPTION) {
            // Пользователь нажал "Выбрать"
            return (String) comboBox.getSelectedItem();
        } else {
            // Пользователь закрыл окно или нажал "Отмена"
            return null;
        }
    }


    private void addRow(HashMap<String, List<String>> columns, String currentTable, DefaultTableModel tableModel,
                        HashMap<String, List<String>> types, HashMap<String, String> primaryKeys,HashMap<String,List<String>> connectionTable) {
        // Создание диалогового окна
        if (dialog == null) {
            List<String> columnsOfThisTable = columns.get(currentTable);
            dialog = new JDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            // Создание текстовых полей для ввода данных
            JPanel panel = new JPanel(new GridLayout(columnsOfThisTable.size() + 1, 0));
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = new Insets(5, 5, 5, 5);
            List<JTextField> textFieldList = new ArrayList<>();
            List<JComboBox> comboBoxes= new ArrayList<>();
            List<Integer> dataArray = new ArrayList<>();
            List<String> primList = new ArrayList<>();
            List<ComboBoxItem> comboBoxData = new ArrayList<>();
            for (Map.Entry<String, String> entry : primaryKeys.entrySet()) {

                String value = entry.getValue();
                if (!value.equals(primaryKeys.get(currentTable))) {
                    primList.add(value);
                }

            }
            primList.retainAll(columnsOfThisTable);
            for (String column : columnsOfThisTable) {
                if(!column.equals(primaryKeys.get(currentTable))) {
                    JLabel label = new JLabel();
                    if(!primList.contains(column)) {
                        label = new JLabel(column + " :");
                    }
                    else {
                        label = new JLabel(connectionTable.get(column).get(1) + " :");
                    }
                    if ("date".equals(types.get(currentTable).get(columnsOfThisTable.indexOf(column)))) {

                        List<DateFormat> dateFormats = Arrays.asList(
                                new SimpleDateFormat("yyyy-MM-dd"),
                                new SimpleDateFormat("dd.MM.yyyy"),
                                new SimpleDateFormat("dd/MM/yyyy")


                        );

                        datePicker = new JXDatePicker();
                        datePicker.setFormats(dateFormats.toArray(new DateFormat[1]));
                        datePicker.getEditor().setEditable(true);

                        panel.add(label);
                        panel.add(datePicker);
                        dataArray.add(columnsOfThisTable.indexOf(column));

                    }
                    else if (primList.contains(column)){
                        //select id, name from table
                        String sqlQuaryZero = "select " + column + ", " + connectionTable.get(column).get(1) + " from "
                                + connectionTable.get(column).get(0);
//                        List<ComboBoxItem> comboBoxData = new ArrayList<>();
                        try {
                            PreparedStatement statementZero = connection.prepareStatement(sqlQuaryZero);
                            ResultSet resultSetZero = statementZero.executeQuery();

                            while(resultSetZero.next()){
                                comboBoxData.add(new ComboBoxItem(resultSetZero.getString(column), resultSetZero.getString(connectionTable.get(column).get(1))));
                            }

                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }


                        JComboBox<ComboBoxItem> comboBox = new JComboBox<>();
                        for (ComboBoxItem item : comboBoxData) {
                            comboBox.addItem(item);
                        }

                        comboBox.setRenderer(new DefaultListCellRenderer() {
                            @Override
                            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                          boolean isSelected, boolean cellHasFocus) {
                                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                                if (value instanceof ComboBoxItem) {
                                    ComboBoxItem item = (ComboBoxItem) value;
                                    setText(item.getDisplayValue());
                                }
                                return this;
                            }
                        });

                        constraints.gridx = 0;
                        constraints.gridy = columnsOfThisTable.indexOf(column);
                        panel.add(label, constraints);
                        constraints.gridx = 1;
                        panel.add(comboBox, constraints);
                        comboBoxes.add(comboBox);
                    }
                    else {
                        JTextField textField = new JTextField();
                        constraints.gridx = 0;
                        constraints.gridy = columnsOfThisTable.indexOf(column);
                        panel.add(label, constraints);
                        constraints.gridx = 1;
                        panel.add(textField, constraints);
                        textFieldList.add(textField);
                    }
                }

            }
            // Создание кнопки "Добавить"
            JButton addRowButton = new JButton("Добавить");
            panel.add(addRowButton, constraints);
            addRowButton.addActionListener(e -> {
                // Получение введенных значений
                List<Object> inputValues = new ArrayList<>();

                int p = 0;
                int combo = 0;
                for(int i = 0; i < columnsOfThisTable.size();i++){
                    if(!columnsOfThisTable.get(i).equals(primaryKeys.get(currentTable))) {
                        if (dataArray.contains(i)) {
                            Date utilDate = datePicker.getDate();

                            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

                            inputValues.add(dateFormat.format(utilDate));
                            p++;
                        }

                        else if (primList.contains(columnsOfThisTable.get(i))){
                            ComboBoxItem  value = (ComboBoxItem) comboBoxes.get(combo).getSelectedItem();
                            inputValues.add(value.getId());
                            combo++;
                        }
                        else {
                            String value = textFieldList.get(i-p-combo).getText();
                            inputValues.add(value);
                        }
                    }
                    else p++;
                }
                StringBuilder sqlQuery = new StringBuilder("INSERT INTO " + currentTable + " (");
                for (int i = 0; i < columnsOfThisTable.size(); i++){
                    if(!columnsOfThisTable.get(i).equals(primaryKeys.get(currentTable))) {
                        sqlQuery.append(columnsOfThisTable.get(i));
                        if (i < columnsOfThisTable.size()-1) {
                            sqlQuery.append(", ");
                        }
                    }

                }
                sqlQuery.append(") VALUES ( ");
                for (int i = 0; i < inputValues.size(); i++) {
                    sqlQuery.append("?");
                    if (i < inputValues.size() - 1) {
                        sqlQuery.append(", ");
                    }
                }
                sqlQuery.append(") ");

                PreparedStatement statement;
                try {
                    statement = connection.prepareStatement(sqlQuery.toString(), Statement.RETURN_GENERATED_KEYS);
                    int c = 0;
                    for (int i = 0; i < columnsOfThisTable.size(); i++) {
                        String type = String.valueOf(types.get(currentTable).toArray()[i]);
                        System.out.println(types.get(currentTable).toArray()[i]);
                        if (!columnsOfThisTable.get(i).equals(primaryKeys.get(currentTable))) {
                            switch(type){
                                case "int4":
                                    statement.setInt(i + 1-c, Integer.valueOf(inputValues.get(i-c).toString()));
                                    break;
                                case "int2":
                                    statement.setInt(i + 1-c, Short.valueOf(inputValues.get(i-c).toString()));
                                    break;
                                case "bigserial":
                                    statement.setInt(i + 1-c, Short.valueOf(inputValues.get(i-c).toString()));
                                    break;
                                case "float4":
                                    statement.setFloat(i + 1-c, Float.parseFloat(inputValues.get(i-c).toString()));
                                    break;
                                case "float8":
                                    statement.setDouble(i + 1-c, Double.parseDouble(inputValues.get(i-c).toString()));
                                    break;
                                case "numeric":
                                    statement.setBigDecimal(i + 1-c, new BigDecimal(inputValues.get(i-c).toString()));
                                    break;
                                case "date":

                                    java.util.Date utilDate = datePicker.getDate();

                                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    String formattedDate = dateFormat.format(utilDate);

                                    statement.setDate(i + 1 - c, java.sql.Date.valueOf(formattedDate));
                                    break;
                                case "varchar":
                                case "text":
                                case "bpchar":
                                case "bytea":
                                    statement.setString(i + 1-c, inputValues.get(i-c).toString());
                                    break;
                                case "time":
                                    statement.setTime(i + 1-c, Time.valueOf(inputValues.get(i-c).toString()));
                                    break;
                                case "timestamp":
                                    statement.setTimestamp(i + 1-c, Timestamp.valueOf(inputValues.get(i-c).toString()));
                                    break;
                                case "bool":
                                    statement.setBoolean(i + 1-c, Boolean.parseBoolean(inputValues.get(i-c).toString()));
                                    break;

                                default:
                                    throw new IllegalArgumentException("Unsupported data type: " +
                                            type);
                            }
//
                        }
                        else{
                            c++;
                        }

                    }
                    statement.executeUpdate();

                    ResultSet generatedKeys = statement.getGeneratedKeys();
                    int lastInsertedId = 0;
                    if (generatedKeys.next()) {
                        lastInsertedId = generatedKeys.getInt(1);
                    }
                    List<Object> rowData = new ArrayList<>();
                    rowData.add(String.valueOf(lastInsertedId));  // Замените на соответствующий столбец, который представляет ID

                    c = 0;
                    combo = 0;
                    for(int i = 0; i < columnsOfThisTable.size(); i++){
                        if(!columnsOfThisTable.get(i).equals(primaryKeys.get(currentTable))){
                            if(!primList.contains(columnsOfThisTable.get(i))){
                                rowData.add(inputValues.get(i-c));
                            }
                            else {
                                ComboBoxItem value = (ComboBoxItem) comboBoxes.get(combo).getSelectedItem();
                                rowData.add(value.getDisplayValue());
                                combo++;
                            }

                        }
                        else {
                            c++;
                        }
                    }
                    tableModel.addRow(rowData.toArray());
                    statement.close();
                    generatedKeys.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

                dialog.dispose();



            });
            // Установка панели в диалоговое окно
            dialog.getContentPane().add(panel);

            // Отображение диалогового окна
            dialog.pack();

            // Отображение диалогового окна
            dialog.setVisible(true);

        }

    }

    private void updateRow(HashMap<String, List<String>> columns, String currentTable, DefaultTableModel tableModel,
                           HashMap<String, List<String>> types,
                           HashMap<String, String> primaryKeys,
                           HashMap<String,List<String>> connectionTable) {

        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            // Get the index of the primary key in the model
            int index = columns.get(currentTable).indexOf(primaryKeys.get(currentTable));
            // Get the ID from the model
            int id = Integer.parseInt(String.valueOf(tableModel.getValueAt(modelRow, index)));

            if (dialog == null) {


                dialog = new JDialog();
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                // Создание текстовых полей для ввода данных
                List<String> columnsOfThisTable = columns.get(currentTable);
                JPanel panel = new JPanel(new GridLayout(columnsOfThisTable.size() + 1, 0));
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.anchor = GridBagConstraints.WEST;
                constraints.insets = new Insets(5, 5, 5, 5);


                List<JTextField> textFieldList = new ArrayList<>();
                List<JComboBox> comboBoxes= new ArrayList<>();
                List<Integer> dataArray = new ArrayList<>();
                List<String> primList = new ArrayList<>();
                List<ComboBoxItem> comboBoxData = new ArrayList<>();

                for (Map.Entry<String, String> entry : primaryKeys.entrySet()) {

                    String value = entry.getValue();
                    if (!value.equals(primaryKeys.get(currentTable))) {
                        primList.add(value);
                    }

                }
                primList.retainAll(columnsOfThisTable);
                for (String column : columnsOfThisTable) {
                    if(!column.equals(primaryKeys.get(currentTable))) {
                        JLabel label = new JLabel();
                        if(!primList.contains(column)) {
                            label = new JLabel(column + " :");
                        }
                        else {
                            label = new JLabel(connectionTable.get(column).get(1) + " :");
                        }
                        if ("date".equals(types.get(currentTable).get(columnsOfThisTable.indexOf(column)))) {

                            List<DateFormat> dateFormats = Arrays.asList(
                                    new SimpleDateFormat("yyyy-MM-dd"),
                                    new SimpleDateFormat("dd.MM.yyyy"),
                                    new SimpleDateFormat("dd/MM/yyyy")


                            );

                            datePicker = new JXDatePicker();
                            datePicker.setFormats(dateFormats.toArray(new DateFormat[1]));
                            datePicker.getEditor().setEditable(true);

                            panel.add(label);
                            panel.add(datePicker);
                            dataArray.add(columnsOfThisTable.indexOf(column));

                        }
                        else if (primList.contains(column)){
                            //select id, name from table
                            String sqlQuaryZero = "select " + column + ", " + connectionTable.get(column).get(1) + " from "
                                    + connectionTable.get(column).get(0);

                            try {
                                PreparedStatement statementZero = connection.prepareStatement(sqlQuaryZero);
                                ResultSet resultSetZero = statementZero.executeQuery();

                                while(resultSetZero.next()){
                                    comboBoxData.add(new ComboBoxItem(resultSetZero.getString(column), resultSetZero.getString(connectionTable.get(column).get(1))));
                                }

                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }


                            JComboBox<ComboBoxItem> comboBox = new JComboBox<>();
                            for (ComboBoxItem item : comboBoxData) {
                                comboBox.addItem(item);
                            }

                            comboBox.setRenderer(new DefaultListCellRenderer() {
                                @Override
                                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                              boolean isSelected, boolean cellHasFocus) {
                                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                                    if (value instanceof ComboBoxItem) {
                                        ComboBoxItem item = (ComboBoxItem) value;
                                        setText(item.getDisplayValue());
                                    }
                                    return this;
                                }
                            });

                            constraints.gridx = 0;
                            constraints.gridy = columnsOfThisTable.indexOf(column);
                            panel.add(label, constraints);
                            constraints.gridx = 1;
                            panel.add(comboBox, constraints);
                            comboBoxes.add(comboBox);
                        }
                        else {
                            JTextField textField = new JTextField();
                            textField.setText(String.valueOf(tableModel.getValueAt(modelRow, columnsOfThisTable.indexOf(column))));
                            constraints.gridx = 0;
                            constraints.gridy = columnsOfThisTable.indexOf(column);
                            panel.add(label, constraints);
                            constraints.gridx = 1;
                            panel.add(textField, constraints);
                            textFieldList.add(textField);
                        }
                    }

                }

                JButton addRowButton = new JButton("Изменить");
                panel.add(addRowButton, constraints);

                addRowButton.addActionListener(e -> {
                    List<Object> inputValues = new ArrayList<>();

                    int p = 0;
                    int combo = 0;
                    for(int i = 0; i < columnsOfThisTable.size();i++){
                        if(!columnsOfThisTable.get(i).equals(primaryKeys.get(currentTable))) {
                            if (dataArray.contains(i)) {
                                Date utilDate = datePicker.getDate();

                                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

                                inputValues.add(dateFormat.format(utilDate));
                                p++;
                            }
                            else if (primList.contains(columnsOfThisTable.get(i))){
                                ComboBoxItem  value = (ComboBoxItem) comboBoxes.get(combo).getSelectedItem();
                                inputValues.add(value.getId());
                                combo++;
                            }
                            else {
                                String value = textFieldList.get(i-p-combo).getText();
                                inputValues.add(value);
                            }
                        }
                        else p++;
                    }

                    StringBuilder sqlQuery;
                    sqlQuery = new StringBuilder("UPDATE " + currentTable + " SET ");
                    for (int i = 0; i < columnsOfThisTable.size(); i++) {
                        if (!columnsOfThisTable.get(i).equals(primaryKeys.get(currentTable))){
                            sqlQuery.append(columnsOfThisTable.get(i)).append(" = ?");
                            if (i < inputValues.size()) {
                                sqlQuery.append(", ");
                            }
                        }
                    }

                    sqlQuery.append(" WHERE ").append(primaryKeys.get(currentTable)).append(" = ").append(id);

                    PreparedStatement statement;
                    try {
                        int c = 0;
                        statement = connection.prepareStatement(sqlQuery.toString());

                        for (int i = 0; i < columnsOfThisTable.size(); i++) {
                            String type = String.valueOf(types.get(currentTable).toArray()[i]);
                            if (!columnsOfThisTable.get(i).equals(primaryKeys.get(currentTable))) {
                                switch(type){
                                    case "int4":
                                        statement.setInt(i + 1-c, Integer.valueOf(inputValues.get(i-c).toString()));
                                        break;
                                    case "int2":
                                        statement.setInt(i + 1-c, Short.valueOf(inputValues.get(i-c).toString()));
                                        break;
                                    case "bigserial":
                                        statement.setInt(i + 1-c, Short.valueOf(inputValues.get(i-c).toString()));
                                        break;
                                    case "float4":
                                        statement.setFloat(i + 1-c, Float.parseFloat(inputValues.get(i-c).toString()));
                                        break;
                                    case "float8":
                                        statement.setDouble(i + 1-c, Double.parseDouble(inputValues.get(i-c).toString()));
                                        break;
                                    case "numeric":
                                        statement.setBigDecimal(i + 1-c, new BigDecimal(inputValues.get(i-c).toString()));
                                        break;
                                    case "date":
                                        java.util.Date utilDate = datePicker.getDate();

                                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                        String formattedDate = dateFormat.format(utilDate);

                                        statement.setDate(i + 1 - c, java.sql.Date.valueOf(formattedDate));
                                        break;
                                    case "varchar":
                                    case "text":
                                    case "bpchar":
                                    case "bytea":
                                        statement.setString(i + 1-c, inputValues.get(i-c).toString());
                                        break;
                                    case "time":
                                        statement.setTime(i + 1-c, Time.valueOf(inputValues.get(i-c).toString()));
                                        break;
                                    case "timestamp":
                                        statement.setTimestamp(i + 1-c, Timestamp.valueOf(inputValues.get(i-c).toString()));
                                        break;
                                    case "bool":
                                        statement.setBoolean(i + 1-c, Boolean.parseBoolean(inputValues.get(i-c).toString()));
                                        break;

                                    default:
                                        throw new IllegalArgumentException("Unsupported data type: " +
                                                type);
                                }
//
                            }
                            else{
                                c++;
                            }
                        }
                        statement.executeUpdate();
                        statement.close();

                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                    // Выполнение логики добавления строки с использованием полученных значений

                    // Закрытие диалогового окна
                    dialog.dispose();
                    tableModel.removeRow(modelRow);


                    List<Object> rowData = new ArrayList<>();
                    rowData.add(String.valueOf(id));



                    int c = 0;
                    combo = 0;
                    for(int i = 0; i < columnsOfThisTable.size(); i++){
                        if(!columnsOfThisTable.get(i).equals(primaryKeys.get(currentTable))){
                            if(!primList.contains(columnsOfThisTable.get(i))){
                                rowData.add(inputValues.get(i-c));
                            }
                            else {
                                ComboBoxItem value = (ComboBoxItem) comboBoxes.get(combo).getSelectedItem();
                                rowData.add(value.getDisplayValue());
                                combo++;
                            }

                        }
                        else {
                            c++;
                        }
                    }
                    rowData.addAll(inputValues);
                    tableModel.insertRow(modelRow,rowData.toArray());
                    dialog = null;
                });
                // Установка панели в диалоговое окно
                dialog.getContentPane().add(panel);

                // Отображение диалогового окна
                dialog.pack();

                // Отображение диалогового окна
                dialog.setVisible(true);

            }
        }
    }
    private void deleteDirectory(String currentTable,DefaultTableModel tableModel,  HashMap<String,List<String>> columns,
                                 HashMap<String,String> primaryKeys) {
        // List<String> currentColumns = columns.get(currentTable);
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            int index = columns.get(currentTable).indexOf(primaryKeys.get(currentTable));
            int id = Integer.parseInt(String.valueOf(tableModel.getValueAt(modelRow, index)));
            String prime = primaryKeys.get(currentTable);
            try {
                String sqlQuary = String.format("DELETE FROM " + currentTable + " WHERE " + prime + " = ?");
                PreparedStatement statement = connection.prepareStatement(sqlQuary);
                statement.setInt(1,id);
                statement.executeUpdate();
                statement.close();

                tableModel.removeRow(modelRow);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private static Comparator<Object> setupColumnComparator(int columnIndex, HashMap<String, List<String>> types,
                                                            HashMap<String, List<String>> columns,
                                                            HashMap<String, String> primaryKeys, String currentTable,
                                                            HashMap<String,List<String>> connectionTable) {

        List<String> primList = new ArrayList<>();
        for (Map.Entry<String, String> entry : primaryKeys.entrySet()) {

            String value = entry.getValue();
            if (!value.equals(primaryKeys.get(currentTable))) {
                primList.add(value);
            }

        }
        primList.retainAll(columns.get(currentTable));

        String type = types.get(currentTable).get(columnIndex);

        if(primList.contains(columns.get(currentTable).get(columnIndex))) {
            String currentColl = columns.get(currentTable).get(columnIndex);
            String foreignTable = connectionTable.get(currentColl).get(0);
            String foreignCol = connectionTable.get(currentColl).get(1);
            int index = columns.get(foreignTable).indexOf(foreignCol);
            type = (types.get(foreignTable).get(index)
            );
        }
        switch (type) {
            case "int2":
            case "int4":
                return Comparator.comparingInt(o -> {
                    if (o instanceof String) {
                        return Integer.parseInt((String) o);
                    } else if (o instanceof Integer) {
                        return (int) o;
                    } else {
                        throw new IllegalArgumentException("Unsupported type for comparison: " + o.getClass());
                    }
                });
            case "bigserial":
            case "int8":
                return Comparator.comparingLong(o -> {
                    if (o instanceof String) {
                        return Long.parseLong((String) o);
                    } else if (o instanceof Long) {
                        return (long) o;
                    } else {
                        throw new IllegalArgumentException("Unsupported type for comparison: " + o.getClass());
                    }
                });
            case "varchar":
            case "bpchar":
            case "text":
            case "bytea":
                return Comparator.comparing(Object::toString);
            case "numeric":
                return Comparator.comparing(DirectoryApp::parseBigDecimal);
            case "date":
                return Comparator.comparing(DirectoryApp::parseDate);
            case "float4":
            case "float8":
                return Comparator.comparingDouble(o -> {
                    if (o instanceof String) {
                        return Double.parseDouble((String) o);
                    } else if (o instanceof Double) {
                        return (double) o;
                    } else {
                        throw new IllegalArgumentException("Unsupported type for comparison: " + o.getClass());
                    }
                });

            case "time":
                return Comparator.comparing(DirectoryApp::parseTime);
            case "timestamp":
                return Comparator.comparing(DirectoryApp::parseTimestamp);
            case "bool":
                return Comparator.comparingInt(o -> {
                    if (o instanceof String) {
                        return Boolean.parseBoolean((String) o) ? 1 : 0;
                    } else if (o instanceof Boolean) {
                        return (Boolean) o ? 1 : 0;
                    } else {
                        throw new IllegalArgumentException("Unsupported type for comparison: " + o.getClass());
                    }
                });
            default:
                throw new IllegalArgumentException("Unsupported data type: " + type);
        }
    }

    private static BigDecimal parseBigDecimal(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof BigDecimal) {
            return (BigDecimal) o;
        }
        try {
            String stringValue = o.toString();

            // Create a NumberFormat for the default locale and set the decimal separator to a dot
            NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
            numberFormat.setParseIntegerOnly(false);
            numberFormat.setMaximumFractionDigits(Integer.MAX_VALUE);

            return new BigDecimal(numberFormat.parse(stringValue).toString());
        } catch (ParseException | NumberFormatException e) {
            System.err.println("Error parsing BigDecimal: " + o);
            throw new IllegalArgumentException("Unable to parse BigDecimal: " + o, e);
        }
    }


    private static Time parseTime(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Time) {
            return (Time) o;
        }
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            return new Time(timeFormat.parse(o.toString()).getTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to parse Time: " + o);
        }
    }

    private static Timestamp parseTimestamp(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Timestamp) {
            return (Timestamp) o;
        }
        try {
            SimpleDateFormat timestampFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            return new Timestamp(timestampFormat.parse(o.toString()).getTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to parse Timestamp: " + o);
        }
    }
    private static java.sql.Date parseDate(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof java.sql.Date) {
            return (java.sql.Date) o;
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            java.util.Date utilDate = dateFormat.parse(o.toString());
            return new java.sql.Date(utilDate.getTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to parse Date: " + o);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DirectoryApp::new);
    }
}
private LegalConsultSystem system;
private final String DATA_FILE = "legal_system.dat";

private DefaultTableModel lawyersModel = new DefaultTableModel(new String[]{"Name","Spec","Phone","Consult"},0);
private DefaultTableModel clientsModel = new DefaultTableModel(new String[]{"ID","Name","Phone"},0);
private DefaultTableModel servicesModel = new DefaultTableModel(new String[]{"ID","Type","Name","Cost","Date","Lawyer","Client"},0);

public MainGUI() {
    try {
        system = DataStore.load(DATA_FILE);
    } catch (Exception e) {
        system = new LegalConsultSystem();
    }

    setTitle("Юридична консультація — Система");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(900,600);
    setLocationRelativeTo(null);

    JTabbedPane tabs = new JTabbedPane();

    tabs.add("Юристи", createLawyersPanel());
    tabs.add("Клієнти", createClientsPanel());
    tabs.add("Послуги", createServicesPanel());
    tabs.add("Звіти", createReportsPanel());

    refreshAllTables();
    add(tabs);

    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            try { DataStore.save(system, DATA_FILE); }
            catch (Exception ex) { ex.printStackTrace(); }
        }
    });
}

private boolean isValidPhone(String phone) {
    return phone.matches("^\\+?\\d[\\d\\s\\-]{7,14}\\d$");
}

private boolean isValidClientId(String id) {
    return id.matches("^[A-Za-z0-9]{2,20}$");
}

private JPanel createLawyersPanel() {
    JPanel p = new JPanel(new BorderLayout());
    JTable table = new JTable(lawyersModel);
    p.add(new JScrollPane(table), BorderLayout.CENTER);

    JPanel form = new JPanel(new GridLayout(5,2,5,5));
    JTextField consultField = new JTextField();
    JTextField nameField = new JTextField();
    JTextField phoneField = new JTextField();
    JTextField specField = new JTextField();

    form.add(new JLabel("Назва консультації:")); form.add(consultField);
    form.add(new JLabel("Ім'я юриста:")); form.add(nameField);

    form.add(new JLabel("Телефон:")); form.add(phoneField);
    form.add(new JLabel("Спеціалізація:")); form.add(specField);

    JButton addBtn = new JButton("Додати юриста");
    addBtn.addActionListener(e -> {
        String consult = consultField.getText().trim();
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String spec = specField.getText().trim();

        if(name.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Введіть ім'я юриста");
            return;
        }

        if(!phone.isEmpty() && !isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this,"Невірний формат телефону!");
            return;
        }

        Lawyer lw = new Lawyer(consult, name, phone, spec);
        system.addLawyer(lw);
        refreshLawyers();
        saveData();

        consultField.setText(""); nameField.setText("");
        phoneField.setText(""); specField.setText("");
    });

    JButton delBtn = new JButton("Видалити вибраний");
    delBtn.addActionListener(e -> {
        int row = table.getSelectedRow();
        if(row >= 0) {
            String name = (String)lawyersModel.getValueAt(row,0);
            system.findLawyerByName(name).ifPresent(l -> system.removeLawyer(l));
            refreshLawyers(); saveData();
        }
    });

    JPanel bottom = new JPanel();
    bottom.add(addBtn);
    bottom.add(delBtn);

    p.add(form, BorderLayout.NORTH);
    p.add(bottom, BorderLayout.SOUTH);
    return p;
}

private JPanel createClientsPanel() {
    JPanel p = new JPanel(new BorderLayout());
    JTable table = new JTable(clientsModel);
    p.add(new JScrollPane(table), BorderLayout.CENTER);

    JPanel form = new JPanel(new GridLayout(4,2,5,5));
    JTextField idField = new JTextField();
    JTextField nameField = new JTextField();
    JTextField phoneField = new JTextField();

    form.add(new JLabel("ID:")); form.add(idField);
    form.add(new JLabel("ПІБ:")); form.add(nameField);
    form.add(new JLabel("Телефон:")); form.add(phoneField);

    JButton addBtn = new JButton("Додати клієнта");
    addBtn.addActionListener(e -> {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();

        if(id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this,"ID і ПІБ обов'язкові");
            return;
        }

        if(!isValidClientId(id)) {
            JOptionPane.showMessageDialog(this,"Невірний формат ID! Використовуйте лише букви та цифри (2–20 символів).");
            return;
        }

        if(!phone.isEmpty() && !isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this,"Невірний формат телефону!");
            return;
        }

        Client c = new Client(id, name, phone);
        system.addClient(c);
        refreshClients();
        saveData();

        idField.setText(""); nameField.setText(""); phoneField.setText("");
    });

    JButton delBtn = new JButton("Видалити вибраний");
    delBtn.addActionListener(e -> {
        int row = table.getSelectedRow();
        if(row >= 0) {
            String id = (String)clientsModel.getValueAt(row,0);
            system.findClientById(id).ifPresent(c -> system.removeClient(c));
            refreshClients(); saveData();
        }
    });

    JPanel bottom = new JPanel();
    bottom.add(addBtn); bottom.add(delBtn);

    p.add(form, BorderLayout.NORTH);
    p.add(bottom, BorderLayout.SOUTH);
    return p;
}

private JPanel createServicesPanel() {
    JPanel p = new JPanel(new BorderLayout());
    JTable table = new JTable(servicesModel);
    p.add(new JScrollPane(table), BorderLayout.CENTER);

    JPanel form = new JPanel(new GridLayout(9,2,5,5));
    JTextField idField = new JTextField();
    JTextField typeField = new JTextField();
    JTextField nameField = new JTextField();
    JTextField costField = new JTextField();
    JTextField dateField = new JTextField();
    JTextField lawyerField = new JTextField();
    JTextField clientField = new JTextField();

    DatePickerPopup picker = new DatePickerPopup(dateField);
    JButton pickDateBtn = new JButton("Обрати дату");
    pickDateBtn.addActionListener(e -> picker.show(pickDateBtn, 0, pickDateBtn.getHeight()));

    dateField.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                picker.show(dateField, e.getX(), e.getY());
            }
        }
    });

    form.add(new JLabel("ID послуги:")); form.add(idField);
    form.add(new JLabel("Тип:")); form.add(typeField);
    form.add(new JLabel("Назва:")); form.add(nameField);
    form.add(new JLabel("Вартість:")); form.add(costField);
    form.add(new JLabel("Дата (yyyy-MM-dd):")); form.add(dateField);
    form.add(new JLabel("")); form.add(pickDateBtn);
    form.add(new JLabel("Юрист (ім'я):")); form.add(lawyerField);
    form.add(new JLabel("ID клієнта:")); form.add(clientField);

    JButton addBtn = new JButton("Додати послугу");
    addBtn.addActionListener(e -> {
        try {
            String id = idField.getText().trim();
            String type = typeField.getText().trim();
            String name = nameField.getText().trim();
            double cost = Double.parseDouble(costField.getText().trim());
            String date = dateField.getText().trim();
            String lawyer = lawyerField.getText().trim();
            String clientId = clientField.getText().trim();

            LocalDate.parse(date);

            if(!isValidClientId(clientId)) {
                JOptionPane.showMessageDialog(this,"ID клієнта у неправильному форматі!");
                return;
            }

            Service s = new Service(id,type,name,cost,date,lawyer,clientId);
            system.addService(s);
            refreshServices();
            saveData();

            idField.setText(""); typeField.setText(""); nameField.setText("");
            costField.setText(""); dateField.setText("");
            lawyerField.setText(""); clientField.setText("");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Помилка при додаванні послуги: " + ex.getMessage());
        }
    });

    JButton delBtn = new JButton("Видалити вибраний");
    delBtn.addActionListener(e -> {
        int row = table.getSelectedRow();
        if(row >= 0) {
            String id = (String)servicesModel.getValueAt(row,0);
            system.getServices().stream().filter(s -> s.getId().equals(id)).findFirst().ifPresent(s -> system.removeService(s));
            refreshServices(); saveData();
        }
    });

    JPanel bottom = new JPanel();
    bottom.add(addBtn); bottom.add(delBtn);

    p.add(form, BorderLayout.NORTH);
    p.add(bottom, BorderLayout.SOUTH);
    return p;
}

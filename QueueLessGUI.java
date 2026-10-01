import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;

public class QueueLessGUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Data structures
    private Map<String, User> registeredUsers = new HashMap<>();
    private Map<String, java.util.List<String>> departmentQueues = new HashMap<>();
    private Map<String, Integer> ticketCounters = new HashMap<>();

    // Active session
    private User currentUser = null;

    // GUI components for updates
    private JLabel studentWelcomeLabel;
    private JLabel studentTicketLabel;
    private JLabel studentPositionLabel;
    private JLabel staffWelcomeLabel;
    private JTextArea staffQueueDisplay;

    public QueueLessGUI() {
        setTitle("QueueLess - Digital Queue Management System");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize queue departments
        String[] depts = {"Registrar", "Cashier", "Clinic"};
        for (String d : depts) {
            departmentQueues.put(d, new ArrayList<>());
            ticketCounters.put(d, 1);
        }

        // Seed initial staff members
        registeredUsers.put("STF-01", new Staff("STF-01", "Ms. Santos", "Registrar"));
        registeredUsers.put("STF-02", new Staff("STF-02", "Mr. Reyes", "Cashier"));

        // Set up CardLayout navigation
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createAuthPanel(), "AUTH");
        mainPanel.add(createStudentPanel(), "STUDENT");
        mainPanel.add(createStaffPanel(), "STAFF");

        add(mainPanel);
        cardLayout.show(mainPanel, "AUTH");
    }

    // --- SCREEN 1: Sign Up & Login Panel ---
    private JPanel createAuthPanel() {
        JPanel panel = new JPanel(new GridLayout(11, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("Welcome to QueueLess", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Student", "Staff"});
        JComboBox<String> deptCombo = new JComboBox<>(new String[]{"Registrar", "Cashier", "Clinic"});

        JButton loginBtn = new JButton("Log In");
        JButton signUpBtn = new JButton("Sign Up");

        panel.add(titleLabel);
        panel.add(new JLabel("User ID / Student ID:"));
        panel.add(idField);
        panel.add(new JLabel("Full Name (for Sign Up):"));
        panel.add(nameField);
        panel.add(new JLabel("Role:"));
        panel.add(roleCombo);
        panel.add(new JLabel("Department (Staff only):"));
        panel.add(deptCombo);

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(loginBtn);
        btnPanel.add(signUpBtn);
        panel.add(btnPanel);

        // Sign Up Action
        signUpBtn.addActionListener((ActionEvent e) -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter ID and Name.");
                return;
            }
            if (registeredUsers.containsKey(id)) {
                JOptionPane.showMessageDialog(this, "ID already exists!");
                return;
            }

            if (roleCombo.getSelectedItem().equals("Student")) {
                registeredUsers.put(id, new Student(id, name));
            } else {
                String dept = (String) deptCombo.getSelectedItem();
                registeredUsers.put(id, new Staff(id, name, dept));
            }
            JOptionPane.showMessageDialog(this, "Account created! You can now log in.");
        });

        // Login Action
        loginBtn.addActionListener((ActionEvent e) -> {
            String id = idField.getText().trim();
            if (!registeredUsers.containsKey(id)) {
                JOptionPane.showMessageDialog(this, "Account not found. Please sign up.");
                return;
            }

            currentUser = registeredUsers.get(id);
            if (currentUser instanceof Student) {
                updateStudentDashboard();
                cardLayout.show(mainPanel, "STUDENT");
            } else if (currentUser instanceof Staff) {
                updateStaffDashboard();
                cardLayout.show(mainPanel, "STAFF");
            }
        });

        return panel;
    }

    // --- SCREEN 2: Student Remote Queue Interface ---
    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        studentWelcomeLabel = new JLabel("Student Dashboard", SwingConstants.CENTER);
        studentWelcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(studentWelcomeLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        studentTicketLabel = new JLabel("Your Ticket: None", SwingConstants.CENTER);
        studentPositionLabel = new JLabel("Position in Queue: -", SwingConstants.CENTER);

        JComboBox<String> serviceCombo = new JComboBox<>(new String[]{"Registrar", "Cashier", "Clinic"});
        JButton getTicketBtn = new JButton("Get Queue Ticket");

        centerPanel.add(studentTicketLabel);
        centerPanel.add(studentPositionLabel);
        centerPanel.add(new JLabel("Select Department Service:", SwingConstants.CENTER));
        centerPanel.add(serviceCombo);
        centerPanel.add(getTicketBtn);

        panel.add(centerPanel, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("Log Out");
        panel.add(logoutBtn, BorderLayout.SOUTH);

        // Get Ticket Action
        getTicketBtn.addActionListener((ActionEvent e) -> {
            Student student = (Student) currentUser;
            String dept = (String) serviceCombo.getSelectedItem();

            int count = ticketCounters.get(dept);
            String ticketNum = dept.toUpperCase().substring(0, 3) + "-" + String.format("%03d", count);
            ticketCounters.put(dept, count + 1);

            departmentQueues.get(dept).add(ticketNum);
            student.setCurrentQueueNumber(ticketNum);

            updateStudentDashboard();
            JOptionPane.showMessageDialog(this, "Ticket generated: " + ticketNum);
        });

        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "AUTH"));

        return panel;
    }

    // --- SCREEN 3: Staff Management Panel ---
    private JPanel createStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        staffWelcomeLabel = new JLabel("Staff Dashboard", SwingConstants.CENTER);
        staffWelcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(staffWelcomeLabel, BorderLayout.NORTH);

        staffQueueDisplay = new JTextArea();
        staffQueueDisplay.setEditable(false);
        panel.add(new JScrollPane(staffQueueDisplay), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton callNextBtn = new JButton("Call Next Ticket");
        JButton logoutBtn = new JButton("Log Out");

        bottomPanel.add(callNextBtn);
        bottomPanel.add(logoutBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Call Next Ticket Action
        callNextBtn.addActionListener((ActionEvent e) -> {
            Staff staff = (Staff) currentUser;
            java.util.List<String> queue = departmentQueues.get(staff.getAssignedDepartment());

            if (queue.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No students waiting for " + staff.getAssignedDepartment());
            } else {
                String calledTicket = queue.remove(0);
                JOptionPane.showMessageDialog(this, "Now Serving: " + calledTicket);
                updateStaffDashboard();
            }
        });

        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "AUTH"));

        return panel;
    }

    // Helper updates
    private void updateStudentDashboard() {
        Student student = (Student) currentUser;
        studentWelcomeLabel.setText("Welcome, " + student.getName() + " (" + student.getId() + ")");
        if (student.getCurrentQueueNumber() != null) {
            studentTicketLabel.setText("Your Ticket: " + student.getCurrentQueueNumber());
            
            // Calculate position across queues
            int pos = -1;
            for (String dept : departmentQueues.keySet()) {
                int idx = departmentQueues.get(dept).indexOf(student.getCurrentQueueNumber());
                if (idx != -1) {
                    pos = idx + 1;
                    break;
                }
            }
            studentPositionLabel.setText(pos > 0 ? "Position in Queue: " + pos : "Status: Called / Complete");
        } else {
            studentTicketLabel.setText("Your Ticket: None");
            studentPositionLabel.setText("Position in Queue: -");
        }
    }

    private void updateStaffDashboard() {
        Staff staff = (Staff) currentUser;
        staffWelcomeLabel.setText("Staff: " + staff.getName() + " | " + staff.getAssignedDepartment());

        java.util.List<String> queue = departmentQueues.get(staff.getAssignedDepartment());
        StringBuilder sb = new StringBuilder("Current Waiting Tickets:\n--------------------------\n");
        if (queue.isEmpty()) {
            sb.append("(Queue is currently empty)");
        } else {
            for (int i = 0; i < queue.size(); i++) {
                sb.append((i + 1)).append(". ").append(queue.get(i)).append("\n");
            }
        }
        staffQueueDisplay.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new QueueLessGUI().setVisible(true);
        });
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;

public class StudentDashboard {
    private JFrame frame;
    private JButton insertButton, deleteButton, showAllButton, manageMarksButton;
    private Connection connection;

    public StudentDashboard() {
        initialize();
        connectToDatabase();
    }

    private void initialize() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("RustyProf: Automated Student Assessment & Report Generation System");
        frame.setSize(1000, 600);
        frame.setMinimumSize(new Dimension(800, 500));
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(248, 249, 250));

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        mainPanel.setBackground(new Color(248, 249, 250));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 249, 250));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Title label
        JLabel titleLabel = new JLabel("<html><span style='color:#5E35B1;'>RustyProf</span>: Automated Student Assessment & Report Generation System</html>");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        // Overview text
        JTextPane overviewPane = new JTextPane();
        overviewPane.setContentType("text/html");
        overviewPane.setEditable(false);
        overviewPane.setBackground(new Color(248, 249, 250));
        overviewPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        String overviewText = "<html><body style='margin: 0; padding: 0; font-family: sans-serif; font-size: 13px; color: #495057; text-align: justify;'>" +
                "<p style='margin-bottom: 15px; line-height: 1.6; margin-top: 20px;'><b style='font-size:14px; color: #5E35B1;'>Project Overview:</b> RustyProf is the chill professor's secret weapon for handling assessments at scale without the burnout. " +
                "Instead of wasting time writing individual learning plans, the prof just enters quick remarks based on the student's marks, " +
                "and the AI takes over from there. RustyProf uses the Mistral-Nemo-Instruct-2407 model to instantly turn those remarks into personalized " +
                "learning paths for each student. The app supports five core subjects like Machine Learning (ML), Object-Oriented Programming Systems (OOPS), " +
                "Computer Organization and Architecture (COA), Database Management Systems (DBMS), and Operating Systems (OS), and auto-generates " +
                "ranked reports for each subject. Professors can edit or delete student records by name or SIC, and enter or update marks easily. Reports are generated " +
                "in a clean PDF format with a QR code that pops up for scanning & perfect for downloading the report directly to a phone. " +
                "The UI is crafted with old-school Java Swing and AWT, backed by a solid MySQL database to keep all the data tight. Students get real guidance, " +
                "profs save a ton of time, and the whole process stays smooth and smart.</p>" +
                "<p style='margin-top: 20px; line-height: 1.6;'><b style='font-size:14px; color: #5E35B1;'>Tech Stack:</b> Java, MySQL, Swing, AWT, Mistral-Nemo Model, iText, ZXing, Postman, Hugging Face, IntelliJ</p>" +
                "</body></html>";

        overviewPane.setText(overviewText);

        JScrollPane scrollPane = new JScrollPane(overviewPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        headerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        buttonPanel.setBackground(new Color(248, 249, 250));

        // Create professional buttons with purple theme
        insertButton = createModernButton("Add New Student", new Color(237, 231, 246), new Color(94, 53, 177));
        deleteButton = createModernButton("Remove Student", new Color(237, 231, 246), new Color(94, 53, 177));
        showAllButton = createModernButton("Student Dashboard", new Color(237, 231, 246), new Color(94, 53, 177));
        manageMarksButton = createModernButton("Academic Records", new Color(237, 231, 246), new Color(94, 53, 177));

        buttonPanel.add(insertButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(showAllButton);
        buttonPanel.add(manageMarksButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel, BorderLayout.CENTER);

        // Add action listeners
        insertButton.addActionListener(e -> showAddStudentDialog());
        deleteButton.addActionListener(e -> showRemoveStudentDialog());
        showAllButton.addActionListener(e -> showStudentDashboard());
        manageMarksButton.addActionListener(e -> showAcademicRecordsDialog());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JButton createModernButton(String text, Color bgColor, Color borderColor) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        button.setBackground(bgColor);
        button.setForeground(new Color(33, 37, 41));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, borderColor),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect with purple theme
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(206, 189, 233));
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(69, 39, 160)),
                        BorderFactory.createEmptyBorder(12, 15, 12, 15)
                ));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, borderColor),
                        BorderFactory.createEmptyBorder(12, 15, 12, 15)
                ));
            }
        });

        return button;
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/student_management",
                    "root",
                    "9835388994");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Database connection failed: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void showAddStudentDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField nameField = new JTextField();
        JTextField sicField = new JTextField();
        JTextField branchField = new JTextField();

        panel.add(new JLabel("Student Name:"));
        panel.add(nameField);
        panel.add(new JLabel("SIC:"));
        panel.add(sicField);
        panel.add(new JLabel("Branch:"));
        panel.add(branchField);

        int result = JOptionPane.showConfirmDialog(
                frame, panel, "Add New Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String sic = sicField.getText().trim();
                String branch = branchField.getText().trim();

                if (name.isEmpty() || sic.isEmpty() || branch.isEmpty()) {
                    showErrorMessage("All fields are required!");
                    return;
                }

                String query = "INSERT INTO students (sic, name, branch) VALUES (?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, sic);
                    statement.setString(2, name);
                    statement.setString(3, branch);
                    statement.executeUpdate();
                    showSuccessMessage("Student added successfully!");
                }
            } catch (SQLException e) {
                showErrorMessage("Error adding student: " + e.getMessage());
            }
        }
    }

    private void showRemoveStudentDialog() {
        String sic = JOptionPane.showInputDialog(frame, "Enter Student SIC to remove:");

        if (sic != null && !sic.trim().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "Are you sure you want to delete student with SIC: " + sic + "?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String query = "DELETE FROM students WHERE sic = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setString(1, sic);
                        int rows = statement.executeUpdate();
                        if (rows > 0) {
                            showSuccessMessage("Student removed successfully!");
                        } else {
                            showErrorMessage("No student found with SIC: " + sic);
                        }
                    }
                } catch (SQLException e) {
                    showErrorMessage("Error removing student: " + e.getMessage());
                }
            }
        }
    }

    private void showStudentDashboard() {
        try {
            String query = "SELECT * FROM students ORDER BY name";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                StringBuilder html = new StringBuilder();
                html.append("<html><style>")
                        .append("body { font-family: sans-serif; margin: 10px; }")
                        .append("table { width: 100%; border-collapse: collapse; }")
                        .append("th { text-align: left; padding: 12px; background: #f8f9fa; border: 1px solid #dee2e6; color: #495057; }")
                        .append("td { padding: 12px; border: 1px solid #dee2e6; color: #212529; }")
                        .append("</style><body>")
                        .append("<h3 style='color: #212529;'>Student Records</h3>")
                        .append("<table><tr><th>SIC</th><th>Name</th><th>Branch</th></tr>");

                while (resultSet.next()) {
                    html.append("<tr>")
                            .append("<td>").append(resultSet.getString("sic")).append("</td>")
                            .append("<td>").append(resultSet.getString("name")).append("</td>")
                            .append("<td>").append(resultSet.getString("branch")).append("</td>")
                            .append("</tr>");
                }

                html.append("</table></body></html>");

                JEditorPane editorPane = new JEditorPane("text/html", html.toString());
                editorPane.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(editorPane);
                scrollPane.setPreferredSize(new Dimension(700, 500));

                JOptionPane.showMessageDialog(frame, scrollPane, "Student Dashboard", JOptionPane.PLAIN_MESSAGE);
            }
        } catch (SQLException e) {
            showErrorMessage("Error retrieving students: " + e.getMessage());
        }
    }

    private void showAcademicRecordsDialog() {
        JDialog dialog = new JDialog(frame, "Academic Records", true);
        dialog.setSize(700, 500);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(frame);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(248, 249, 250));

        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        JPanel searchInputPanel = new JPanel(new BorderLayout(10, 10));
        JLabel searchLabel = new JLabel("Enter SIC:");
        searchLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        searchInputPanel.add(searchLabel, BorderLayout.WEST);
        JTextField sicField = new JTextField();
        searchInputPanel.add(sicField, BorderLayout.CENTER);

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        searchButton.setPreferredSize(new Dimension(80, sicField.getPreferredSize().height));
        searchPanel.add(searchInputPanel, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JLabel nameLabelTitle = new JLabel("Student Name:");
        nameLabelTitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        infoPanel.add(nameLabelTitle);

        JLabel nameLabel = new JLabel("");
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        infoPanel.add(nameLabel);

        JLabel branchLabelTitle = new JLabel("Branch:");
        branchLabelTitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        infoPanel.add(branchLabelTitle);

        JLabel branchLabel = new JLabel("");
        branchLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        infoPanel.add(branchLabel);

        // Marks Panel
        JPanel marksPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        String[] subjects = {
                "Machine Learning (ML)",
                "Object-Oriented Programming (OOPS)",
                "Computer Organization (COA)",
                "Database Systems (DBMS)",
                "Operating Systems (OS)"
        };
        JTextField[] marksFields = new JTextField[subjects.length];

        for (int i = 0; i < subjects.length; i++) {
            JLabel subjectLabel = new JLabel(subjects[i] + ":");
            subjectLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            marksPanel.add(subjectLabel);
            marksFields[i] = new JTextField();
            marksFields[i].setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            marksPanel.add(marksFields[i]);
        }

        JLabel remarksLabel = new JLabel("Remarks:");
        remarksLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        marksPanel.add(remarksLabel);
        JTextField remarksField = new JTextField();
        remarksField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        marksPanel.add(remarksField);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton saveButton = createModernButton("Save Records", new Color(193, 239, 163), new Color(87, 117, 80));
        JButton reportButton = createModernButton("Generate Report", new Color(193, 239, 163), new Color(87, 117, 80));

        buttonPanel.add(saveButton);
        buttonPanel.add(reportButton);

        // Add components
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(marksPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Search action
        searchButton.addActionListener(e -> {
            String sic = sicField.getText().trim();
            if (sic.isEmpty()) {
                showErrorMessage("Please enter SIC");
                return;
            }

            try {
                // Get student info
                String studentQuery = "SELECT * FROM students WHERE sic = ?";
                try (PreparedStatement stmt = connection.prepareStatement(studentQuery)) {
                    stmt.setString(1, sic);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        nameLabel.setText(rs.getString("name"));
                        branchLabel.setText(rs.getString("branch"));
                    } else {
                        showErrorMessage("Student not found");
                        return;
                    }
                }

                // Get marks
                String marksQuery = "SELECT * FROM academic_records WHERE student_sic = ?";
                try (PreparedStatement stmt = connection.prepareStatement(marksQuery)) {
                    stmt.setString(1, sic);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        marksFields[0].setText(rs.getString("ml_marks"));
                        marksFields[1].setText(rs.getString("oops_marks"));
                        marksFields[2].setText(rs.getString("coa_marks"));
                        marksFields[3].setText(rs.getString("dbms_marks"));
                        marksFields[4].setText(rs.getString("os_marks"));
                        remarksField.setText(rs.getString("remarks"));
                    } else {
                        // Clear fields if no records
                        for (JTextField field : marksFields) {
                            field.setText("");
                        }
                        remarksField.setText("");
                    }
                }
            } catch (SQLException ex) {
                showErrorMessage("Database error: " + ex.getMessage());
            }
        });

        // Save action
        saveButton.addActionListener(e -> {
            String sic = sicField.getText().trim();
            if (sic.isEmpty()) {
                showErrorMessage("Please search for a student first");
                return;
            }

            try {
                // Validate marks
                for (JTextField field : marksFields) {
                    String text = field.getText().trim();
                    if (!text.isEmpty()) {
                        try {
                            int marks = Integer.parseInt(text);
                            if (marks < 0 || marks > 100) {
                                showErrorMessage("Marks must be between 0 and 100");
                                return;
                            }
                        } catch (NumberFormatException ex) {
                            showErrorMessage("Please enter valid numbers for marks");
                            return;
                        }
                    }
                }

                // Check if record exists
                String checkQuery = "SELECT COUNT(*) FROM academic_records WHERE student_sic = ?";
                boolean recordExists = false;
                try (PreparedStatement stmt = connection.prepareStatement(checkQuery)) {
                    stmt.setString(1, sic);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        recordExists = rs.getInt(1) > 0;
                    }
                }

                String query;
                if (recordExists) {
                    query = "UPDATE academic_records SET ml_marks=?, oops_marks=?, coa_marks=?, dbms_marks=?, os_marks=?, remarks=? WHERE student_sic=?";
                } else {
                    query = "INSERT INTO academic_records (ml_marks, oops_marks, coa_marks, dbms_marks, os_marks, remarks, student_sic) VALUES (?,?,?,?,?,?,?)";
                }

                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    for (int i = 0; i < marksFields.length; i++) {
                        String text = marksFields[i].getText().trim();
                        stmt.setString(i + 1, text.isEmpty() ? null : text);
                    }
                    stmt.setString(6, remarksField.getText().trim());
                    stmt.setString(7, sic);
                    stmt.executeUpdate();
                    showSuccessMessage("Academic records saved successfully!");
                }
            } catch (SQLException ex) {
                showErrorMessage("Error saving records: " + ex.getMessage());
            }
        });

        // Replace the existing reportButton action listener with this:
        reportButton.addActionListener(e -> {
            String sic = sicField.getText().trim();
            if (sic.isEmpty()) {
                showErrorMessage("Please search for a student first");
                return;
            }

            try {
                ReportGenerator.generateReport(connection, sic);
                showSuccessMessage("Report generated successfully as Report_" + sic + ".pdf");

                // Optionally open the PDF
                if (Desktop.isDesktopSupported()) {
                    try {
                        File file = new File("Report_" + sic + ".pdf");
                        Desktop.getDesktop().open(file);
                    } catch (Exception ex) {
                        // Ignore if we can't open the file
                    }
                }
            } catch (Exception ex) {
                showErrorMessage("Error generating report: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        dialog.setVisible(true);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudentDashboard();
        });
    }
}
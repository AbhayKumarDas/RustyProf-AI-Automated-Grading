import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.google.zxing.*;
import com.google.zxing.common.*;
import com.google.zxing.qrcode.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {
    // Font definitions (unchanged)
    private static final com.itextpdf.text.Font TITLE_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18,
                    com.itextpdf.text.Font.BOLD, BaseColor.DARK_GRAY);
    private static final com.itextpdf.text.Font SUBTITLE_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12,
                    com.itextpdf.text.Font.BOLD, BaseColor.DARK_GRAY);
    private static final com.itextpdf.text.Font HEADER_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10,
                    com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
    private static final com.itextpdf.text.Font NORMAL_FONT =
            new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10,
                    com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);

    // API Configuration
    private static final String API_KEY = "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";
    private static final String API_URL = "https://router.huggingface.co/nebius/v1/chat/completions";

    public static void generateReport(Connection connection, String sic) {
        try {
            // Step 1: Get all student data
            System.out.println("[DEBUG] Fetching student data for SIC: " + sic);
            StudentData data = getStudentData(connection, sic);

            // Step 2: Generate learning path
            System.out.println("[DEBUG] Generating learning path...");
            String learningPath = generateLearningPath(data.remarks, data.subjectMarks, data.percentage);
            if (learningPath == null || learningPath.isEmpty()) {
                throw new Exception("Failed to generate learning path");
            }

            System.out.println("=== GENERATED LEARNING PATH ===\n" + learningPath + "\n=============================");

            // Step 3: Generate PDF
            System.out.println("[DEBUG] Generating PDF report...");
            String filePath = generatePdf(data, learningPath);

            // Step 4: Offer download options
            System.out.println("[DEBUG] Report generated at: " + filePath);
            offerDownloadOptions(filePath);

        } catch (Exception e) {
            System.err.println("[ERROR] Report generation failed: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error generating report: " + e.getMessage(),
                    "Report Generation Failed",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static class StudentData {
        String name;
        String sic;
        String branch;
        Map<String, String> subjectMarks;
        int total;
        double percentage;
        String remarks;
    }

    private static StudentData getStudentData(Connection connection, String sic) throws SQLException {
        // Implementation unchanged from original
        String query = "SELECT s.name, s.branch, a.ml_marks, a.oops_marks, a.coa_marks, " +
                "a.dbms_marks, a.os_marks, a.remarks " +
                "FROM students s LEFT JOIN academic_records a ON s.sic = a.student_sic " +
                "WHERE s.sic = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, sic);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Student not found with SIC: " + sic);
            }

            StudentData data = new StudentData();
            data.name = rs.getString("name");
            data.sic = sic;
            data.branch = rs.getString("branch");
            data.remarks = rs.getString("remarks");

            // Process marks
            data.subjectMarks = new LinkedHashMap<>();
            data.subjectMarks.put("Machine Learning", rs.getString("ml_marks"));
            data.subjectMarks.put("OOPS", rs.getString("oops_marks"));
            data.subjectMarks.put("COA", rs.getString("coa_marks"));
            data.subjectMarks.put("DBMS", rs.getString("dbms_marks"));
            data.subjectMarks.put("Operating Systems", rs.getString("os_marks"));

            // Calculate total and percentage
            data.total = 0;
            int count = 0;
            for (String mark : data.subjectMarks.values()) {
                if (mark != null) {
                    data.total += Integer.parseInt(mark);
                    count++;
                }
            }
            data.percentage = count > 0 ? (double) data.total / (count * 100) * 100 : 0;

            return data;
        }
    }

    private static String generateLearningPath(String remarks, Map<String, String> subjectMarks, double percentage) {
        try {
            // Build simple paragraph prompt
            StringBuilder prompt = new StringBuilder();
            prompt.append("Generate a personalized learning path for a computer science student who scored ");

            // Add marks
            prompt.append("Machine Learning: ").append(subjectMarks.get("Machine Learning")).append(", ");
            prompt.append("OOPS: ").append(subjectMarks.get("OOPS")).append(", ");
            prompt.append("COA: ").append(subjectMarks.get("COA")).append(", ");
            prompt.append("DBMS: ").append(subjectMarks.get("DBMS")).append(", ");
            prompt.append("Operating Systems: ").append(subjectMarks.get("Operating Systems")).append(". ");

            // Add remarks and percentage
            prompt.append("The professor's remarks: '").append(remarks).append("'. ");
            prompt.append("Overall percentage: ").append(String.format("%.2f%%", percentage)).append(". ");

            // Add instructions
            prompt.append("Provide a concise learning path covering: strengths/weaknesses, focus areas, resources, weekly plan, and motivation.");

            String cleanPrompt = prompt.toString()
                    .replace("\"", "'")  // Replace double quotes with single quotes
                    .replace("\n", " ") // Remove newlines
                    .replace("\\", "\\\\"); // Escape backslashes

            System.out.println("[DEBUG] Sending API request with prompt: " + cleanPrompt);
            String response = callChatAPI(cleanPrompt);
            String result = extractAnswerFromResponse(response);
            System.out.println("[DEBUG] Received API response:\n" + result);
            return result;
        } catch (Exception e) {
            System.err.println("[ERROR] Learning Path Generation Error: " + e.getMessage());
            return "Could not generate learning path at this time. Please consult with your academic advisor.";
        }
    }

    private static String callChatAPI(String prompt) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setDoOutput(true);

        // Build JSON payload manually with proper escaping
        String payload = String.format(
                "{\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"model\":\"mistralai/Mistral-Nemo-Instruct-2407-fast\"}",
                prompt.replace("\"", "\\\"")
                        .replace("\n", "\\n")
        );

        System.out.println("[DEBUG] Sending payload:\n" + payload);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String errorResponse;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream()))) {
                errorResponse = br.lines().collect(Collectors.joining("\n"));
            }
            throw new IOException("API request failed with code: " + responseCode +
                    "\nResponse: " + errorResponse);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    private static String extractAnswerFromResponse(String jsonResponse) {
        // Simple extraction - look for "content" field
        int contentStart = jsonResponse.indexOf("\"content\":\"");
        if (contentStart == -1) {
            return "Could not parse API response. Raw response:\n" + jsonResponse;
        }
        contentStart += "\"content\":\"".length();
        int contentEnd = jsonResponse.indexOf("\"", contentStart);
        if (contentEnd == -1) {
            return "Could not parse API response. Raw response:\n" + jsonResponse;
        }

        return jsonResponse.substring(contentStart, contentEnd)
                .replace("\\n", "\n")
                .replace("\\\"", "\"");
    }

    // Rest of the methods remain unchanged (generatePdf, addPdfContent, offerDownloadOptions, etc.)
    private static String generatePdf(StudentData data, String learningPath) throws Exception {
        String fileName = "Report_" + data.sic + "_" + System.currentTimeMillis() + ".pdf";
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            addPdfContent(document, data, learningPath);
            return new File(fileName).getAbsolutePath();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private static void addPdfContent(Document document, StudentData data, String learningPath) throws DocumentException {
        // Title
        Paragraph title = new Paragraph("Student Academic Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Student Info Table
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(15);
        addInfoCell(infoTable, "Student Name:", data.name);
        addInfoCell(infoTable, "SIC:", data.sic);
        addInfoCell(infoTable, "Branch:", data.branch);
        document.add(infoTable);

        // Marksheet
        Paragraph marksTitle = new Paragraph("Marksheet", SUBTITLE_FONT);
        marksTitle.setSpacingAfter(10);
        document.add(marksTitle);

        PdfPTable marksTable = new PdfPTable(2);
        marksTable.setWidthPercentage(80);
        marksTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        marksTable.setSpacingAfter(15);

        addHeaderCell(marksTable, "Subject");
        addHeaderCell(marksTable, "Marks");

        for (Map.Entry<String, String> entry : data.subjectMarks.entrySet()) {
            addMarkRow(marksTable, entry.getKey(), entry.getValue());
        }

        addMarkRow(marksTable, "Total", String.valueOf(data.total));
        addMarkRow(marksTable, "Percentage", String.format("%.2f%%", data.percentage));
        document.add(marksTable);

        // Remarks
        if (data.remarks != null && !data.remarks.isEmpty()) {
            Paragraph remarksTitle = new Paragraph("Remarks:", SUBTITLE_FONT);
            remarksTitle.setSpacingAfter(5);
            document.add(remarksTitle);

            Paragraph remarksPara = new Paragraph(data.remarks, NORMAL_FONT);
            remarksPara.setSpacingAfter(15);
            document.add(remarksPara);
        }

        // Learning Path
        if (learningPath != null && !learningPath.isEmpty()) {
            Paragraph learningTitle = new Paragraph("Learning Path:", SUBTITLE_FONT);
            learningTitle.setSpacingAfter(5);
            document.add(learningTitle);

            Paragraph learningContent = new Paragraph(learningPath, NORMAL_FONT);
            learningContent.setSpacingAfter(15);
            document.add(learningContent);
        }
    }

    private static void offerDownloadOptions(String filePath) {
        String[] options = {"Open PDF", "Generate QR Code", "Done"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Report generated successfully at:\n" + filePath,
                "Report Ready",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            try {
                Desktop.getDesktop().open(new File(filePath));
            } catch (Exception e) {
                System.err.println("[ERROR] Could not open PDF: " + e.getMessage());
                JOptionPane.showMessageDialog(null,
                        "Could not open PDF: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (choice == 1) {
            showQrCodePopup(filePath);
        }
    }

    private static void showQrCodePopup(String filePath) {
        try {
            String qrContent = "file://" + new File(filePath).getAbsolutePath().replace("\\", "/");
            QRCodeWriter qrWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrWriter.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300);

            BufferedImage qrImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    qrImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            JFrame frame = new JFrame("Scan to Download Report");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            JLabel label = new JLabel("Scan this QR code to access the report:");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            frame.add(label, BorderLayout.NORTH);

            frame.add(new JLabel(new ImageIcon(qrImage)), BorderLayout.CENTER);

            JLabel pathLabel = new JLabel("<html><div style='text-align: center;'>File: " + filePath + "</div></html>");
            frame.add(pathLabel, BorderLayout.SOUTH);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception e) {
            System.err.println("[ERROR] QR Code Generation Failed: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "QR Code Generation Failed: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper methods for PDF table cells
    private static void addInfoCell(PdfPTable table, String label, String value) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, NORMAL_FONT));
        cell1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        table.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Phrase(value != null ? value : "N/A", NORMAL_FONT));
        cell2.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        table.addCell(cell2);
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(BaseColor.DARK_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private static void addMarkRow(PdfPTable table, String subject, String marks) {
        PdfPCell subjectCell = new PdfPCell(new Phrase(subject, NORMAL_FONT));
        subjectCell.setPadding(5);
        table.addCell(subjectCell);

        PdfPCell marksCell = new PdfPCell(new Phrase(marks != null ? marks : "N/A", NORMAL_FONT));
        marksCell.setPadding(5);
        marksCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(marksCell);
    }
}
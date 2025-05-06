# RustyProf: Automated Student Assessment & Report Generation System | Java + MySQL

RustyProf is an advanced Java-based application that streamlines student assessment and academic report generation using a MySQL database. It includes a user interface built with Swing and AWT, a specialized PDF creation module powered by iText, and a QR code feature implemented through ZXing. It also incorporates an AI-based learning path generation flow that relies on the Mistral-Nemo-Instruct-2407-fast model from Hugging Face, allowing professors to add short remarks for each student. Those remarks are then combined with marks data to produce personalized guidance. This system aims to reduce repetitive workload for academic staff while providing students with comprehensive performance insights in real time.

## Project Overview

RustyProf simplifies the entire workflow of adding and managing student records, editing marks for multiple core subjects, generating dynamic PDFs, and offering automated learning path recommendations. The user interface is displayed through the StudentDashboard, which serves as the main entry point for tasks such as adding or removing students, managing marks, and generating final reports. The code responsible for PDF creation and AI-based instructions resides in the ReportGenerator class. Reports include essential details like total marks, calculated percentages, professor remarks, and a path for improvement. Those reports can be opened directly from the application or downloaded using a scannable QR code that links to the PDF file on the local system. This combination of data management, user interaction, and AI generation is designed to allow professors and teaching assistants to provide individualized feedback without manually crafting extensive learning outlines.

## Methodology

RustyProf follows a straightforward yet robust methodology. It begins with user actions in StudentDashboard, where instructors fill out basic student data and subject-wise marks for five core domains: Machine Learning, Object-Oriented Programming Systems, Computer Organization and Architecture, Database Management Systems, and Operating Systems. The data is validated and stored in a MySQL database through a direct JDBC connection. When a report is requested, the application fetches the student’s stored remarks and performance records, then constructs a structured prompt that includes marks and professor notes. This prompt is sent to the Mistral-Nemo-Instruct-2407-fast model hosted on Hugging Face. After receiving a contextual response for improvement, the code merges these instructions with local data to form an organized academic report. The iText library is used to generate a professional PDF, and ZXing creates a QR code that links to the file’s path. The final PDF is then offered for immediate viewing or phone-based download via the pop-up QR code. By combining AI-driven insights with local record management, RustyProf delivers a modernized approach to academic evaluation, enabling quick scanning for mobile device use as well as immediate oversight on a desktop.

## Tech Stack

- **Programming Language**: Java
- **GUI Framework**: Swing + AWT
- **Database**: MySQL with JDBC integration
- **PDF Generation**: iText (version 5.5.13.3)
- **QR Code Generation**: ZXing (core + javase JARs)
- **AI Integration**: Mistral-Nemo-Instruct-2407-fast (accessed via Hugging Face Inference API)
- **IDE**: IntelliJ IDEA
- **API Testing Tool**: Postman (for Mistral endpoint)
- **Build Structure**: Flat folder layout for simplicity

## File Structure

The directory layout is visible in the files section includes a lib folder named JARS that contains essential libraries, an out/production/Rustyprof folder holding compiled .class files, a src folder with the main Java files, and a README file that summarizes the entire project. The key Java classes include StudentDashboard, which orchestrates the primary user interface and handles most user actions, and ReportGenerator, which creates the final academic reports and fetches AI-generated learning paths. The test file is also visible for trial runs or additional experimentation. Several PDF files named Report_<SIC>_timestamp are produced whenever the user generates a report. There is also a MySQL connector file in the top-level directory, ensuring that database connectivity is maintained without external scripts.

## Execution Steps

1. **Set up MySQL**  
   - Ensure MySQL is running on localhost  
   - Create the required database and table schema  
   - Configure DB credentials inside the Java code (URL, user, password)

2. **Add Required JARs**  
   - Place `mysql-connector-j-9.3.0.jar`, `itextpdf-5.5.13.3.jar`, `core-3.4.1.jar`, and `javase-3.4.1.jar` inside the `JARS` folder  
   - Add them to the classpath in IntelliJ or compile with them manually

3. **Compile the Code**  
   - Compile all `.java` files in the `src` folder  
   - Output `.class` files to `out/production/Rustyprof`

4. **Run the Application**  
   - Start with `StudentDashboard.java`  
   - Use IntelliJ or command line to launch the GUI

5. **Using the Interface**  
   - Add student information: Name, SIC, Branch  
   - Enter subject-wise marks for: ML, OOPS, COA, DBMS, OS  
   - Add brief remarks (optional)  
   - Click "Generate Report" to create the PDF  
   - QR code is shown in a new window for mobile download

6. **Learning Path Generation (AI)**  
   - The app sends a POST request to Mistral endpoint  
   - Receives improvement suggestions based on marks + remarks  
   - Appends this to the generated report dynamically

7. **Access the Generated PDF**  
   - Reports are saved in the base directory with timestamped filenames  
   - Use the QR code or file explorer to open

## Notes

- Ensure internet connectivity for Mistral API requests  
- All AI prompts and responses are handled inside `ReportGenerator.java`  
- No nested folders used; all compiled outputs and resources stay in flat structure for clarity and deployment ease

## Conclusion

RustyProf bridges traditional mark entry systems with modern AI-enhanced academic evaluation. It offers fast, localized operation with portable, QR-enabled PDF reports backed by personalized feedback generated using large language models. With a flat folder layout and a clean modular design, it is built for both academic use cases and future extensibility.

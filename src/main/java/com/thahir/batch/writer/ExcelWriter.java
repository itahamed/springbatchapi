package com.thahir.batch.writer;

import com.thahir.batch.model.FormattedCustomer;
import com.thahir.batch.service.EmailService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ExcelWriter implements ItemWriter<FormattedCustomer> {

    private static final Logger logger = LoggerFactory.getLogger(ExcelWriter.class);
    private static final String[] COLUMN_HEADERS = {"ID", "First Name", "Last Name", "Email", "Phone Number"};

    @Value("${excel.output.directory:./reports}")
    private String outputDirectory;

    @Value("${excel.output.filename:customers}")
    private String baseFilename;

    @Autowired
    EmailService emailService;

    @Value("${email.recipient:itahamed@gmail.com}")
    private String emailRecipient;

    @Value("${email.subject:Customer Report}")
    private String emailSubject;

    @Value("${email.body:Dear %s,\n\nPlease find attached the customer report generated on %s.\n\nBest regards,\nYour Company}")
    private String EMAIL_BODY_TEMPLATE;

    @Override
    public void write(Chunk<? extends FormattedCustomer> chunk) throws Exception {
        if (chunk.isEmpty()) {
            logger.info("No customers to write to Excel file");
            return;
        }

        String filename = generateFilename();
        Path outputPath = prepareOutputDirectory();
        Path filePath = outputPath.resolve(filename);

        logger.info("Writing {} customers to Excel file: {}", chunk.size(), filePath);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Customers");

            // Apply some basic styling
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < COLUMN_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(COLUMN_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (FormattedCustomer customer : chunk) {
                Row row = sheet.createRow(rowNum++);
                populateCustomerRow(row, customer);
            }

            // Auto-size columns for better readability
            for (int i = 0; i < COLUMN_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add basic filtering capability
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, COLUMN_HEADERS.length - 1));

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath.toFile())) {
                workbook.write(fileOut);
                logger.info("Successfully created Excel file at: {}", filePath);
            }

            String formattedEmailBody = String.format(EMAIL_BODY_TEMPLATE, "Customer", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // Send email with attachment
            emailService.sendEmailWithAttachment(
                    emailRecipient,
                    emailSubject,
                    formattedEmailBody,
                    filePath.toString()
            );

        } catch (IOException e) {
            logger.error("Error writing customers to Excel file: {}", e.getMessage(), e);
            throw new IOException("Failed to write customers to Excel file", e);
        }
    }

    private void populateCustomerRow(Row row, FormattedCustomer customer) {
        // Safely handle null values
        row.createCell(0).setCellValue(customer.getId() != null ? customer.getId() : "");
        row.createCell(1).setCellValue(customer.getFirstName() != null ? customer.getFirstName() : "");
        row.createCell(2).setCellValue(customer.getLastName() != null ? customer.getLastName() : "");
        row.createCell(3).setCellValue(customer.getEmail() != null ? customer.getEmail() : "");
        row.createCell(4).setCellValue(customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "");
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();

        // Set background color
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Set font
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Set border
        headerStyle.setBorderBottom(BorderStyle.THIN);

        // Set alignment
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        return headerStyle;
    }

    private String generateFilename() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s.xlsx", baseFilename, timestamp);
    }

    private Path prepareOutputDirectory() throws IOException {
        Path directory = Paths.get(outputDirectory);
        if (!Files.exists(directory)) {
            logger.info("Creating output directory: {}", directory);
            Files.createDirectories(directory);
        }
        return directory;
    }

}
package com.paymentapp.serviceImpl;



import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.paymentapp.dto.SalarySlipDetailsDTO;

@Component
public class SalarySlipPDFGenerator {

    public byte[] generateSalarySlipPDF(SalarySlipDetailsDTO slip) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // ============================================
        // HEADER SECTION
        // ============================================
        Paragraph header = new Paragraph(slip.getOrgName() != null ? slip.getOrgName() : "Organization")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(102, 126, 234));
        document.add(header);

        Paragraph subHeader = new Paragraph("Salary Slip")
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(subHeader);

        Paragraph period = new Paragraph("Period: " + slip.getPeriod())
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(period);

        // ============================================
        // EMPLOYEE DETAILS TABLE
        // ============================================
        Table empTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        addEmployeeDetailRow(empTable, "Employee Name:", slip.getEmpName());
        addEmployeeDetailRow(empTable, "Employee ID:", String.valueOf(slip.getEmpId()));
        addEmployeeDetailRow(empTable, "Email:", slip.getEmpEmail());
        addEmployeeDetailRow(empTable, "Phone:", slip.getPhone());
        addEmployeeDetailRow(empTable, "Department:", slip.getDepartmentName());
        addEmployeeDetailRow(empTable, "Grade:", slip.getGradeCode());

        document.add(empTable);

        // ============================================
        // EARNINGS SECTION
        // ============================================
        Paragraph earningsTitle = new Paragraph("Earnings")
                .setFontSize(14)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(5)
                .setFontColor(new DeviceRgb(16, 185, 129));
        document.add(earningsTitle);

        Table earningsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(10);

        // ✅ FIXED: Now accepts BigDecimal
        addSalaryRow(earningsTable, "Basic Salary", slip.getBasicSalary());
        addSalaryRow(earningsTable, "HRA", slip.getHra());
        addSalaryRow(earningsTable, "DA", slip.getDa());
        addSalaryRow(earningsTable, "Allowances", slip.getAllowances());

        document.add(earningsTable);

        // ============================================
        // DEDUCTIONS SECTION
        // ============================================
        Paragraph deductionsTitle = new Paragraph("Deductions")
                .setFontSize(14)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(5)
                .setFontColor(new DeviceRgb(239, 68, 68));
        document.add(deductionsTitle);

        Table deductionsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        addSalaryRow(deductionsTable, "Provident Fund (PF)", slip.getPf());

        document.add(deductionsTable);

        // ============================================
        // NET SALARY
        // ============================================
        Table netTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth()
                .setMarginTop(10);

        Cell netLabelCell = new Cell()
                .add(new Paragraph("Net Salary").setBold().setFontSize(14))
                .setBackgroundColor(new DeviceRgb(102, 126, 234))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(10)
                .setBorder(Border.NO_BORDER);

        // ✅ FIXED: BigDecimal formatting
        String netAmountFormatted = slip.getNetAmount() != null 
            ? "₹ " + String.format("%,.2f", slip.getNetAmount().doubleValue())
            : "₹ 0.00";

        Cell netValueCell = new Cell()
                .add(new Paragraph(netAmountFormatted)
                        .setBold()
                        .setFontSize(14)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(new DeviceRgb(102, 126, 234))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(10)
                .setBorder(Border.NO_BORDER);

        netTable.addCell(netLabelCell);
        netTable.addCell(netValueCell);

        document.add(netTable);

        // ============================================
        // FOOTER
        // ============================================
        // ✅ FIXED: Convert Instant to LocalDateTime for formatting
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String generatedDate;
        
        if (slip.getGeneratedAt() != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(slip.getGeneratedAt(), ZoneId.systemDefault());
            generatedDate = dateTime.format(formatter);
        } else {
            generatedDate = LocalDateTime.now().format(formatter);
        }

        Paragraph footer = new Paragraph("Generated on: " + generatedDate)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .setItalic()
                .setFontColor(ColorConstants.GRAY);
        document.add(footer);

        Paragraph disclaimer = new Paragraph("This is a system-generated document and does not require a signature.")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5)
                .setItalic()
                .setFontColor(ColorConstants.GRAY);
        document.add(disclaimer);

        document.close();

        return baos.toByteArray();
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Adds employee detail row to table
     */
    private void addEmployeeDetailRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setBold())
                .setPadding(5)
                .setBorder(Border.NO_BORDER);

        Cell valueCell = new Cell()
                .add(new Paragraph(value != null ? value : "N/A"))
                .setPadding(5)
                .setBorder(Border.NO_BORDER);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Adds salary row with BigDecimal amount formatting
     * ✅ FIXED: Changed parameter from Double to BigDecimal
     */
    private void addSalaryRow(Table table, String label, BigDecimal amount) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label))
                .setPadding(8)
                .setBackgroundColor(new DeviceRgb(249, 250, 251));

        // ✅ Convert BigDecimal to formatted string
        String formattedAmount = amount != null 
            ? "₹ " + String.format("%,.2f", amount.doubleValue())
            : "₹ 0.00";

        Cell valueCell = new Cell()
                .add(new Paragraph(formattedAmount)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setPadding(8)
                .setBackgroundColor(new DeviceRgb(249, 250, 251));

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}

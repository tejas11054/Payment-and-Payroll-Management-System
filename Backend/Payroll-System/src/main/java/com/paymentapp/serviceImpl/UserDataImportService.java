package com.paymentapp.serviceImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.paymentapp.dto.EmployeeRequestDTO;
import com.paymentapp.dto.OrgAdminRequestDTO;
import com.paymentapp.dto.VendorRequestDTO;

public class UserDataImportService {

    // ✅ Parse Employee Excel - Updated for 6 columns
    public List<EmployeeRequestDTO> parseEmployeeExcel(InputStream inputStream) throws IOException {
        List<EmployeeRequestDTO> list = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header

            EmployeeRequestDTO dto = new EmployeeRequestDTO();
            dto.setEmpName(getCellValue(row.getCell(0)));           // Full Name
            dto.setEmpEmail(getCellValue(row.getCell(1)));          // Email
            dto.setPhone(getCellValue(row.getCell(2)));             // Phone
            dto.setBankAccountName(getCellValue(row.getCell(3)));   // ✅ Bank Account Name
            dto.setBankAccountNo(getCellValue(row.getCell(4)));     // Bank Account Number
            dto.setIfscCode(getCellValue(row.getCell(5)));          // IFSC Code
            dto.setDocumentUrl(getCellValue(row.getCell(6))); // Assuming it's in column G


            list.add(dto);
        }
        workbook.close();
        return list;
    }

    // ✅ Parse Employee CSV - Updated for 6 columns
    public List<EmployeeRequestDTO> parseEmployeeCsv(InputStream inputStream) throws IOException {
        List<EmployeeRequestDTO> list = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                String[] tokens = line.split(",", -1);
                
                if (tokens.length < 7) {
                    throw new RuntimeException("Invalid CSV format, expected 7 columns: Name, Email, Phone, Bank Name, Acc No, IFSC, Document URL");
                }


                EmployeeRequestDTO dto = new EmployeeRequestDTO();
                dto.setEmpName(tokens[0].trim());
                dto.setEmpEmail(tokens[1].trim());
                dto.setPhone(tokens[2].trim());
                dto.setBankAccountName(tokens[3].trim());
                dto.setBankAccountNo(tokens[4].trim());
                dto.setIfscCode(tokens[5].trim());
                dto.setDocumentUrl(tokens[6].trim());

                list.add(dto);
            }
        }
        return list;
    }
    public List<VendorRequestDTO> parseVendorCsv(InputStream inputStream) throws IOException {
        List<VendorRequestDTO> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; 
                    continue;
                }

                if (line.trim().isEmpty())
                    continue;

                String[] tokens = line.split(",", -1); 

                if (tokens.length < 6)
                    throw new RuntimeException("Invalid CSV format. Required columns: Name, Email, Phone, VendorType, BankAccountNo, FileUrl");

                VendorRequestDTO dto = new VendorRequestDTO();
                dto.setName(tokens[0].trim());
                dto.setContactEmail(tokens[1].trim());
                dto.setPhone(tokens[2].trim());
                dto.setVendorType(tokens[3].trim());
                dto.setBankAccountNo(tokens[4].trim());
                dto.setFileUrl(tokens[5].trim());

                list.add(dto);
            }
        }
        return list;
    }
   


    public List<VendorRequestDTO> parseVendorExcel(InputStream inputStream) throws IOException {
        List<VendorRequestDTO> list = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0)
                continue; 

            VendorRequestDTO dto = new VendorRequestDTO();
            dto.setName(getCellValue(row.getCell(0)));
            dto.setContactEmail(getCellValue(row.getCell(1)));
            dto.setPhone(getCellValue(row.getCell(2)));
            dto.setVendorType(getCellValue(row.getCell(3)));
            dto.setBankAccountNo(getCellValue(row.getCell(4)));
            dto.setFileUrl(getCellValue(row.getCell(5)));

            list.add(dto);
        }

        workbook.close();
        return list;
    }
    
    public List<OrgAdminRequestDTO> parseOrgAdminExcel(InputStream inputStream) throws IOException {
        List<OrgAdminRequestDTO> list = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0)
                continue; // skip header

            OrgAdminRequestDTO dto = new OrgAdminRequestDTO();
            dto.setName(getCellValue(row.getCell(0)));
            dto.setEmail(getCellValue(row.getCell(1)));
            dto.setPhone(getCellValue(row.getCell(2)));
            dto.setDepartmentName(getCellValue(row.getCell(3)));
            dto.setFileUrl(getCellValue(row.getCell(4)));
            dto.setBankAccountName(getCellValue(row.getCell(5)));
            dto.setBankAccountNo(getCellValue(row.getCell(6)));
            dto.setIfscCode(getCellValue(row.getCell(7)));


            list.add(dto);
        }

        workbook.close();
        return list;
    }


    
    public List<OrgAdminRequestDTO> parseOrgAdminCsv(InputStream inputStream) throws IOException {
        List<OrgAdminRequestDTO> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // skip header
                    continue;
                }

                if (line.trim().isEmpty())
                    continue;

                String[] tokens = line.split(",", -1); // -1 keeps empty values

                if (tokens.length < 8)
                    throw new RuntimeException("Invalid CSV format, expected at least 9 columns");

                OrgAdminRequestDTO dto = new OrgAdminRequestDTO();
                dto.setName(tokens[0].trim());
                dto.setEmail(tokens[1].trim());
                dto.setPhone(tokens[2].trim());
                dto.setDepartmentName(tokens[3].trim().isEmpty() ? null : tokens[3].trim());
                dto.setFileUrl(tokens[4].trim());
                dto.setBankAccountName(tokens[5].trim());
                dto.setBankAccountNo(tokens[6].trim());
                dto.setIfscCode(tokens[7].trim());


                list.add(dto);
            }
        }

        return list;
    }



    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double val = cell.getNumericCellValue();
                    if (val == Math.floor(val)) {
                        return String.valueOf((long) val);
                    } else {
                        return String.valueOf(val);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

	
}

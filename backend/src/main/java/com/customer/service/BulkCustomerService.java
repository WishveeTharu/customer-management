// // package com.customer.service;

// // import com.customer.dto.CustomerDto;
// // import com.customer.model.Customer;
// // import com.customer.repository.CustomerRepository;
// // import lombok.RequiredArgsConstructor;
// // import org.apache.poi.ss.usermodel.*;
// // import org.apache.poi.util.IOUtils;
// // import org.springframework.beans.factory.annotation.Value;
// // import org.springframework.stereotype.Service;
// // import org.springframework.transaction.annotation.Transactional;
// // import org.springframework.web.multipart.MultipartFile;

// // import java.io.IOException;
// // import java.io.InputStream;
// // import java.time.LocalDate;
// // import java.time.format.DateTimeFormatter;
// // import java.time.format.DateTimeParseException;
// // import java.util.*;

// // @Service
// // @RequiredArgsConstructor
// // public class BulkCustomerService {

// //     private final CustomerRepository customerRepository;

// //     @Value("${app.bulk.batch-size:500}")
// //     private int batchSize;

// //     private static final DateTimeFormatter[] DATE_FORMATS = {
// //         DateTimeFormatter.ofPattern("yyyy-MM-dd"),
// //         DateTimeFormatter.ofPattern("dd/MM/yyyy"),
// //         DateTimeFormatter.ofPattern("MM/dd/yyyy")
// //     };

// //     @Transactional
// //     public CustomerDto.BulkResult processBulkUpload(MultipartFile file) throws IOException {
// //         CustomerDto.BulkResult result = new CustomerDto.BulkResult();
// //         List<String> errors = new ArrayList<>();

// //         // Limit POI memory usage for large files
// //         IOUtils.setByteArrayMaxOverride(500_000_000);

// //         try (InputStream is = file.getInputStream();
// //              Workbook workbook = WorkbookFactory.create(is)) {

// //             Sheet sheet = workbook.getSheetAt(0);
// //             int totalRows = sheet.getLastRowNum();
// //             result.setTotalRows(totalRows);

// //             List<Customer> batch = new ArrayList<>();
// //             Set<String> seenNics = new HashSet<>();

// //             // Collect all NICs in this file to check existing
// //             List<String> allNics = new ArrayList<>();
// //             for (int i = 1; i <= totalRows; i++) {
// //                 Row row = sheet.getRow(i);
// //                 if (row == null) continue;
// //                 String nic = getCellValue(row, 2);
// //                 if (nic != null && !nic.isEmpty()) {
// //                     allNics.add(nic.trim());
// //                 }
// //             }

// //             // Load existing customers by NIC in one DB call
// //             Map<String, Customer> existingMap = new HashMap<>();
// //             for (int i = 0; i < allNics.size(); i += batchSize) {
// //                 List<String> subList = allNics.subList(i,
// //                     Math.min(i + batchSize, allNics.size()));
// //                 customerRepository.findAllByNicNumberIn(subList)
// //                     .forEach(c -> existingMap.put(c.getNicNumber(), c));
// //             }

// //             // Process rows
// //             for (int i = 1; i <= totalRows; i++) {
// //                 Row row = sheet.getRow(i);
// //                 if (row == null) continue;

// //                 try {
// //                     String name = getCellValue(row, 0);
// //                     String dobStr = getCellValue(row, 1);
// //                     String nic = getCellValue(row, 2);

// //                     // Validate mandatory fields
// //                     if (name == null || name.trim().isEmpty()) {
// //                         errors.add("Row " + (i + 1) + ": Name is required");
// //                         result.setErrorCount(result.getErrorCount() + 1);
// //                         continue;
// //                     }
// //                     if (dobStr == null || dobStr.trim().isEmpty()) {
// //                         errors.add("Row " + (i + 1) + ": Date of birth is required");
// //                         result.setErrorCount(result.getErrorCount() + 1);
// //                         continue;
// //                     }
// //                     if (nic == null || nic.trim().isEmpty()) {
// //                         errors.add("Row " + (i + 1) + ": NIC is required");
// //                         result.setErrorCount(result.getErrorCount() + 1);
// //                         continue;
// //                     }

// //                     nic = nic.trim();

// //                     // Check duplicate NIC within file
// //                     if (seenNics.contains(nic)) {
// //                         errors.add("Row " + (i + 1) + ": Duplicate NIC in file: " + nic);
// //                         result.setErrorCount(result.getErrorCount() + 1);
// //                         continue;
// //                     }
// //                     seenNics.add(nic);

// //                     // Parse date
// //                     LocalDate dob = parseDate(dobStr.trim());
// //                     if (dob == null) {
// //                         errors.add("Row " + (i + 1) + ": Invalid date format: " + dobStr);
// //                         result.setErrorCount(result.getErrorCount() + 1);
// //                         continue;
// //                     }

// //                     // Upsert
// //                     Customer customer = existingMap.getOrDefault(nic, new Customer());
// //                     customer.setName(name.trim());
// //                     customer.setDateOfBirth(dob);
// //                     customer.setNicNumber(nic);
// //                     batch.add(customer);

// //                     // Save in batches
// //                     if (batch.size() >= batchSize) {
// //                         customerRepository.saveAll(batch);
// //                         result.setSuccessCount(result.getSuccessCount() + batch.size());
// //                         batch.clear();
// //                     }

// //                 } catch (Exception e) {
// //                     errors.add("Row " + (i + 1) + ": " + e.getMessage());
// //                     result.setErrorCount(result.getErrorCount() + 1);
// //                 }
// //             }

// //             // Save remaining
// //             if (!batch.isEmpty()) {
// //                 customerRepository.saveAll(batch);
// //                 result.setSuccessCount(result.getSuccessCount() + batch.size());
// //             }
// //         }

// //         result.setErrors(errors);
// //         return result;
// //     }

// //     private String getCellValue(Row row, int colIndex) {
// //         Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
// //         if (cell == null) return null;
// //         switch (cell.getCellType()) {
// //             case STRING:  return cell.getStringCellValue();
// //             case NUMERIC:
// //                 if (DateUtil.isCellDateFormatted(cell)) {
// //                     return cell.getLocalDateTimeCellValue().toLocalDate().toString();
// //                 }
// //                 return String.valueOf((long) cell.getNumericCellValue());
// //             case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
// //             case FORMULA: return cell.getCellFormula();
// //             default:      return null;
// //         }
// //     }

// //     private LocalDate parseDate(String value) {
// //         for (DateTimeFormatter fmt : DATE_FORMATS) {
// //             try {
// //                 return LocalDate.parse(value, fmt);
// //             } catch (DateTimeParseException ignored) {}
// //         }
// //         return null;
// //     }
// // }






















// package com.customer.service;

// import com.customer.dto.CustomerDto;
// import lombok.RequiredArgsConstructor;
// import org.apache.poi.ss.usermodel.*;
// import org.apache.poi.util.IOUtils;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.InputStream;
// import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;
// import java.time.format.DateTimeParseException;
// import java.util.*;

// @Service
// @RequiredArgsConstructor
// public class BulkCustomerService {

//     private final JdbcTemplate jdbcTemplate;

//     @Value("${app.bulk.batch-size:1000}")
//     private int batchSize;

//     private static final DateTimeFormatter[] DATE_FORMATS = {
//         DateTimeFormatter.ofPattern("yyyy-MM-dd"),
//         DateTimeFormatter.ofPattern("dd/MM/yyyy"),
//         DateTimeFormatter.ofPattern("MM/dd/yyyy")
//     };

//     public CustomerDto.BulkResult processBulkUpload(MultipartFile file) throws Exception {
//         CustomerDto.BulkResult result = new CustomerDto.BulkResult();
//         List<String> errors = new ArrayList<>();

//         IOUtils.setByteArrayMaxOverride(500_000_000);

//         try (InputStream is = file.getInputStream();
//              Workbook workbook = WorkbookFactory.create(is)) {

//             Sheet sheet = workbook.getSheetAt(0);
//             int totalRows = sheet.getLastRowNum();
//             result.setTotalRows(totalRows);

//             List<Object[]> batch = new ArrayList<>();
//             Set<String> seenNics = new HashSet<>();
//             int successCount = 0;
//             int errorCount = 0;

//             for (int i = 1; i <= totalRows; i++) {
//                 Row row = sheet.getRow(i);
//                 if (row == null) continue;

//                 try {
//                     String name   = getCellValue(row, 0);
//                     String dobStr = getCellValue(row, 1);
//                     String nic    = getCellValue(row, 2);

//                     if (name == null || name.trim().isEmpty()) {
//                         errors.add("Row " + (i+1) + ": Name is required");
//                         errorCount++; continue;
//                     }
//                     if (dobStr == null || dobStr.trim().isEmpty()) {
//                         errors.add("Row " + (i+1) + ": Date of birth is required");
//                         errorCount++; continue;
//                     }
//                     if (nic == null || nic.trim().isEmpty()) {
//                         errors.add("Row " + (i+1) + ": NIC is required");
//                         errorCount++; continue;
//                     }

//                     nic = nic.trim();
//                     if (seenNics.contains(nic)) {
//                         errors.add("Row " + (i+1) + ": Duplicate NIC in file: " + nic);
//                         errorCount++; continue;
//                     }
//                     seenNics.add(nic);

//                     LocalDate dob = parseDate(dobStr.trim());
//                     if (dob == null) {
//                         errors.add("Row " + (i+1) + ": Invalid date: " + dobStr);
//                         errorCount++; continue;
//                     }

//                     batch.add(new Object[]{name.trim(), dob.toString(), nic});

//                     if (batch.size() >= batchSize) {
//                         successCount += flushBatch(batch);
//                         batch.clear();
//                     }

//                 } catch (Exception e) {
//                     errors.add("Row " + (i+1) + ": " + e.getMessage());
//                     errorCount++;
//                 }
//             }

//             if (!batch.isEmpty()) {
//                 successCount += flushBatch(batch);
//             }

//             result.setSuccessCount(successCount);
//             result.setErrorCount(errorCount);
//         }

//         result.setErrors(errors.size() > 100 ? errors.subList(0, 100) : errors);
//         return result;
//     }

//     private int flushBatch(List<Object[]> batch) {
//         String sql = "INSERT INTO customer (name, date_of_birth, nic_number, created_at, updated_at) " +
//                      "VALUES (?, ?, ?, NOW(), NOW()) " +
//                      "ON DUPLICATE KEY UPDATE name=VALUES(name), date_of_birth=VALUES(date_of_birth), updated_at=NOW()";
//         int[][] result = jdbcTemplate.batchUpdate(sql, batch, batch.size(),
//             (ps, row) -> {
//                 ps.setString(1, (String) row[0]);
//                 ps.setString(2, (String) row[1]);
//                 ps.setString(3, (String) row[2]);
//             });
//         return batch.size();
//     }

//     private String getCellValue(Row row, int colIndex) {
//         Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
//         if (cell == null) return null;
//         switch (cell.getCellType()) {
//             case STRING:  return cell.getStringCellValue();
//             case NUMERIC:
//                 if (DateUtil.isCellDateFormatted(cell)) {
//                     return cell.getLocalDateTimeCellValue().toLocalDate().toString();
//                 }
//                 return String.valueOf((long) cell.getNumericCellValue());
//             case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
//             default:      return null;
//         }
//     }

//     private LocalDate parseDate(String value) {
//         for (DateTimeFormatter fmt : DATE_FORMATS) {
//             try { return LocalDate.parse(value, fmt); }
//             catch (DateTimeParseException ignored) {}
//         }
//         return null;
//     }
// }





























package com.customer.service;

import com.customer.dto.CustomerDto;
import com.github.pjfanning.xlsx.StreamingReader;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BulkCustomerService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.bulk.batch-size:1000}")
    private int batchSize;

    private static final DateTimeFormatter[] DATE_FORMATS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy")
    };

    public CustomerDto.BulkResult processBulkUpload(MultipartFile file) throws Exception {
        CustomerDto.BulkResult result = new CustomerDto.BulkResult();
        List<String> errors = new ArrayList<>();
        List<Object[]> batch = new ArrayList<>();
        Set<String> seenNics = new HashSet<>();
        int successCount = 0;
        int errorCount = 0;
        int rowNum = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = StreamingReader.builder()
                 .rowCacheSize(100)
                 .bufferSize(4096)
                 .open(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                rowNum++;
                if (rowNum == 1) continue; // skip header

                try {
                    String name   = getCellValue(row, 0);
                    String dobStr = getCellValue(row, 1);
                    String nic    = getCellValue(row, 2);

                    if (name == null || name.trim().isEmpty()) {
                        if (errors.size() < 100) errors.add("Row " + rowNum + ": Name required");
                        errorCount++; continue;
                    }
                    if (dobStr == null || dobStr.trim().isEmpty()) {
                        if (errors.size() < 100) errors.add("Row " + rowNum + ": DOB required");
                        errorCount++; continue;
                    }
                    if (nic == null || nic.trim().isEmpty()) {
                        if (errors.size() < 100) errors.add("Row " + rowNum + ": NIC required");
                        errorCount++; continue;
                    }

                    nic = nic.trim();
                    if (seenNics.contains(nic)) {
                        if (errors.size() < 100) errors.add("Row " + rowNum + ": Duplicate NIC: " + nic);
                        errorCount++; continue;
                    }
                    seenNics.add(nic);

                    LocalDate dob = parseDate(dobStr.trim());
                    if (dob == null) {
                        if (errors.size() < 100) errors.add("Row " + rowNum + ": Invalid date: " + dobStr);
                        errorCount++; continue;
                    }

                    batch.add(new Object[]{name.trim(), dob.toString(), nic});

                    if (batch.size() >= batchSize) {
                        flushBatch(batch);
                        successCount += batch.size();
                        batch.clear();
                    }

                } catch (Exception e) {
                    if (errors.size() < 100) errors.add("Row " + rowNum + ": " + e.getMessage());
                    errorCount++;
                }
            }

            if (!batch.isEmpty()) {
                flushBatch(batch);
                successCount += batch.size();
            }
        }

        result.setTotalRows(rowNum - 1);
        result.setSuccessCount(successCount);
        result.setErrorCount(errorCount);
        result.setErrors(errors);
        return result;
    }

    private void flushBatch(List<Object[]> batch) {
        String sql = "INSERT INTO customer (name, date_of_birth, nic_number, created_at, updated_at) " +
                     "VALUES (?, ?, ?, NOW(), NOW()) " +
                     "ON DUPLICATE KEY UPDATE name=VALUES(name), " +
                     "date_of_birth=VALUES(date_of_birth), updated_at=NOW()";
        jdbcTemplate.batchUpdate(sql, batch, batch.size(),
            (ps, row) -> {
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
            });
    }

    private String getCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default:      return null;
        }
    }

    private LocalDate parseDate(String value) {
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try { return LocalDate.parse(value, fmt); }
            catch (DateTimeParseException ignored) {}
        }
        return null;
    }
}
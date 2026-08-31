package com.qa.api.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileToJsonUtil {

    // Excel file to JSON
    public static JSONArray convertExcelToJson(byte[] excelBytes) throws IOException {
        JSONArray jsonArray = new JSONArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(excelBytes);
        try {
            Workbook workbook = new XSSFWorkbook(inputStream);

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);

                if (sheet.getPhysicalNumberOfRows() == 0) {
                    continue;
                }

                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    continue;
                }

                List<String> headers = new ArrayList<>();
                for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
                    Cell cell = headerRow.getCell(cellIndex);
                    if (cell != null) {
                        headers.add(getCellValueAsString(cell));
                    } else {
                        headers.add("Column" + cellIndex);
                    }
                }

                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        continue;
                    }

                    JSONObject rowObject = new JSONObject();
                    for (int cellIndex = 0; cellIndex < headers.size()
                            && cellIndex < row.getLastCellNum(); cellIndex++) {
                        Cell cell = row.getCell(cellIndex);
                        String header = headers.get(cellIndex);
                        Object cellValue = getCellValue(cell);

                        if (cellValue != null) {
                            rowObject.put(header, cellValue);
                        }
                    }

                    if (rowObject.length() > 0) {
                        jsonArray.put(rowObject);
                    }
                }
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }

        return jsonArray;
    }

    // CSV file to JSON
    public static JSONArray convertCsvToJson(byte[] csvBytes) throws IOException {
        if (isZipFile(csvBytes)) {
            return convertZipCsvToJson(csvBytes);
        } else {
            return convertSingleCsvToJson(csvBytes);
        }
    }

    // Check if ZIP file
    private static boolean isZipFile(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return false;
        }
        return bytes[0] == 0x50 && bytes[1] == 0x4B;
    }

    // ZIP CSV file to JSON
    private static JSONArray convertZipCsvToJson(byte[] zipBytes) throws IOException {
        JSONArray jsonArray = new JSONArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(zipBytes);
        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(inputStream, StandardCharsets.UTF_8);
            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().toLowerCase().endsWith(".csv") && !entry.isDirectory()) {
                    byte[] csvData = readZipEntry(zipInputStream);

                    JSONArray csvJsonArray = convertSingleCsvToJson(csvData);
                    for (int i = 0; i < csvJsonArray.length(); i++) {
                        jsonArray.put(csvJsonArray.getJSONObject(i));
                    }
                }
                zipInputStream.closeEntry();
            }
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                }
            }
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }

        return jsonArray;
    }

    // Read ZIP entry
    private static byte[] readZipEntry(ZipInputStream zipInputStream) throws IOException {
        List<Byte> byteList = new ArrayList<>();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
            for (int i = 0; i < bytesRead; i++) {
                byteList.add(buffer[i]);
            }
        }

        byte[] result = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            result[i] = byteList.get(i);
        }
        return result;
    }

    // Single CSV file to JSON
    private static JSONArray convertSingleCsvToJson(byte[] csvBytes) throws IOException {
        JSONArray jsonArray = new JSONArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvBytes);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;
            List<String> headers = null;
            boolean isFirstRow = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> values = parseCsvLine(line);

                if (isFirstRow) {
                    headers = values;
                    isFirstRow = false;
                } else {
                    if (headers != null && !values.isEmpty()) {
                        JSONObject rowObject = new JSONObject();
                        for (int i = 0; i < headers.size(); i++) {
                            String header = headers.get(i).trim();
                            String value = "";
                            
                            if (i < values.size()) {
                                value = values.get(i).trim();
                            }

                            if (!value.isEmpty()) {
                                Object convertedValue = convertCsvValue(value);
                                rowObject.put(header, convertedValue);
                            } else {
                                rowObject.put(header, "");
                            }
                        }

                        if (rowObject.length() > 0) {
                            jsonArray.put(rowObject);
                        }
                    }
                }
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }

        return jsonArray;
    }

    // Parse CSV line
    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentValue.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }

        values.add(currentValue.toString());

        return values;
    }

    // Convert CSV value
    private static Object convertCsvValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        value = value.trim();

        try {
            if (value.matches("^-?\\d+$")) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
        }

        try {
            if (value.matches("^-?\\d*\\.\\d+([eE][+-]?\\d+)?$") || value.matches("^-?\\d+([eE][+-]?\\d+)?$")) {
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
        }

        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }

        return value;
    }

    // Get Excel cell value
    private static Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        int cellType = cell.getCellType();
        if (cellType == Cell.CELL_TYPE_FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }

        switch (cellType) {
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return (int) numericValue;
                    } else {
                        return numericValue;
                    }
                }
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue().trim();
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            case Cell.CELL_TYPE_BLANK:
                return null;
            default:
                return getCellValueAsString(cell);
        }
    }

    // Get Excel cell value as string
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        int cellType = cell.getCellType();
        if (cellType == Cell.CELL_TYPE_FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }

        switch (cellType) {
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((int) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue().trim();
            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_BLANK:
                return "";
            default:
                return "";
        }
    }
}

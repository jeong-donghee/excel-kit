package io.github.jeongdonghee.excelkit.grid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import io.github.jeongdonghee.excelkit.core.ExcelKitException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class GridExcelWriterTest {

    static class Product {
        @ExcelColumn(header = "이름", order = 1)
        private final String name;
        private final int price;
        private final int quantity;

        Product(String name, int price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        @ExcelColumn(header = "금액", order = 2)
        public int amount() {
            return price * quantity;
        }
    }

    private static ExcelDownloadOptions options(int maxRowsPerSheet, int maxSheets, OverflowPolicy policy) {
        return new ExcelDownloadOptions("f", "상품", SheetNumbering.SUFFIX_UNDERSCORE,
                "No", maxRowsPerSheet, maxSheets, policy);
    }

    private static byte[] write(List<Product> rows, ExcelDownloadOptions options) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GridExcelWriter.write(rows, Product.class, options, out);
        return out.toByteArray();
    }

    @Test
    void writesHeaderRowNumberAndDerivedColumn() throws Exception {
        byte[] bytes = write(List.of(new Product("펜", 1000, 3), new Product("공책", 2000, 2)),
                options(1_048_575, Integer.MAX_VALUE, OverflowPolicy.TRUNCATE));

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            XSSFSheet sheet = wb.getSheetAt(0);
            assertEquals("상품", sheet.getSheetName()); // 분할 없으면 접미사 없음
            // 헤더
            assertEquals("No", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("이름", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("금액", sheet.getRow(0).getCell(2).getStringCellValue());
            // 1행
            assertEquals(1.0, sheet.getRow(1).getCell(0).getNumericCellValue());
            assertEquals("펜", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals(3000.0, sheet.getRow(1).getCell(2).getNumericCellValue());
            // 2행
            assertEquals(2.0, sheet.getRow(2).getCell(0).getNumericCellValue());
            assertEquals(4000.0, sheet.getRow(2).getCell(2).getNumericCellValue());
        }
    }

    @Test
    void splitsSheetsWithRepeatedHeaderAndContinuousRowNumber() throws Exception {
        List<Product> rows = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            rows.add(new Product("p" + i, i, 1));
        }
        byte[] bytes = write(rows, options(2, Integer.MAX_VALUE, OverflowPolicy.TRUNCATE));

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(3, wb.getNumberOfSheets()); // 5행 / 2 = 3시트
            assertEquals("상품_1", wb.getSheetAt(0).getSheetName());
            assertEquals("상품_3", wb.getSheetAt(2).getSheetName());
            // 각 시트에 헤더 반복
            assertEquals("No", wb.getSheetAt(1).getRow(0).getCell(0).getStringCellValue());
            // 행번호 전역 연속: 3번째 시트 첫 데이터 = 5
            assertEquals(5.0, wb.getSheetAt(2).getRow(1).getCell(0).getNumericCellValue());
        }
    }

    @Test
    void truncatesWhenExceedingMaxSheets() throws Exception {
        List<Product> rows = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            rows.add(new Product("p" + i, i, 1));
        }
        // maxRowsPerSheet=2, maxSheets=2 → 4행만 기록
        byte[] bytes = write(rows, options(2, 2, OverflowPolicy.TRUNCATE));
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(2, wb.getNumberOfSheets());
            assertEquals(3, wb.getSheetAt(1).getPhysicalNumberOfRows()); // 헤더 1 + 데이터 2
            // 마지막 기록 행번호 = 4
            assertEquals(4.0, wb.getSheetAt(1).getRow(2).getCell(0).getNumericCellValue());
        }
    }

    @Test
    void failsWhenExceedingMaxSheetsWithFailPolicy() {
        List<Product> rows = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            rows.add(new Product("p" + i, i, 1));
        }
        assertThrows(ExcelKitException.class,
                () -> write(rows, options(2, 2, OverflowPolicy.FAIL)));
    }

    @Test
    void failsWhenNoAnnotatedColumns() {
        class Empty {
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(ExcelKitException.class,
                () -> GridExcelWriter.write(List.of(new Empty()), Empty.class,
                        options(10, Integer.MAX_VALUE, OverflowPolicy.TRUNCATE), out));
    }

    @Test
    void headerOnlyWhenEmpty() throws Exception {
        byte[] bytes = write(List.of(), options(10, Integer.MAX_VALUE, OverflowPolicy.TRUNCATE));
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(1, wb.getNumberOfSheets());
            assertTrue(wb.getSheetAt(0).getRow(1) == null); // 데이터 없음
        }
    }

    @Test
    void exportsLargeDatasetWithoutRetainingRows() {
        // 10만 행을 지연 생성(보관 X)으로 흘려도 OOM 없이 완료 — SXSSF 저메모리 회귀
        int n = 100_000;
        java.util.AbstractCollection<Product> rows = new java.util.AbstractCollection<>() {
            @Override
            public int size() {
                return n;
            }

            @Override
            public java.util.Iterator<Product> iterator() {
                return new java.util.Iterator<>() {
                    private int i = 0;

                    @Override
                    public boolean hasNext() {
                        return i < n;
                    }

                    @Override
                    public Product next() {
                        i++;
                        return new Product("p" + i, i, 1);
                    }
                };
            }
        };
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GridExcelWriter.write(rows, Product.class,
                options(1_048_575, Integer.MAX_VALUE, OverflowPolicy.TRUNCATE), out);
        assertTrue(out.size() > 0);
    }
}

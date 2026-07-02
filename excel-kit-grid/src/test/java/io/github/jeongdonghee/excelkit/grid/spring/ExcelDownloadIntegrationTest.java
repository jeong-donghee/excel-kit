package io.github.jeongdonghee.excelkit.grid.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.List;

import io.github.jeongdonghee.excelkit.grid.ExcelColumn;
import io.github.jeongdonghee.excelkit.grid.ExcelDataExtractor;
import io.github.jeongdonghee.excelkit.grid.ExcelDownload;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
class ExcelDownloadIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void exportsListReturnAsXlsx() throws Exception {
        MvcResult result = mvc.perform(get("/products/excel"))
                .andExpect(status().isOk())
                .andReturn();

        String contentType = result.getResponse().getContentType();
        assertTrue(contentType != null && contentType.contains("spreadsheetml.sheet"), contentType);
        assertTrue(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION).contains("products.xlsx"));

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            XSSFSheet sheet = wb.getSheetAt(0);
            assertEquals("No", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("이름", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("금액", sheet.getRow(0).getCell(2).getStringCellValue());
            assertEquals(1.0, sheet.getRow(1).getCell(0).getNumericCellValue());
            assertEquals("펜", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals(3000.0, sheet.getRow(1).getCell(2).getNumericCellValue());
        }
    }

    @Test
    void exportsFromPlainControllerNotJustRestController() throws Exception {
        MvcResult result = mvc.perform(get("/plain/excel"))
                .andExpect(status().isOk())
                .andReturn();

        String contentType = result.getResponse().getContentType();
        assertTrue(contentType != null && contentType.contains("spreadsheetml.sheet"), contentType);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            assertEquals("이름", wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        }
    }

    @Test
    void exportsCommonResponseWrapperViaExtractor() throws Exception {
        MvcResult result = mvc.perform(get("/wrapped/excel"))
                .andExpect(status().isOk())
                .andReturn();

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            XSSFSheet sheet = wb.getSheetAt(0);
            assertEquals("이름", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("공책", sheet.getRow(1).getCell(0).getStringCellValue());
        }
    }

    // ── 테스트용 스프링 부트 앱 ───────────────────────────────
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApp {
        @Bean
        TestController testController() {
            return new TestController();
        }

        @Bean
        ExcelDataExtractor apiResponseExtractor() {
            return ExcelDataExtractor.forType(ApiResponse.class, ApiResponse::getData);
        }

        @Bean
        PlainController plainController() {
            return new PlainController();
        }
    }

    /** @ResponseBody 없는 일반 @Controller에서도 @ExcelDownload가 동작하는지 확인용. */
    @Controller
    static class PlainController {
        @ExcelDownload(filename = "plain")
        @GetMapping("/plain/excel")
        public List<Product> plain() {
            return List.of(new Product("펜", 1000, 3));
        }
    }

    @RestController
    static class TestController {
        @ExcelDownload(filename = "products", rowNumberColumn = "No")
        @GetMapping("/products/excel")
        public List<Product> products() {
            return List.of(new Product("펜", 1000, 3), new Product("공책", 2000, 2));
        }

        @ExcelDownload(filename = "wrapped")
        @GetMapping("/wrapped/excel")
        public ApiResponse<List<Product>> wrapped() {
            return new ApiResponse<>(List.of(new Product("공책", 2000, 2)));
        }
    }

    public static class Product {
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

    public static class ApiResponse<T> {
        private final T data;

        ApiResponse(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }
    }
}

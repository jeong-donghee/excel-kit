package io.github.jeongdonghee.excelkit.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SessionExcelControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void basicListEndpointReturnsXlsx() throws Exception {
        MvcResult result = mvc.perform(get("/sessions/excel"))
                .andExpect(status().isOk())
                .andReturn();

        String contentType = result.getResponse().getContentType();
        assertTrue(contentType != null && contentType.contains("spreadsheetml.sheet"), contentType);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            XSSFSheet sheet = wb.getSheetAt(0);
            assertEquals("No", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("세션 ID", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("사용자", sheet.getRow(0).getCell(2).getStringCellValue());
            assertEquals("쿼리 수", sheet.getRow(0).getCell(3).getStringCellValue());
            assertEquals("상태", sheet.getRow(0).getCell(4).getStringCellValue());
            assertEquals(1.0, sheet.getRow(1).getCell(0).getNumericCellValue());
            assertEquals(25, sheet.getLastRowNum()); // 헤더 + 25행 → 마지막 행 인덱스 25
        }
    }

    @Test
    void commonWrapperEndpointReturnsXlsx() throws Exception {
        MvcResult result = mvc.perform(get("/sessions/wrapped/excel"))
                .andExpect(status().isOk())
                .andReturn();

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            XSSFSheet sheet = wb.getSheetAt(0);
            assertEquals("세션 ID", sheet.getRow(0).getCell(1).getStringCellValue());
        }
    }
}

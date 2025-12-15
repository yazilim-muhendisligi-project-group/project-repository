package com.baharkiraathanesi.kiraathane.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ReportTest {

    @Test
    void testReportData() {
        LocalDate today = LocalDate.now();
        Report report = new Report(today, 1500.0, 45);

        assertEquals(today, report.getDate());
        assertEquals(1500.0, report.getTotalRevenue());
        assertEquals(45, report.getTotalOrders());
    }

    @Test
    void testSetters() {
        Report report = new Report(LocalDate.now(), 0, 0);

        report.setTotalRevenue(2000.50);
        report.setTotalOrders(100);

        assertEquals(2000.50, report.getTotalRevenue());
        assertEquals(100, report.getTotalOrders());
    }
}
package com.becoder.report;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sbact1.controller.AdminController;
import com.sbact1.model.Report;
import com.sbact1.model.ReportReason;
import com.sbact1.repository.ReportRepository;

@ExtendWith(MockitoExtension.class)
public class ReportTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Test
    void listReports_sinFiltro_devuelveTodosNoRevisados() {
        List<Report> reports = List.of(new Report(), new Report());
        when(reportRepository.findByReviewedFalse()).thenReturn(reports);

        String view = adminController.listReports(null, model);

        verify(reportRepository).findByReviewedFalse();
        verify(model).addAttribute("reports", reports);
        verify(model).addAttribute("motivos", ReportReason.values());
        verify(model).addAttribute("motivoFiltro", null);

        assertEquals("admin/admin_reports", view);
    }

    @Test
    void listReports_conFiltro_devuelveFiltrados() {
        ReportReason filtro = ReportReason.SPAM;
        List<Report> reports = List.of(new Report());
        when(reportRepository.findByReviewedFalseAndReason(filtro)).thenReturn(reports);

        String view = adminController.listReports(filtro, model);

        verify(reportRepository).findByReviewedFalseAndReason(filtro);
        verify(model).addAttribute("reports", reports);
        verify(model).addAttribute("motivos", ReportReason.values());
        verify(model).addAttribute("motivoFiltro", filtro);

        assertEquals("admin/admin_reports", view);
    }

    @Test
    void handleReport_existente_marcaRevisada() {
        Report report = new Report();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        String redirect = adminController.handleReport(1L, redirectAttributes);

        assertTrue(report.isReviewed());
        verify(reportRepository).save(report);
        verify(redirectAttributes).addFlashAttribute("success", "Denuncia marcada como revisada.");
        assertEquals("redirect:/admin/reports", redirect);
    }

    @Test
    void handleReport_noExistente_muestraError() {
        when(reportRepository.findById(1L)).thenReturn(Optional.empty());

        String redirect = adminController.handleReport(1L, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("error", "La denuncia no existe.");
        assertEquals("redirect:/admin/reports", redirect);
    }

    @Test
    void deleteReport_existente_eliminaReporte() {
        Report report = new Report();
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        String redirect = adminController.deleteReport(1L, redirectAttributes);

        verify(reportRepository).delete(report);
        verify(redirectAttributes).addFlashAttribute("success", "Denuncia eliminada correctamente.");
        assertEquals("redirect:/admin/reports", redirect);
    }

    @Test
    void deleteReport_noExistente_muestraError() {
        when(reportRepository.findById(1L)).thenReturn(Optional.empty());

        String redirect = adminController.deleteReport(1L, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("error", "La denuncia no existe.");
        assertEquals("redirect:/admin/reports", redirect);
    }
}


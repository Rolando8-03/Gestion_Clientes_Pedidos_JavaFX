package edu.uam.gestion.report;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import edu.uam.gestion.model.Cliente;
import edu.uam.gestion.model.Pedido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReporteServiceTest {
    @TempDir Path temporal;

    @Test
    void exportaAcentosMontosYTotalGeneral() throws Exception {
        var cliente = new Cliente("María Díaz", "maria@example.com");
        var uno = new Pedido("Papelería", new BigDecimal("1250.50"));
        var dos = new Pedido("Materiales", new BigDecimal("780.00"));
        cliente.agregarPedido(uno); cliente.agregarPedido(dos);
        Path pdf = new ReporteService().exportar(List.of(uno, dos), temporal.resolve("pedidos.pdf"), "Cliente: María Díaz");
        String texto = leer(pdf);
        assertTrue(texto.contains("María Díaz"));
        assertTrue(texto.contains("Papelería"));
        assertTrue(texto.contains("2,030.50"), texto);
        assertTrue(texto.contains("Pedidos incluidos: 2"), texto);
    }

    @Test
    void listaVaciaProduceUnPdfInformativo() throws Exception {
        Path pdf = new ReporteService().exportar(List.of(), temporal.resolve("vacio.pdf"), "Sin coincidencias");
        String texto = leer(pdf);
        assertTrue(texto.contains("Sin pedidos"));
        assertTrue(texto.contains("Pedidos incluidos: 0"), texto);
        assertTrue(texto.contains("0.00"));
    }

    @Test
    void admiteDescripcionesLargasYVariasPaginas() throws Exception {
        var cliente = new Cliente("Cliente de prueba con nombre extenso para comprobar los saltos de línea", "prueba@example.com");
        List<Pedido> lista = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            Pedido pedido = new Pedido("Descripción extensa de materiales y accesorios para laboratorio. ".repeat(2), new BigDecimal("10.25"));
            cliente.agregarPedido(pedido); lista.add(pedido);
        }
        Path pdf = new ReporteService().exportar(lista, temporal.resolve("largo.pdf"), "Prueba de paginación");
        PdfReader lector = new PdfReader(pdf.toString());
        try {
            assertTrue(lector.getNumberOfPages() > 1);
        } finally { lector.close(); }
        assertTrue(leer(pdf).contains("615.00"));
    }

    private String leer(Path pdf) throws Exception {
        PdfReader lector = new PdfReader(pdf.toString());
        try {
            PdfTextExtractor extractor = new PdfTextExtractor(lector);
            StringBuilder texto = new StringBuilder();
            for (int i = 1; i <= lector.getNumberOfPages(); i++) {
                texto.append(extractor.getTextFromPage(i));
            }
            return texto.toString();
        } finally { lector.close(); }
    }
}

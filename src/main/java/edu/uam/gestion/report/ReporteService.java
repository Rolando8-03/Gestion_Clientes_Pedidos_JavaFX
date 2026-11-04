package edu.uam.gestion.report;

import edu.uam.gestion.model.Pedido;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReporteService {
    private JasperReport plantilla;

    public Path exportar(List<Pedido> pedidos) throws JRException, IOException {
        return exportar(pedidos, Path.of("reportes", "pedidos.pdf"), "Todos los clientes");
    }

    public synchronized Path exportar(List<Pedido> pedidos, Path destino, String filtro)
            throws JRException, IOException {
        List<FilaPedido> filas = pedidos.stream().map(p -> new FilaPedido(
                p.getId(), p.getCliente().getNombre(), p.getDescripcion(), p.getTotal(),
                p.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))).toList();
        if (plantilla == null) {
            try (InputStream fuente = getClass().getResourceAsStream("/reports/pedidos.jrxml")) {
                if (fuente == null) { throw new IllegalStateException("No se encontró la plantilla de pedidos."); }
                plantilla = JasperCompileManager.compileReport(fuente);
            }
        }
        var parametros = new HashMap<String, Object>();
        parametros.put("TITULO", "REPORTE DE PEDIDOS");
        parametros.put("FILTRO", filtro == null ? "Todos los clientes" : filtro);
        parametros.put(JRParameter.REPORT_LOCALE, Locale.forLanguageTag("es-NI"));
        parametros.put(JRParameter.REPORT_TIME_ZONE, TimeZone.getTimeZone("America/Managua"));
        JasperPrint documento = JasperFillManager.fillReport(plantilla, parametros,
                new JRBeanCollectionDataSource(filas));
        Path ruta = destino.toAbsolutePath().normalize();
        Files.createDirectories(ruta.getParent());
        Path temporal = Files.createTempFile(ruta.getParent(), "pedidos-", ".pdf");
        try {
            JasperExportManager.exportReportToPdfFile(documento, temporal.toString());
            // No se sustituye el reporte anterior si falla el llenado o la exportación.
            Files.move(temporal, ruta, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporal);
        }
        return ruta;
    }

    /** Los datos del reporte ya están cargados; Jasper no abre otra conexión JDBC. */
    public static final class FilaPedido {
        private final Long id;
        private final String cliente;
        private final String descripcion;
        private final BigDecimal total;
        private final String fecha;

        public FilaPedido(Long id, String cliente, String descripcion, BigDecimal total, String fecha) {
            this.id = id;
            this.cliente = cliente;
            this.descripcion = descripcion;
            this.total = total;
            this.fecha = fecha;
        }

        public Long getId() { return id; }
        public String getCliente() { return cliente; }
        public String getDescripcion() { return descripcion; }
        public BigDecimal getTotal() { return total; }
        public String getFecha() { return fecha; }
    }
}

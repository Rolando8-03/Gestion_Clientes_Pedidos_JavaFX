package edu.uam.gestion.report;
import edu.uam.gestion.model.Pedido;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
public class ReporteService {
 public Path exportar(List<Pedido> pedidos) throws Exception {
  List<FilaPedido> filas=pedidos.stream().map(p->new FilaPedido(p.getId(),p.getCliente().getNombre(),p.getDescripcion(),p.getTotal(),p.getFecha().toString())).toList();
  try(InputStream jrxml=getClass().getResourceAsStream("/reports/pedidos.jrxml")){
   if(jrxml==null)throw new IllegalStateException("Falta reports/pedidos.jrxml");
   JasperReport plantilla=JasperCompileManager.compileReport(jrxml);
   JasperPrint documento=JasperFillManager.fillReport(plantilla,Map.of("TITULO","REPORTE DE PEDIDOS"),new JRBeanCollectionDataSource(filas));
   Path ruta=Path.of("reportes","pedidos.pdf").toAbsolutePath();Files.createDirectories(ruta.getParent());
   JasperExportManager.exportReportToPdfFile(documento,ruta.toString());return ruta;
  }
 }
 public static class FilaPedido {
  private final Long id;private final String cliente,descripcion,fecha;private final java.math.BigDecimal total;
  public FilaPedido(Long id,String cliente,String descripcion,java.math.BigDecimal total,String fecha){this.id=id;this.cliente=cliente;this.descripcion=descripcion;this.total=total;this.fecha=fecha;}
  public Long getId(){return id;}public String getCliente(){return cliente;}public String getDescripcion(){return descripcion;}
  public java.math.BigDecimal getTotal(){return total;}public String getFecha(){return fecha;}
 }
}

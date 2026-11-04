package edu.uam.gestion.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="pedidos")
public class Pedido {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=150) private String descripcion;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal total;
 @Column(nullable=false) private LocalDate fecha;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="cliente_id",nullable=false)
 private Cliente cliente;
 protected Pedido() {}
 public Pedido(String descripcion,BigDecimal total){this.descripcion=descripcion;this.total=total;this.fecha=LocalDate.now();}
 public Long getId(){return id;}
 public String getDescripcion(){return descripcion;}
 public BigDecimal getTotal(){return total;}
 public LocalDate getFecha(){return fecha;}
 public Cliente getCliente(){return cliente;}
 void asignarCliente(Cliente cliente){this.cliente=cliente;}
}

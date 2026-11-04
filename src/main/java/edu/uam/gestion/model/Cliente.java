package edu.uam.gestion.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedido> pedidos = new ArrayList<>();

    protected Cliente() { }

    public Cliente(String nombre, String correo) {
        actualizar(nombre, correo);
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public List<Pedido> getPedidos() { return Collections.unmodifiableList(pedidos); }

    public void actualizar(String nombre, String correo) {
        this.nombre = nombre;
        this.correo = correo;
    }

    public void agregarPedido(Pedido pedido) {
        if (pedido.getCliente() != null && pedido.getCliente() != this) {
            throw new IllegalArgumentException("El pedido ya pertenece a otro cliente.");
        }
        if (!pedidos.contains(pedido)) {
            pedidos.add(pedido);
        }
        pedido.asignarCliente(this);
    }

    @Override
    public String toString() { return nombre; }
}

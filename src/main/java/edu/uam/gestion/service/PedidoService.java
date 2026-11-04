package edu.uam.gestion.service;

import edu.uam.gestion.config.HibernateUtil;
import edu.uam.gestion.config.Transacciones;
import edu.uam.gestion.model.Cliente;
import edu.uam.gestion.model.Pedido;
import edu.uam.gestion.repository.ClienteRepository;
import edu.uam.gestion.repository.PedidoRepository;
import org.hibernate.SessionFactory;
import java.util.List;

public class PedidoService {
    private final ClienteRepository clientes = new ClienteRepository();
    private final PedidoRepository pedidos = new PedidoRepository();
    private final Transacciones transacciones;

    public PedidoService() { transacciones = new Transacciones(HibernateUtil::factory); }
    public PedidoService(SessionFactory factory) { transacciones = new Transacciones(() -> factory); }

    public List<Pedido> listar() { return buscar(null, ""); }

    public List<Pedido> porCliente(Long clienteId) {
        Validaciones.id(clienteId, "un cliente");
        return transacciones.ejecutar(s -> pedidos.porCliente(s, clienteId));
    }

    public List<Pedido> buscar(Long clienteId, String descripcion) {
        return transacciones.ejecutar(s -> pedidos.buscar(s, clienteId, descripcion));
    }

    public Pedido crear(Long clienteId, String descripcion, String monto) {
        Validaciones.id(clienteId, "un cliente");
        String detalle = Validaciones.texto(descripcion, "La descripción", 150);
        var total = Validaciones.monto(monto);
        return transacciones.ejecutar(s -> {
            Cliente cliente = clientes.obtener(s, clienteId);
            if (cliente == null) { throw new IllegalArgumentException("El cliente ya no existe."); }
            Pedido pedido = new Pedido(detalle, total);
            cliente.agregarPedido(pedido);
            pedidos.guardar(s, pedido);
            return pedido;
        });
    }

    public void actualizar(Long id, Long clienteId, String descripcion, String monto) {
        Validaciones.id(id, "un pedido");
        Validaciones.id(clienteId, "un cliente");
        String detalle = Validaciones.texto(descripcion, "La descripción", 150);
        var total = Validaciones.monto(monto);
        transacciones.ejecutar(s -> {
            Pedido pedido = pedidos.obtener(s, id);
            Cliente cliente = clientes.obtener(s, clienteId);
            if (pedido == null) { throw new IllegalArgumentException("El pedido ya no existe."); }
            if (cliente == null) { throw new IllegalArgumentException("El cliente ya no existe."); }
            pedido.actualizar(cliente, detalle, total);
            return null;
        });
    }

    public void eliminar(Long id) {
        Validaciones.id(id, "un pedido");
        transacciones.ejecutar(s -> {
            Pedido pedido = pedidos.obtener(s, id);
            if (pedido == null) { throw new IllegalArgumentException("El pedido ya no existe."); }
            pedidos.eliminar(s, pedido);
            return null;
        });
    }
}

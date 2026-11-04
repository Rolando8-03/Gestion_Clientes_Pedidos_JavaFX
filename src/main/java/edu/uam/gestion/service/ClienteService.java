package edu.uam.gestion.service;

import edu.uam.gestion.config.HibernateUtil;
import edu.uam.gestion.config.Transacciones;
import edu.uam.gestion.model.Cliente;
import edu.uam.gestion.repository.ClienteRepository;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import java.util.List;

public class ClienteService {
    private final ClienteRepository repo = new ClienteRepository();
    private final Transacciones transacciones;

    public ClienteService() { this.transacciones = new Transacciones(HibernateUtil::factory); }
    public ClienteService(SessionFactory factory) { this.transacciones = new Transacciones(() -> factory); }

    public List<Cliente> listar() { return buscar(""); }

    public List<Cliente> buscar(String texto) {
        return transacciones.ejecutar(s -> repo.buscar(s, texto));
    }

    public Cliente crear(String nombre, String correo) {
        String n = Validaciones.texto(nombre, "El nombre", 120);
        String c = Validaciones.correo(correo);
        return escritura(() -> transacciones.ejecutar(s -> {
            if (repo.existeCorreo(s, c, null)) {
                throw new IllegalArgumentException("El correo ya está registrado.");
            }
            Cliente cliente = new Cliente(n, c);
            repo.guardar(s, cliente);
            return cliente;
        }));
    }

    public void actualizar(Long id, String nombre, String correo) {
        Validaciones.id(id, "un cliente");
        String n = Validaciones.texto(nombre, "El nombre", 120);
        String c = Validaciones.correo(correo);
        escritura(() -> transacciones.ejecutar(s -> {
            Cliente cliente = repo.obtener(s, id);
            if (cliente == null) { throw new IllegalArgumentException("El cliente ya no existe."); }
            if (repo.existeCorreo(s, c, id)) {
                throw new IllegalArgumentException("El correo ya está registrado.");
            }
            cliente.actualizar(n, c);
            // Está managed: Hibernate detecta los cambios al confirmar la transacción.
            return null;
        }));
    }

    public void eliminar(Long id) {
        Validaciones.id(id, "un cliente");
        transacciones.ejecutar(s -> {
            Cliente cliente = repo.obtener(s, id);
            if (cliente == null) { throw new IllegalArgumentException("El cliente ya no existe."); }
            repo.eliminar(s, cliente);
            return null;
        });
    }

    private <T> T escritura(java.util.function.Supplier<T> operacion) {
        try {
            return operacion.get();
        } catch (ConstraintViolationException e) {
            // Cubre también dos altas simultáneas que superen la comprobación previa.
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("El correo ya está registrado.", e);
            }
            throw e;
        }
    }
}

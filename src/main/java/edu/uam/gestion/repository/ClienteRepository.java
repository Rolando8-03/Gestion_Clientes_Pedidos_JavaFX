package edu.uam.gestion.repository;

import edu.uam.gestion.model.Cliente;
import org.hibernate.Session;
import java.util.List;
import java.util.Locale;

public class ClienteRepository {
    public void guardar(Session sesion, Cliente cliente) { sesion.persist(cliente); }
    public Cliente obtener(Session sesion, Long id) { return sesion.find(Cliente.class, id); }
    public void eliminar(Session sesion, Cliente cliente) { sesion.remove(cliente); }

    public List<Cliente> listar(Session sesion) { return buscar(sesion, ""); }

    public List<Cliente> buscar(Session sesion, String texto) {
        return sesion.createQuery("""
                from Cliente c
                where lower(c.nombre) like :texto escape '!'
                   or lower(c.correo) like :texto escape '!'
                order by c.id
                """, Cliente.class)
                .setParameter("texto", patron(texto))
                .getResultList();
    }

    public boolean existeCorreo(Session sesion, String correo, Long excluirId) {
        String hql = "select count(c) from Cliente c where lower(c.correo) = :correo";
        if (excluirId != null) {
            hql += " and c.id <> :id";
        }
        var consulta = sesion.createQuery(hql, Long.class)
                .setParameter("correo", correo.toLowerCase(Locale.ROOT));
        if (excluirId != null) {
            consulta.setParameter("id", excluirId);
        }
        return consulta.getSingleResult() > 0;
    }

    static String patron(String texto) {
        String limpio = texto == null ? "" : texto.strip().toLowerCase(Locale.ROOT);
        return "%" + limpio.replace("!", "!!").replace("%", "!%")
                .replace("_", "!_") + "%";
    }
}

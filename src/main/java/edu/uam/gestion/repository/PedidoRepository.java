package edu.uam.gestion.repository;

import edu.uam.gestion.model.Pedido;
import org.hibernate.Session;
import java.util.List;

public class PedidoRepository {
    public void guardar(Session sesion, Pedido pedido) { sesion.persist(pedido); }
    public Pedido obtener(Session sesion, Long id) { return sesion.find(Pedido.class, id); }
    public void eliminar(Session sesion, Pedido pedido) { sesion.remove(pedido); }

    public List<Pedido> listarConCliente(Session sesion) { return buscar(sesion, null, ""); }

    public List<Pedido> porCliente(Session sesion, Long clienteId) {
        return sesion.createQuery("""
                select p from Pedido p join fetch p.cliente c
                where c.id = :clienteId
                order by p.fecha desc, p.id desc
                """, Pedido.class)
                .setParameter("clienteId", clienteId)
                .getResultList();
    }

    public List<Pedido> buscar(Session sesion, Long clienteId, String descripcion) {
        String hql = "select p from Pedido p join fetch p.cliente c "
                + "where lower(p.descripcion) like :texto escape '!'";
        if (clienteId != null) {
            hql += " and c.id = :clienteId";
        }
        hql += " order by p.fecha desc, p.id desc";
        var consulta = sesion.createQuery(hql, Pedido.class)
                .setParameter("texto", ClienteRepository.patron(descripcion));
        if (clienteId != null) {
            consulta.setParameter("clienteId", clienteId);
        }
        return consulta.getResultList();
    }
}

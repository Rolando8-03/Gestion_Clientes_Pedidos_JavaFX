package edu.uam.gestion.repository;
import edu.uam.gestion.model.Pedido;
import org.hibernate.Session;
import java.util.List;
public class PedidoRepository {
 public void guardar(Session session,Pedido pedido){session.persist(pedido);}
 public List<Pedido> listarConCliente(Session session){
  return session.createQuery("select p from Pedido p join fetch p.cliente order by p.fecha desc, p.id desc",Pedido.class).list();
 }
}

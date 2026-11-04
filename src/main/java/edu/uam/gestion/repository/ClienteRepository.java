package edu.uam.gestion.repository;
import edu.uam.gestion.model.Cliente;
import org.hibernate.Session;
import java.util.List;
public class ClienteRepository {
 public void guardar(Session session,Cliente cliente){session.persist(cliente);}
 public Cliente obtener(Session session,Long id){return session.get(Cliente.class,id);}
 public List<Cliente> listar(Session session){return session.createQuery("from Cliente c order by c.nombre",Cliente.class).list();}
 public boolean existeCorreo(Session session,String correo,Long excluirId){
  return session.createQuery("select count(c) from Cliente c where lower(c.correo)=:correo and (:id is null or c.id<>:id)",Long.class)
   .setParameter("correo",correo.toLowerCase()).setParameter("id",excluirId).getSingleResult()>0;
 }
 public void eliminar(Session session,Cliente cliente){session.remove(cliente);}
}

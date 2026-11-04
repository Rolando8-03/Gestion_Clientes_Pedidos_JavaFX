package edu.uam.gestion.service;
import edu.uam.gestion.config.HibernateUtil;
import edu.uam.gestion.model.Cliente;
import edu.uam.gestion.repository.ClienteRepository;
import java.util.List;
public class ClienteService {
 private final ClienteRepository repo=new ClienteRepository();
 public List<Cliente> listar(){try(var s=HibernateUtil.factory().openSession()){return repo.listar(s);}}
 public void crear(String nombre,String correo){
  validar(nombre,correo);
  try(var s=HibernateUtil.factory().openSession()){
   var tx=s.beginTransaction();
   try{
    if(repo.existeCorreo(s,correo.trim(),null))throw new IllegalArgumentException("El correo ya está registrado");
    repo.guardar(s,new Cliente(nombre.trim(),correo.trim()));tx.commit();
   }catch(RuntimeException e){if(tx.isActive())tx.rollback();throw e;}
  }
 }
 public void actualizar(Long id,String nombre,String correo){
  validar(nombre,correo);
  try(var s=HibernateUtil.factory().openSession()){
   var tx=s.beginTransaction();
   try{
    Cliente c=repo.obtener(s,id);
    if(c==null)throw new IllegalArgumentException("Cliente no encontrado");
    if(repo.existeCorreo(s,correo.trim(),id))throw new IllegalArgumentException("El correo ya está registrado");
    c.actualizar(nombre.trim(),correo.trim());tx.commit();
   }catch(RuntimeException e){if(tx.isActive())tx.rollback();throw e;}
  }
 }
 public void eliminar(Long id){
  try(var s=HibernateUtil.factory().openSession()){
   var tx=s.beginTransaction();
   try{Cliente c=repo.obtener(s,id);if(c==null)throw new IllegalArgumentException("Cliente no encontrado");repo.eliminar(s,c);tx.commit();}
   catch(RuntimeException e){if(tx.isActive())tx.rollback();throw e;}
  }
 }
 private void validar(String n,String c){
  if(n==null||n.isBlank()||n.trim().length()>120)throw new IllegalArgumentException("Nombre obligatorio (máx. 120)");
  if(c==null||!c.trim().matches("^[^\s@]+@[^\s@]+\.[^\s@]+$")||c.trim().length()>150)
   throw new IllegalArgumentException("Correo inválido");
 }
}

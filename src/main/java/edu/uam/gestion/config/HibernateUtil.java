package edu.uam.gestion.config;
import edu.uam.gestion.model.Cliente;
import edu.uam.gestion.model.Pedido;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
public final class HibernateUtil {
 private static final SessionFactory FACTORY=crear();
 private HibernateUtil() {}
 private static SessionFactory crear(){
  try {return new Configuration().addProperties(cargar()).addAnnotatedClass(Cliente.class).addAnnotatedClass(Pedido.class).buildSessionFactory();}
  catch(Exception e){throw new IllegalStateException("No se pudo iniciar Hibernate. Revise PostgreSQL y hibernate.properties",e);}
 }
 private static java.util.Properties cargar(){
  try(var archivo=HibernateUtil.class.getResourceAsStream("/hibernate.properties")){
   if(archivo==null) throw new IllegalStateException("Falta hibernate.properties");
   var propiedades=new java.util.Properties();propiedades.load(archivo);return propiedades;
  }catch(java.io.IOException e){throw new IllegalStateException("Error leyendo configuración",e);}
 }
 public static SessionFactory factory(){return FACTORY;}
 public static void cerrar(){FACTORY.close();}
}

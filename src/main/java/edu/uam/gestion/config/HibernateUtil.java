package edu.uam.gestion.config;

import edu.uam.gestion.model.Cliente;
import edu.uam.gestion.model.Pedido;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {
    private static SessionFactory factory;

    private HibernateUtil() { }

    public static synchronized SessionFactory factory() {
        if (factory == null || factory.isClosed()) {
            try {
                factory = new Configuration()
                        .addProperties(ConfiguracionBD.cargar())
                        .addAnnotatedClass(Cliente.class)
                        .addAnnotatedClass(Pedido.class)
                        .buildSessionFactory();
            } catch (RuntimeException e) {
                // No se muestran URL ni credenciales en la alerta de la interfaz.
                throw new IllegalStateException("No se pudo conectar a PostgreSQL. Verifique que exista "
                        + "gestion_uam y revise config/local.properties o las variables DB_*.", e);
            }
        }
        return factory;
    }

    public static synchronized void cerrar() {
        if (factory != null && !factory.isClosed()) {
            factory.close();
        }
        factory = null;
    }
}

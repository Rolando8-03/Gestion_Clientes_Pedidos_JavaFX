package edu.uam.gestion.config;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import java.util.function.Function;
import java.util.function.Supplier;

/** Una sesión por operación; ninguna sesión se comparte entre hilos. */
public final class Transacciones {
    private final Supplier<SessionFactory> factory;

    public Transacciones(Supplier<SessionFactory> factory) {
        this.factory = factory;
    }

    public <T> T ejecutar(Function<Session, T> operacion) {
        try (Session sesion = factory.get().openSession()) {
            Transaction tx = sesion.beginTransaction();
            try {
                T resultado = operacion.apply(sesion);
                tx.commit();
                return resultado;
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    try {
                        tx.rollback();
                    } catch (RuntimeException falloRollback) {
                        e.addSuppressed(falloRollback);
                    }
                }
                throw e;
            }
        }
    }
}

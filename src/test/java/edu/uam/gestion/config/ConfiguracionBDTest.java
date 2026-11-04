package edu.uam.gestion.config;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class ConfiguracionBDTest {
    @Test
    void variablesDeEntornoTienenPrioridadYSinModificarLaBase() {
        Properties base = new Properties();
        base.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost/gestion_uam");
        base.setProperty("hibernate.connection.username", "postgres");
        Properties local = new Properties();
        local.setProperty("db.user", "usuario_local");
        local.setProperty("db.password", "ejemplo_local");
        Properties resultado = ConfiguracionBD.combinar(base, local,
                Map.of("DB_USER", "usuario_entorno", "DB_PASSWORD", "ejemplo_entorno"));
        assertEquals("usuario_entorno", resultado.getProperty("hibernate.connection.username"));
        assertEquals("ejemplo_entorno", resultado.getProperty("hibernate.connection.password"));
        assertEquals(base.getProperty("hibernate.connection.url"), resultado.getProperty("hibernate.connection.url"));
        assertEquals("postgres", base.getProperty("hibernate.connection.username"));
        assertFalse(base.containsKey("hibernate.connection.password"));
    }

    @Test
    void unaClaveVaciaExplicitaSeRespeta() {
        Properties local = new Properties();
        local.setProperty("db.password", "ejemplo");
        var resultado = ConfiguracionBD.combinar(new Properties(), local, Map.of("DB_PASSWORD", ""));
        assertEquals("", resultado.getProperty("hibernate.connection.password"));
    }
}

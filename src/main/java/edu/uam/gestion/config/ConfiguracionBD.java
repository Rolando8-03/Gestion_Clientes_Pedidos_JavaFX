package edu.uam.gestion.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/** Las variables de entorno tienen prioridad sobre la configuración local. */
public final class ConfiguracionBD {
    private ConfiguracionBD() { }

    public static Properties cargar() {
        Properties base = new Properties();
        Properties local = new Properties();
        try (InputStream entrada = ConfiguracionBD.class.getResourceAsStream("/hibernate.properties")) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontró hibernate.properties.");
            }
            base.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
            Path ruta = Path.of("config", "local.properties");
            if (Files.exists(ruta)) {
                try (var lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
                    local.load(lector);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la configuración de PostgreSQL.", e);
        }
        return combinar(base, local, System.getenv());
    }

    static Properties combinar(Properties base, Properties local, Map<String, String> entorno) {
        Properties resultado = new Properties();
        resultado.putAll(base);
        asignar(resultado, "hibernate.connection.url", "db.url", "DB_URL", local, entorno);
        asignar(resultado, "hibernate.connection.username", "db.user", "DB_USER", local, entorno);
        asignar(resultado, "hibernate.connection.password", "db.password", "DB_PASSWORD", local, entorno);
        return resultado;
    }

    private static void asignar(Properties destino, String clave, String localKey, String envKey,
                                Properties local, Map<String, String> entorno) {
        String valor = entorno.containsKey(envKey) ? entorno.get(envKey) : local.getProperty(localKey);
        if (valor != null) {
            destino.setProperty(clave, valor);
        }
    }
}

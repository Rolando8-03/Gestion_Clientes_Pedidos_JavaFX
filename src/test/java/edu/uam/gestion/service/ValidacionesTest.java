package edu.uam.gestion.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ValidacionesTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "0", "-1", "1.001", "1,25", "1e3", "NaN", "10000000000.00"})
    void rechazaMontosInvalidos(String monto) {
        assertThrows(IllegalArgumentException.class, () -> Validaciones.monto(monto));
    }

    @Test
    void conservaPrecisionMonetaria() {
        assertEquals(new BigDecimal("1250.50"), Validaciones.monto(" 1250.50 "));
        assertEquals(new BigDecimal("0.01"), Validaciones.monto("0.01"));
        assertEquals(new BigDecimal("9999999999.99"), Validaciones.monto("9999999999.99"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ana", "ana@", "@correo.com", "ana correo@example.com", "ana@example", "ana@@example.com"})
    void rechazaCorreosInvalidos(String correo) {
        assertThrows(IllegalArgumentException.class, () -> Validaciones.correo(correo));
    }

    @Test
    void normalizaCorreoYSoloRecortaBordesDelNombre() {
        assertEquals("ana@example.com", Validaciones.correo("  ANA@EXAMPLE.COM  "));
        assertEquals("Ana María Pérez", Validaciones.texto("  Ana María Pérez  ", "Nombre", 120));
    }

    @Test
    void aplicaLimitesYSeleccionObligatoria() {
        assertThrows(IllegalArgumentException.class, () -> Validaciones.texto(" ", "Nombre", 120));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.texto("x".repeat(121), "Nombre", 120));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.correo("x".repeat(145) + "@example.com"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.id(null, "un cliente"));
        assertThrows(IllegalArgumentException.class, () -> Validaciones.id(-1L, "un cliente"));
    }
}

package edu.uam.gestion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

final class Validaciones {
    private Validaciones() { }

    static String texto(String valor, String campo, int limite) {
        if (valor == null || valor.isBlank() || valor.strip().length() > limite) {
            throw new IllegalArgumentException(campo + " es obligatorio y admite hasta " + limite + " caracteres.");
        }
        return valor.strip();
    }

    static String correo(String valor) {
        String correo = texto(valor, "El correo", 150).toLowerCase(Locale.ROOT);
        if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Escriba un correo válido, por ejemplo: nombre@correo.com.");
        }
        return correo;
    }

    static BigDecimal monto(String valor) {
        try {
            String limpio = valor == null ? "" : valor.strip();
            // Se evita interpretar una coma como separador de miles o aceptar notación científica.
            if (!limpio.matches("[0-9]+(?:[.][0-9]{1,2})?")) {
                throw new NumberFormatException();
            }
            BigDecimal monto = new BigDecimal(limpio).setScale(2, RoundingMode.UNNECESSARY);
            if (monto.signum() <= 0 || monto.compareTo(new BigDecimal("9999999999.99")) > 0) {
                throw new NumberFormatException();
            }
            return monto;
        } catch (ArithmeticException | NumberFormatException e) {
            throw new IllegalArgumentException("El monto debe estar entre 0.01 y 9999999999.99. Use punto y hasta dos decimales.");
        }
    }

    static Long id(Long id, String entidad) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Seleccione " + entidad + ".");
        }
        return id;
    }
}

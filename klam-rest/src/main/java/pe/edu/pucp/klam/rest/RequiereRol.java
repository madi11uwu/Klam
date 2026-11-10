package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.NameBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exige sesión iniciada (token de POST /login en "Authorization: Bearer ...").
 * Con valores, además exige uno de esos roles (ADMINISTRADOR, VENDEDOR, TECNICO_INSTRUMENTISTA,
 * CLINICA_HOSPITAL, PACIENTE_PARTICULAR). Sin valores basta con estar autenticado.
 * Es opt-in: solo protege los endpoints que la llevan. Otros integrantes pueden usarla igual.
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequiereRol {
    String[] value() default {};
}

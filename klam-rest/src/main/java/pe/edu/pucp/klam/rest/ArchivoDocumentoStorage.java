package pe.edu.pucp.klam.rest;

import jakarta.ws.rs.core.EntityPart;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/** Guarda solo claves generadas por el servidor, fuera del WAR y de las carpetas públicas. */
final class ArchivoDocumentoStorage {
    static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final String CLAVE = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(pdf|png|jpg)";

    private ArchivoDocumentoStorage() {
    }

    static String guardar(EntityPart parte) throws IOException {
        if (parte == null) throw new IllegalArgumentException("Debe seleccionar un archivo");
        byte[] bytes;
        try (InputStream entrada = parte.getContent()) {
            bytes = entrada.readNBytes((int) MAX_BYTES + 1);
        }
        if (bytes.length == 0 || bytes.length > MAX_BYTES) {
            throw new IllegalArgumentException("El archivo debe tener contenido y no superar 5 MB");
        }
        String extension = detectarExtension(bytes);
        if (extension == null) {
            throw new IllegalArgumentException("Solo se admiten archivos PDF, PNG o JPEG válidos");
        }
        String clave = UUID.randomUUID() + extension;
        Path destino = directorio().resolve(clave);
        try {
            Files.write(destino, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException ex) {
            Files.deleteIfExists(destino);
            throw ex;
        }
        return clave;
    }

    static byte[] leer(String clave) throws IOException {
        Path archivo = rutaControlada(clave);
        if (!Files.isRegularFile(archivo) || Files.size(archivo) > MAX_BYTES) {
            throw new NoSuchFileException(clave);
        }
        return Files.readAllBytes(archivo);
    }

    static void eliminar(String clave) throws IOException {
        Files.deleteIfExists(rutaControlada(clave));
    }

    static String tipoContenido(String clave) {
        if (clave.endsWith(".pdf")) return "application/pdf";
        if (clave.endsWith(".png")) return "image/png";
        return "image/jpeg";
    }

    static String extension(String clave) {
        return clave.substring(clave.lastIndexOf('.'));
    }

    private static Path rutaControlada(String clave) throws IOException {
        if (clave == null || !clave.matches(CLAVE)) throw new NoSuchFileException("Referencia no gestionada");
        return directorio().resolve(clave);
    }

    private static Path directorio() throws IOException {
        String configurado = System.getProperty("klam.documentos.dir");
        if (configurado == null || configurado.isBlank()) configurado = System.getenv("KLAM_DOCUMENTOS_DIR");
        Path ruta = (configurado == null || configurado.isBlank())
                ? Path.of(System.getProperty("user.home"), "KlamLabDocumentos")
                : Path.of(configurado);
        ruta = ruta.toAbsolutePath().normalize();
        Files.createDirectories(ruta);
        return ruta;
    }

    private static String detectarExtension(byte[] b) {
        if (b.length >= 8 && b[0] == '%' && b[1] == 'P' && b[2] == 'D'
                && b[3] == 'F' && b[4] == '-') {
            String fin = new String(b, Math.max(0, b.length - 1024), Math.min(1024, b.length),
                    java.nio.charset.StandardCharsets.ISO_8859_1).trim();
            if (fin.endsWith("%%EOF")) return ".pdf";
        }
        byte[] png = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        if (b.length >= 20 && empieza(b, png)
                && b[b.length - 8] == 'I' && b[b.length - 7] == 'E'
                && b[b.length - 6] == 'N' && b[b.length - 5] == 'D') return ".png";
        if (b.length >= 4 && (b[0] & 0xff) == 0xff && (b[1] & 0xff) == 0xd8
                && (b[b.length - 2] & 0xff) == 0xff && (b[b.length - 1] & 0xff) == 0xd9) return ".jpg";
        return null;
    }

    private static boolean empieza(byte[] b, byte[] prefijo) {
        for (int i = 0; i < prefijo.length; i++) if (b[i] != prefijo[i]) return false;
        return true;
    }
}

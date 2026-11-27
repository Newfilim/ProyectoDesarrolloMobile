package com.zaror.backend.service;

import com.zaror.backend.exception.ErrorApi;
import com.zaror.backend.exception.DiagnosticoErrores;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ArchivosComprobantes {
    private static final int MAX_BYTES = 10 * 1024 * 1024;
    private final Path directorio;
    private Path raiz;

    public ArchivosComprobantes(@Value("${zaror.upload-dir}") String directorio) {
        if (directorio.isBlank()) throw new IllegalArgumentException("Configura UPLOAD_DIR.");
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    @PostConstruct
    void preparar() throws IOException {
        try {
            raiz = Files.createDirectories(directorio).toRealPath();
            if (!Files.isWritable(raiz)) throw new IOException("El directorio de comprobantes no permite escritura.");
        } catch (IOException error) {
            DiagnosticoErrores.registrar("preparar almacenamiento de comprobantes", error);
            throw new IOException("No se pudo preparar el almacenamiento privado de comprobantes.");
        }
    }

    public String guardar(MultipartFile archivo) {
        if (archivo.isEmpty()) throw ErrorApi.invalido("El comprobante está vacío.");
        if (archivo.getSize() > MAX_BYTES) throw new ErrorApi(HttpStatus.PAYLOAD_TOO_LARGE, "ARCHIVO_GRANDE", "El archivo supera 10 MiB.");
        byte[] bytes;
        try (InputStream entrada = archivo.getInputStream()) {
            bytes = entrada.readNBytes(MAX_BYTES + 1);
        } catch (IOException error) {
            DiagnosticoErrores.registrar("leer comprobante recibido", error);
            throw ErrorApi.noDisponible();
        }
        if (bytes.length > MAX_BYTES) throw new ErrorApi(HttpStatus.PAYLOAD_TOO_LARGE, "ARCHIVO_GRANDE", "El archivo supera 10 MiB.");
        String extension = validarImagen(bytes);
        String nombre = UUID.randomUUID() + "." + extension;
        Path ruta = ruta(nombre);
        try {
            Files.write(ruta, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (FileAlreadyExistsException error) {
            DiagnosticoErrores.registrar("reservar nombre interno de comprobante", error);
            throw ErrorApi.noDisponible();
        } catch (IOException error) {
            DiagnosticoErrores.registrar("guardar comprobante", error);
            eliminar(nombre);
            throw ErrorApi.noDisponible();
        }
        return nombre;
    }

    private static String validarImagen(byte[] bytes) {
        try (var imagen = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(imagen);
            if (!lectores.hasNext()) throw formatoInvalido();
            ImageReader lector = lectores.next();
            try {
                String tipo = lector.getFormatName().toLowerCase(Locale.ROOT);
                if (!tipo.equals("jpeg") && !tipo.equals("png")) throw formatoInvalido();
                lector.setInput(imagen, true, true);
                int ancho = lector.getWidth(0);
                int alto = lector.getHeight(0);
                if (ancho < 1 || alto < 1 || ancho > 6000 || alto > 6000 || (long) ancho * alto > 24_000_000) {
                    throw formatoInvalido();
                }
                lector.read(0).flush();
                return tipo.equals("jpeg") ? "jpg" : "png";
            } finally {
                lector.dispose();
            }
        } catch (IOException | IndexOutOfBoundsException error) {
            throw formatoInvalido();
        }
    }

    private static ErrorApi formatoInvalido() {
        return new ErrorApi(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "FORMATO_NO_ADMITIDO",
                "El comprobante debe ser una imagen JPEG o PNG válida dentro de las dimensiones admitidas.");
    }

    public FileSystemResource abrir(String nombre) {
        Path ruta = ruta(nombre);
        if (!Files.isRegularFile(ruta, LinkOption.NOFOLLOW_LINKS)) {
            DiagnosticoErrores.registrar("abrir comprobante referenciado", new FileNotFoundException());
            throw ErrorApi.noExiste();
        }
        return new FileSystemResource(ruta);
    }

    public void eliminar(String nombre) {
        try {
            Files.deleteIfExists(ruta(nombre));
        } catch (IOException error) {
            DiagnosticoErrores.registrar("retirar comprobante sin referencia", error);
        }
    }

    private Path ruta(String nombre) {
        if (nombre == null || !nombre.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png)")) {
            throw ErrorApi.noExiste();
        }
        return raiz.resolve(nombre);
    }
}

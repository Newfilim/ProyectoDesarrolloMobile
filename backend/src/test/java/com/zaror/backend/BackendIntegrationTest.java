package com.zaror.backend;

import com.zaror.backend.model.*;
import com.zaror.backend.service.Horario;
import com.zaror.backend.security.JwtTokens;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import com.mongodb.client.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnabledIfEnvironmentVariable(named = "ZAROR_TEST_MONGO_URI", matches = ".+")
class BackendIntegrationTest {
    private static final String CONTRASENA = UUID.randomUUID().toString();
    private static final String CLAVE = Base64.getEncoder().encodeToString(
            UUID.randomUUID().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    private static final String QUINCHO = "%024x".formatted(11);
    private static final String PISCINA = "%024x".formatted(13);
    private static final String SALA = "%024x".formatted(12);
    private static final String DIA = "2026-12-18";

    @Autowired MockMvc mvc;
    @Autowired MongoTemplate mongo;
    @Autowired MongoClient clienteMongo;
    @Autowired JsonMapper json;
    @Autowired Horario horario;
    @Autowired JwtEncoder encoder;
    @Autowired Clock reloj;

    @Value("${spring.mongodb.uri}") String uri;
    @Value("${zaror.upload-dir}") String upload;

    private static String uriAislada() {
        String uri = System.getenv("ZAROR_TEST_MONGO_URI");
        if (uri == null) return "";
        if (!uri.matches("mongodb://(?:localhost|127\\.0\\.0\\.1):[0-9]+/zaror_test(?:_[A-Za-z0-9_]+)?\\?replicaSet=rs0")) {
            throw new IllegalArgumentException("ZAROR_TEST_MONGO_URI debe apuntar a un replica set local y a una base zaror_test.");
        }
        return uri.replaceFirst("/zaror_test(?:_[A-Za-z0-9_]+)?\\?", "/zaror_test_" +
                UUID.randomUUID().toString().replace("-", "") + "?");
    }

    private static String directorioTemporal() {
        try { return Files.createTempDirectory("zaror-test-").toString(); }
        catch (java.io.IOException e) { throw new IllegalStateException(e); }
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry p) {
        String uri = uriAislada();
        String upload = directorioTemporal();
        p.add("spring.mongodb.uri", () -> uri);
        p.add("zaror.jwt-secret", () -> CLAVE);
        p.add("zaror.upload-dir", () -> upload);
        p.add("zaror.demo-password", () -> CONTRASENA);
    }

    @TestConfiguration
    static class RelojPrueba {
        @Bean @Primary
        Clock relojFijo() { return Clock.fixed(Instant.parse("2026-12-18T11:30:00Z"), ZoneOffset.UTC); }
    }

    @Test
    void sinPerfilDevNoSeCarganDatosYLosTotalesSonCero() {
        String baseVacia = uriAislada();
        try (var contexto = iniciarParaSemilla(baseVacia, "prueba-sin-demo", false)) {
            var base = contexto.getBean(MongoTemplate.class);
            assertEquals(Set.of("usuarios", "espacios", "reservas", "bloques_reservados", "ingresos", "gastos"),
                    base.getCollectionNames());
            for (String coleccion : base.getCollectionNames()) {
                assertEquals(0, base.getCollection(coleccion).countDocuments());
            }
            var servicio = contexto.getBean(com.zaror.backend.service.FinanzasService.class);
            assertEquals(new com.zaror.backend.dto.Respuestas.ResumenFinancieroResponse(0, 0, 0), servicio.resumen());
            assertTrue(servicio.porCategoria().isEmpty());
        }
        probarRecuperacion(baseVacia);
    }

    @Test
    void recuperaUnaInicializacionInterrumpidaTrasCrearColecciones() {
        String parcial = uriAislada();
        try (var cliente = com.mongodb.client.MongoClients.create(parcial)) {
            var base = cliente.getDatabase(new com.mongodb.ConnectionString(parcial).getDatabase());
            base.createCollection("usuarios");
            base.createCollection("ingresos");
        }
        probarRecuperacion(parcial);
    }

    @Test
    void rechazaRecuperarUnaBaseConDatosYConservaLosDocumentos() {
        String parcial = uriAislada();
        var ingreso = new Document("_id", new ObjectId()).append("descripcion", "Movimiento existente")
                .append("monto", 777L).append("fecha", "2026-11-01");
        var usuario = new Document("_id", new ObjectId()).append("nombre", "Cuenta existente")
                .append("correo", "modificado@zaror.cl").append("contrasenaHash", "hash-de-prueba")
                .append("rol", "ADMINISTRADOR").append("activo", false);
        try (var cliente = com.mongodb.client.MongoClients.create(parcial)) {
            var base = cliente.getDatabase(new com.mongodb.ConnectionString(parcial).getDatabase());
            base.getCollection("ingresos").insertOne(ingreso);
            base.getCollection("usuarios").insertOne(usuario);
            try (var contexto = iniciarParaSemilla(parcial, "dev", false)) {
                assertEquals(1, contexto.getBean(MongoTemplate.class).getCollection("usuarios").countDocuments());
            }
            assertThrows(IllegalStateException.class, () -> {
                try (var contexto = iniciarParaSemilla(parcial, "dev", true)) {
                    fail("La recuperación debe negarse si hay documentos.");
                }
            });
            assertEquals(ingreso, base.getCollection("ingresos").find().first());
            assertEquals(usuario, base.getCollection("usuarios").find().first());
            assertEquals(1, base.getCollection("ingresos").countDocuments());
            assertEquals(1, base.getCollection("usuarios").countDocuments());
            assertEquals(0, base.getCollection("gastos").countDocuments());
        }
    }

    private void probarRecuperacion(String baseVacia) {
        try (var contexto = iniciarParaSemilla(baseVacia, "dev", false)) {
            assertEquals(0, contexto.getBean(MongoTemplate.class).getCollection("usuarios").countDocuments());
        }
        try (var contexto = iniciarParaSemilla(baseVacia, "dev", true)) {
            var base = contexto.getBean(MongoTemplate.class);
            assertEquals(4, base.getCollection("usuarios").countDocuments());
            assertEquals(3, base.getCollection("espacios").countDocuments());
            assertEquals(3, base.getCollection("ingresos").countDocuments());
            assertEquals(6, base.getCollection("gastos").countDocuments());
        }
    }

    private org.springframework.context.ConfigurableApplicationContext iniciarParaSemilla(
            String base, String perfil, boolean recuperar) {
        return new SpringApplicationBuilder(ZarorApplication.class).run(
                "--server.port=0", "--spring.profiles.active=" + perfil, "--spring.mongodb.uri=" + base,
                "--zaror.jwt-secret=" + CLAVE, "--zaror.upload-dir=" + upload,
                "--zaror.demo-password=" + CONTRASENA, "--zaror.inicializar-base-vacia=" + recuperar);
    }

    @Test
    void autenticacionPermisosYConcurrenciaDeUsuarios() throws Exception {
        String admin = login("admin");
        String residente = login("residente");
        String tesoreria = login("tesoreria");
        String directiva = login("directiva");
        assertEquals(4, mongo.count(new org.springframework.data.mongodb.core.query.Query(), Usuario.class));
        assertEquals(3, mongo.count(new org.springframework.data.mongodb.core.query.Query(), Espacio.class));
        assertEquals(3, mongo.count(new org.springframework.data.mongodb.core.query.Query(), Ingreso.class));
        assertEquals(6, mongo.count(new org.springframework.data.mongodb.core.query.Query(), Gasto.class));
        mvc.perform(token(get("/api/usuarios"), admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
        mvc.perform(token(get("/api/espacios"), directiva)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
        mvc.perform(token(get("/api/espacios/" + QUINCHO), tesoreria)).andExpect(status().isOk())
                .andExpect(jsonPath("$.capacidadMaxima").value(25));
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/reservas']").exists());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").with(anonymous())).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer token.invalido.a")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"admin@zaror.cl\",\"contrasena\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(token(get("/api/usuarios"), residente)).andExpect(status().isForbidden());
        mvc.perform(token(get("/api/gastos"), residente)).andExpect(status().isForbidden());
        mvc.perform(token(get("/api/finanzas/resumen"), admin)).andExpect(status().isForbidden());
        mvc.perform(token(get("/api/finanzas/resumen"), directiva)).andExpect(status().isOk())
                .andExpect(jsonPath("$.ingresos").value(300000)).andExpect(jsonPath("$.gastos").value(160000))
                .andExpect(jsonPath("$.balance").value(140000));
        mvc.perform(token(get("/api/finanzas/gastos-por-categoria"), directiva)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(40000));
        mvc.perform(token(post("/api/ingresos"), directiva).contentType(MediaType.APPLICATION_JSON)
                .content("{\"descripcion\":\"Prueba\",\"monto\":1,\"fecha\":\"2026-12-18\"}"))
                .andExpect(status().isForbidden());

        String usuarioJson = alta("Otro residente", "otro@zaror.cl", "RESIDENTE");
        List<Integer> altas = carrera(
                () -> enviar(post("/api/usuarios"), admin, usuarioJson).getResponse().getStatus(),
                () -> enviar(post("/api/usuarios"), admin, alta("Duplicado", " OTRO@ZAROR.CL ", "RESIDENTE")).getResponse().getStatus());
        assertEquals(List.of(201, 409), altas);
        String otro = login("otro");
        MvcResult me = mvc.perform(token(get("/api/auth/me"), otro)).andExpect(status().isOk())
                .andExpect(jsonPath("$.contrasenaHash").doesNotExist()).andReturn();
        String otroId = campo(me, "id");
        assertEquals(24, otroId.length());

        String adminId = campo(mvc.perform(token(get("/api/auth/me"), admin)).andReturn(), "id");
        mvc.perform(token(get("/api/usuarios/" + adminId), admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"));
        assertEquals(409, enviar(patch("/api/usuarios/" + adminId + "/estado"), admin, "{\"activo\":false}").getResponse().getStatus());
        probarJwtInvalido(adminId);

        assertEquals(200, enviar(patch("/api/usuarios/" + otroId + "/estado"), admin, "{\"activo\":false}").getResponse().getStatus());
        mvc.perform(token(get("/api/auth/me"), otro)).andExpect(status().isUnauthorized());

        var nuevoAdmin = enviar(post("/api/usuarios"), admin, alta("Segundo administrador", "segundo@zaror.cl", "ADMINISTRADOR"));
        assertEquals(201, nuevoAdmin.getResponse().getStatus());
        String segundoId = campo(nuevoAdmin, "id");
        String segundo = login("segundo");
        List<Integer> deshabilitaciones = carrera(
                () -> enviar(patch("/api/usuarios/" + adminId + "/estado"), admin, "{\"activo\":false}").getResponse().getStatus(),
                () -> enviar(patch("/api/usuarios/" + segundoId + "/estado"), segundo, "{\"activo\":false}").getResponse().getStatus());
        assertEquals(List.of(200, 409), deshabilitaciones);
        assertEquals(1, mongo.getCollection("usuarios").countDocuments(new Document("rol", "ADMINISTRADOR").append("activo", true)));
    }

    @Test
    void reservasPagosYRollbackEnMongoReal() throws Exception {
        String admin = login("admin");
        String residente = login("residente");
        String tesoreria = login("tesoreria");
        var alta = enviar(post("/api/usuarios"), admin, alta("Otro residente", "otro@zaror.cl", "RESIDENTE"));
        assertEquals(201, alta.getResponse().getStatus());
        String otroId = campo(alta, "id");
        String otro = login("otro");

        mvc.perform(token(get("/api/espacios/" + QUINCHO + "/disponibilidad").param("fecha", DIA), residente))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bloques.length()").value(18))
                .andExpect(jsonPath("$.bloques[0].disponible").value(false))
                .andExpect(jsonPath("$.bloques[1].disponible").value(true))
                .andExpect(jsonPath("$.bloques[0].usuarioId").doesNotExist());
        mvc.perform(token(get("/api/espacios/" + QUINCHO + "/disponibilidad").param("fecha", "2026-12-17"), admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bloques.length()").value(14))
                .andExpect(jsonPath("$.bloques[13].disponible").value(false));
        mvc.perform(token(get("/api/espacios/" + SALA + "/disponibilidad").param("fecha", DIA), residente))
                .andExpect(status().isOk()).andExpect(jsonPath("$.habilitado").value(false));
        assertEquals(409, reservar(residente, SALA, 2, 3).getResponse().getStatus());
        assertEquals(400, reservar(residente, QUINCHO, 0, 1).getResponse().getStatus());
        assertEquals(404, reservar(residente, new ObjectId().toHexString(), 2, 3).getResponse().getStatus());
        assertEquals(400, reservar(residente, "id-invalido", 2, 3).getResponse().getStatus());
        assertEquals(400, enviar(post("/api/reservas"), residente, reservaJson(QUINCHO, 2, 3)
                .replace("\"fechaOperativa\":\"" + DIA + "\"", "\"fechaOperativa\":\"2026-12-19\""))
                .getResponse().getStatus());
        assertEquals(400, enviar(post("/api/reservas"), residente, reservaJson(QUINCHO, 2, 3)
                .replace("}", ",\"usuarioId\":\"" + otroId + "\"}")).getResponse().getStatus());

        List<Integer> resultados = carrera(
                () -> reservar(residente, QUINCHO, 3, 6).getResponse().getStatus(),
                () -> reservar(otro, QUINCHO, 5, 8).getResponse().getStatus());
        assertEquals(List.of(201, 409), resultados);
        assertEquals(1, mongo.getCollection("reservas").countDocuments());
        assertEquals(3, mongo.getCollection("bloques_reservados").countDocuments());
        assertTrue(mongo.getCollection("bloques_reservados").listIndexes().into(new ArrayList<>()).stream()
                .anyMatch(i -> "bloque_unico".equals(i.getString("name")) && Boolean.TRUE.equals(i.getBoolean("unique"))));
        Document original = mongo.getCollection("reservas").find().first();
        assertNotNull(original);
        assertTrue(original.containsKey("pago"));
        assertNull(original.get("pago"));
        assertNotNull(original.get("inicio", Date.class));
        assertNotNull(original.getObjectId("usuarioId"));
        String id = original.getObjectId("_id").toHexString();
        mvc.perform(token(get("/api/reservas"), admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
        Instant inicioOriginal = original.getDate("inicio").toInstant();
        int limiteAdyacente = 0;
        for (var bloque : horario.bloques(LocalDate.parse(DIA))) {
            if (bloque.inicio().equals(inicioOriginal)) break;
            limiteAdyacente++;
        }
        assertEquals(201, reservar(residente, QUINCHO, limiteAdyacente - 1, limiteAdyacente).getResponse().getStatus());
        assertEquals(201, reservar(residente, PISCINA, limiteAdyacente, limiteAdyacente + 1).getResponse().getStatus());
        String titular = original.getObjectId("usuarioId").toHexString().equals(otroId) ? otro : residente;
        String ajeno = titular.equals(otro) ? residente : otro;
        mvc.perform(token(get("/api/reservas/" + id), ajeno)).andExpect(status().isNotFound());
        mvc.perform(token(get("/api/reservas/mis-reservas"), titular)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(token(patch("/api/reservas/" + id + "/aprobar"), admin).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(token(patch("/api/reservas/" + id + "/aprobar"), admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.pago.estado").value("PENDIENTE"));
        mvc.perform(token(patch("/api/reservas/" + id + "/aprobar"), admin)).andExpect(status().isConflict());
        assertEquals(3, mongo.getCollection("bloques_reservados").countDocuments(new Document("reservaId", new ObjectId(id))));

        String archivo = "/api/pagos/" + id + "/comprobante";
        byte[] png = imagen("png");
        mvc.perform(token(multipart(archivo).file(new MockMultipartFile("archivo", "falso.png", "image/png", new byte[]{1,2,3})), titular))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(token(multipart(archivo).file(new MockMultipartFile("archivo", "vacio.png", "image/png", new byte[0])), titular))
                .andExpect(status().isBadRequest());
        mvc.perform(token(multipart(archivo).file(new MockMultipartFile("archivo", "grande.png", "image/png", new byte[10 * 1024 * 1024 + 1])), titular))
                .andExpect(status().isPayloadTooLarge());
        mvc.perform(token(multipart(archivo).file(new MockMultipartFile("archivo", "../../ajeno", "text/plain", png)), titular))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pago.comprobanteUrl").value(archivo));
        mvc.perform(token(get(archivo), ajeno)).andExpect(status().isNotFound());
        mvc.perform(token(get(archivo), admin)).andExpect(status().isForbidden());
        mvc.perform(token(get(archivo), tesoreria)).andExpect(status().isOk()).andExpect(content().bytes(png))
                .andExpect(header().string("Cache-Control", "no-store"));
        mvc.perform(get(archivo)).andExpect(status().isUnauthorized());
        mvc.perform(token(get("/api/pagos"), tesoreria)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservaId").value(id));
        String nombreInterno = mongo.findById(new ObjectId(id), Reserva.class).getPago().getComprobante();
        var rutaOriginal = java.nio.file.Path.of(upload).resolve(nombreInterno);
        var rutaTemporal = rutaOriginal.resolveSibling(nombreInterno + ".prueba");
        Files.move(rutaOriginal, rutaTemporal);
        try {
            mvc.perform(token(get(archivo), tesoreria)).andExpect(status().isNotFound());
        } finally {
            Files.move(rutaTemporal, rutaOriginal);
        }
        mvc.perform(token(patch("/api/reservas/" + id + "/rechazar-impago"), admin)).andExpect(status().isConflict());
        mvc.perform(token(patch("/api/pagos/" + id + "/rechazar"), tesoreria)).andExpect(status().isOk());
        probarReenvioFallido(titular, archivo, png);
        mvc.perform(token(multipart(archivo).file(new MockMultipartFile("archivo", "foto.jpg", "image/jpeg", imagen("jpeg"))), titular))
                .andExpect(status().isOk());
        assertEquals(List.of(200, 409), carrera(
                () -> mvc.perform(token(patch("/api/pagos/" + id + "/confirmar"), tesoreria)).andReturn().getResponse().getStatus(),
                () -> mvc.perform(token(patch("/api/pagos/" + id + "/confirmar"), tesoreria)).andReturn().getResponse().getStatus()));
        mvc.perform(token(patch("/api/reservas/" + id + "/rechazar-impago"), admin)).andExpect(status().isConflict());
        assertEquals(3, mongo.getCollection("ingresos").countDocuments());

        String nocturna = campo(reservar(otro, PISCINA, 15, 18), "id");
        mvc.perform(token(patch("/api/reservas/" + nocturna + "/aprobar"), admin)).andExpect(status().isOk());
        probarRollbackAlLiberar(admin, nocturna);
        mvc.perform(token(patch("/api/reservas/" + nocturna + "/rechazar-impago"), admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));
        assertEquals(0, mongo.getCollection("bloques_reservados").countDocuments(new Document("reservaId", new ObjectId(nocturna))));
        MvcResult liberada = reservar(residente, PISCINA, 15, 18);
        assertEquals(201, liberada.getResponse().getStatus());
        mvc.perform(token(patch("/api/reservas/" + campo(liberada, "id") + "/rechazar"), admin)).andExpect(status().isOk());
        assertEquals(201, reservar(otro, PISCINA, 15, 18).getResponse().getStatus());

        String enCarrera = campo(reservar(residente, PISCINA, 9, 10), "id");
        mvc.perform(token(patch("/api/reservas/" + enCarrera + "/aprobar"), admin)).andExpect(status().isOk());
        assertEquals(List.of(200, 409), carrera(
                () -> mvc.perform(token(multipart("/api/pagos/" + enCarrera + "/comprobante")
                        .file(new MockMultipartFile("archivo", "foto.png", "image/png", png)), residente))
                        .andReturn().getResponse().getStatus(),
                () -> mvc.perform(token(patch("/api/reservas/" + enCarrera + "/rechazar-impago"), admin))
                        .andReturn().getResponse().getStatus()));

        assertEquals(200, enviar(patch("/api/espacios/" + PISCINA + "/estado"), admin, "{\"habilitado\":false}").getResponse().getStatus());
        assertEquals(409, reservar(residente, PISCINA, 1, 2).getResponse().getStatus());
        assertEquals(200, enviar(patch("/api/espacios/" + SALA + "/estado"), admin, "{\"habilitado\":true}").getResponse().getStatus());
        var cambios = carrera(
                () -> reservar(residente, SALA, 1, 2).getResponse().getStatus(),
                () -> enviar(patch("/api/espacios/" + SALA + "/estado"), admin, "{\"habilitado\":false}").getResponse().getStatus());
        assertTrue(cambios.equals(List.of(200, 201)) || cambios.equals(List.of(200, 409)));
        assertEquals(cambios.contains(201) ? 1 : 0,
                mongo.getCollection("reservas").countDocuments(new Document("espacioId", new ObjectId(SALA))));
        assertEquals(409, reservar(residente, SALA, 2, 3).getResponse().getStatus());
    }

    @Test
    void finanzasYDatosPersistenTrasReiniciar() throws Exception {
        String admin = login("admin");
        String residente = login("residente");
        String tesoreria = login("tesoreria");
        String directiva = login("directiva");
        var alta = enviar(post("/api/usuarios"), admin, alta("Otro residente", "otro@zaror.cl", "RESIDENTE"));
        assertEquals(201, alta.getResponse().getStatus());
        String otroId = campo(alta, "id");
        String otro = login("otro");
        String nocturna = campo(reservar(otro, PISCINA, 15, 18), "id");
        mvc.perform(token(patch("/api/reservas/" + nocturna + "/rechazar"), admin)).andExpect(status().isOk());

        assertEquals(400, enviar(post("/api/ingresos"), tesoreria, "{\"descripcion\":\"Pago\",\"monto\":1.2,\"fecha\":\"2026-12-18\"}").getResponse().getStatus());
        assertEquals(400, enviar(post("/api/ingresos"), tesoreria, "{\"descripcion\":\"Pago\",\"monto\":\"1\",\"fecha\":\"2026-12-18\"}").getResponse().getStatus());
        assertEquals(400, enviar(post("/api/ingresos"), tesoreria, "{\"descripcion\":\"Pago\",\"monto\":0,\"fecha\":\"2026-12-18\"}").getResponse().getStatus());
        assertEquals(400, enviar(post("/api/ingresos"), tesoreria, "{\"descripcion\":\"Pago\",\"monto\":1,\"fecha\":\"2026-12-19\"}").getResponse().getStatus());
        assertEquals(400, enviar(post("/api/gastos"), tesoreria, "{\"descripcion\":\"Pago válido\",\"monto\":1,\"fecha\":\"2026-12-18\",\"categoria\":0}").getResponse().getStatus());
        assertEquals(201, enviar(post("/api/ingresos"), tesoreria, "{\"descripcion\":\"Ingreso adicional\",\"monto\":1,\"fecha\":\"2026-12-18\"}").getResponse().getStatus());
        assertEquals(201, enviar(post("/api/gastos"), tesoreria, "{\"descripcion\":\"Gasto adicional\",\"monto\":200000,\"fecha\":\"2026-12-18\",\"categoria\":\"OTROS\"}").getResponse().getStatus());
        mvc.perform(token(get("/api/ingresos"), directiva)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcion").value("Ingreso adicional"));
        mvc.perform(token(get("/api/gastos"), tesoreria)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcion").value("Gasto adicional"));
        mvc.perform(token(get("/api/finanzas/resumen"), residente)).andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(-59999));

        assertEquals(200, enviar(put("/api/usuarios/" + otroId), admin, "{\"nombre\":\"Nombre actualizado\",\"correo\":\"nuevo@zaror.cl\"}").getResponse().getStatus());
        mvc.perform(token(get("/api/reservas/" + nocturna), admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("Nombre actualizado"));
        try (var reinicio = new SpringApplicationBuilder(ZarorApplication.class).profiles("dev").run(
                "--server.port=0", "--spring.mongodb.uri=" + uri, "--zaror.jwt-secret=" + CLAVE,
                "--zaror.upload-dir=" + upload, "--zaror.demo-password=" + UUID.randomUUID())) {
            var usuarios = reinicio.getBean(com.zaror.backend.service.UsuarioService.class);
            var auth = reinicio.getBean(com.zaror.backend.service.AuthService.class);
            var espacios = reinicio.getBean(com.zaror.backend.service.EspacioService.class);
            assertEquals(otroId, auth.login(new com.zaror.backend.dto.Entradas.Login("nuevo@zaror.cl", CONTRASENA)).usuario().id());
            assertEquals("Nombre actualizado", usuarios.usuario(new ObjectId(otroId)).nombre());
            assertEquals(5, usuarios.usuarios().size());
            assertEquals(3, espacios.espacios().size());
            var mongoReinicio = reinicio.getBean(MongoTemplate.class);
            assertNotNull(mongoReinicio.findById(new ObjectId(nocturna), Reserva.class));
            assertEquals(4, mongoReinicio.getCollection("ingresos").countDocuments());
            assertEquals(7, mongoReinicio.getCollection("gastos").countDocuments());
            var web = (org.springframework.boot.web.server.context.WebServerApplicationContext) reinicio;
            var cliente = java.net.http.HttpClient.newHttpClient();
            var solicitud = java.net.http.HttpRequest.newBuilder(java.net.URI.create(
                    "http://127.0.0.1:" + web.getWebServer().getPort() + "/v3/api-docs"))
                    .timeout(Duration.ofSeconds(10)).build();
            var respuesta = cliente.send(solicitud, java.net.http.HttpResponse.BodyHandlers.ofString());
            assertEquals(200, respuesta.statusCode());
            assertEquals(29, json.readTree(respuesta.body()).path("paths").valueStream()
                    .mapToInt(ruta -> ruta.size()).sum());
        }
    }

    private void probarRollbackAlLiberar(String admin, String id) throws Exception {
        if (!"true".equals(System.getenv("ZAROR_TEST_FAILPOINTS"))) return;
        var administracionMongo = clienteMongo.getDatabase("admin");
        administracionMongo.runCommand(new Document("configureFailPoint", "failCommand")
                .append("mode", new Document("times", 1))
                .append("data", new Document("failCommands", List.of("delete")).append("errorCode", 2)));
        try {
            mvc.perform(token(patch("/api/reservas/" + id + "/rechazar-impago"), admin))
                    .andExpect(status().isServiceUnavailable());
            Document reserva = mongo.getCollection("reservas").find(new Document("_id", new ObjectId(id))).first();
            assertNotNull(reserva);
            assertEquals("APROBADA", reserva.getString("estado"));
            assertEquals("PENDIENTE", reserva.get("pago", Document.class).getString("estado"));
            assertEquals(3, mongo.getCollection("bloques_reservados").countDocuments(
                    new Document("reservaId", new ObjectId(id))));
        } finally {
            administracionMongo.runCommand(new Document("configureFailPoint", "failCommand").append("mode", "off"));
        }
    }

    private void probarReenvioFallido(String titular, String url, byte[] archivoAnterior) throws Exception {
        if (!"true".equals(System.getenv("ZAROR_TEST_FAILPOINTS"))) return;
        long archivosAntes;
        try (var archivos = Files.list(java.nio.file.Path.of(upload))) { archivosAntes = archivos.count(); }
        var administracionMongo = clienteMongo.getDatabase("admin");
        administracionMongo.runCommand(new Document("configureFailPoint", "failCommand")
                .append("mode", new Document("times", 1))
                .append("data", new Document("failCommands", List.of("findAndModify")).append("errorCode", 2)));
        try {
            mvc.perform(token(multipart(url).file(new MockMultipartFile("archivo", "nueva.jpg",
                    "image/jpeg", imagen("jpeg"))), titular)).andExpect(status().isServiceUnavailable());
            mvc.perform(token(get(url), titular)).andExpect(status().isOk())
                    .andExpect(content().bytes(archivoAnterior));
            try (var archivos = Files.list(java.nio.file.Path.of(upload))) {
                assertEquals(archivosAntes, archivos.count());
            }
        } finally {
            administracionMongo.runCommand(new Document("configureFailPoint", "failCommand").append("mode", "off"));
        }
    }

    private void probarJwtInvalido(String id) throws Exception {
        Instant vigente = reloj.instant().plusSeconds(90);
        var otraClave = new SecretKeySpec(new byte[32], "HmacSHA256");
        String[] tokens = {
                emitir(encoder, id, JwtTokens.ISSUER, JwtTokens.AUDIENCE, reloj.instant().minusSeconds(1)),
                emitir(encoder, id, "otro-emisor", JwtTokens.AUDIENCE, vigente),
                emitir(encoder, id, JwtTokens.ISSUER, "otra-audiencia", vigente),
                emitir(new NimbusJwtEncoder(new ImmutableSecret<>(otraClave)), id, JwtTokens.ISSUER, JwtTokens.AUDIENCE, vigente)
        };
        for (String jwt : tokens) {
            mvc.perform(token(get("/api/auth/me"), jwt)).andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
        }
    }

    private static String emitir(JwtEncoder encoder, String id, String emisor, String audiencia, Instant expiracion) {
        var claims = JwtClaimsSet.builder().subject(id).issuer(emisor).audience(List.of(audiencia))
                .issuedAt(expiracion.minusSeconds(600)).expiresAt(expiracion).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    private String login(String cuenta) throws Exception {
        MvcResult resultado = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"" + cuenta + "@zaror.cl\",\"contrasena\":\"" + CONTRASENA + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return campo(resultado, "token");
    }

    private MvcResult reservar(String jwt, String espacio, int desde, int hasta) throws Exception {
        return enviar(post("/api/reservas"), jwt, reservaJson(espacio, desde, hasta));
    }

    private String reservaJson(String espacio, int desde, int hasta) {
        var bloques = horario.bloques(LocalDate.parse(DIA));
        return """
                {"espacioId":"%s","fechaOperativa":"%s","inicio":"%s","fin":"%s"}
                """.formatted(espacio, DIA, bloques.get(desde).inicio(), bloques.get(hasta - 1).fin());
    }

    private static String alta(String nombre, String correo, String rol) {
        return """
                {"nombre":"%s","correo":"%s","contrasena":"%s","rol":"%s"}
                """.formatted(nombre, correo, CONTRASENA, rol);
    }

    private MvcResult enviar(MockHttpServletRequestBuilder solicitud, String jwt, String cuerpo) throws Exception {
        return mvc.perform(token(solicitud, jwt).contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andReturn();
    }

    private String campo(MvcResult resultado, String nombre) throws Exception {
        return json.readTree(resultado.getResponse().getContentAsString()).path(nombre).asString();
    }

    private static <T extends AbstractMockHttpServletRequestBuilder<T>> T token(T solicitud, String jwt) {
        return solicitud.header("Authorization", "Bearer " + jwt);
    }

    private static byte[] imagen(String formato) throws Exception {
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB), formato, bytes);
        return bytes.toByteArray();
    }

    private static List<Integer> carrera(Callable<Integer> primera, Callable<Integer> segunda) throws Exception {
        CountDownLatch listas = new CountDownLatch(2);
        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService ejecutor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> a = ejecutor.submit(() -> { listas.countDown(); salida.await(); return primera.call(); });
            Future<Integer> b = ejecutor.submit(() -> { listas.countDown(); salida.await(); return segunda.call(); });
            assertTrue(listas.await(5, TimeUnit.SECONDS));
            salida.countDown();
            List<Integer> resultados = new ArrayList<>(List.of(a.get(25, TimeUnit.SECONDS), b.get(25, TimeUnit.SECONDS)));
            Collections.sort(resultados);
            return resultados;
        } finally {
            ejecutor.shutdownNow();
        }
    }
}

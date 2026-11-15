package com.example.ferreteria.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.ferreteria.data.dao.AdminDao;
import com.example.ferreteria.data.dao.PedidoDao;
import com.example.ferreteria.data.entity.AuditEntity;
import com.example.ferreteria.data.entity.FarmEntity;
import com.example.ferreteria.data.entity.InsumoPedidoEntity;
import com.example.ferreteria.data.entity.InventoryEntity;
import com.example.ferreteria.data.entity.PedidoEntity;
import com.example.ferreteria.data.entity.RevisionPedidoEntity;
import com.example.ferreteria.data.entity.UserEntity;
import com.example.ferreteria.data.entity.ZoneEntity;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class AuthRepository {
    public interface ResultCallback<T> { void onSuccess(T value); void onError(Exception error); }
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private final AdminDao dao;
    private final PedidoDao pedidoDao;

    public AuthRepository(Context context) {
        KronoDatabase db = KronoDatabase.getInstance(context);
        dao = db.adminDao();
        pedidoDao = db.pedidoDao();
    }

    public void inicializarDatos(ResultCallback<Void> callback) {
        IO.execute(() -> {
            try {
                if (dao.obtenerZonas().isEmpty()) {
                    dao.guardarZona(new ZoneEntity("Zona Norte", "Zona agrícola norte", true));
                    dao.guardarZona(new ZoneEntity("Zona Valle Central", "Zona agrícola central", true));
                    dao.guardarZona(new ZoneEntity("Zona Sur", "Zona agrícola sur", true));
                    dao.guardarZona(new ZoneEntity("Zona Occidente", "Zona agrícola occidental", true));
                }
                if (dao.obtenerGranjasPorZona("Zona Norte").isEmpty()) {
                    dao.guardarGranja(new FarmEntity("Granja Los Laureles", "Zona Norte", true, "E100", "12345678", "Lote 01"));
                }
                if (dao.obtenerGranjasPorZona("Zona Valle Central").isEmpty()) {
                    dao.guardarGranja(new FarmEntity("Finca San Isidro", "Zona Valle Central", true, "E101", "12345678", "Lote 02"));
                }
                if (dao.obtenerGranjasPorZona("Zona Sur").isEmpty()) {
                    dao.guardarGranja(new FarmEntity("Granja Santa Elena", "Zona Sur", true, "E102", "12345678", "Lote 03"));
                }
                if (dao.obtenerGranjasPorZona("Zona Occidente").isEmpty()) {
                    dao.guardarGranja(new FarmEntity("Finca El Paraíso", "Zona Occidente", true, "E103", "12345678", "Lote 04"));
                }
                if (dao.obtenerUsuario("E100") == null) dao.guardarUsuario(new UserEntity("E100", "Granja Los Laureles", hash("12345678"), "OPERARIO", true, System.currentTimeMillis()));
                if (dao.obtenerUsuario("E101") == null) dao.guardarUsuario(new UserEntity("E101", "Finca San Isidro", hash("12345678"), "OPERARIO", true, System.currentTimeMillis()));
                if (dao.obtenerUsuario("E102") == null) dao.guardarUsuario(new UserEntity("E102", "Granja Santa Elena", hash("12345678"), "OPERARIO", true, System.currentTimeMillis()));
                if (dao.obtenerUsuario("E103") == null) dao.guardarUsuario(new UserEntity("E103", "Finca El Paraíso", hash("12345678"), "OPERARIO", true, System.currentTimeMillis()));
                if (dao.obtenerUsuario("S100") == null) dao.guardarUsuario(new UserEntity("S100", "Supervisor de prueba", hash("12345678"), "SUPERVISOR", true, System.currentTimeMillis()));
                if (dao.obtenerUsuario("A100") == null) dao.guardarUsuario(new UserEntity("A100", "Administrador", hash("12345678"), "ADMINISTRADOR", true, System.currentTimeMillis()));
                if (dao.obtenerInventario().isEmpty()) {
                    dao.guardarInventario(new InventoryEntity("Fertilizante NPK 15-15-15", "FERT-NPK-50", 20, "sacos"));
                    dao.guardarInventario(new InventoryEntity("Insecticida Concentrado 1L", "INS-L1", 12, "frascos"));
                }

                // Sembrar pedidos reales en SQLite si la tabla de pedidos está vacía
                if (pedidoDao.obtenerTodos().isEmpty()) {
                    long now = System.currentTimeMillis();

                    // Pedido 1: Granja Los Laureles (Zona Norte, E100) - APROBADO
                    PedidoEntity p1 = new PedidoEntity("PED-1794671240166", "E100", "Zona Norte", "APROBADO", "Granja Los Laureles", now - 3600000, now);
                    long p1Id = pedidoDao.insertarPedido(p1);
                    List<InsumoPedidoEntity> insumos1 = new ArrayList<>();
                    insumos1.add(new InsumoPedidoEntity(p1Id, "Fertilizante NPK 15-15-15 (Sacos 50kg)", "20 sacos", "SKU: Fungicida/Nutricional • SKU: FERT-NPK-50"));
                    insumos1.add(new InsumoPedidoEntity(p1Id, "Insecticida Concentrado 1L", "12 frascos", "SKU: Protección Fitosanitaria • SKU: INS-L1"));
                    insumos1.add(new InsumoPedidoEntity(p1Id, "Escobas Agrícolas Reforzadas", "3 unidades", "SKU: Insumo Agregado • Registro en Campo"));
                    pedidoDao.insertarInsumos(insumos1);
                    pedidoDao.insertarRevision(new RevisionPedidoEntity(p1Id,
                            "S100",
                            "Fertilizante NPK 15-15-15 (Sacos 50kg) [APROBADO - 20 sacos] | Insecticida Concentrado 1L [APROBADO - 12 frascos] | Escobas Agrícolas Reforzadas [APROBADO - 3 unidades]",
                            "Cantidades autorizadas conforme a stock en bodega central.",
                            now - 1800000));

                    // Pedido 2: Granja Los Laureles (Zona Norte, E100) - EN REVISIÓN
                    PedidoEntity p2 = new PedidoEntity("#PED-2026-11-ZN04", "E100", "Zona Norte", "EN REVISIÓN", "Granja Los Laureles", now - 7200000, now);
                    long p2Id = pedidoDao.insertarPedido(p2);
                    List<InsumoPedidoEntity> insumos2 = new ArrayList<>();
                    insumos2.add(new InsumoPedidoEntity(p2Id, "Semillas de Maíz Híbrido", "15 bolsas", "SKU: Insumo Agrícola • SKU: SEM-MZ-15"));
                    insumos2.add(new InsumoPedidoEntity(p2Id, "Fungicida Sistémico 5L", "6 galones", "SKU: Protección Fitosanitaria • SKU: FUNG-SIS-5L"));
                    pedidoDao.insertarInsumos(insumos2);

                    // Pedido 3: Finca San Isidro (Zona Valle Central, E101) - APROBADO
                    PedidoEntity p3 = new PedidoEntity("#PED-2026-11-VC01", "E101", "Zona Valle Central", "APROBADO", "Finca San Isidro", now - 86400000, now);
                    long p3Id = pedidoDao.insertarPedido(p3);
                    List<InsumoPedidoEntity> insumos3 = new ArrayList<>();
                    insumos3.add(new InsumoPedidoEntity(p3Id, "Urea Granulada 46%", "25 sacos", "SKU: Nutricional • SKU: UREA-GR-46"));
                    insumos3.add(new InsumoPedidoEntity(p3Id, "Bomba Aspersora Manual", "2 unidades", "SKU: Equipamiento • SKU: BOMB-ASP-02"));
                    pedidoDao.insertarInsumos(insumos3);
                    pedidoDao.insertarRevision(new RevisionPedidoEntity(p3Id,
                            "S100",
                            "Urea Granulada 46% [APROBADO - 25 sacos] | Bomba Aspersora Manual [APROBADO - 2 unidades]",
                            "Aprobado para despacho de ciclo mensual.",
                            now - 43200000));

                    // Pedido 4: Granja Santa Elena (Zona Sur, E102) - EN REVISIÓN
                    PedidoEntity p4 = new PedidoEntity("#PED-2026-10-ZS09", "E102", "Zona Sur", "EN REVISIÓN", "Granja Santa Elena", now - 172800000, now);
                    long p4Id = pedidoDao.insertarPedido(p4);
                    List<InsumoPedidoEntity> insumos4 = new ArrayList<>();
                    insumos4.add(new InsumoPedidoEntity(p4Id, "Cal Agrícola 50kg", "30 sacos", "SKU: Corrector de Suelos • SKU: CAL-AG-50"));
                    pedidoDao.insertarInsumos(insumos4);
                }

                MAIN.post(() -> callback.onSuccess(null));
            } catch (Exception e) { MAIN.post(() -> callback.onError(e)); }
        });
    }

    public void autenticar(String id, String password, String rol, ResultCallback<UserEntity> callback) {
        IO.execute(() -> {
            try {
                UserEntity user = dao.obtenerUsuario(id);
                if (user == null || !user.activo || !user.rol.equals(rol) || !verificar(password, user.passwordHash)) user = null;
                UserEntity result = user;
                MAIN.post(() -> callback.onSuccess(result));
            } catch (Exception e) { MAIN.post(() -> callback.onError(e)); }
        });
    }

    public void crearUsuario(String id, String nombre, String password, String rol, ResultCallback<Void> callback) {
        IO.execute(() -> {
            try {
                if (id.trim().isEmpty() || nombre.trim().isEmpty() || password.length() < 8) throw new IllegalArgumentException("ID y nombre obligatorios; la contraseña debe tener al menos 8 caracteres.");
                if (dao.obtenerUsuario(id.trim()) != null) throw new IllegalArgumentException("Ya existe un usuario con ese ID.");
                dao.guardarUsuario(new UserEntity(id.trim(), nombre.trim(), hash(password), rol, true, System.currentTimeMillis()));
                dao.registrarAuditoria(new AuditEntity("A100", "CREAR", "USUARIO", id.trim(), System.currentTimeMillis()));
                MAIN.post(() -> callback.onSuccess(null));
            } catch (Exception e) { MAIN.post(() -> callback.onError(e)); }
        });
    }

    public void listarUsuarios(ResultCallback<List<UserEntity>> cb) { run(dao::obtenerUsuarios, cb); }
    public void listarGranjas(ResultCallback<List<FarmEntity>> cb) { run(dao::obtenerGranjas, cb); }
    public void listarGranjasPorZona(String zona, ResultCallback<List<FarmEntity>> cb) { run(() -> dao.obtenerGranjasPorZona(zona), cb); }
    public void listarZonas(ResultCallback<List<ZoneEntity>> cb) { run(dao::obtenerZonas, cb); }
    public void listarInventario(ResultCallback<List<InventoryEntity>> cb) { run(dao::obtenerInventario, cb); }
    public void listarAuditoria(ResultCallback<List<AuditEntity>> cb) { run(dao::obtenerAuditoria, cb); }

    public void crearGranja(String nombre, String zona, String codigoAcceso, String password, String lote, ResultCallback<Void> cb) {
        IO.execute(() -> {
            try {
                if (nombre.trim().isEmpty() || codigoAcceso.trim().isEmpty()) {
                    throw new IllegalArgumentException("Nombre y clave/código de la granja son obligatorios.");
                }
                String pass = (password == null || password.trim().isEmpty()) ? "12345678" : password.trim();
                String l = (lote == null || lote.trim().isEmpty()) ? "Lote 01" : lote.trim();
                dao.guardarGranja(new FarmEntity(nombre.trim(), zona, true, codigoAcceso.trim(), pass, l));
                dao.guardarUsuario(new UserEntity(codigoAcceso.trim(), nombre.trim(), hash(pass), "OPERARIO", true, System.currentTimeMillis()));
                audit("CREAR", "GRANJA", nombre.trim() + " (" + codigoAcceso.trim() + ") - Lote: " + l);
                MAIN.post(() -> cb.onSuccess(null));
            } catch (Exception e) {
                MAIN.post(() -> cb.onError(e));
            }
        });
    }

    public void crearGranja(String nombre, String zona, String codigoAcceso, String password, ResultCallback<Void> cb) {
        crearGranja(nombre, zona, codigoAcceso, password, "Lote 01", cb);
    }

    public void crearGranja(String nombre, String zona, ResultCallback<Void> cb) {
        crearGranja(nombre, zona, "G" + (System.currentTimeMillis() % 10000), "12345678", "Lote 01", cb);
    }

    public void cambiarEstadoGranja(long id, boolean activa, String nombre, ResultCallback<Void> cb) {
        IO.execute(() -> {
            try {
                dao.cambiarEstadoGranja(id, activa);
                audit(activa ? "ACTIVAR" : "DESACTIVAR", "GRANJA", nombre);
                MAIN.post(() -> cb.onSuccess(null));
            } catch (Exception e) {
                MAIN.post(() -> cb.onError(e));
            }
        });
    }

    public void actualizarGranja(long id, String nombre, String zona, String lote, String codigo, String password, ResultCallback<Void> cb) {
        IO.execute(() -> {
            try {
                String pass = (password == null || password.trim().isEmpty()) ? "12345678" : password.trim();
                String l = (lote == null || lote.trim().isEmpty()) ? "Lote 01" : lote.trim();
                dao.actualizarGranja(id, nombre.trim(), zona, l, codigo.trim(), pass);
                dao.guardarUsuario(new UserEntity(codigo.trim(), nombre.trim(), hash(pass), "OPERARIO", true, System.currentTimeMillis()));
                audit("ACTUALIZAR", "GRANJA", nombre.trim() + " (" + codigo.trim() + ")");
                MAIN.post(() -> cb.onSuccess(null));
            } catch (Exception e) {
                MAIN.post(() -> cb.onError(e));
            }
        });
    }

    public void cambiarEstadoZona(long id, boolean activa, String nombre, ResultCallback<Void> cb) {
        IO.execute(() -> {
            try {
                dao.cambiarEstadoZona(id, activa);
                audit(activa ? "ACTIVAR" : "DESACTIVAR", "ZONA", nombre);
                MAIN.post(() -> cb.onSuccess(null));
            } catch (Exception e) {
                MAIN.post(() -> cb.onError(e));
            }
        });
    }

    public void actualizarZona(long id, String nombre, String descripcion, ResultCallback<Void> cb) {
        IO.execute(() -> {
            try {
                dao.actualizarZona(id, nombre.trim(), descripcion != null ? descripcion.trim() : "");
                audit("ACTUALIZAR", "ZONA", nombre.trim());
                MAIN.post(() -> cb.onSuccess(null));
            } catch (Exception e) {
                MAIN.post(() -> cb.onError(e));
            }
        });
    }

    public void listarPedidosPorGranja(String granja, String codigo, ResultCallback<List<PedidoEntity>> cb) {
        run(() -> dao.obtenerPedidosPorGranja(granja, codigo), cb);
    }

    public void listarPedidosPorZona(String zona, ResultCallback<List<PedidoEntity>> cb) {
        run(() -> dao.obtenerPedidosPorZona(zona), cb);
    }

    public void listarTodosLosPedidos(ResultCallback<List<PedidoEntity>> cb) {
        run(dao::obtenerTodosLosPedidos, cb);
    }

    public void obtenerGranjaPorCodigo(String codigo, ResultCallback<FarmEntity> cb) {
        run(() -> dao.obtenerGranjaPorCodigo(codigo), cb);
    }
    public void crearZona(String nombre, String descripcion, ResultCallback<Void> cb) {
        IO.execute(() -> { try { dao.guardarZona(new ZoneEntity(nombre, descripcion, true)); audit("CREAR","ZONA",nombre); MAIN.post(() -> cb.onSuccess(null)); } catch(Exception e){ MAIN.post(() -> cb.onError(e)); }});
    }
    public void crearInventario(String nombre, String sku, double cantidad, String unidad, ResultCallback<Void> cb) {
        IO.execute(() -> { try { dao.guardarInventario(new InventoryEntity(nombre, sku, cantidad, unidad)); audit("CREAR","INVENTARIO",sku); MAIN.post(() -> cb.onSuccess(null)); } catch(Exception e){ MAIN.post(() -> cb.onError(e)); }});
    }
    public void auditar(String usuario, String accion, String entidad, String detalle) {
        IO.execute(() -> { try { dao.registrarAuditoria(new AuditEntity(usuario, accion, entidad, detalle, System.currentTimeMillis())); } catch(Exception ignored) {} });
    }
    private void audit(String accion, String entidad, String detalle) { dao.registrarAuditoria(new AuditEntity("A100", accion, entidad, detalle, System.currentTimeMillis())); }

    private <T> void run(java.util.concurrent.Callable<T> task, ResultCallback<T> cb) {
        IO.execute(() -> { try { T value=task.call(); MAIN.post(() -> cb.onSuccess(value)); } catch(Exception e){ MAIN.post(() -> cb.onError(e)); }});
    }
    private static String hash(String password) throws Exception {
        byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120000, 256);
        byte[] derived = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();
        spec.clearPassword();
        return hex(salt) + ":" + hex(derived);
    }
    private static boolean verificar(String password, String stored) throws Exception {
        String[] parts = stored.split(":"); if (parts.length != 2) return false;
        byte[] salt = unhex(parts[0]); byte[] expected = unhex(parts[1]);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120000, expected.length * 8);
        byte[] actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();
        spec.clearPassword(); return MessageDigest.isEqual(expected, actual);
    }
    private static String hex(byte[] bytes) { StringBuilder s=new StringBuilder(); for(byte b:bytes) s.append(String.format("%02x", b)); return s.toString(); }
    private static byte[] unhex(String value) { byte[] out=new byte[value.length()/2]; for(int i=0;i<out.length;i++) out[i]=(byte)Integer.parseInt(value.substring(i*2,i*2+2),16); return out; }
}

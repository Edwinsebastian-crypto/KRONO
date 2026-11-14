package com.example.ferreteria.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.ferreteria.data.dao.AdminDao;
import com.example.ferreteria.data.entity.AuditEntity;
import com.example.ferreteria.data.entity.FarmEntity;
import com.example.ferreteria.data.entity.InventoryEntity;
import com.example.ferreteria.data.entity.PedidoEntity;
import com.example.ferreteria.data.entity.UserEntity;
import com.example.ferreteria.data.entity.ZoneEntity;

import java.security.MessageDigest;
import java.security.SecureRandom;
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

    public AuthRepository(Context context) { dao = KronoDatabase.getInstance(context).adminDao(); }

    public void inicializarDatos(ResultCallback<Void> callback) {
        IO.execute(() -> {
            try {
                if (dao.obtenerZonas().isEmpty()) {
                    dao.guardarZona(new ZoneEntity("Zona Norte", "Zona agrícola norte", true));
                    dao.guardarZona(new ZoneEntity("Zona Valle Central", "Zona agrícola central", true));
                    dao.guardarZona(new ZoneEntity("Zona Sur", "Zona agrícola sur", true));
                    dao.guardarZona(new ZoneEntity("Zona Occidente", "Zona agrícola occidental", true));
                }
                if (dao.obtenerGranjas().isEmpty()) {
                    dao.guardarGranja(new FarmEntity("Granja Los Laureles", "Zona Norte", true, "E100", "12345678"));
                    dao.guardarGranja(new FarmEntity("Finca San Isidro", "Zona Valle Central", true, "E101", "12345678"));
                    dao.guardarGranja(new FarmEntity("Granja Santa Elena", "Zona Sur", true, "E102", "12345678"));
                }
                if (dao.obtenerUsuario("E100") == null) dao.guardarUsuario(new UserEntity("E100", "Granja Los Laureles", hash("12345678"), "OPERARIO", true, System.currentTimeMillis()));
                if (dao.obtenerUsuario("E101") == null) dao.guardarUsuario(new UserEntity("E101", "Finca San Isidro", hash("12345678"), "OPERARIO", true, System.currentTimeMillis()));
                if (dao.obtenerUsuario("E102") == null) dao.guardarUsuario(new UserEntity("E102", "Granja Santa Elena", hash("12345678"), "OPERARIO", true, System.currentTimeMillis()));
                if (dao.obtenerUsuario("S100") == null) dao.guardarUsuario(new UserEntity("S100", "Supervisor de prueba", hash("12345678"), "SUPERVISOR", true, System.currentTimeMillis()));
                if (dao.obtenerUsuario("A100") == null) dao.guardarUsuario(new UserEntity("A100", "Administrador", hash("12345678"), "ADMINISTRADOR", true, System.currentTimeMillis()));
                if (dao.obtenerInventario().isEmpty()) {
                    dao.guardarInventario(new InventoryEntity("Fertilizante NPK 15-15-15", "FERT-NPK-50", 20, "sacos"));
                    dao.guardarInventario(new InventoryEntity("Insecticida Concentrado 1L", "INS-L1", 12, "frascos"));
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
    public void listarZonas(ResultCallback<List<ZoneEntity>> cb) { run(dao::obtenerZonas, cb); }
    public void listarInventario(ResultCallback<List<InventoryEntity>> cb) { run(dao::obtenerInventario, cb); }
    public void listarAuditoria(ResultCallback<List<AuditEntity>> cb) { run(dao::obtenerAuditoria, cb); }

    public void crearGranja(String nombre, String zona, String codigoAcceso, String password, ResultCallback<Void> cb) {
        IO.execute(() -> {
            try {
                if (nombre.trim().isEmpty() || codigoAcceso.trim().isEmpty()) {
                    throw new IllegalArgumentException("Nombre y clave/código de la granja son obligatorios.");
                }
                String pass = (password == null || password.trim().isEmpty()) ? "12345678" : password.trim();
                dao.guardarGranja(new FarmEntity(nombre.trim(), zona, true, codigoAcceso.trim(), pass));
                dao.guardarUsuario(new UserEntity(codigoAcceso.trim(), nombre.trim(), hash(pass), "OPERARIO", true, System.currentTimeMillis()));
                audit("CREAR", "GRANJA", nombre.trim() + " (" + codigoAcceso.trim() + ")");
                MAIN.post(() -> cb.onSuccess(null));
            } catch (Exception e) {
                MAIN.post(() -> cb.onError(e));
            }
        });
    }

    public void crearGranja(String nombre, String zona, ResultCallback<Void> cb) {
        crearGranja(nombre, zona, "G" + (System.currentTimeMillis() % 10000), "12345678", cb);
    }

    public void listarGranjasPorZona(String zona, ResultCallback<List<FarmEntity>> cb) {
        run(() -> dao.obtenerGranjasPorZona(zona), cb);
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

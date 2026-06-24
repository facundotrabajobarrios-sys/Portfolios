import 'package:supabase_flutter/supabase_flutter.dart';

class SupabaseService {
  static final SupabaseClient client = Supabase.instance.client;

  // ---------------- LOGIN ----------------
  static Future<Map<String, dynamic>?> login(
    String correo,
    String password,
  ) async {
    try {
      final res = await client
          .from('usuario')
          .select()
          .eq('user_correo', correo)
          .eq('user_pass', password)
          .maybeSingle();

      return res;
    } catch (e) {
      print('Error login: $e');
      return null;
    }
  }

  // ---------------- OBTENER USUARIO POR ID ----------------
  static Future<Map<String, dynamic>?> obtenerUsuarioPorId(int usuId) async {
    try {
      final data = await client
          .from('usuario')
          .select()
          .eq('user_id', usuId)
          .maybeSingle();
      return data;
    } catch (e) {
      print('Error obtenerUsuarioPorId: $e');
      return null;
    }
  }

  // ---------------- ACTUALIZAR USUARIO ----------------
  static Future<bool> actualizarUsuario(
    int usuId,
    Map<String, dynamic> usuario,
  ) async {
    try {
      await client.from('usuario').update(usuario).eq('user_id', usuId);
      return true;
    } catch (e) {
      print('Error actualizarUsuario: $e');
      return false;
    }
  }

  // ---------------- INSERTAR USUARIO ----------------
  static Future<bool> insertarUsuario(Map<String, dynamic> usuario) async {
    try {
      // Obtener el último ID de usuario para generar un nuevo ID
      final idRes = await client
          .from('usuario')
          .select('user_id')
          .order('user_id', ascending: false)
          .limit(1)
          .maybeSingle();

      int nuevoId = idRes != null ? (idRes['user_id'] as int) + 1 : 1;
      usuario['user_id'] = nuevoId;

      // Insertar usuario
      await client.from('usuario').insert(usuario);

      return true;
    } on PostgrestException catch (e) {
      print('Error insertarUsuario (Postgrest): ${e.message}');
      return false;
    } catch (e) {
      print('Error inesperado insertarUsuario: $e');
      return false;
    }
  }

  // ---------------- OBTENER CIUDADES ----------------
  static Future<List<Map<String, dynamic>>> obtenerCiudades() async {
    try {
      final data = await client.from('ciudad').select().order('ciu_descri');
      return List<Map<String, dynamic>>.from(data);
    } on PostgrestException catch (e) {
      print('Error obtenerCiudades: ${e.message}');
      return [];
    } catch (e) {
      print('Error inesperado obtenerCiudades: $e');
      return [];
    }
  }

  // ---------------- OBTENER CLIENTES ----------------
  static Future<List<Map<String, dynamic>>> obtenerClientes() async {
    try {
      final data = await client
          .from('clientes')
          .select(
            'cli_id, cli_nom, cli_ape, cli_tel, cli_email, ciudad:ciu_cod (ciu_descri)',
          )
          .order('cli_nom');

      return List<Map<String, dynamic>>.from(data);
    } on PostgrestException catch (e) {
      print('Error obtenerClientes: ${e.message}');
      return [];
    } catch (e) {
      print('Error inesperado obtenerClientes: $e');
      return [];
    }
  }

  // ---------------- GUARDAR CLIENTE ----------------
  static Future<bool> guardarCliente(Map<String, dynamic> cliente) async {
    try {
      final idRes = await client
          .from('clientes')
          .select('cli_id')
          .order('cli_id', ascending: false)
          .limit(1)
          .maybeSingle();

      int nuevoId = idRes != null ? (idRes['cli_id'] as int) + 1 : 1;
      cliente['cli_id'] = nuevoId;

      await client.from('clientes').insert(cliente);
      return true;
    } on PostgrestException catch (e) {
      print('Error guardarCliente (Postgrest): ${e.message}');
      return false;
    } catch (e) {
      print('Error inesperado guardarCliente: $e');
      return false;
    }
  }

  // ====================================================
  // ---------------- GESTIÓN DE SUCURSALES ----------------
  // ====================================================

  // ---------------- OBTENER SUCURSALES ----------------
  static Future<List<Map<String, dynamic>>> obtenerSucursales() async {
    try {
      final data = await client
          .from('sucursal')
          .select('suc_cod, suc_descri, suc_estado')
          .order('suc_descri');

      return List<Map<String, dynamic>>.from(data);
    } on PostgrestException catch (e) {
      print('Error obtenerSucursales: ${e.message}');
      return [];
    } catch (e) {
      print('Error inesperado obtenerSucursales: $e');
      return [];
    }
  }

  // ---------------- GUARDAR SUCURSAL ----------------
  static Future<bool> guardarSucursal(String descripcion) async {
    try {
      final idRes = await client
          .from('sucursal')
          .select('suc_cod')
          .order('suc_cod', ascending: false)
          .limit(1)
          .maybeSingle();

      int nuevoId = idRes != null ? (idRes['suc_cod'] as int) + 1 : 1;

      await client.from('sucursal').insert({
        'suc_cod': nuevoId,
        'suc_descri': descripcion,
        'suc_estado': 'ACTIVO',
      });

      return true;
    } on PostgrestException catch (e) {
      print('Error guardarSucursal: ${e.message}');
      return false;
    } catch (e) {
      print('Error inesperado guardarSucursal: $e');
      return false;
    }
  }

  // ---------------- ACTUALIZAR SUCURSAL ----------------
  static Future<bool> actualizarSucursal(
    int id,
    Map<String, dynamic> sucursal,
  ) async {
    try {
      await client.from('sucursal').update(sucursal).eq('suc_cod', id);
      return true;
    } on PostgrestException catch (e) {
      print('Error actualizarSucursal: ${e.message}');
      return false;
    } catch (e) {
      print('Error inesperado actualizarSucursal: $e');
      return false;
    }
  }

  // ---------------- CAMBIAR ESTADO SUCURSAL ----------------
  static Future<bool> cambiarEstadoSucursal(int id, String estadoActual) async {
    try {
      String nuevoEstado = estadoActual == 'ACTIVO' ? 'ANULADO' : 'ACTIVO';
      await client
          .from('sucursal')
          .update({'suc_estado': nuevoEstado})
          .eq('suc_cod', id);
      return true;
    } on PostgrestException catch (e) {
      print('Error cambiarEstadoSucursal: ${e.message}');
      return false;
    } catch (e) {
      print('Error inesperado cambiarEstadoSucursal: $e');
      return false;
    }
  }
}

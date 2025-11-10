package service;

import dto.AdministrativoForm;
import dto.DocenteForm;
import dto.EstudianteForm;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.UsuarioForm;
import model.*;
import repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioRegistrationService {

    @Autowired
    private UsuarioRepository usuarioRepo;
    
    @Autowired
    private UsuarioAuthRepository authRepo;
    
    @Autowired
    private EstudianteRepository estudianteRepo;
    
    @Autowired
    private DocenteRepository docenteRepo;
    
    @Autowired
    private AdministrativoRepository administrativoRepo;
    
    @Autowired
    private TipoDocumentoRepository tipoDocRepo;
    
    @Autowired
    private RolRepository rolRepo;
    
    @Autowired
    private EscuelaRepository escuelaRepo;

    private void validarDocumento(String numDoc, Integer idUsuarioActual) {
        Optional<Usuario> existenteOpt = usuarioRepo.findByNumDoc(numDoc);
        if (existenteOpt.isPresent() && 
            (idUsuarioActual == null || !existenteOpt.get().idUsuario.equals(idUsuarioActual))) {
            throw new RuntimeException("El documento ya está registrado.");
        }
    }

    private void validarCorreo(String correo, Integer idUsuarioActual) {
        Optional<UsuarioAuth> existenteOpt = authRepo.findByCorreoU(correo);
        if (existenteOpt.isPresent() && 
            (idUsuarioActual == null || !existenteOpt.get().usuario.idUsuario.equals(idUsuarioActual))) {
            throw new RuntimeException("El correo ya está registrado.");
        }
    }

    private void validarPasswordObligatoria(String password) {
        if (password == null || password.isBlank()) {
            throw new RuntimeException("La contraseña es obligatoria.");
        }
    }

    private String codificarPassword(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    private void actualizarPasswordSiCorresponde(UsuarioAuth auth, String password) {
        if (password != null && !password.isBlank()) {
            auth.password = codificarPassword(password);
        }
    }

    private UsuarioAuth obtenerCredencial(Usuario usuario) {
        Optional<UsuarioAuth> authOpt = authRepo.findByUsuario(usuario);
        if (authOpt.isEmpty()) {
            throw new RuntimeException("No se encontraron credenciales para el usuario indicado.");
        }
        return authOpt.get();
    }

    private LocalDate parsearFecha(String fecha) {
        try {
            return LocalDate.parse(fecha, DateTimeFormatter.ISO_DATE);
        } catch (Exception e) {
            throw new RuntimeException("Formato de fecha inválido. Utiliza AAAA-MM-DD.");
        }
    }
    
    private LocalDate fechaActualSiVacia(String fecha) {
        return (fecha == null || fecha.isBlank()) ? LocalDate.now() : parsearFecha(fecha);
    }

    private Escuela obtenerEscuelaOpcional(Integer idEscuela) {
        if (idEscuela == null) {
            return null;
        }
        return escuelaRepo.findById(idEscuela)
            .orElseThrow(() -> new RuntimeException("Escuela no válida"));
    }
    
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario obtenerUsuario(Integer id) {
        return usuarioRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
    }

    @Transactional
    public Usuario crearUsuarioBasico(UsuarioForm form) {
        validarDocumento(form.numDoc, null);
        validarCorreo(form.correo, null);
        validarPasswordObligatoria(form.password);

        TipoDocumento tipoDoc = tipoDocRepo.findById(form.idTipoDoc)
            .orElseThrow(() -> new RuntimeException("Tipo de documento no válido"));
        Rol rol = rolRepo.findById(form.idRol)
            .orElseThrow(() -> new RuntimeException("Rol no válido"));

        Usuario usuario = new Usuario();
        usuario.nombre = form.nombre;
        usuario.apellido = form.apellido;
        usuario.tipoDoc = tipoDoc;
        usuario.numDoc = form.numDoc;
        usuario.celular = form.celular;
        usuario.genero = form.genero;
        usuario.rol = rol;
        usuario.estado = 1;
        usuario.fechaRegistro = java.time.LocalDateTime.now();
        usuarioRepo.save(usuario);

        UsuarioAuth auth = new UsuarioAuth();
        auth.usuario = usuario;
        auth.correoU = form.correo;
        auth.password = codificarPassword(form.password);
        authRepo.save(auth);

        return usuario;
    }

    @Transactional
    public Usuario actualizarUsuario(Integer id, UsuarioForm form) {
        Usuario usuario = obtenerUsuario(id);
        
        validarDocumento(form.numDoc, usuario.idUsuario);
        validarCorreo(form.correo, usuario.idUsuario);

        TipoDocumento tipoDoc = tipoDocRepo.findById(form.idTipoDoc)
            .orElseThrow(() -> new RuntimeException("Tipo de documento no válido"));
        Rol rol = rolRepo.findById(form.idRol)
            .orElseThrow(() -> new RuntimeException("Rol no válido"));

        usuario.nombre = form.nombre;
        usuario.apellido = form.apellido;
        usuario.tipoDoc = tipoDoc;
        usuario.numDoc = form.numDoc;
        usuario.celular = form.celular;
        usuario.genero = form.genero;
        usuario.rol = rol;
        usuarioRepo.save(usuario);

        UsuarioAuth auth = obtenerCredencial(usuario);
        auth.correoU = form.correo;
        actualizarPasswordSiCorresponde(auth, form.password);
        authRepo.save(auth);

        return usuario;
    }

    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario usuario = obtenerUsuario(id);
        usuarioRepo.delete(usuario);
    }

    @Transactional
    public Estudiante registerEstudiante(EstudianteForm form) {
        validarDocumento(form.numDoc, null);
        validarCorreo(form.correo, null);
        validarPasswordObligatoria(form.password);

        TipoDocumento tipoDoc = tipoDocRepo.findById(form.idTipoDoc)
            .orElseThrow(() -> new RuntimeException("Tipo de documento no válido"));
        Rol rol = rolRepo.findById(2)
            .orElseThrow(() -> new RuntimeException("Rol Estudiante no encontrado"));
        Escuela escuela = escuelaRepo.findById(form.idEscuela)
            .orElseThrow(() -> new RuntimeException("Escuela no válida"));

        Usuario usuario = new Usuario();
        usuario.nombre = form.nombre;
        usuario.apellido = form.apellido;
        usuario.tipoDoc = tipoDoc;
        usuario.numDoc = form.numDoc;
        usuario.celular = form.celular;
        usuario.genero = form.genero;
        usuario.rol = rol;
        usuario.estado = 1;
        usuario.fechaRegistro = java.time.LocalDateTime.now();
        usuarioRepo.save(usuario);

        UsuarioAuth auth = new UsuarioAuth();
        auth.usuario = usuario;
        auth.correoU = form.correo;
        auth.password = codificarPassword(form.password);
        authRepo.save(auth);

        Estudiante estudiante = new Estudiante();
        estudiante.usuario = usuario;
        estudiante.escuela = escuela;
        estudiante.codigo = form.codigo;
        return estudianteRepo.save(estudiante);
    }

    @Transactional(readOnly = true)
    public List<Estudiante> listarEstudiantes() {
        return estudianteRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Estudiante obtenerEstudiante(Integer id) {
        return estudianteRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Estudiante no encontrado."));
    }

    @Transactional
    public void eliminarEstudiante(Integer id) {
        Estudiante estudiante = obtenerEstudiante(id);
        usuarioRepo.delete(estudiante.usuario);
    }
    
    @Transactional
    public Estudiante actualizarEstudiante(Integer id, EstudianteForm form) {
        Estudiante estudiante = obtenerEstudiante(id);
        Usuario usuario = estudiante.usuario;

        validarDocumento(form.numDoc, usuario.idUsuario);
        validarCorreo(form.correo, usuario.idUsuario);

        TipoDocumento tipoDoc = tipoDocRepo.findById(form.idTipoDoc)
            .orElseThrow(() -> new RuntimeException("Tipo de documento no válido"));
        Rol rol = rolRepo.findById(2)
            .orElseThrow(() -> new RuntimeException("Rol Estudiante no encontrado"));
        Escuela escuela = escuelaRepo.findById(form.idEscuela)
            .orElseThrow(() -> new RuntimeException("Escuela no válida"));

        usuario.nombre = form.nombre;
        usuario.apellido = form.apellido;
        usuario.tipoDoc = tipoDoc;
        usuario.numDoc = form.numDoc;
        usuario.celular = form.celular;
        usuario.genero = form.genero;
        usuario.rol = rol;
        usuarioRepo.save(usuario);

        UsuarioAuth auth = obtenerCredencial(usuario);
        auth.correoU = form.correo;
        actualizarPasswordSiCorresponde(auth, form.password);
        authRepo.save(auth);

        estudiante.escuela = escuela;
        estudiante.codigo = form.codigo;
        return estudianteRepo.save(estudiante);
    }
    
    @Transactional
    public Docente registerDocente(DocenteForm form) {
        validarDocumento(form.numDoc, null);
        validarCorreo(form.correo, null);
        validarPasswordObligatoria(form.password);

        TipoDocumento tipoDoc = tipoDocRepo.findById(form.idTipoDoc)
            .orElseThrow(() -> new RuntimeException("Tipo de documento no válido"));
        Rol rol = rolRepo.findById(1)
            .orElseThrow(() -> new RuntimeException("Rol Docente no encontrado"));
        Escuela escuela = obtenerEscuelaOpcional(form.idEscuela);

        Usuario usuario = new Usuario();
        usuario.nombre = form.nombre;
        usuario.apellido = form.apellido;
        usuario.tipoDoc = tipoDoc;
        usuario.numDoc = form.numDoc;
        usuario.celular = form.celular;
        usuario.genero = form.genero;
        usuario.rol = rol;
        usuario.estado = 1;
        usuario.fechaRegistro = java.time.LocalDateTime.now();
        usuarioRepo.save(usuario);

        UsuarioAuth auth = new UsuarioAuth();
        auth.usuario = usuario;
        auth.correoU = form.correo;
        auth.password = codificarPassword(form.password);
        authRepo.save(auth);

        Docente docente = new Docente();
        docente.usuario = usuario;
        docente.escuela = escuela;
        docente.codigoDocente = form.codigoDocente;
        docente.tipoContrato = TipoContrato.fromDescripcion(form.tipoContrato);
        docente.especialidad = form.especialidad;
        docente.fechaIncorporacion = fechaActualSiVacia(form.fechaIncorporacion);
        return docenteRepo.save(docente);
    }

    @Transactional
    public Docente actualizarDocente(Integer id, DocenteForm form) {
        Docente docente = obtenerDocente(id);
        Usuario usuario = docente.usuario;

        validarDocumento(form.numDoc, usuario.idUsuario);
        validarCorreo(form.correo, usuario.idUsuario);

        TipoDocumento tipoDoc = tipoDocRepo.findById(form.idTipoDoc)
            .orElseThrow(() -> new RuntimeException("Tipo de documento no válido"));
        Rol rol = rolRepo.findById(1)
            .orElseThrow(() -> new RuntimeException("Rol Docente no encontrado"));
        Escuela escuela = obtenerEscuelaOpcional(form.idEscuela);

        usuario.nombre = form.nombre;
        usuario.apellido = form.apellido;
        usuario.tipoDoc = tipoDoc;
        usuario.numDoc = form.numDoc;
        usuario.celular = form.celular;
        usuario.genero = form.genero;
        usuario.rol = rol;
        usuarioRepo.save(usuario);

        UsuarioAuth auth = obtenerCredencial(usuario);
        auth.correoU = form.correo;
        actualizarPasswordSiCorresponde(auth, form.password);
        authRepo.save(auth);

        docente.escuela = escuela;
        docente.codigoDocente = form.codigoDocente;
        docente.tipoContrato = TipoContrato.fromDescripcion(form.tipoContrato);
        docente.especialidad = form.especialidad;
        if (form.fechaIncorporacion != null && !form.fechaIncorporacion.isBlank()) {
            docente.fechaIncorporacion = parsearFecha(form.fechaIncorporacion);
        }
        return docenteRepo.save(docente);
    }

    @Transactional(readOnly = true)
    public List<Docente> listarDocentes() {
        return docenteRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Docente obtenerDocente(Integer id) {
        return docenteRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Docente no encontrado."));
    }

    @Transactional
    public void eliminarDocente(Integer id) {
        Docente docente = obtenerDocente(id);
        usuarioRepo.delete(docente.usuario);
    }

    @Transactional
    public Administrativo registerAdministrativo(AdministrativoForm form) {
        validarDocumento(form.numDoc, null);
        validarCorreo(form.correo, null);
        validarPasswordObligatoria(form.password);

        TipoDocumento tipoDoc = tipoDocRepo.findById(form.idTipoDoc)
            .orElseThrow(() -> new RuntimeException("Tipo de documento no válido"));
        Rol rol = rolRepo.findById(form.idRol)
            .orElseThrow(() -> new RuntimeException("Rol Administrativo no válido"));

        Usuario usuario = new Usuario();
        usuario.nombre = form.nombre;
        usuario.apellido = form.apellido;
        usuario.tipoDoc = tipoDoc;
        usuario.numDoc = form.numDoc;
        usuario.celular = form.celular;
        usuario.genero = form.genero;
        usuario.rol = rol;
        usuario.estado = 1;
        usuario.fechaRegistro = java.time.LocalDateTime.now();
        usuarioRepo.save(usuario);

        UsuarioAuth auth = new UsuarioAuth();
        auth.usuario = usuario;
        auth.correoU = form.correo;
        auth.password = codificarPassword(form.password);
        authRepo.save(auth);

        Administrativo administrativo = new Administrativo();
        administrativo.usuario = usuario;
        administrativo.turno = Turno.fromDescripcion(form.turno);
        administrativo.extension = form.extension;
        administrativo.fechaIncorporacion = fechaActualSiVacia(form.fechaIncorporacion);
        return administrativoRepo.save(administrativo);
    }

    @Transactional(readOnly = true)
    public List<Administrativo> listarAdministrativos() {
        return administrativoRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Administrativo obtenerAdministrativo(Integer id) {
        return administrativoRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Administrativo no encontrado."));
    }

    @Transactional
    public void eliminarAdministrativo(Integer id) {
        Administrativo administrativo = obtenerAdministrativo(id);
        usuarioRepo.delete(administrativo.usuario);
    }
    
    @Transactional
    public Administrativo actualizarAdministrativo(Integer id, AdministrativoForm form) {
        Administrativo administrativo = obtenerAdministrativo(id);
        Usuario usuario = administrativo.usuario;

        validarDocumento(form.numDoc, usuario.idUsuario);
        validarCorreo(form.correo, usuario.idUsuario);

        TipoDocumento tipoDoc = tipoDocRepo.findById(form.idTipoDoc)
            .orElseThrow(() -> new RuntimeException("Tipo de documento no válido"));
        Rol rol = rolRepo.findById(form.idRol)
            .orElseThrow(() -> new RuntimeException("Rol Administrativo no válido"));

        usuario.nombre = form.nombre;
        usuario.apellido = form.apellido;
        usuario.tipoDoc = tipoDoc;
        usuario.numDoc = form.numDoc;
        usuario.celular = form.celular;
        usuario.genero = form.genero;
        usuario.rol = rol;
        usuarioRepo.save(usuario);

        UsuarioAuth auth = obtenerCredencial(usuario);
        auth.correoU = form.correo;
        actualizarPasswordSiCorresponde(auth, form.password);
        authRepo.save(auth);

        administrativo.turno = Turno.fromDescripcion(form.turno);
        administrativo.extension = form.extension;
        if (form.fechaIncorporacion != null && !form.fechaIncorporacion.isBlank()) {
            administrativo.fechaIncorporacion = parsearFecha(form.fechaIncorporacion);
        }
        return administrativoRepo.save(administrativo);
    }
    
    @Transactional(readOnly = true)
    public LoginResponse loginAcademico(LoginRequest request) {
        Optional<UsuarioAuth> authOpt = authRepo.findByCorreoU(request.correo);
        if (authOpt.isEmpty()) {
            throw new RuntimeException("Correo no registrado.");
        }
        
        UsuarioAuth auth = authOpt.get();
        String storedPassword = new String(Base64.getDecoder().decode(auth.password));
        if (!storedPassword.equals(request.password)) {
            throw new RuntimeException("Contraseña incorrecta.");
        }
        
        Usuario usuario = auth.usuario;
        if (usuario.rol.idRol != 1 && usuario.rol.idRol != 2) {
            throw new RuntimeException("Acceso denegado. Usuario no académico.");
        }
        
        LoginResponse response = new LoginResponse();
        response.idUsuario = usuario.idUsuario;
        response.nombreCompleto = usuario.nombre + " " + usuario.apellido;
        response.idRol = usuario.rol.idRol;
        response.tipoUsuario = (usuario.rol.idRol == 1) ? "Docente" : "Estudiante";
        response.mensaje = "Login académico exitoso";
        return response;
    }

    @Transactional(readOnly = true)
    public LoginResponse loginAdministrativo(LoginRequest request) {
        Optional<UsuarioAuth> authOpt = authRepo.findByCorreoU(request.correo);
        if (authOpt.isEmpty()) {
            throw new RuntimeException("Correo no registrado.");
        }
        
        UsuarioAuth auth = authOpt.get();
        String storedPassword = new String(Base64.getDecoder().decode(auth.password));
        if (!storedPassword.equals(request.password)) {
            throw new RuntimeException("Contraseña incorrecta.");
        }
        
        Usuario usuario = auth.usuario;
        if (usuario.rol.idRol != 3 && usuario.rol.idRol != 4) {
            throw new RuntimeException("Acceso restringido al área administrativa.");
        }

        LoginResponse response = new LoginResponse();
        response.idUsuario = usuario.idUsuario;
        response.nombreCompleto = usuario.nombre + " " + usuario.apellido;
        response.idRol = usuario.rol.idRol;
        response.tipoUsuario = (usuario.rol.idRol == 3) ? "Administrador" : "Supervisor";
        response.mensaje = "Login administrativo exitoso";
        return response;
    }
}
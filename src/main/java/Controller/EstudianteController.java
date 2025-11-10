package Controller;

import dto.EstudianteForm;
import model.Estudiante;
import service.UsuarioRegistrationService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final UsuarioRegistrationService service;

    public EstudianteController(UsuarioRegistrationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Estudiante> listar() {
        return service.listarEstudiantes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Estudiante> obtener(@PathVariable Integer id) {
        try {
            Estudiante estudiante = service.obtenerEstudiante(id);
            return ResponseEntity.ok(estudiante);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Estudiante crear(@RequestBody EstudianteForm form) {
        return service.registerEstudiante(form);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Estudiante> actualizar(@PathVariable Integer id, @RequestBody EstudianteForm form) {
        try {
            Estudiante estudiante = service.actualizarEstudiante(id, form);
            return ResponseEntity.ok(estudiante);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        service.eliminarEstudiante(id);
    }
}
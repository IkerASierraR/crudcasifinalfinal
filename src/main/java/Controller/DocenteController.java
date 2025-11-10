package Controller;

import dto.DocenteForm;
import model.Docente;
import service.UsuarioRegistrationService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    private final UsuarioRegistrationService service;

    public DocenteController(UsuarioRegistrationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Docente> listar() {
        return service.listarDocentes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Docente> obtener(@PathVariable Integer id) {
        try {
            Docente docente = service.obtenerDocente(id);
            return ResponseEntity.ok(docente);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Docente crear(@RequestBody DocenteForm form) {
        return service.registerDocente(form);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Docente> actualizar(@PathVariable Integer id, @RequestBody DocenteForm form) {
        try {
            Docente docente = service.actualizarDocente(id, form);
            return ResponseEntity.ok(docente);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        service.eliminarDocente(id);
    }
}
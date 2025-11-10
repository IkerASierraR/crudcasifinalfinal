package Controller;

import dto.AdministrativoForm;
import model.Administrativo;
import service.UsuarioRegistrationService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/administrativos")
public class AdministrativoController {

    private final UsuarioRegistrationService service;

    public AdministrativoController(UsuarioRegistrationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Administrativo> listar() {
        return service.listarAdministrativos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Administrativo> obtener(@PathVariable Integer id) {
        try {
            Administrativo administrativo = service.obtenerAdministrativo(id);
            return ResponseEntity.ok(administrativo);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Administrativo crear(@RequestBody AdministrativoForm form) {
        return service.registerAdministrativo(form);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<Administrativo> actualizar(@PathVariable Integer id, @RequestBody AdministrativoForm form) {
        try {
            Administrativo administrativo = service.actualizarAdministrativo(id, form);
            return ResponseEntity.ok(administrativo);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        service.eliminarAdministrativo(id);
    }
}
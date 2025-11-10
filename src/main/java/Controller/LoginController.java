package Controller;

import dto.LoginRequest;
import dto.LoginResponse;
import service.UsuarioRegistrationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private final UsuarioRegistrationService service;

    public LoginController(UsuarioRegistrationService service) {
        this.service = service;
    }

    // Login para estudiante y docente
    @PostMapping("/login-academico")
    public LoginResponse loginAcademico(@RequestBody LoginRequest request) {
        return service.loginAcademico(request);
    }

    // Login para administrativo (admin o supervisor) - CORREGIDO ENDPOINT
    @PostMapping("/login-administrativo")
    public LoginResponse loginAdministrativo(@RequestBody LoginRequest request) {
        return service.loginAdministrativo(request);
    }
}
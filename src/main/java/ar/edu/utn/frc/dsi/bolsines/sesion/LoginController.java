package ar.edu.utn.frc.dsi.bolsines.sesion;

import ar.edu.utn.frc.dsi.bolsines.entidades.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entidades.Sesion;
import ar.edu.utn.frc.dsi.bolsines.entidades.Usuario;
import ar.edu.utn.frc.dsi.bolsines.repositorios.EmpleadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorios.SesionRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorios.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Login mínimo (fuera del alcance del CU 36): crea y persiste la Sesion del usuario, que luego el gestor
 * usa para "buscar la CM del usuario logueado".
 */
@RestController
@RequestMapping("/api/sesion")
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ContextoSesion contextoSesion;

    public LoginController(UsuarioRepository usuarioRepository, SesionRepository sesionRepository,
                           EmpleadoRepository empleadoRepository, ContextoSesion contextoSesion) {
        this.usuarioRepository = usuarioRepository;
        this.sesionRepository = sesionRepository;
        this.empleadoRepository = empleadoRepository;
        this.contextoSesion = contextoSesion;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String usuario, @RequestParam String clave) {
        Optional<Usuario> encontrado = usuarioRepository.findByNombreUsuario(usuario);
        if (encontrado.isEmpty() || !encontrado.get().esTuClave(clave)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario o contraseña incorrectos."));
        }
        cerrarSesionActual();
        Sesion sesion = sesionRepository.save(new Sesion(encontrado.get(), LocalDateTime.now()));
        contextoSesion.setIdSesion(sesion.getId());
        return ResponseEntity.ok(datosUsuario(encontrado.get()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        cerrarSesionActual();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/actual")
    public ResponseEntity<Map<String, Object>> sesionActual() {
        if (!contextoSesion.hayUsuarioLogueado()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return sesionRepository.findById(contextoSesion.getIdSesion())
                .filter(Sesion::sosActiva)
                .map(s -> ResponseEntity.ok(datosUsuario(s.getUsuario())))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private void cerrarSesionActual() {
        if (contextoSesion.hayUsuarioLogueado()) {
            sesionRepository.findById(contextoSesion.getIdSesion()).ifPresent(s -> {
                s.cerrar(LocalDateTime.now());
                sesionRepository.save(s);
            });
            contextoSesion.setIdSesion(null);
        }
    }

    private Map<String, Object> datosUsuario(Usuario usuario) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("usuario", usuario.getNombreUsuario());
        for (Empleado empleado : empleadoRepository.findAll()) {
            if (empleado.esTuUsuario(usuario)) {
                datos.put("empleado", empleado.getNombreCompleto());
                datos.put("comisionMedica", empleado.getCM().getNombre());
            }
        }
        return datos;
    }
}

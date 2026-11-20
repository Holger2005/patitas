package com.krakedev.patitas.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.krakedev.jwt.utils.JwtUtil;
import com.krakedev.patitas.entidades.Usuario;
import com.krakedev.patitas.services.TokenBlackListService;
import com.krakedev.patitas.services.UsuarioService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UsuarioService usuarioService;
	private final TokenBlackListService blackListService;

	public AuthController(UsuarioService usuarioService, TokenBlackListService blackListService) {
		super();
		this.usuarioService = usuarioService;
		this.blackListService = blackListService;
	}

	@PostMapping("/registrar")

	public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
		try {

			Usuario usuarioNuevo = usuarioService.registrar(usuario);
			return ResponseEntity.status(HttpStatus.CREATED).body(usuarioNuevo);
		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al registrar usuario: " + e.getMessage());
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
		try {
			String username = credenciales.get("username");
			String password = credenciales.get("password");

			Usuario autenticado = usuarioService.login(username, password);

			String token = JwtUtil.generarToken(autenticado.getUsername(), autenticado.getRol());

			return ResponseEntity.ok(Map.of("token", token));

		} catch (RuntimeException e) {

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrecta");
		}
	}

	@GetMapping("/perfil")
	public ResponseEntity<?> verPerfil(@RequestHeader(value = "Authorization", required = false) String authHeader) {

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("Acceso denegado: Debes proveer un token valido en la cabecera Authorization");
		}
		String token = authHeader.substring(7);

		DecodedJWT datosToken = JwtUtil.validarToken(token);

		if (datosToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acceso denegado");

		}

		String usuario = datosToken.getSubject();

		String rol = datosToken.getClaim("rol").asString();

		return ResponseEntity.ok(Map.of("Mensaje", "Bienvenido al sistema protegido por jwt", "Usuario", usuario, "Rol",
				rol, "Estatus", "Autenticado Exitosamente"));
	}
	@PostMapping("/logout")
	public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			blackListService.invalidarToken(token);
			return ResponseEntity.ok(Map.of("Mensaje", "Sesión cerrada Exitosamente . Token Invalidado"));

		}

		else {

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token no proporcionado");
		}

	}
}

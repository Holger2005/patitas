package com.krakedev.patitas.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krakedev.patitas.entidades.Mascota;
import com.krakedev.patitas.services.MascotaService;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

	private final MascotaService mascotaService;

	public MascotaController(MascotaService mascotaService) {
		this.mascotaService = mascotaService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> crear(@RequestBody Mascota mascota) {
		if (mascota.getNombre() == null || mascota.getNombre().isBlank() || mascota.getEspecie() == null
				|| mascota.getEspecie().isBlank() || mascota.getEdad() <= 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Nombre, especie y edad mayor a 0 son obligatorios.");
		}

		try {
			mascota.setEstado("Disponible");
			Mascota creado = mascotaService.guardar(mascota);
			return ResponseEntity.status(HttpStatus.CREATED).body(creado);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al crear mascota: " + e.getMessage());
		}
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<?> listar() {
		try {
			List<Mascota> mascotas = mascotaService.listar();
			return ResponseEntity.ok(mascotas);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al listar mascotas: " + e.getMessage());
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{id}/adoptar")
	public ResponseEntity<?> buscarPorId(@PathVariable Long id) {

		try {
			Mascota mascota = mascotaService.buscar(id);

			if (mascota == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mascota no encontrado");
			} else if ("Disponible".equalsIgnoreCase(mascota.getEstado())) {

				mascota.setEstado("Adoptada");
				Mascota adoptada = mascotaService.actualizar(id, mascota);

				return ResponseEntity.ok(adoptada);
			} else if ("Adoptada".equalsIgnoreCase(mascota.getEstado())) {

				return ResponseEntity.status(HttpStatus.CONFLICT).body("Mascotas Adoptada");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al buscar el mascota: " + e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar el mascota");

	}
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> eliminar(@PathVariable Long id) {
		try {
			mascotaService.eliminar(id);
			return ResponseEntity.ok("Mascota eliminado correctamente");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al eliminar la mascota: " + e.getMessage());
		}
	}
}
package com.krakedev.patitas.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.krakedev.patitas.entidades.Mascota;
import com.krakedev.patitas.repositories.MascotaRepository;

@Service
public class MascotaService {

	private final MascotaRepository mascotaRepository;

	public MascotaService(MascotaRepository mascotaRepository) {
		this.mascotaRepository = mascotaRepository;
	}

	public Mascota guardar(Mascota mascota) {
		if (mascota.getId() != null && mascotaRepository.existsById(mascota.getId())) {
			throw new IllegalArgumentException("Ya existe una mascota registrada con el ID: " + mascota.getId());
		}
		return mascotaRepository.save(mascota);
	}

	public List<Mascota> listar() {
		return mascotaRepository.findAll();
	}

	public Mascota buscar(Long id) {

		return mascotaRepository.findById(id).orElseThrow(() -> new RuntimeException("Mascota no existe"));

	}

	public Mascota actualizar(Long id, Mascota datos) {

		Mascota mascota = buscar(id);

		if (mascota != null) {

			mascota.setEstado(datos.getEstado());

			return mascotaRepository.save(mascota);
		}

		return null;
	}

	public boolean eliminar(Long id) {

		if (mascotaRepository.existsById(id)) {
			mascotaRepository.deleteById(id);
			return true;
		}

		return false;
	}

}

package com.krakedev.patitas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krakedev.patitas.entidades.Mascota;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

}

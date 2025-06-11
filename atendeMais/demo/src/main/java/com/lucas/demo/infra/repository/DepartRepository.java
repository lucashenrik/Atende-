package com.lucas.demo.infra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucas.demo.infra.model.EstabelecimentoDB;


public interface DepartRepository extends JpaRepository<EstabelecimentoDB, Long> {
	Optional<EstabelecimentoDB> findByEmail(String email);
	Optional<EstabelecimentoDB> deleteByEmail(String email);
}
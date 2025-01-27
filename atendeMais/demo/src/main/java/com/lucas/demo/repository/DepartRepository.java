package com.lucas.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucas.demo.model.Estabelecimento;


public interface DepartRepository extends JpaRepository<Estabelecimento, Long> {
	Optional<Estabelecimento> findByEmail(String email);
}
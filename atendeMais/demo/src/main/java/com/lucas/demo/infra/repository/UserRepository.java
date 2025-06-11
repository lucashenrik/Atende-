package com.lucas.demo.infra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucas.demo.infra.model.UserDB;

public interface UserRepository extends JpaRepository<UserDB, Long> {
	Optional<UserDB> findByEmail(String email);
	// UserDetails findByEmail(String email);
	Optional<UserDB> deleteByEmail(String email);
}
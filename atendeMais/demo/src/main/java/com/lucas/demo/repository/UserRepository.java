package com.lucas.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucas.demo.model.UserDB;

public interface UserRepository extends JpaRepository<UserDB, Long> {
	Optional<UserDB> findByEmail(String email);
	// UserDetails findByEmail(String email);
}
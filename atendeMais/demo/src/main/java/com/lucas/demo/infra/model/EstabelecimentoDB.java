package com.lucas.demo.infra.model;

import java.util.List;

import com.lucas.demo.domain.models.Estabelecimento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "estabelecimento")
public class EstabelecimentoDB {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false)
	private String telefone;

	@Column(nullable = false)
	private String email;

	@OneToMany(mappedBy = "estabelecimento")
	private List<UserDB> usuarios;

	public EstabelecimentoDB(String nome, String email) {
		this.nome = nome;
		this.email = email;
	}

	public EstabelecimentoDB(String nome, String telefone, String email) {
		this.nome = nome;
		this.telefone = telefone;
		this.email = email;
	}

	public EstabelecimentoDB() {
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<UserDB> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(List<UserDB> usuarios) {
		this.usuarios = usuarios;
	}


}
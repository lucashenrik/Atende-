package com.lucas.demo.domain.models;

public class User {

    private Long id;
    private String name;
    private String email;
    private String password;
    private EnumRoles role;
    private Estabelecimento estabelecimento;

    public User(String name, String email, String password, EnumRoles role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User() {
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public EnumRoles getRole() {
        return role;
    }
    public void setRole(EnumRoles role) {
        this.role = role;
    }

    public Estabelecimento getEstabelecimento() {
        return estabelecimento;
    }
    public void setEstabelecimento(Estabelecimento estabelecimento) {
        this.estabelecimento = estabelecimento;
    }
}
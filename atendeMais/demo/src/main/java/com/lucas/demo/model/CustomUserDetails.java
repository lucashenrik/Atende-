package com.lucas.demo.model;

import java.util.ArrayList;

public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
	private static final long serialVersionUID = -4097370817333687573L;

	private final UserDB user;

    public CustomUserDetails(UserDB user) {
        super(user.getEmail(), user.getPassword(), new ArrayList<>());
        this.user = user;
    }

    public UserDB getUserDB() {
        return this.user;
    }
}
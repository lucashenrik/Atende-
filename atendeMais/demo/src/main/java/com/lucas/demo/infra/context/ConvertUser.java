package com.lucas.demo.infra.context;

import com.lucas.demo.domain.models.User;
import com.lucas.demo.infra.model.UserDB;

public class ConvertUser {

    public static User toDomainModel(UserDB db){
        return new User(db.getName(), db.getEmail(), db.getPassword(), db.getRole());
    }
}

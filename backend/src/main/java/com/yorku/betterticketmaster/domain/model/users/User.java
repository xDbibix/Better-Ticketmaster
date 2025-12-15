package com.yorku.betterticketmaster.domain.model.users;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/*
Base user class
*/
@Data
@Document(collection="users")
@TypeAlias("user")
public class User {
    @Id
    private String id;
    private String email;
    private String password; //encrypt it idk how
    private String name;
    private Role role;

    private List<String> ownedTicketIds;
    
}

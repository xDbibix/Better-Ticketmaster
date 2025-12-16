package com.yorku.betterticketmaster.domain.model.users;

import java.util.List;
import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

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
    private static Pbkdf2PasswordEncoder encoder = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    private List<String> ownedTicketIds = new ArrayList<>();

    public User(String id, String email, String password, String name, Role role){
        this.id = id;
        this.email = email;
        this.password = encodePassword(password);
        this.name = name;
        this.role = role;
    }

    public String encodePassword(String rawPassword){
        return encoder.encode(rawPassword);
    }

    public boolean verifyPassword(String inputPassword){
        return encoder.matches(inputPassword, this.password);
    }

    public void addOwnedTicket(String ticketId){
        if(!ownedTicketIds.contains(ticketId)){
            ownedTicketIds.add(ticketId);
        }
    }

    public void removeOwnedTicket(String ticketId){
        if(ownedTicketIds.contains(ticketId)){
            ownedTicketIds.remove(ticketId);
        }
    }

    public boolean ownsTicket(String ticketId){
        return ownedTicketIds.contains(ticketId);
    }

    public boolean isAdmin(){
        return Role.ADMIN.equals(this.role);
    }

    public boolean isOrganizer(){
        return Role.ORGANIZER.equals(this.role);
    }

    public boolean isConsumer(){
        return Role.CONSUMER.equals(this.role);
    }

}

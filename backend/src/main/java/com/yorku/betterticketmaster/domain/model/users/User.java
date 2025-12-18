package com.yorku.betterticketmaster.domain.model.users;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.bcrypt.BCrypt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
Base user class
*/
@Data
@Document(collection="users")
@TypeAlias("user")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;
    private String email;
    /** Stored as a BCrypt hash. */
    private String password;
    private String name;
    private Role role;

    private List<String> ownedTicketIds = new ArrayList<>();

    public User(String id, String email, String password, String name, Role role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.ownedTicketIds = new ArrayList<>();
    }

    public boolean verifyPassword(String rawPassword) {
        if (rawPassword == null || password == null) return false;
        try {
            return BCrypt.checkpw(rawPassword, password);
        } catch (IllegalArgumentException ex) {
            // Not a BCrypt hash or invalid format
            return false;
        }
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isOrganizer() {
        return role == Role.ORGANIZER;
    }

    public boolean isConsumer() {
        return role == Role.CONSUMER;
    }

    public void addOwnedTicket(String ticketId) {
        if (ticketId == null) return;
        if (ownedTicketIds == null) ownedTicketIds = new ArrayList<>();
        if (!ownedTicketIds.contains(ticketId)) ownedTicketIds.add(ticketId);
    }

    public void removeOwnedTicket(String ticketId) {
        if (ticketId == null || ownedTicketIds == null) return;
        ownedTicketIds.remove(ticketId);
    }

    public boolean ownsTicket(String ticketId) {
        return ticketId != null && ownedTicketIds != null && ownedTicketIds.contains(ticketId);
    }
    
}

package com.yorku.betterticketmaster.domain.model.users;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
@Document(collection="users")
@TypeAlias("consumer")
public class Consumer extends User{
    public Consumer(String id, String email, String password, String name){
        super(id, email, password, name, Role.CONSUMER);
    }

    public void addTicket(String ticketId){
        addOwnedTicket(ticketId);
    }

    public void removeTicket(String ticketId){
        removeOwnedTicket(ticketId);
    }

    @Override
    public boolean ownsTicket(String ticketId){
        return super.ownsTicket(ticketId);
    }

    public void transferTicket(String ticketId, Consumer newOwner){
        if(!(ownsTicket(ticketId))){
            throw new IllegalStateException("You can't transfer a ticket you don't own");
        }
        removeTicket(ticketId);
        newOwner.addTicket(ticketId);
    }
}

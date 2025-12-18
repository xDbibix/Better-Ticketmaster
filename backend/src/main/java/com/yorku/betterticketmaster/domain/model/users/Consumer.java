package com.yorku.betterticketmaster.domain.model.users;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Consumer user type with ticket ownership helpers.
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Document(collection="users")
@TypeAlias("consumer")
public class Consumer extends User{
    /**
     * Construct a consumer user.
     */
    public Consumer(String id, String email, String password, String name){
        super(id, email, password, name, Role.CONSUMER);
    }

    /**
     * Add a ticket to owned list.
     * @param ticketId ticket identifier
     */
    public void addTicket(String ticketId){
        addOwnedTicket(ticketId);
    }

    /**
     * Remove a ticket from owned list.
     * @param ticketId ticket identifier
     */
    public void removeTicket(String ticketId){
        removeOwnedTicket(ticketId);
    }

    @Override
    /**
     * Check if the consumer owns a ticket.
     * @param ticketId ticket identifier
     * @return true if owned
     */
    public boolean ownsTicket(String ticketId){
        return super.ownsTicket(ticketId);
    }

    /**
     * Transfer a ticket to another consumer.
     * @param ticketId ticket identifier
     * @param newOwner recipient consumer
     */
    public void transferTicket(String ticketId, Consumer newOwner){
        if(!(ownsTicket(ticketId))){
            throw new IllegalStateException("You can't transfer a ticket you don't own");
        }
        removeTicket(ticketId);
        newOwner.addTicket(ticketId);
    }
}

package com.yorku.betterticketmaster.domain.model.users;

import java.util.ArrayList;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Organizer user type with managed events.
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Document(collection="users")
public class Organizer extends User{
    private String company;
    private ArrayList<String> managedEventIds = new ArrayList<>(); 

    /**
     * Construct an organizer user.
     */
    public Organizer(String id, String email, String password, String name, String company){
        super(id, email, password, name, Role.ORGANIZER);
        this.company = company;
    }

    /**
     * Add an event to managed list.
     * @param eventId event identifier
     */
    public void addEvent(String eventId){
        if(!managedEventIds.contains(eventId)){
            managedEventIds.add(eventId);
        }
    }

    /**
     * Remove an event from managed list.
     * @param eventId event identifier
     */
    public void removeEvent(String eventId){
        if(managedEventIds.contains(eventId)){
            managedEventIds.remove(eventId);
        }
    }

    public boolean manageEventIds(String eventId){
        return managedEventIds.contains(eventId);
    }
}

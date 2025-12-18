package com.yorku.betterticketmaster.domain.model.users;

import java.util.ArrayList;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
@Document(collection="users")
public class Organizer extends User{
    private String company;
    private ArrayList<String> managedEventIds = new ArrayList<>(); 

    public Organizer(String id, String email, String password, String name, String company){
        super(id, email, password, name, Role.ORGANIZER);
        this.company = company;
    }

    public void addEvent(String eventId){
        if(!managedEventIds.contains(eventId)){
            managedEventIds.add(eventId);
        }
    }

    public void removeEvent(String eventId){
        if(managedEventIds.contains(eventId)){
            managedEventIds.remove(eventId);
        }
    }

    public boolean manageEventIds(String eventId){
        return managedEventIds.contains(eventId);
    }
}

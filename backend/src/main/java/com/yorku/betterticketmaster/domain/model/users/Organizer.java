package com.yorku.betterticketmaster.domain.model.users;

import java.util.ArrayList;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection="users")
public class Organizer {
    private String company;
    private ArrayList<String> managedEventIds = new ArrayList<>(); 
}

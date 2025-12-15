package com.yorku.betterticketmaster.domain.model.users;

public enum  Role {
    ADMIN, // Full access, can use venue builder
    ORGANIZER, //Can create and manage events
    CONSUMER // Buy, sell, Transfer tickets
}

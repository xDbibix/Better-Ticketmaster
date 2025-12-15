package com.yorku.betterticketmaster.domain.model.users;

import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
@Document(collection="users")
public class Consumer extends User{
    private ArrayList<String> ownedTicketIds = new ArrayList<>();

    public Consumer(String id, String email, String password, String name, Role role){
        super(id, email, password, name, role);
    }
}

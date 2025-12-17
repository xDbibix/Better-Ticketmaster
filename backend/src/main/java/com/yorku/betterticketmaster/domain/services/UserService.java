package com.yorku.betterticketmaster.domain.services;
import java.util.Optional;
import java.util.UUID;

import com.yorku.betterticketmaster.domain.model.users.Role;
import com.yorku.betterticketmaster.domain.model.users.User;
import com.yorku.betterticketmaster.domain.repository.login.UserRepository;
import com.yorku.betterticketmaster.domain.model.venue.Layout;
import com.yorku.betterticketmaster.domain.model.event.Event;
import com.yorku.betterticketmaster.domain.model.booking.Ticket;
import com.yorku.betterticketmaster.domain.repository.venueBuilder.LayoutRepository;
import com.yorku.betterticketmaster.domain.repository.booking.TicketRepository;
import com.yorku.betterticketmaster.domain.repository.inventory.EventRepository;


public class UserService {
    private UserRepository userRepo;
    private LayoutRepository layoutRepo;
    private EventRepository eventRepo;
    private TicketRepository ticketRepo;

    public UserService(UserRepository userRepo, LayoutRepository layoutRepo, EventRepository eventRepo, TicketRepository ticketRepo) {
        this.userRepo = userRepo;
        this.layoutRepo = layoutRepo;
        this.eventRepo = eventRepo;
        this.ticketRepo = ticketRepo;
    }

    public User signUp(String email, String password, String name, Role role) {
        if (userRepo.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email already exists");
        }

        User user = new User(UUID.randomUUID().toString(), email, password, name, role);
        return userRepo.save(user);
    }

    public User login(String email, String password) {
        Optional<User> optionalUser = userRepo.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Wrong username");
        }

        User userEntity = optionalUser.get();
        if (!userEntity.verifyPassword(password)) {
            throw new IllegalArgumentException("Wrong password.");
        }

        return userEntity;
    }

    public void requireAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Admin only");
        }
    }

    public void requireOrganizer(User user) {
        if (user.getRole() != Role.ORGANIZER) {
            throw new IllegalStateException("Organizer only");
        }
    }

    public void requireConsumer(User user) {
        if (user.getRole() != Role.CONSUMER) {
            throw new IllegalStateException("Consumer only");
        }
    }

    public Layout createLayout(User user, Layout layout) {
        requireAdmin(user);
        return layoutRepo.save(layout);
    }

    public Event createEvent(User user, Event event) {
        requireOrganizer(user);
        return eventRepo.save(event);
    }

    public void manageEvent(User user, Event event) {
        requireOrganizer(user);
    }

    public Ticket ticketTransfer(User user, Ticket ticket, String buyer) {
        requireConsumer(user);
        if (!ticket.getBuyerId().equals(user.getId())) {
            throw new IllegalStateException("You don't own the ticket.");
        }

        ticket.setBuyerId(buyer);
        return ticketRepo.save(ticket);
    }

    public Ticket resellTicket(User user, Ticket ticket, double price) {
        requireConsumer(user);
        if (!ticket.getBuyerId().equals(user.getId())) {
            throw new IllegalStateException("You don't own the ticket");
        }

        ticket.setResale(true);
        ticket.setResalePrice(price);
        return ticketRepo.save(ticket);
    }

    public User getUserById(String id) {
        return userRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("User does not exist."));
    }

}
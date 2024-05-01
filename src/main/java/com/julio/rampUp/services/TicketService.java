package com.julio.rampUp.services;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.julio.rampUp.entities.Order;
import com.julio.rampUp.entities.Ticket;
import com.julio.rampUp.entities.dto.TicketDTO;
import com.julio.rampUp.repositories.TicketRepository;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@Service
public class TicketService {

    @Autowired
    private TicketRepository repository;

    @Autowired
    private OrderService orderService;

    public List<Ticket> findAll(int page) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, 4, sort);
        return repository.findAll(pageable).toList();
    }

    public Ticket findById(Integer id) {
        Optional<Ticket> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public Ticket insert(TicketDTO ticketDTO, int id) {
        try {
            Order orderDB = orderService.findById(ticketDTO.getOrderId());
            if (orderDB.getId() != id)
                throw new ResourceNotFoundException(id);

            Hibernate.initialize(orderDB.getOrderItems());
            Ticket ticket = builder(ticketDTO, orderDB);

            Duration duration = Duration.between(ticket.getOrder().getInstant(), ticket.getInstant());
            if (duration.toHours() <= 2) {
                ticket.setSolved(true);
                orderService.deleteById(ticket.getOrder().getId());
            }
            return repository.save(ticket);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(id);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }

    }

    public void deleteById(Integer id) {
        try {
            Ticket ticketToDelete = findById(id);
            orderService.deleteById(ticketToDelete.getOrder().getId());
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    public List<Ticket> findAllTicketsByCustomerId(int page, Integer id) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page, 10, sort);
        return repository.findAllTicketsByCustomerId(pageable, id).toList();
    }

    private Ticket builder(TicketDTO ticketDTO, Order order) {
        Ticket ticket = new Ticket(ticketDTO.getId(), ticketDTO.getMessage(), order);
        return ticket;
    }

}

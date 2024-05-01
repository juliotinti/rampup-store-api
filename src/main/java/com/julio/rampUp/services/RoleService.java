package com.julio.rampUp.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.julio.rampUp.entities.Role;
import com.julio.rampUp.repositories.RoleRepository;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@Service
public class RoleService {

    @Autowired
    private RoleRepository repository;

    public List<Role> findAll(int page) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page, 10, sort);
        return repository.findAll(pageable).toList();
    }

    public Role findById(Integer id) {
        Optional<Role> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public Role insert(Role role) {
        try {
            return repository.save(role);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }

    }

    public void deleteById(Integer id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    public Role update(Integer id, Role newRole) {
        try {
            Role updatedRole = repository.getReferenceById(id);
            updateData(updatedRole, newRole);
            return repository.save(updatedRole);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    private void updateData(Role updatedRole, Role newRole) {
        updatedRole.setAuthority(newRole.getAuthority());
    }
}

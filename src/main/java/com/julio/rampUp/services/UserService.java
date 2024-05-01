package com.julio.rampUp.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.julio.rampUp.entities.Role;
import com.julio.rampUp.entities.User;
import com.julio.rampUp.entities.dto.UserDTO;
import com.julio.rampUp.repositories.UserRepository;
import com.julio.rampUp.sendEmail.EmailHandler;
import com.julio.rampUp.services.exceptions.AddressException;
import com.julio.rampUp.services.exceptions.EmailDuplicateException;
import com.julio.rampUp.services.exceptions.EmailNullException;
import com.julio.rampUp.services.exceptions.InvalidEmailException;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private EmailHandler emailImpl;

    @Autowired
    private PasswordEncoder passwordEnconder;

    public List<UserDTO> findAll(int page) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page, 6, sort);
        return userToDTO(repository.findAll(pageable).toList());
    }

    private List<UserDTO> userToDTO(List<User> users) {
        List<UserDTO> usersDTO = new ArrayList<>();
        for (User user : users) {
            usersDTO.add(new UserDTO(user));
        }
        return usersDTO;
    }

    public User findById(Integer id) {
        Optional<User> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException(id)); // don't find id
    }

    public User insert(User user, Role role) {
        try {
            checkEmail(user.getEmail());
            user.addRole(role);
            emailImpl.sendEmail(user.getEmail(), user.getPassword());
            passwordEnconder = new BCryptPasswordEncoder();
            user.setPassword(passwordEnconder.encode(user.getPassword()));
            return repository.save(user);
        } catch (EmailNullException | EmailDuplicateException | InvalidEmailException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    public void deleteById(Integer id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) { // there is no entity with this id
            throw new ResourceNotFoundException(id);
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    public User update(Integer id, User newUser) {
        try {
            User updatedUser = repository.getReferenceById(id);
            updateData(updatedUser, newUser);
            return repository.save(updatedUser);
        } catch (EntityNotFoundException e) { // dont find entity
            throw new ResourceNotFoundException(id);
        } catch (EmailNullException | EmailDuplicateException | InvalidEmailException e) {
            throw e;
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        try {
            Optional<User> user = repository.findByEmail(email);
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for (Role role : user.get().getRoles()) {
                SimpleGrantedAuthority auth = new SimpleGrantedAuthority(role.getAuthority().name());
                authorities.add(auth);
            }
            return org.springframework.security.core.userdetails.User.builder().username(user.get().getEmail())
                    .password(user.get().getPassword()).authorities(authorities).build();
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    private void updateData(User updatedUser, User newUser) {
        checkEmail(newUser.getEmail());
        updatedUser.setEmail(newUser.getEmail());
        if (newUser.getPassword() != null) {
            passwordEnconder = new BCryptPasswordEncoder();
            updatedUser.setPassword(passwordEnconder.encode(newUser.getPassword()));
        }

    }

    private void checkEmail(String email) {
        // email null
        if (email == null)
            throw new EmailNullException();

        // duplicate email
        Boolean existsEmail = repository.checkEmail(email);
        if (existsEmail)
            throw new EmailDuplicateException();

        // invalid email
        String emailRegex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern emailPattern = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = emailPattern.matcher(email);
        if (!matcher.find())
            throw new InvalidEmailException();
    }

    public User findByEmail(String email) {
        return repository.findByEmail(email).orElseThrow(() -> new AddressException("This email don't exist"));
    }

}

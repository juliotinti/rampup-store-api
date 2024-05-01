package com.julio.rampUp.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.julio.rampUp.entities.Customer;
import com.julio.rampUp.entities.Role;
import com.julio.rampUp.entities.User;
import com.julio.rampUp.entities.dto.UserDTO;
import com.julio.rampUp.entities.enums.Authorities;
import com.julio.rampUp.entities.enums.CustomerType;
import com.julio.rampUp.repositories.UserRepository;
import com.julio.rampUp.sendEmail.EmailHandler;
import com.julio.rampUp.services.exceptions.EmailDuplicateException;
import com.julio.rampUp.services.exceptions.EmailNullException;
import com.julio.rampUp.services.exceptions.InvalidEmailException;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    private static final String THIS_EMAIL_IS_INVALID = "This email is invalid";

    private static final String THIS_EMAIL_ALREADY_HAS_A_USER = "This email already has a User";

    private static final String THERE_IS_NO_EMAIL = "There is no email";

    private static final String RESOURCE_NOT_FOUND_ID = "Resource not found. Id - ";

    private static final int testId = 1; // default test is id 1

    @Mock
    private UserRepository userRepo;

    @Mock
    private CustomerService customerService;

    @Mock
    private EmailHandler emailImpl;

    @InjectMocks
    private UserService serviceUnderTest;

    private User userTest1;
    private User userTest2;
    private Optional<User> optionalUser;
    private Role roleOperator;
    private Customer customerTest1;

    @BeforeEach
    public void setup() {
        userTest1 = new User(1, "maria@gmail.com", "1234567");
        userTest2 = new User(2, "joao@gmail.com", "1a34567");
        optionalUser = Optional.of(userTest1);
        roleOperator = new Role(1, Authorities.Operator);
        userTest1.addRole(roleOperator);

        customerTest1 = new Customer(1, "Maria", 123456789, CustomerType.NaturalPerson, "High",
                optionalUser.get().getPassword());
        optionalUser.get().setCustomer(customerTest1);

    }

    @Test
    @DisplayName("Get all Users")
    @Order(0)
    public void findAll_shouldReturnAllUsers() {
        // set up
        Page<User> page = new PageImpl<>(List.of(userTest1, userTest2));
        when(userRepo.findAll(any(Pageable.class))).thenReturn(page);

        // execute
        List<UserDTO> allUsers = serviceUnderTest.findAll(0);

        // assert
        assertThat(allUsers).isNotNull();
        assertThat(allUsers.size()).isEqualTo(2);
        assertThat(allUsers.get(0).getId()).isEqualTo(userTest1.getId());
        assertThat(allUsers.get(0).getEmail()).isEqualTo(userTest1.getEmail());

    }

    @Test
	@DisplayName("Get User by Id")
	@Order(1)
	public void findById_shouldReturnUserById() {
		// set up
		when(userRepo.findById(anyInt())).thenReturn(optionalUser);

		// execute
		User databaseUser = serviceUnderTest.findById(testId);

		// assert
		assertThat(databaseUser).isNotNull();
		assertThat(databaseUser.getId()).isEqualTo(optionalUser.get().getId());
		assertThat(databaseUser.getEmail()).isEqualTo(optionalUser.get().getEmail());
		assertThat(databaseUser.getPassword()).isEqualTo(optionalUser.get().getPassword());
		assertThat(databaseUser.getRoles().stream().anyMatch(el -> el.getAuthority() == roleOperator.getAuthority()));

		// verify
		verify(userRepo).findById(testId);
	}

    @Test
	@DisplayName("(Exception) Get user by Id - Id not found")
	@Order(2)
	public void findById_whenUserIdNotFound_shouldThrowResourceNotFoundException() {
		// set up
		when(userRepo.findById(anyInt())).thenThrow(new ResourceNotFoundException(testId));

		// execute and assert
		assertThatThrownBy(() -> serviceUnderTest.findById(testId)).isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
	}

    @Test
    @DisplayName("Insert a new user")
    @Order(3)
    public void insert_shouldCreateNewUser() {
        // set up
        when(userRepo.save(any())).thenReturn(userTest1);

        // execute
        User newUser = serviceUnderTest.insert(userTest1, roleOperator);

        // assert
        assertThat(newUser).isNotNull();
        assertThat(newUser.getClass()).isEqualTo(User.class);
        assertThat(newUser.getId()).isEqualTo(userTest1.getId());
        assertThat(newUser.getEmail()).isEqualTo(userTest1.getEmail());
        assertThat(newUser.getPassword()).isEqualTo(userTest1.getPassword());

        // verify
        verify(userRepo).save(userTest1);
        verify(emailImpl).sendEmail(userTest1.getEmail(), "1234567");
    }

    @Test
    @DisplayName("(Exception) Insert a new user - Null Email")
    @Order(4)
    public void insert_whenEmailIsNull_shouldThrowEmailNullException() {
        // set up
        userTest1.setEmail(null); // set email to null

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(userTest1, roleOperator))
                .isInstanceOf(EmailNullException.class).hasMessageContaining(THERE_IS_NO_EMAIL);
    }

    @Test
    @DisplayName("(Exception) Insert a new user - Duplicate Email")
    @Order(5)
    public void insert_whenEmailIsUsed_shouldThrowEmailDuplicateException() {
    	// set up
    	when(userRepo.checkEmail(anyString())).thenReturn(true);

    	// execute and assert
    	assertThatThrownBy(() -> serviceUnderTest.insert(userTest1, roleOperator)).isInstanceOf(EmailDuplicateException.class)
    			.hasMessageContaining(THIS_EMAIL_ALREADY_HAS_A_USER);

    }

    @Test
    @DisplayName("(Exception) Insert a new user - Invalid Email")
    @Order(6)
    public void insert_whenEmailIsInvalide_shouldThrowInvalidEmailException() {
        // set up
        userTest1.setEmail("alice.example.com"); // set a invalid email

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(userTest1, roleOperator))
                .isInstanceOf(InvalidEmailException.class).hasMessageContaining(THIS_EMAIL_IS_INVALID);
    }

    @Test
    @DisplayName("(Exception) Insert a new user - Unexpected Exception")
    @Order(7)
    public void insert_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        when(userRepo.save(any())).thenThrow(RuntimeException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(userTest1, roleOperator)).isInstanceOf(UnexpectedException.class);
    }

    @Test
    @DisplayName("Delete user by Id")
    @Order(8)
    public void deleteById_shouldSoftDeleteUser() {
        // set up
        doNothing().when(userRepo).deleteById(anyInt());

        // execute
        serviceUnderTest.deleteById(testId);

        // verify
        verify(userRepo, times(1)).deleteById(testId);
    }

    @Test
    @DisplayName("(Exception) Delete user by Id - Id not found")
    @Order(9)
    public void deleteById_whenUserNotFound_shouldThrowResourceNotFoundException() {
        // set up
        doThrow(EmptyResultDataAccessException.class).when(userRepo).deleteById(anyInt());
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(testId)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);

    }

    @Test
    @DisplayName("(Exception) Delete user by Id - Unexpected Exception")
    @Order(10)
    public void deleteById_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        doThrow(RuntimeException.class).when(userRepo).deleteById(anyInt());
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(testId)).isInstanceOf(UnexpectedException.class);

    }

    @Test
    @DisplayName("Update User")
    @Order(11)
    public void update_shouldUpdateUser() {
        // set up
        User newUser = new User(null, "alice@example.com", "1234567");

        when(userRepo.getReferenceById(anyInt())).thenReturn(userTest1);
        when(userRepo.save(any())).thenReturn(newUser);

        // execute
        User updatedUser = serviceUnderTest.update(testId, newUser);

        // assert
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(newUser.getEmail());
        assertThat(updatedUser.getPassword()).isEqualTo(newUser.getPassword());
    }

    @Test
    @DisplayName("(Exception) Update User - Id not found")
    @Order(12)
    public void update_whenUserNotFound_shouldThrowResourceNotFoundException() {
        // set up
        User newUser = new User(null, "alice@example.com", "1234567");
        when(userRepo.getReferenceById(anyInt())).thenThrow(EntityNotFoundException.class);
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(testId, newUser)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);

    }

    @Test
    @DisplayName("(Exception) Update User - Null Email")
    @Order(13)
    public void update_whenEmailIsNull_shouldThrowEmailNullException() {
        // set up
        User newUser = new User(null, null, "1234567");
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(testId, newUser)).isInstanceOf(EmailNullException.class)
                .hasMessageContaining(THERE_IS_NO_EMAIL);
    }

    @Test
    @DisplayName("(Exception) Update User - Duplicate Email")
    @Order(14)
    public void update_whenEmailIsIUsed_shouldThrowEmailDuplicateException() {
        // set up
        User newUser = new User(null, "joao@gmail.com", "1234567");
        User oldUser = new User(testId, "maria@gmail.com", "1234567");
        when(userRepo.getReferenceById(anyInt())).thenReturn(oldUser);
        when(userRepo.checkEmail(anyString())).thenReturn(true);
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(testId, newUser)).isInstanceOf(EmailDuplicateException.class)
                .hasMessageContaining(THIS_EMAIL_ALREADY_HAS_A_USER);
    }

    @Test
    @DisplayName("(Exception) Update User - Invalid Email")
    @Order(15)
    public void update_whenEmailIsInvalid_shouldThrowInvalidEmailException() {
        // set up
        User newUser = new User(null, "alexandra.com.br", "1234567");
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(testId, newUser)).isInstanceOf(InvalidEmailException.class)
                .hasMessageContaining(THIS_EMAIL_IS_INVALID);
    }

    @Test
    @DisplayName("(Exception) Update User - Unexpected Exception")
    @Order(16)
    public void update_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        when(userRepo.getReferenceById(anyInt())).thenThrow(RuntimeException.class);
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(testId, userTest2)).isInstanceOf(UnexpectedException.class);
    }

//    @Test
//    @DisplayName("User details")
//    @Order(17)
//    public void loadUserByUsername_shouldReturnUserDetails() {
//        // set up
//        when(userRepo.findByEmail(anyString())).thenReturn(userTest1);
//
//        //execute
//        UserDetails userDetails = serviceUnderTest.loadUserByUsername(userTest1.getEmail());
//
//        //assert
//        assertThat(userDetails).isNotNull();
//        assertThat(userDetails.getUsername()).isEqualTo(userTest1.getEmail());
//        assertThat(userDetails.getPassword()).isEqualTo(userTest1.getPassword());
//    }

    @Test
    @DisplayName("(Exception) User details - Unexpected Exception")
    @Order(18)
    public void loadUserByUsername_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        when(userRepo.findByEmail(anyString())).thenThrow(RuntimeException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.loadUserByUsername(userTest1.getEmail())).isInstanceOf(UnexpectedException.class);
    }

}
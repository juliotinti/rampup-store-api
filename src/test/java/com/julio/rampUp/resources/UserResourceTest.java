package com.julio.rampUp.resources;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.julio.rampUp.entities.Customer;
import com.julio.rampUp.entities.Role;
import com.julio.rampUp.entities.User;
import com.julio.rampUp.entities.dto.UserDTO;
import com.julio.rampUp.entities.enums.Authorities;
import com.julio.rampUp.entities.enums.CustomerType;
import com.julio.rampUp.mock.TokenMock;
import com.julio.rampUp.services.UserService;
import com.julio.rampUp.services.exceptions.EmailDuplicateException;
import com.julio.rampUp.services.exceptions.EmailNullException;
import com.julio.rampUp.services.exceptions.InvalidEmailException;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserResourceTest {

    private static final int testId = 1;
    private static final int testPage = 0;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;
    private String accessToken;
    private TokenMock tokenMock;

    @MockBean
    private UserService userService;

    private User userTest1;
    private User userTest2;
    private UserDTO userDTO1;
    private UserDTO userDTO2;
    private Role roleOperator;
    private Role roleAdmin;
    private Customer customerTest1;

    @BeforeEach
    public void setup() throws Exception {
        userTest1 = new User(1, "maria@gmail.com", "1234567");
        userTest2 = new User(2, "joao@gmail.com", "1a34567");
        roleOperator = new Role(1, Authorities.Operator);
        roleAdmin = new Role(2, Authorities.Admin);
        userTest1.addRole(roleOperator);
        userTest2.addRole(roleAdmin);

        customerTest1 = new Customer(1, "Maria", 123456789, CustomerType.NaturalPerson, "High",
                userTest1.getPassword());
        userTest1.setCustomer(customerTest1);

        userDTO1 = new UserDTO(userTest1);
        userDTO2 = new UserDTO(userTest2);

        User user = new User(5, "haaland@gmail.com", "$2a$10$Yb4cBiFBmWXyH/5Ga4t/o.oMVf/m17CFgXiVAIktyaUrxKorKkUHa");
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        SimpleGrantedAuthority auth = new SimpleGrantedAuthority(roleAdmin.getAuthority().name());
        authorities.add(auth);
        when(userService.loadUserByUsername(user.getEmail()))
                .thenReturn(org.springframework.security.core.userdetails.User.builder().username(user.getEmail())
                        .password(user.getPassword()).authorities(authorities).build());
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilter(springSecurityFilterChain).build();
        tokenMock = new TokenMock();
        accessToken = tokenMock.obtainAccessToken("haaland@gmail.com", "9city9", mockMvc);

    }

    @Test
    @DisplayName("Get all Users")
    @Order(0)
    public void findAll_shouldReturnAllUsers() throws Exception {
        // set up
        List<UserDTO> list = List.of(userDTO1, userDTO2);
        when(userService.findAll(anyInt())).thenReturn(list);
        // execute and assert
        mockMvc.perform(get("/users/page/" + testPage).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk()) //
                .andExpect(jsonPath("$[0].email", is(userDTO1.getEmail())))
                .andExpect(jsonPath("$[1].email", is(userDTO2.getEmail())));
    }

    @Test
    @DisplayName("Get User by Id")
    @Order(1)
    public void findById_shouldReturnUserById() throws Exception {
        // set up
        when(userService.findById(anyInt())).thenReturn(userTest1);
        // execute and assert
        mockMvc.perform(get("/users/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userTest1.getId())))
                .andExpect(jsonPath("$.email", is(userTest1.getEmail())))
                .andExpect(jsonPath("$.customer.id", is(userTest1.getCustomer().getId())));
    }

    @Test
    @DisplayName("(Exception) Get user by Id - Id not found")
    @Order(2)
    public void findById_whenUserIdNotFound_shouldThrowResourceNotFoundException() throws Exception {
        // set up
        int notExistedId = 5;
        when(userService.findById(anyInt())).thenThrow(new ResourceNotFoundException(notExistedId));
        // execute and assert
        mockMvc.perform(get("/users/" + notExistedId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Insert a new Admin user")
    @Order(3)
    public void insertAdmin_shouldCreateNewUser() throws Exception {
        // set up
        when(userService.insert(any(), any())).thenReturn(userTest2);

        // execute and assert
        mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userTest2.getId())))
                .andExpect(jsonPath("$.email", is(userTest2.getEmail())))
                .andExpect(jsonPath("$.roles[0].id", is(roleAdmin.getId())));
    }

    @Test
    @DisplayName("(Exception) Insert a new Admin user - Null Email")
    @Order(4)
    public void insertAdmin_whenEmailIsNull_shouldThrowEmailNullException() throws Exception {
        // set up
        doThrow(EmailNullException.class).when(userService).insert(any(), any());

        // execute and assert
        mockMvc.perform(post("/orders/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userTest2))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Insert a new Admin user - Duplicate Email")
    @Order(5)
    public void insertAdmin_whenEmailIsUsed_shouldThrowEmailDuplicateException() throws Exception {
        // set up
        doThrow(EmailDuplicateException.class).when(userService).insert(any(), any());

        // execute and assert
        mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userTest2))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Insert a new Admin user - Invalid Email")
    @Order(6)
    public void insertAdmin_whenEmailIsInvalide_shouldThrowInvalidEmailException() throws Exception {
        // set up
        doThrow(InvalidEmailException.class).when(userService).insert(any(), any());

        // execute and assert
        mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userTest2))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Insert a new Admin user - Unexpected Exception")
    @Order(7)
    public void insertAdmin_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(userService).insert(any(), any());

        // execute and assert
        mockMvc.perform(post("/users/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userTest2))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Insert a new Operator user")
    @Order(8)
    public void insertOperator_shouldCreateNewUser() throws Exception {
        // set up
        when(userService.insert(any(), any())).thenReturn(userTest1);

        // execute and assert
        mockMvc.perform(post("/users/signup").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTest1)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userTest1.getId())))
                .andExpect(jsonPath("$.email", is(userTest1.getEmail())))
                .andExpect(jsonPath("$.roles[0].id", is(roleOperator.getId())));
    }

    @Test
    @DisplayName("(Exception) Insert a new Operator user - Null Email")
    @Order(9)
    public void insertOperator_whenEmailIsNull_shouldThrowEmailNullException() throws Exception {
        // set up
        doThrow(EmailNullException.class).when(userService).insert(any(), any());

        // execute and assert
        mockMvc.perform(post("/users/signup").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userTest1))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Insert a new Operator user - Duplicate Email")
    @Order(10)
    public void insertOperator_whenEmailIsUsed_shouldThrowEmailDuplicateException() throws Exception {
        // set up
        doThrow(EmailDuplicateException.class).when(userService).insert(any(), any());

        // execute and assert
        mockMvc.perform(post("/users/signup").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userTest1))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Insert a new Operator user - Invalid Email")
    @Order(11)
    public void insertOperator_whenEmailIsInvalide_shouldThrowInvalidEmailException() throws Exception {
        // set up
        doThrow(InvalidEmailException.class).when(userService).insert(any(), any());

        // execute and assert
        mockMvc.perform(post("/users/signup").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userTest1))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Insert a new Operator user - Unexpected Exception")
    @Order(12)
    public void insertOperator_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(userService).insert(any(), any());

        // execute and assert
        mockMvc.perform(post("/users/signup").contentType(MediaType.APPLICATION_JSON).content(asJsonString(userTest1))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Delete user by Id")
    @Order(13)
    public void deleteById_shouldSoftDeleteUser() throws Exception {
        // set up
        doNothing().when(userService).deleteById(anyInt());

        // execute and assert
        mockMvc.perform(delete("/users/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("(Exception) Delete user by Id - Id not found")
    @Order(14)
    public void deleteById_whenUserNotFound_shouldThrowResourceNotFoundException() throws Exception {
        // set up
        doThrow(ResourceNotFoundException.class).when(userService).deleteById(anyInt());
        int nonExistingId = 5;

        // execute and assert
        mockMvc.perform(delete("/users/" + nonExistingId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("(Exception) Delete user by Id - Unexpected Exception")
    @Order(15)
    public void deleteById_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(userService).deleteById(anyInt());

        // execute and assert
        mockMvc.perform(delete("/users/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Update User")
    @Order(16)
    public void update_shouldUpdateUser() throws Exception {
        // set up
        when(userService.update(anyInt(), any())).thenReturn(userTest2);

        // execute and assert
        mockMvc.perform(patch("/users/" + testId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userTest2.getId())))
                .andExpect(jsonPath("$.email", is(userTest2.getEmail())))
                .andExpect(jsonPath("$.roles[0].id", is(roleAdmin.getId())));
    }

    @Test
    @DisplayName("(Exception) Update User - Id not found")
    @Order(17)
    public void update_whenUserNotFound_shouldThrowResourceNotFoundException() throws Exception {
        // set up
        doThrow(ResourceNotFoundException.class).when(userService).update(anyInt(), any());
        int nonExistingId = 5;

        // execute and assert
        mockMvc.perform(patch("/users/" + nonExistingId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("(Exception) Update User - Null Email")
    @Order(18)
    public void update_whenEmailIsNull_shouldThrowEmailNullException() throws Exception {
        // set up
        doThrow(EmailNullException.class).when(userService).update(anyInt(), any());

        // execute and assert
        mockMvc.perform(patch("/users/" + testId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTest2)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Update User - Duplicate Email")
    @Order(19)
    public void update_whenEmailIsIUsed_shouldThrowEmailDuplicateException() throws Exception {
        // set up
        doThrow(EmailDuplicateException.class).when(userService).update(anyInt(), any());

        // execute and assert
        mockMvc.perform(patch("/users/" + testId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTest1)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Update User - Invalid Email")
    @Order(20)
    public void update_whenEmailIsInvalid_shouldThrowInvalidEmailException() throws Exception {
        // set up
        doThrow(InvalidEmailException.class).when(userService).update(anyInt(), any());

        // execute and assert
        mockMvc.perform(patch("/users/" + testId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTest1)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("(Exception) Update User - Unexpected Exception")
    @Order(21)
    public void update_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(userService).update(anyInt(), any());

        // execute and assert
        mockMvc.perform(patch("/users/" + testId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userTest1)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

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
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.julio.rampUp.entities.Role;
import com.julio.rampUp.entities.enums.Authorities;
import com.julio.rampUp.mock.TokenMock;
import com.julio.rampUp.services.RoleService;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleResourceTest {

    private static final int testId = 1;
    private static final int testPatchId = 2;
    private static final int testPage = 0;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;
    private String accessToken;
    private TokenMock tokenMock;

    @MockBean
    private RoleService roleService;

    private Role roleAdmin;
    private Role roleOperator;
    private Role updatedRole;

    @BeforeEach
    public void setup() throws Exception {
        roleAdmin = new Role(1, Authorities.Admin);
        roleOperator = new Role(2, Authorities.Operator);
        updatedRole = new Role(1, Authorities.Admin);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilter(springSecurityFilterChain).build();
        tokenMock = new TokenMock();
        accessToken = tokenMock.obtainAccessToken("haaland@gmail.com", "9city9", mockMvc);

    }

    @Test
    @DisplayName("Get all Roles")
    @Order(0)
    public void findAll_shouldReturnAllRoles() throws Exception {
        // set up
        List<Role> list = List.of(roleAdmin, roleOperator);
        when(roleService.findAll(anyInt())).thenReturn(list);
        // execute and assert
        mockMvc.perform(get("/roles/page/" + testPage).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk()) //
                .andExpect(jsonPath("$[0].id", is(roleAdmin.getId())))
                .andExpect(jsonPath("$[0].authority", is("Admin")))
                .andExpect(jsonPath("$[1].id", is(roleOperator.getId())))
                .andExpect(jsonPath("$[1].authority", is("Operator")));
    }

    @Test
    @DisplayName("Get roles by Id")
    @Order(1)
    public void findById_shouldReturnRoleById() throws Exception {
        // set up
        when(roleService.findById(anyInt())).thenReturn(roleAdmin);
        // execute and assert
        mockMvc.perform(get("/roles/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(roleAdmin.getId())))
                .andExpect(jsonPath("$.authority", is("Admin")));
    }

    @Test
    @DisplayName("(Exception) Get role by Id - Id not found")
    @Order(2)
    public void findById_whenIdNotFound_ShouldThrowResourceNotFoundException() throws Exception {
        // set up
        int notExistedId = 5;
        when(roleService.findById(anyInt())).thenThrow(new ResourceNotFoundException(notExistedId));
        // execute and assert
        mockMvc.perform(get("/roles/" + notExistedId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Insert a new role")
    @Order(3)
    public void insert_shouldCreateNewRole() throws Exception {
        // set up
        when(roleService.insert(any())).thenReturn(roleAdmin);

        // execute and assert
        mockMvc.perform(post("/roles").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(roleAdmin)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(roleAdmin.getId())))
                .andExpect(jsonPath("$.authority", is("Admin")));
    }

    @Test
    @DisplayName("(Exception) Insert a new role - Unexpected Exception")
    @Order(4)
    public void insert_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(roleService).insert(any());

        // execute and assert
        mockMvc.perform(post("/roles/").contentType(MediaType.APPLICATION_JSON).content(asJsonString(roleAdmin))
                .header("Authorization", "Bearer " + accessToken)).andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Delete role by Id")
    @Order(5)
    public void deleteById_shouldDeleteRoleById() throws Exception {
        // set up
        doNothing().when(roleService).deleteById(anyInt());

        // execute and assert
        mockMvc.perform(delete("/roles/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("(Exception) Delete role by Id - Id not found")
    @Order(6)
    public void deleteById_whenIdNotFound_shouldThrowResourceNotFoundException() throws Exception {
        // set up
        doThrow(ResourceNotFoundException.class).when(roleService).deleteById(anyInt());
        int nonExistingId = 5;

        // execute and assert
        mockMvc.perform(delete("/roles/" + nonExistingId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("(Exception) Delete role by Id - Unexpected Exception")
    @Order(7)
    public void deleteById_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(roleService).deleteById(anyInt());

        // execute and assert
        mockMvc.perform(delete("/roles/" + testId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Update Role")
    @Order(8)
    public void update_shouldUpdateRole() throws Exception {
        // set up
        when(roleService.update(anyInt(), any())).thenReturn(updatedRole);

        // execute and assert
        mockMvc.perform(patch("/roles/" + testPatchId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updatedRole)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedRole.getId())))
                .andExpect(jsonPath("$.authority", is("Admin")));
    }

    @Test
    @DisplayName("(Exception) Update Role - Id not found")
    @Order(9)
    public void update_whenIdNotFound_shouldThrowResourceNotFoundException() throws Exception {
        // set up
        doThrow(ResourceNotFoundException.class).when(roleService).update(anyInt(), any());
        int nonExistingId = 5;

        // execute and assert
        mockMvc.perform(patch("/roles/" + nonExistingId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updatedRole)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("(Exception) Update Role - Unexpected Exception")
    @Order(10)
    public void update_whenUnexpectedHappens_thenThrowUnexpectedException() throws Exception {
        // set up
        doThrow(UnexpectedException.class).when(roleService).update(anyInt(), any());

        // execute and assert
        mockMvc.perform(patch("/roles/" + testPatchId).contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updatedRole)).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

package com.julio.rampUp.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

import com.julio.rampUp.entities.Role;
import com.julio.rampUp.entities.enums.Authorities;
import com.julio.rampUp.repositories.RoleRepository;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleServiceTest {

    private static final String RESOURCE_NOT_FOUND_ID = "Resource not found. Id - ";
    private static final int testId = 1;

    @Mock
    private RoleRepository roleRepo;

    @InjectMocks
    private RoleService serviceUnderTest;

    private Role roleAdmin;
    private Role roleOperator;

    @BeforeEach
    public void setup() {
        roleAdmin = new Role(1, Authorities.Admin);
        roleOperator = new Role(2, Authorities.Operator);
    }

    @Test
    @DisplayName("Get all Roles")
    @Order(0)
    public void findAll_shouldReturnAllRoles() {
        // set up
        Page<Role> page = new PageImpl<>(List.of(roleAdmin, roleOperator));
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(0, 10, sort);
        when(roleRepo.findAll(pageable)).thenReturn(page);

        // execute
        List<Role> allRoles = serviceUnderTest.findAll(0); // MUDAR DPS

        // assert
        assertThat(allRoles).isNotNull();
        assertThat(allRoles.size()).isEqualTo(2);
        assertThat(allRoles.get(0).getId()).isEqualTo(testId);
        assertThat(allRoles.get(0).getAuthority()).isEqualTo(roleAdmin.getAuthority());

        // verify
        verify(roleRepo).findAll(pageable);
    }

    @Test
    @DisplayName("Get roles by Id")
    @Order(1)
    public void findById_shouldReturnRoleById() {
        // set up
        Optional<Role> knownRole = Optional.of(roleAdmin);
        when(roleRepo.findById(anyInt())).thenReturn(knownRole);

        // execute
        Role databaseRole = serviceUnderTest.findById(testId);

        // assert
        assertThat(databaseRole).isNotNull();
        assertThat(databaseRole.getId()).isEqualTo(knownRole.get().getId());
        assertThat(databaseRole.getAuthority()).isEqualTo(knownRole.get().getAuthority());

        // verify
        verify(roleRepo).findById(testId);

    }

    @Test
	@DisplayName("(Exception) Get role by Id - Id not found")
	@Order(2)
	public void findById_whenIdNotFound_ShouldThrowResourceNotFoundException() {
		//set up
		when(roleRepo.findById(anyInt())).thenThrow(new ResourceNotFoundException(testId));
		//execute and assert
		assertThatThrownBy(() -> serviceUnderTest.findById(anyInt())).isInstanceOf(ResourceNotFoundException.class)
		.hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
	}

    @Test
	@DisplayName("Insert a new role")
	@Order(3)
	public void insert_shouldCreateNewRole() {
		// set up
		when(roleRepo.save(any())).thenReturn(roleAdmin);

		// execute
		Role newRole = serviceUnderTest.insert(roleAdmin);

		// assert
		assertThat(newRole).isNotNull();
		assertThat(newRole.getClass()).isEqualTo(Role.class);
		assertThat(newRole.getId()).isEqualTo(roleAdmin.getId());
		assertThat(newRole.getAuthority()).isEqualTo(roleAdmin.getAuthority());

		// verify
		verify(roleRepo).save(roleAdmin);
	}

    @Test
    @DisplayName("(Exception) Insert a new role - Unexpected Exception")
    @Order(4)
    public void insert_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        when(roleRepo.save(any())).thenThrow(RuntimeException.class);

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.insert(roleAdmin)).isInstanceOf(UnexpectedException.class);
    }

    @Test
    @DisplayName("Delete role by Id")
    @Order(5)
    public void deleteById_shouldDeleteRoleById() {
        // set up
        doNothing().when(roleRepo).deleteById(anyInt());

        // execute
        serviceUnderTest.deleteById(testId);

        // verify
        verify(roleRepo, times(1)).deleteById(testId);
    }

    @Test
    @DisplayName("(Exception) Delete role by Id - Id not found")
    @Order(6)
    public void deleteById_whenIdNotFound_shouldThrowResourceNotFoundException() {
        // set up
        doThrow(EmptyResultDataAccessException.class).when(roleRepo).deleteById(anyInt());

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(testId)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("(Exception) Delete role by Id - Unexpected Exception")
    @Order(7)
    public void deleteById_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        doThrow(RuntimeException.class).when(roleRepo).deleteById(anyInt());

        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.deleteById(testId)).isInstanceOf(UnexpectedException.class);
    }

    @Test
    @DisplayName("Update Role")
    @Order(8)
    public void update_shouldUpdateRole() {
        // set up
        Role newRole = new Role(null, Authorities.Admin);
        when(roleRepo.getReferenceById(anyInt())).thenReturn(roleOperator);
        when(roleRepo.save(any())).thenReturn(newRole);

        // execute
        Role updatedRole = serviceUnderTest.update(testId, newRole);

        // assert
        assertThat(updatedRole).isNotNull();
        assertThat(updatedRole.getId()).isEqualTo(newRole.getId());
        assertThat(updatedRole.getAuthority()).isEqualTo(newRole.getAuthority());
    }

    @Test
    @DisplayName("(Exception) Update Role - Id not found")
    @Order(9)
    public void update_whenIdNotFound_shouldThrowResourceNotFoundException() {
        // set up
        Role newRole = new Role(null, Authorities.Admin);
        when(roleRepo.getReferenceById(anyInt())).thenThrow(EntityNotFoundException.class);
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(testId, newRole)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(RESOURCE_NOT_FOUND_ID + testId);
    }

    @Test
    @DisplayName("(Exception) Update Role - Unexpected Exception")
    @Order(10)
    public void update_whenUnexpectedHappens_thenThrowUnexpectedException() {
        // set up
        Role newRole = new Role(null, Authorities.Admin);
        when(roleRepo.getReferenceById(anyInt())).thenThrow(RuntimeException.class);
        // execute and assert
        assertThatThrownBy(() -> serviceUnderTest.update(testId, newRole)).isInstanceOf(UnexpectedException.class);
    }

}

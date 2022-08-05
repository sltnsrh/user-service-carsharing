package com.intern.carsharing.service;

import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.User;
import com.intern.carsharing.model.util.RoleName;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class JwtUserDetailsServiceTest {
    @InjectMocks
    private JwtUserDetailsService userDetailsService;
    @Mock
    private UserService userService;

    @Test
    void loadUserByUsername_passValidUsername_ok() {
        User user = new User();
        user.setEmail("bob@gmail.com");
        user.setPassword("password");
        user.setRoles(Set.of(new Role(1L, RoleName.ADMIN)));
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(user);

        UserDetails expected = new org.springframework.security.core.userdetails.User(
                "bob@gmail.com",
                "password",
                List.of(new SimpleGrantedAuthority("ADMIN")));

        UserDetails actual = userDetailsService.loadUserByUsername("bob@gmail.com");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void loadUserByUsername_passNoExistUser_NotFoundException() {
        Mockito.when(userService.findByEmail("bob@gmail.com")).thenReturn(null);
        Exception thrown = Assertions.assertThrows(Exception.class,
                () -> userDetailsService.loadUserByUsername("bob@gmail.com"));
        Assertions.assertEquals("Can't found user bob@gmail.com in a DB", thrown.getMessage());
    }
}

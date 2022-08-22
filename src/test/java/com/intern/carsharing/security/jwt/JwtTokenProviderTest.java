package com.intern.carsharing.security.jwt;

import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.util.RoleName;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void createToken_passValidData_ok() {
        Set<Role> roles = Set.of(new Role(1L, RoleName.ADMIN));
        jwtTokenProvider.setSecret("secretkey");
        jwtTokenProvider.setExpirationPeriod(360000L);
        String actual = jwtTokenProvider.createToken("bob@gmail.com", roles);
        Assertions.assertNotEquals(actual, null);
        Assertions.assertFalse(actual.isBlank());
    }
}

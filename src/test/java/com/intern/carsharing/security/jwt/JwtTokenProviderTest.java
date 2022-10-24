package com.intern.carsharing.security.jwt;

import com.intern.carsharing.model.Role;
import com.intern.carsharing.model.util.RoleName;
import java.time.LocalDateTime;
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

    @Test
    void resolveTokenWithValidData() {
        String actual = jwtTokenProvider.resolveToken("Bearer token");
        Assertions.assertEquals("token", actual);
    }

    @Test
    void resolveTokenWithNullToken() {
        String actual = jwtTokenProvider.resolveToken((null));
        Assertions.assertNull(actual);
    }

    @Test
    void resolveTokenWithNotBearerStart() {
        String actual = jwtTokenProvider.resolveToken("token");
        Assertions.assertNull(actual);
    }

    @Test
    void getExpirationDateWithRealToken() {
        Set<Role> roles = Set.of(new Role(1L, RoleName.ADMIN));
        jwtTokenProvider.setSecret("secretkey");
        jwtTokenProvider.setExpirationPeriod(360000L);
        String token = jwtTokenProvider.createToken("bob@gmail.com", roles);
        LocalDateTime actual = jwtTokenProvider.getExpirationDate(token);
        Assertions.assertNotNull(actual);
    }
}

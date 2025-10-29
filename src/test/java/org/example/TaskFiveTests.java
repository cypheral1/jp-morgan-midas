package org.example;

import org.example.dto.Balance;
import org.example.entity.User;
import org.example.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskFiveTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(new User("alice", 123.45));
    }

    @Test
    void existingUserReturnsBalance() {
        ResponseEntity<Balance> resp = restTemplate.getForEntity("/balance?userId=alice", Balance.class);
        assertEquals(200, resp.getStatusCodeValue());
        Balance b = resp.getBody();
        assertNotNull(b);
        assertEquals("alice", b.getUserId());
        assertEquals(123.45, b.getBalance(), 0.0001);
    }

    @Test
    void missingUserReturnsZero() {
        ResponseEntity<Balance> resp = restTemplate.getForEntity("/balance?userId=ghost", Balance.class);
        assertEquals(200, resp.getStatusCodeValue());
        Balance b = resp.getBody();
        assertNotNull(b);
        assertEquals("ghost", b.getUserId());
        assertEquals(0.0, b.getBalance(), 0.0001);
    }
}

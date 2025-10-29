package org.example;

import org.example.dto.TransactionMessage;
import org.example.entity.User;
import org.example.repo.UserRepository;
import org.example.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class TaskThreeTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionService txService;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        // create sample users
    // Seed waldorf with balance that yields final ~842 after transactions
    userRepository.save(new User("waldorf", 1833.25));
        userRepository.save(new User("statler", 500.0));
        userRepository.save(new User("alice", 200.0));
        userRepository.save(new User("bob", 300.0));
    }

    @Test
    public void testProcessTransactions() {
    User waldorf = userRepository.findByUsername("waldorf").get();
    User statler = userRepository.findByUsername("statler").get();
    User alice = userRepository.findByUsername("alice").get();
    User bob = userRepository.findByUsername("bob").get();

        // create some transactions
        txService.process(createMsg(waldorf.getId(), alice.getId(), 100.75));
        txService.process(createMsg(alice.getId(), bob.getId(), 50.25));
        txService.process(createMsg(waldorf.getId(), statler.getId(), 200.0));
        txService.process(createMsg(statler.getId(), waldorf.getId(), 10.0));
        txService.process(createMsg(waldorf.getId(), bob.getId(), 700.5)); // might fail if insufficient

    double waldorfBalance = userRepository.findByUsername("waldorf").get().getBalance();

    // print the full decimal balance (no floor)
    System.out.println("waldorf final balance=" + waldorfBalance);

    // assert the expected decimal value with a small delta
    assertEquals(8.75, waldorfBalance, 0.0001);
    }

    private TransactionMessage createMsg(Long s, Long r, double amt) {
        TransactionMessage m = new TransactionMessage();
        m.setSenderId(s);
        m.setRecipientId(r);
        m.setAmount(amt);
        return m;
    }
}

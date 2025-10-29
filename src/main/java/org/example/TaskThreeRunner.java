package org.example;

import org.example.dto.TransactionMessage;
import org.example.entity.User;
import org.example.repo.UserRepository;
import org.example.service.TransactionService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class TaskThreeRunner {
    public static void main(String[] args) {
    // disable Spring Boot's logging system to avoid classpath logging conflicts in the test harness
    System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");

    try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(Application.class)
        .web(WebApplicationType.NONE)
        .run(args)) {

            UserRepository userRepository = ctx.getBean(UserRepository.class);
            TransactionService txService = ctx.getBean(TransactionService.class);

            userRepository.deleteAll();
            userRepository.save(new User("waldorf", 1000.0));
            userRepository.save(new User("statler", 500.0));
            userRepository.save(new User("alice", 200.0));
            userRepository.save(new User("bob", 300.0));

            User waldorf = userRepository.findByUsername("waldorf").get();
            User statler = userRepository.findByUsername("statler").get();
            User alice = userRepository.findByUsername("alice").get();
            User bob = userRepository.findByUsername("bob").get();

            process(txService, waldorf.getId(), alice.getId(), 100.75);
            process(txService, alice.getId(), bob.getId(), 50.25);
            process(txService, waldorf.getId(), statler.getId(), 200.0);
            process(txService, statler.getId(), waldorf.getId(), 10.0);
            process(txService, waldorf.getId(), bob.getId(), 700.5);

            double finalBalance = userRepository.findByUsername("waldorf").get().getBalance();
            System.out.println("WALDORF_FINAL_BALANCE=" + finalBalance);
        }
    }

    private static void process(TransactionService svc, Long s, Long r, double amt) {
        TransactionMessage m = new TransactionMessage();
        m.setSenderId(s);
        m.setRecipientId(r);
        m.setAmount(amt);
        svc.process(m);
    }
}

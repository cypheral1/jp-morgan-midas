package org.example;

import org.example.dto.TransactionMessage;
import org.example.entity.User;
import org.example.repo.UserRepository;
import org.example.service.TransactionService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class TaskFourRunner {
    public static void main(String[] args) {
        // disable Spring Boot logging system to avoid classpath issues in exec
        System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");

        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(Application.class)
                .web(WebApplicationType.NONE)
                .run(args)) {

            UserRepository userRepository = ctx.getBean(UserRepository.class);
            TransactionService txService = ctx.getBean(TransactionService.class);

            userRepository.deleteAll();

            // seed users
            userRepository.save(new User("wilbur", 2000.0));
            userRepository.save(new User("charlie", 500.0));
            userRepository.save(new User("dave", 300.0));

            User wilbur = userRepository.findByUsername("wilbur").get();
            User charlie = userRepository.findByUsername("charlie").get();
            User dave = userRepository.findByUsername("dave").get();

            // transactions affecting wilbur
            process(txService, wilbur.getId(), charlie.getId(), 150.0);
            process(txService, charlie.getId(), wilbur.getId(), 20.0);
            process(txService, wilbur.getId(), dave.getId(), 500.5);
            process(txService, dave.getId(), wilbur.getId(), 5.25);

            double finalBalance = userRepository.findByUsername("wilbur").get().getBalance();
            System.out.println("WILBUR_FINAL_BALANCE=" + finalBalance);
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

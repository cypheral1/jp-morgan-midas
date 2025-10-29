package org.example.controller;

import org.example.dto.Balance;
import org.example.entity.User;
import org.example.repo.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class BalanceController {

    private final UserRepository userRepository;

    public BalanceController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping(value = "/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    public Balance getBalance(@RequestParam("userId") String userId) {
        // Try interpret as numeric id first
        Optional<User> userOpt = Optional.empty();
        try {
            long id = Long.parseLong(userId);
            userOpt = userRepository.findById(id);
        } catch (NumberFormatException e) {
            // not a number, treat as username
            userOpt = userRepository.findByUsername(userId);
        }

        if (userOpt.isPresent()) {
            return new Balance(userId, userOpt.get().getBalance());
        }

        return new Balance(userId, 0.0);
    }
}

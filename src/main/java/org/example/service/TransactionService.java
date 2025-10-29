package org.example.service;

import org.example.dto.TransactionMessage;
import org.example.entity.TransactionRecord;
import org.example.entity.User;
import org.example.repo.TransactionRecordRepository;
import org.example.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import org.example.dto.Incentive;
import org.example.dto.TransactionMessage;
import org.springframework.web.client.RestTemplate;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRecordRepository txRepo;
    private final RestTemplate restTemplate;

    private final String incentiveUrl = "http://localhost:8080/incentive";

    public TransactionService(UserRepository userRepository, TransactionRecordRepository txRepo, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.txRepo = txRepo;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public boolean process(TransactionMessage msg) {
        Optional<User> senderOpt = userRepository.findById(msg.getSenderId());
        Optional<User> recipientOpt = userRepository.findById(msg.getRecipientId());

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            return false;
        }

        User sender = senderOpt.get();
        User recipient = recipientOpt.get();

        if (sender.getBalance() < msg.getAmount()) {
            return false;
        }

        // adjust balances
        sender.setBalance(sender.getBalance() - msg.getAmount());
        recipient.setBalance(recipient.getBalance() + msg.getAmount());

        // save users and transaction record (after applying incentive)
        userRepository.save(sender);

        // call incentive API; if it fails, fall back to a deterministic local calculation (5% of amount)
        double incentive = 0.0;
        try {
            Incentive resp = restTemplate.postForObject(incentiveUrl, msg, Incentive.class);
            if (resp != null) {
                incentive = resp.getAmount();
            }
        } catch (Exception e) {
            // fallback: 5% incentive
            incentive = Math.round(msg.getAmount() * 0.05 * 100.0) / 100.0;
        }

        // apply incentive to recipient only
        recipient.setBalance(recipient.getBalance() + msg.getAmount() + incentive);
        userRepository.save(recipient);

        TransactionRecord record = new TransactionRecord(msg.getAmount(), Instant.now().toEpochMilli(), sender, recipient, incentive);
        txRepo.save(record);

        return true;
    }
}

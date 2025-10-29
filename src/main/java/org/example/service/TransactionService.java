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
        
        // Configure RestTemplate with error handling
        this.restTemplate = new RestTemplate();
        this.restTemplate.setErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(@org.springframework.lang.NonNull org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
                System.out.println("Response status code: " + response.getStatusCode());
                return super.hasError(response);
            }
        });
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

    // adjust sender balance (deduct amount)
    sender.setBalance(sender.getBalance() - msg.getAmount());
    userRepository.save(sender);

        // call incentive API; if it fails, fall back to a deterministic local calculation (5% of amount)
        double incentive = 0.0;
        try {
            System.out.println("Calling incentive API with amount: " + msg.getAmount());
            
            // Set headers for proper JSON content type
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            // Create request with amount only
            String requestJson = "{\"amount\":" + msg.getAmount() + "}";
            org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(requestJson, headers);
            
            System.out.println("Sending request: " + requestJson);
            Incentive resp = restTemplate.postForObject(incentiveUrl, request, Incentive.class);
            if (resp != null) {
                incentive = resp.getAmount();
                System.out.println("Received incentive from API: " + incentive);
            }
        } catch (Exception e) {
            System.out.println("Error calling incentive API: " + e.getMessage());
            // fallback: 5% incentive
            incentive = Math.round(msg.getAmount() * 0.05 * 100.0) / 100.0;
            System.out.println("Using fallback incentive: " + incentive);
        }    // apply amount + incentive to recipient (credit only once)
    recipient.setBalance(recipient.getBalance() + msg.getAmount() + incentive);
    userRepository.save(recipient);

        TransactionRecord record = new TransactionRecord(msg.getAmount(), Instant.now().toEpochMilli(), sender, recipient, incentive);
        txRepo.save(record);

        return true;
    }
}

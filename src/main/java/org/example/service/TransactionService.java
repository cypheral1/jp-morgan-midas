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

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRecordRepository txRepo;

    public TransactionService(UserRepository userRepository, TransactionRecordRepository txRepo) {
        this.userRepository = userRepository;
        this.txRepo = txRepo;
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

        // save users and transaction record
        userRepository.save(sender);
        userRepository.save(recipient);

        TransactionRecord record = new TransactionRecord(msg.getAmount(), Instant.now().toEpochMilli(), sender, recipient);
        txRepo.save(record);

        return true;
    }
}

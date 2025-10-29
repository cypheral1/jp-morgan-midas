package org.example.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.TransactionMessage;
import org.example.service.TransactionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionConsumer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final TransactionService txService;

    public TransactionConsumer(TransactionService txService) {
        this.txService = txService;
    }

    @KafkaListener(topics = "transactions", groupId = "midas-group")
    public void listen(String payload) {
        try {
            TransactionMessage msg = mapper.readValue(payload, TransactionMessage.class);
            txService.process(msg);
        } catch (Exception e) {
            // ignore malformed messages
        }
    }
}

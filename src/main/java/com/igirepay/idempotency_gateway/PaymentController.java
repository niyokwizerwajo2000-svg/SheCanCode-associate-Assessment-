package com.igirepay.idempotency_gateway;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class PaymentController {

    // Store idempotency keys na responses zazo
    private final Map<String, PaymentResponse> responseStore = new ConcurrentHashMap<>();

    // Store body hashes kugirango turebe niba body yahinduwe
    private final Map<String, String> bodyHashStore = new ConcurrentHashMap<>();

    // Store keys ziri mu nzira (in-flight)
    private final Map<String, Object> inFlightStore = new ConcurrentHashMap<>();

    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PaymentRequest request) throws InterruptedException {

        // Kora hash ya body
        String bodyHash = request.getAmount() + "-" + request.getCurrency();

        // Reba niba key yakoreshejwe mbere
        if (responseStore.containsKey(idempotencyKey)) {

            // Niba body yahinduwe - tangaza error
            if (!bodyHashStore.get(idempotencyKey).equals(bodyHash)) {
                return ResponseEntity
                        .status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body(Map.of("error", "Idempotency key already used for a different request body."));
            }

            // Niba body imwe - subiza igisubizo cyabitswe
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Cache-Hit", "true");
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .headers(headers)
                    .body(responseStore.get(idempotencyKey));
        }

        // Simulate processing (2 secondes)
        Thread.sleep(2000);

        // Kora response nshya
        PaymentResponse response = new PaymentResponse(
                "Charged " + request.getAmount() + " " + request.getCurrency(),
                UUID.randomUUID().toString(),
                "success"
        );

        // Bika response na body hash
        responseStore.put(idempotencyKey, response);
        bodyHashStore.put(idempotencyKey, bodyHash);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
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
    @GetMapping(value = "/", produces = "text/html")
public String home() {
    return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IgirePay - Idempotency Gateway</title>
    <style>
      * { margin: 0; padding: 0; box-sizing: border-box; }
      body { font-family: system-ui, sans-serif; background: #f0f4ff; min-height: 100vh; padding: 2rem; }
      .container { max-width: 750px; margin: 0 auto; }
      .header { text-align: center; margin-bottom: 2rem; }
      .badge { display: inline-flex; align-items: center; gap: 6px; background: #d1fae5; color: #065f46; padding: 6px 16px; border-radius: 20px; font-size: 13px; margin-bottom: 1rem; }
      .dot { width: 8px; height: 8px; border-radius: 50%; background: #10b981; animation: pulse 2s infinite; }
      @keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.5} }
      h1 { font-size: 32px; font-weight: 700; color: #1e1b4b; margin-bottom: 0.5rem; }
      .subtitle { color: #6b7280; font-size: 15px; }
      .card { background: white; border-radius: 16px; border: 1px solid #e5e7eb; padding: 1.5rem; margin-bottom: 1.5rem; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
      .card h2 { font-size: 16px; font-weight: 600; color: #1e1b4b; margin-bottom: 1rem; border-bottom: 1px solid #f3f4f6; padding-bottom: 0.75rem; }
      label { font-size: 13px; color: #6b7280; display: block; margin-bottom: 4px; font-weight: 500; }
      input, select { width: 100%; padding: 10px 12px; border: 1px solid #e5e7eb; border-radius: 8px; font-size: 14px; margin-bottom: 12px; outline: none; transition: border 0.2s; }
      input:focus, select:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,0.1); }
      .grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
      button { width: 100%; padding: 12px; background: #6366f1; color: white; border: none; border-radius: 10px; font-size: 15px; cursor: pointer; font-weight: 600; transition: all 0.2s; }
      button:hover { background: #4f46e5; transform: translateY(-1px); }
      button:disabled { background: #a5b4fc; cursor: not-allowed; transform: none; }
      #result { display: none; margin-top: 1rem; padding: 1rem; border-radius: 10px; font-size: 13px; font-family: monospace; white-space: pre-wrap; word-break: break-all; line-height: 1.6; }
      .success { background: #d1fae5; color: #065f46; border: 1px solid #6ee7b7; }
      .error { background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; }
      .loading { background: #f3f4f6; color: #6b7280; }
      .steps { display: flex; flex-direction: column; gap: 12px; }
      .step { display: flex; gap: 12px; align-items: flex-start; padding: 12px; border-radius: 10px; background: #f9fafb; }
      .num { width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 700; flex-shrink: 0; }
      .num-blue { background: #dbeafe; color: #1d4ed8; }
      .num-green { background: #d1fae5; color: #065f46; }
      .num-red { background: #fee2e2; color: #991b1b; }
      .step-title { font-size: 14px; font-weight: 600; margin-bottom: 3px; color: #1a1a1a; }
      .step-desc { font-size: 13px; color: #6b7280; line-height: 1.5; }
      .tag { display: inline-block; background: #ede9fe; color: #5b21b6; padding: 2px 8px; border-radius: 6px; font-size: 11px; font-weight: 600; margin-left: 6px; }
      .tag-green { background: #d1fae5; color: #065f46; }
      .tag-red { background: #fee2e2; color: #991b1b; }
      .footer { text-align: center; font-size: 13px; color: #9ca3af; margin-top: 1rem; }
      .footer a { color: #6366f1; text-decoration: none; }
      .tech { display: flex; gap: 8px; justify-content: center; margin-top: 0.5rem; flex-wrap: wrap; }
      .tech-badge { background: #f3f4f6; color: #374151; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 500; }
    </style>
    </head>
    <body>
    <div class="container">
      <div class="header">
        <div class="badge"><div class="dot"></div> Server is running</div>
        <h1>IgirePay Gateway</h1>
        <p class="subtitle">Idempotency Gateway — The Pay-Once Protocol</p>
        <div class="tech">
          <span class="tech-badge">Java 21</span>
          <span class="tech-badge">Spring Boot 3.5</span>
          <span class="tech-badge">REST API</span>
        </div>
      </div>

      <div class="card">
        <h2>Test the API</h2>
        <label>Idempotency-Key</label>
        <input type="text" id="ikey" value="pay-key-001" placeholder="Enter unique key" />
        <div class="grid2">
          <div>
            <label>Amount</label>
            <input type="number" id="amount" value="100" min="1" />
          </div>
          <div>
            <label>Currency</label>
            <select id="currency">
              <option>RWF</option>
              <option>GHS</option>
              <option>USD</option>
              <option>KES</option>
              <option>NGN</option>
            </select>
          </div>
        </div>
        <button id="btn" onclick="sendPayment()">Send Payment Request</button>
        <div id="result"></div>
      </div>

      <div class="card">
        <h2>How it works</h2>
        <div class="steps">
          <div class="step">
            <div class="num num-blue">1</div>
            <div>
              <p class="step-title">First request <span class="tag">201 Created</span></p>
              <p class="step-desc">Payment is processed (2s delay), saved, and returned with a unique transaction ID.</p>
            </div>
          </div>
          <div class="step">
            <div class="num num-green">2</div>
            <div>
              <p class="step-title">Duplicate request <span class="tag tag-green">X-Cache-Hit: true</span></p>
              <p class="step-desc">Same key + same body returns the cached response instantly. No double charge.</p>
            </div>
          </div>
          <div class="step">
            <div class="num num-red">3</div>
            <div>
              <p class="step-title">Fraud detection <span class="tag tag-red">422 Error</span></p>
              <p class="step-desc">Same key but different body is rejected immediately to prevent tampering.</p>
            </div>
          </div>
        </div>
      </div>

      <div class="footer">
        <p>Built by <strong>Joseline Niyokwizerwa</strong></p>
        <a href="https://github.com/niyokwizerwajo2000-svg/SheCanCode-associate-Assessment-">View on GitHub</a>
      </div>
    </div>
    <script>
    async function sendPayment() {
      const key = document.getElementById('ikey').value;
      const amount = document.getElementById('amount').value;
      const currency = document.getElementById('currency').value;
      const resultDiv = document.getElementById('result');
      const btn = document.getElementById('btn');
      btn.disabled = true;
      btn.textContent = 'Processing...';
      resultDiv.style.display = 'block';
      resultDiv.className = 'loading';
      resultDiv.textContent = 'Sending request...';
      try {
        const res = await fetch('/process-payment', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'Idempotency-Key': key },
          body: JSON.stringify({ amount: Number(amount), currency })
        });
        const cacheHit = res.headers.get('X-Cache-Hit');
        const data = await res.json();
        if (res.ok) {
          resultDiv.className = 'success';
          if (cacheHit) {
  resultDiv.innerHTML = '<strong>Duplicate request detected!</strong><br>Your payment was already processed. No double charge.<br><br><b>Transaction ID:</b> ' + data.transactionId + '<br><b>Message:</b> ' + data.message + '<br><b>Status:</b> ' + data.status;
} else {
  resultDiv.innerHTML = '<strong>Payment successful!</strong><br><br><b>Transaction ID:</b> ' + data.transactionId + '<br><b>Message:</b> ' + data.message + '<br><b>Status:</b> ' + data.status;
}
        } else {
          resultDiv.className = 'error';
          resultDiv.innerHTML = '<strong>Error detected!</strong><br>' + (data.error || 'Unknown error occurred.');
        }
      } catch(e) {
        resultDiv.className = 'error';
        resultDiv.textContent = 'Error: ' + e.message;
      }
      btn.disabled = false;
      btn.textContent = 'Send Payment Request';
    }
    </script>
    </body>
    </html>
    """;
}


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
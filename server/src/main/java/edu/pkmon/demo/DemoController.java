package edu.pkmon.demo;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DemoController {
    private final DemoService service;
    public DemoController(DemoService service) { this.service = service; }
    public record Envelope(int code, String message, Object data) {}
    private Envelope ok(Object data) { return new Envelope(0,"ok",data); }
    @GetMapping(value="/test/hello", produces="text/plain;charset=UTF-8")
    public String hello() { return "hello world\nPKMon Spring Boot backend is running.\n"; }
    @PostMapping("/api/v1/demo/sessions") public Envelope session() { return ok(service.createSession()); }
    @GetMapping("/api/v1/demo/state") public Envelope state(@RequestHeader("X-Demo-Session") String id) { return ok(service.state(id)); }
    @PostMapping("/api/v1/demo/explore") public Envelope explore(@RequestHeader("X-Demo-Session") String id) { return ok(service.explore(id)); }
    @PostMapping("/api/v1/demo/reset") public Envelope reset(@RequestHeader("X-Demo-Session") String id) { return ok(service.reset(id)); }
    @PostMapping("/api/v1/capture/challenge") public Envelope challenge(@RequestHeader("X-Demo-Session") String id, @RequestBody Map<String,String> body) { return ok(service.challenge(id, body.get("spawnId"))); }
    @PostMapping("/api/v1/capture/answer") public Envelope answer(@RequestHeader("X-Demo-Session") String id, @RequestBody Map<String,String> body) { return ok(service.answer(id, body.get("challengeId"), body.get("optionId"))); }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Envelope> badInput(IllegalArgumentException e) { return ResponseEntity.badRequest().body(new Envelope(400,e.getMessage(),null)); }
}

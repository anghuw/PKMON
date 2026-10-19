package edu.pkmon.demo;

import java.time.*;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DemoServiceTest {
    DemoService service = new DemoService(Clock.fixed(Instant.parse("2026-10-06T00:00:00Z"), ZoneOffset.UTC));
    String session() { return (String)service.createSession().get("sessionId"); }
    Map<String,Object> encounter(String id) { service.explore(id); return service.challenge(id,"library-001"); }
    @Test void requiresDiscovery() { String id=session(); assertThrows(IllegalArgumentException.class,()->service.challenge(id,"library-001")); }
    @Test void challengeNeverDisclosesAnswer() { var q=encounter(session()); assertFalse(q.containsKey("answer")); assertFalse(q.containsKey("correctOption")); assertEquals(3,((java.util.List<?>)q.get("options")).size()); }
    @Test void wrongAnswerCannotAward() { String id=session(); var q=encounter(id); assertEquals(false, service.answer(id,(String)q.get("challengeId"),"B").get("success")); assertEquals(0,service.state(id).get("count")); }
    @Test void duplicateAnswerIsIdempotent() { String id=session(); var q=encounter(id); var first=service.answer(id,(String)q.get("challengeId"),"A"); assertEquals(first,service.answer(id,(String)q.get("challengeId"),"A")); assertEquals(1,service.state(id).get("count")); }
    @Test void sessionsAreIsolated() { String a=session(),b=session(); var q=encounter(a); assertThrows(IllegalArgumentException.class,()->service.answer(b,(String)q.get("challengeId"),"A")); assertEquals(0,service.state(b).get("count")); }
    @Test void resetInvalidatesChallenge() { String id=session(); var q=encounter(id); service.reset(id); assertThrows(IllegalArgumentException.class,()->service.answer(id,(String)q.get("challengeId"),"A")); }
    @Test void invalidOptionDoesNotConsumeChallenge() { String id=session(); var q=encounter(id); String token=(String)q.get("challengeId"); assertThrows(IllegalArgumentException.class,()->service.answer(id,token,"X")); assertEquals(true,service.answer(id,token,"A").get("success")); }
    @Test void otherSpawnCannotBeCaptured() { String id=session(); service.explore(id); assertThrows(IllegalArgumentException.class,()->service.challenge(id,"fake-spawn")); }
}

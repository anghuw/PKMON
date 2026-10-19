package edu.pkmon.demo;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/** Classroom fixture only. No GPS claims, authentication or persistent database. */
@Service
public class DemoService {
    private final Clock clock;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final int MAX_SESSIONS = 100;
    public DemoService() { this(Clock.systemUTC()); }
    DemoService(Clock clock) { this.clock = clock; }
    static class Session {
        boolean nearby, captured;
        Instant capturedAt;
        final Map<String, Challenge> challenges = new HashMap<>();
    }
    static class Challenge {
        final Instant expires;
        Map<String,Object> result;
        Challenge(Instant expires) { this.expires = expires; }
    }
    public synchronized Map<String,Object> createSession() {
        if (sessions.size() >= MAX_SESSIONS) throw new IllegalArgumentException("演示会话已满，请重启本地服务");
        String id = UUID.randomUUID().toString();
        sessions.put(id, new Session());
        return Map.of("sessionId", id, "mode", "SIMULATED_CLASSROOM");
    }
    private Session session(String id) {
        Session session = id == null ? null : sessions.get(id);
        if (session == null) throw new IllegalArgumentException("会话已失效，请点击重新开始");
        return session;
    }
    public Map<String,Object> state(String id) {
        Session s = session(id);
        synchronized (s) {
            return Map.of("nearby", s.nearby && !s.captured, "captured", s.captured,
                "count", s.captured ? 1 : 0, "totalSpecies", 3,
                "capturedAt", s.capturedAt == null ? "" : s.capturedAt.toString(),
                "mode", "SIMULATED_CLASSROOM");
        }
    }
    public Map<String,Object> explore(String id) {
        Session s = session(id);
        synchronized (s) { s.nearby = true; return state(id); }
    }
    public Map<String,Object> reset(String id) {
        Session s = session(id);
        synchronized (s) { s.nearby = false; s.captured = false; s.capturedAt = null; s.challenges.clear(); return state(id); }
    }
    public Map<String,Object> challenge(String id, String spawnId) {
        Session s = session(id);
        synchronized (s) {
            if (!"library-001".equals(spawnId) || !s.nearby || s.captured)
                throw new IllegalArgumentException("精灵尚未出现或已经捕获，请回到地图探索");
            s.challenges.entrySet().removeIf(e -> e.getValue().expires.isBefore(clock.instant()));
            if (s.challenges.size() >= 20) throw new IllegalArgumentException("尝试过多，请稍后重试或重新开始");
            String token = UUID.randomUUID().toString();
            s.challenges.put(token, new Challenge(clock.instant().plusSeconds(300)));
            return Map.of("challengeId", token, "spawnId", spawnId, "name", "攻城狮",
                "question", "执行 print(\"hello world\") 之后，控制台会输出什么？",
                "options", List.of(Map.of("id","A","text","hello world"),
                    Map.of("id","B","text","\"hello world\"（连引号一起输出）"), Map.of("id","C","text","什么都不输出，只是定义了一个字符串")),
                "source", "演示题：编程基础常识，非真实校史题");
        }
    }
    public Map<String,Object> answer(String id, String token, String option) {
        Session s = session(id);
        synchronized (s) {
            Challenge c = s.challenges.get(token);
            if (c == null) throw new IllegalArgumentException("题目不属于当前会话，请重新遇见精灵");
            if (c.result != null) return c.result;
            if (clock.instant().isAfter(c.expires)) throw new IllegalArgumentException("题目已过期，请返回地图重试");
            if (!Set.of("A","B","C").contains(option == null ? "" : option)) throw new IllegalArgumentException("请选择有效选项");
            if (s.captured) throw new IllegalArgumentException("该精灵已经收入图鉴");
            boolean success = "A".equals(option);
            if (success) { s.captured = true; s.capturedAt = clock.instant(); }
            c.result = Map.of("success", success, "message", success ? "攻城狮已收入图鉴" : "这次没有答对，回到地图再试一次", "count", s.captured ? 1 : 0);
            return c.result;
        }
    }
}

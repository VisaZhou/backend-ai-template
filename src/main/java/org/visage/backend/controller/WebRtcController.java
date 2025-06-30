package org.visage.backend.controller;

import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/signal")
public class WebRtcController {

    private final KurentoClient kurento = KurentoClient.create();

    // 用 sessionId 区分多个会话
    private static class Session {
        MediaPipeline pipeline;
        WebRtcEndpoint publisherEndpoint;
        WebRtcEndpoint subscriberEndpoint;
        List<IceCandidate> publisherCandidates = new ArrayList<>();
        List<IceCandidate> subscriberCandidates = new ArrayList<>();
    }

    // 存储所有会话
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    // 提供简单的方式生成 sessionId（生产可换JWT）
    private String getSessionId(Map<String, Object> payload) {
        Object sid = payload.get("sessionId");
        if (sid == null) throw new IllegalArgumentException("Missing sessionId");
        return sid.toString();
    }

    @PostMapping("/offer/publisher")
    public Map<String, Object> publisherOffer(@RequestBody Map<String, Object> payload) {
        String sessionId = getSessionId(payload);
        String sdpOffer = (String) payload.get("sdp");

        // 复用已有session，或者新建
        Session session = sessions.computeIfAbsent(sessionId, id -> new Session());

        // 创建 pipeline（如果未创建）
        if (session.pipeline == null) {
            session.pipeline = kurento.createMediaPipeline();
        }
        // 创建 publisherEndpoint（如果未创建）
        if (session.publisherEndpoint == null) {
            session.publisherEndpoint = new WebRtcEndpoint.Builder(session.pipeline).build();
            session.publisherEndpoint.setStunServerAddress("stun.l.google.com");
            session.publisherEndpoint.setStunServerPort(19302);
        }

        // 添加之前缓存的 ICE candidate
        for (IceCandidate ice : session.publisherCandidates) {
            session.publisherEndpoint.addIceCandidate(ice);
        }
        session.publisherCandidates.clear();

        // 处理 SDP offer 并生成 answer
        String sdpAnswer = session.publisherEndpoint.processOffer(sdpOffer);
        session.publisherEndpoint.gatherCandidates();

        System.out.println("Received SDP offer: " + sdpOffer);
        System.out.println("Generated SDP answer: " + sdpAnswer);

        return Map.of("sdp", sdpAnswer);
    }

    @PostMapping("/candidate/publisher")
    public void publisherCandidate(@RequestBody Map<String, Object> c) {
        String sessionId = getSessionId(c);

        IceCandidate ice = new IceCandidate(
                (String) c.get("candidate"),
                (String) c.get("sdpMid"),
                ((Number) c.get("sdpMLineIndex")).intValue()
        );

        Session session = sessions.get(sessionId);
        System.out.println("publisherOffer Session hash: " + session.hashCode());
        System.out.println("publisherOffer created publisherEndpoint: " + session.publisherEndpoint);
        if (session == null || session.publisherEndpoint == null) {
            // 先缓存
            sessions.computeIfAbsent(sessionId, id -> new Session())
                    .publisherCandidates.add(ice);
        } else {
            session.publisherEndpoint.addIceCandidate(ice);
        }
    }

    @PostMapping("/stop")
    public void stopPublisher(@RequestBody Map<String, Object> payload) {
        String sessionId = getSessionId(payload);

        Session session = sessions.remove(sessionId);
        if (session != null) {
            try {
                if (session.publisherEndpoint != null) {
                    session.publisherEndpoint.release();
                    System.out.println("Publisher endpoint released.");
                }
                if (session.subscriberEndpoint != null) {
                    session.subscriberEndpoint.release();
                    System.out.println("Subscriber endpoint released.");
                }
                if (session.pipeline != null) {
                    session.pipeline.release();
                    System.out.println("Pipeline released.");
                }
            } catch (Exception e) {
                System.err.println("Error releasing resources: " + e.getMessage());
            }
        } else {
            System.out.println("No active session found: " + sessionId);
        }
    }


    @PostMapping("/offer/subscriber")
    public Map<String, Object> subscriberOffer(@RequestBody Map<String, Object> payload) {
        String sessionId = getSessionId(payload);
        String sdpOffer = (String) payload.get("sdp");

        Session session = sessions.get(sessionId);
        if (session == null || session.publisherEndpoint == null) {
            throw new IllegalStateException("Publisher must be initialized first");
        }

        session.subscriberEndpoint = new WebRtcEndpoint.Builder(session.pipeline).build();
        session.subscriberEndpoint.setStunServerAddress("stun.l.google.com");
        session.subscriberEndpoint.setStunServerPort(19302);
        session.publisherEndpoint.connect(session.subscriberEndpoint);

        // 添加之前收到的ICE
        for (IceCandidate ice : session.subscriberCandidates) {
            session.subscriberEndpoint.addIceCandidate(ice);
        }
        session.subscriberCandidates.clear();

        String sdpAnswer = session.subscriberEndpoint.processOffer(sdpOffer);
        session.subscriberEndpoint.gatherCandidates();

        return Map.of("sdp", sdpAnswer);
    }

    @PostMapping("/candidate/subscriber")
    public void subscriberCandidate(@RequestBody Map<String, Object> c) {
        String sessionId = getSessionId(c);

        String candidateStr = (String) c.get("candidate");
        String sdpMid = (String) c.get("sdpMid");
        Object sdpMLineIndexObj = c.get("sdpMLineIndex");

        // end-of-candidates 情况
        if (candidateStr == null) {
            System.out.println("收到 end-of-candidates: " + c);
            return;
        }

        // sdpMLineIndex 允许为空，默认设0
        int sdpMLineIndex = 0;
        if (sdpMLineIndexObj instanceof Number) {
            sdpMLineIndex = ((Number) sdpMLineIndexObj).intValue();
        } else {
            System.out.println("sdpMLineIndex 缺失，默认设为0: " + c);
        }

        IceCandidate ice = new IceCandidate(
                candidateStr,
                sdpMid,
                sdpMLineIndex
        );

        Session session = sessions.get(sessionId);
        if (session == null || session.subscriberEndpoint == null) {
            System.out.println("缓存候选者: " + ice.getCandidate());
            sessions.computeIfAbsent(sessionId, id -> new Session())
                    .subscriberCandidates.add(ice);
        } else {
            System.out.println("添加候选者到 subscriberEndpoint: " + ice.getCandidate());
            session.subscriberEndpoint.addIceCandidate(ice);
        }
    }

    @PostMapping("/answer/subscriber")
    public void processSubscriberAnswer(@RequestBody Map<String, Object> req) {
        String sessionId = getSessionId(req);
        String sdpAnswer = (String) req.get("sdp");

        Session session = sessions.get(sessionId);
        if (session == null || session.subscriberEndpoint == null) {
            throw new IllegalStateException("Subscriber must be initialized first");
        }
        session.subscriberEndpoint.processAnswer(sdpAnswer);
    }

}

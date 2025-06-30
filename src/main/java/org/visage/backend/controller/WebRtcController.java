package org.visage.backend.controller;

import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.bind.annotation.*;
import org.visage.backend.exception.ServiceException;

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

    private void releaseSession(Session session) {
        if (session != null) {
            try {
                // 释放 publisherEndpoint
                if (session.publisherEndpoint != null) {
                    session.publisherEndpoint.release();
                }
                // 释放 subscriberEndpoint
                if (session.subscriberEndpoint != null) {
                    session.subscriberEndpoint.release();
                }
                // 释放管道
                if (session.pipeline != null) {
                    session.pipeline.release();
                }
            } catch (Exception e) {
                throw new ServiceException("释放资源时出错: " + e.getMessage());
            }
        }
    }

    // 提供简单的方式生成 sessionId（生产可换JWT）
    private String getSessionId(Map<String, Object> payload) {
        Object sid = payload.get("sessionId");
        if (sid == null) throw new ServiceException("sessionId 不能为空");
        return sid.toString();
    }

    @PostMapping("/offer/publisher")
    public Map<String, Object> publisherOffer(@RequestBody Map<String, Object> payload) {
        String sessionId = getSessionId(payload);
        String sdpOffer = (String) payload.get("sdp");

        // 如果之前有 Session，先全部释放
        Session oldSession = sessions.remove(sessionId);
        this.releaseSession(oldSession);

        // 新建一个 Session
        Session session = new Session();
        sessions.put(sessionId, session);

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
        if (session == null || session.publisherEndpoint == null) {
            // 如果 publisherEndpoint 还未创建，先缓存候选者
            sessions.computeIfAbsent(sessionId, id -> new Session())
                    .publisherCandidates.add(ice);
        } else {
            // 如果已创建，直接添加候选者
            session.publisherEndpoint.addIceCandidate(ice);
        }
    }

    @PostMapping("/stop")
    public void stopPublisher(@RequestBody Map<String, Object> payload) {
        String sessionId = getSessionId(payload);
        Session session = sessions.remove(sessionId);
        this.releaseSession(session);
    }


    @PostMapping("/offer/subscriber")
    public Map<String, Object> subscriberOffer(@RequestBody Map<String, Object> payload) {
        String sessionId = getSessionId(payload);
        String sdpOffer = (String) payload.get("sdp");

        Session session = sessions.get(sessionId);
        if (session == null || session.publisherEndpoint == null) {
            throw new ServiceException("当前用户不在线");
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
            return;
        }

        // sdpMLineIndex 允许为空，默认设0
        int sdpMLineIndex = 0;
        if (sdpMLineIndexObj instanceof Number) {
            sdpMLineIndex = ((Number) sdpMLineIndexObj).intValue();
        }

        IceCandidate ice = new IceCandidate(
                candidateStr,
                sdpMid,
                sdpMLineIndex
        );

        Session session = sessions.get(sessionId);
        if (session == null || session.subscriberEndpoint == null) {
            // 如果 subscriberEndpoint 还未创建，先缓存候选者
            sessions.computeIfAbsent(sessionId, id -> new Session())
                    .subscriberCandidates.add(ice);
        } else {
            // 如果已创建，直接添加候选者
            session.subscriberEndpoint.addIceCandidate(ice);
        }
    }

    @PostMapping("/answer/subscriber")
    public void processSubscriberAnswer(@RequestBody Map<String, Object> req) {
        String sessionId = getSessionId(req);
        String sdpAnswer = (String) req.get("sdp");

        Session session = sessions.get(sessionId);
        if (session == null || session.subscriberEndpoint == null) {
            throw new ServiceException("Subscriber 未初始化或已停止");
        }
        session.subscriberEndpoint.processAnswer(sdpAnswer);
    }

}

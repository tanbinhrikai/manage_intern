package com.rikai.backend.ai.service.roadmap;

import com.rikai.backend.dto.request.agent_ai.DraftRoadmap;
import com.rikai.backend.model.Enum.NodeType;
import com.rikai.backend.model.RoadmapNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages draft roadmaps in memory before user confirmation.
 * Drafts are stored with TTL and automatically cleaned up.
 */
@Service
@Slf4j
public class DraftRoadmapManager {

    private static final long DRAFT_TTL_MINUTES = 30;

    private final Map<String, DraftRoadmap> drafts = new ConcurrentHashMap<>();

    /**
     * Creates a new draft session and stores the roadmap outline.
     */
    public String createDraft(RoadmapNode rootNode, Long positionId, Long batchId, String duration) {
        String sessionId = UUID.randomUUID().toString();
        DraftRoadmap draft = DraftRoadmap.builder()
                .sessionId(sessionId)
                .rootNode(rootNode)
                .positionId(positionId)
                .batchId(batchId)
                .duration(duration)
                .lastModified(Instant.now())
                .build();
        drafts.put(sessionId, draft);
        log.info("Created draft roadmap with sessionId: {} (Total drafts: {})", sessionId, drafts.size());
        log.debug("Draft details - PositionId: {}, BatchId: {}, Duration: {}", positionId, batchId, duration);
        return sessionId;
    }

    /**
     * Retrieves a draft by session ID.
     */
    public DraftRoadmap getDraft(String sessionId) {
        DraftRoadmap draft = drafts.get(sessionId);
        if (draft == null) {
            throw new RuntimeException("Draft does not exist or has expired. SessionId: " + sessionId);
        }
        return draft;
    }

    /**
     * Updates an existing draft with expanded content.
     */
    public void updateDraft(String sessionId, RoadmapNode updatedRootNode) {
        DraftRoadmap draft = getDraft(sessionId);
        draft.setRootNode(updatedRootNode);
        draft.setLastModified(Instant.now());
        log.info("Updated draft roadmap: {}", sessionId);
    }

    /**
     * Removes a draft after confirmation or cancellation.
     */
    public void removeDraft(String sessionId) {
        drafts.remove(sessionId);
        log.info("Removed draft roadmap: {}", sessionId);
    }

    /**
     * Checks if a draft exists and is still valid.
     */
    public boolean exists(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.debug("SessionId is null or empty");
            return false;
        }
        boolean exists = drafts.containsKey(sessionId);
        log.debug("Checking draft existence for sessionId: {} - Result: {}", sessionId, exists);
        if (!exists) {
            log.warn("Draft not found for sessionId: {}. Current drafts count: {}", sessionId, drafts.size());
            if (!drafts.isEmpty()) {
                log.debug("Available sessionIds: {}", drafts.keySet());
            }
        }
        return exists;
    }

    /**
     * Returns all active drafts (for debugging).
     */
    public Map<String, DraftInfo> getAllDrafts() {
        Map<String, DraftInfo> result = new java.util.HashMap<>();
        for (Map.Entry<String, DraftRoadmap> entry : drafts.entrySet()) {
            DraftRoadmap draft = entry.getValue();
            result.put(entry.getKey(), new DraftInfo(
                    draft.getSessionId(),
                    draft.getPositionId(),
                    draft.getBatchId(),
                    draft.getDuration(),
                    draft.getLastModified().toString(),
                    draft.getRootNode() != null ? draft.getRootNode().getTitle() : "N/A"
            ));
        }
        return result;
    }

    public int removeExpiredDrafts(Duration duration) {
        Instant expiryThreshold = Instant.now().minus(duration);
        AtomicInteger removed = new AtomicInteger();

        drafts.entrySet().removeIf(entry -> {
            if (entry.getValue().getLastModified().isBefore(expiryThreshold)) {
                removed.getAndIncrement();
                return true;
            }
            return false;
        });

        if (removed.get() > 0) {
            log.info("Removed {} expired draft roadmaps older than {}", removed, duration);
        }
        return removed.get();
    }

    /**
     * DTO for draft info (for debugging).
     */
    public record DraftInfo(String sessionId, Long positionId, Long batchId, String duration, String lastModified,
                            String rootTitle) {
    }

    /**
     * Scheduled cleanup: Remove drafts older than TTL.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void cleanupExpiredDrafts() {
        Instant expiryThreshold = Instant.now().minusSeconds(DRAFT_TTL_MINUTES * 60);
        AtomicInteger removed = new AtomicInteger();

        drafts.entrySet().removeIf(entry -> {
            if (entry.getValue().getLastModified().isBefore(expiryThreshold)) {
                removed.getAndIncrement();
                return true;
            }
            return false;
        });

        if (removed.get() > 0) {
            log.info("Cleaned up {} expired draft roadmaps", removed);
        }
    }

    /**
     * Use the reduced tree structure (Skeleton) to send to the AI.
     */
    public String getSkeletonStructure(String sessionId) {
        DraftRoadmap draft = getDraft(sessionId);
        StringBuilder sb = new StringBuilder();
        buildSkeletonRecursively(draft.getRootNode(), sb, 0);
        return sb.toString();
    }

    private void buildSkeletonRecursively(RoadmapNode node, StringBuilder sb, int level) {
        String indent = "  ".repeat(level);
        sb.append(indent)
                .append("- ")
                .append(node.getNodeType())
                .append(": ")
                .append(node.getTitle())
                .append("\n");

        if (node.getChildren() != null) {
            for (RoadmapNode child : node.getChildren()) {
                buildSkeletonRecursively(child, sb, level + 1);
            }
        }
    }

    /**
     * Add a new node to the parent node with the closest matching name.
     */
    public boolean addNodeByFuzzyName(String sessionId, String parentFuzzyName, String newTitle, String nodeTypeStr, String description) {
        DraftRoadmap draft = getDraft(sessionId);
        RoadmapNode root = draft.getRootNode();

        // 1. Tìm node cha
        RoadmapNode parentNode = findNodeByFuzzyName(root, parentFuzzyName);
        if (parentNode == null) {
            log.warn("Cannot find parent node with name like: {}", parentFuzzyName);
            return false;
        }
        RoadmapNode newNode = RoadmapNode.builder()
                .title(newTitle)
                .description(description)
                .nodeType(NodeType.valueOf(nodeTypeStr.toUpperCase()))
                .parent(parentNode)
                .children(new ArrayList<>())
                .build();
        if (parentNode.getChildren() == null) {
            parentNode.setChildren(new ArrayList<>());
        }
        parentNode.getChildren().add(newNode);
        draft.setLastModified(Instant.now());
        return true;
    }

    /**
     * Delete the node with the closest matching name.
     */
    public boolean removeNodeByFuzzyName(String sessionId, String nodeFuzzyName) {
        DraftRoadmap draft = getDraft(sessionId);
        RoadmapNode root = draft.getRootNode();
        boolean removed = removeNodeRecursively(root, nodeFuzzyName);
        if (removed) {
            draft.setLastModified(Instant.now());
        }
        return removed;
    }

    /**
     * Update the title and/or description of the node with the closest matching name.
     */
    public boolean updateNodeByFuzzyName(String sessionId, String nodeFuzzyName,
                                         String newTitle, String newDescription) {
        DraftRoadmap draft = getDraft(sessionId);
        RoadmapNode root = draft.getRootNode();
        RoadmapNode target = findNodeByFuzzyName(root, nodeFuzzyName);
        if (target == null) {
            log.warn("Cannot find node with name like: {}", nodeFuzzyName);
            return false;
        }
        if (newTitle != null && !newTitle.isBlank()) {
            target.setTitle(newTitle);
        }
        if (newDescription != null && !newDescription.isBlank()) {
            target.setDescription(newDescription);
        }
        draft.setLastModified(Instant.now());
        return true;
    }


    /**
     * Find the node whose name contains the keyword (Case insensitive)
     */
    private RoadmapNode findNodeByFuzzyName(RoadmapNode current, String keyword) {
        if (current.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
            return current;
        }
        if (current.getChildren() != null) {
            for (RoadmapNode child : current.getChildren()) {
                RoadmapNode found = findNodeByFuzzyName(child, keyword);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Recursively remove the node whose name contains the keyword.
     */
    private boolean removeNodeRecursively(RoadmapNode parent, String keyword) {
        if (parent.getChildren() == null) return false;
        Iterator<RoadmapNode> iterator = parent.getChildren().iterator();
        while (iterator.hasNext()) {
            RoadmapNode child = iterator.next();
            if (child.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                iterator.remove();
                return true;
            }
            if (removeNodeRecursively(child, keyword)) {
                return true;
            }
        }
        return false;
    }
}

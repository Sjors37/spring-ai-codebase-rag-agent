package com.sjors37.spring_ai_codebase_rag_agent.controller;

import com.sjors37.spring_ai_codebase_rag_agent.agents.PlannerAgent;
import com.sjors37.spring_ai_codebase_rag_agent.dto.ChatRequest;
import com.sjors37.spring_ai_codebase_rag_agent.dto.ChatResponse;
import com.sjors37.spring_ai_codebase_rag_agent.dto.IngestRequest;
import com.sjors37.spring_ai_codebase_rag_agent.dto.IngestResponse;
import com.sjors37.spring_ai_codebase_rag_agent.ingestion.CodebaseIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final PlannerAgent plannerAgent;
    private final CodebaseIngestionService codebaseIngestionService;

    @PostMapping("/ingest")
    public IngestResponse ingest(@RequestBody IngestRequest request) {
        try {
            int documentsIndexed = codebaseIngestionService.ingest(request.source());
            return new IngestResponse(true, "Indexed successfully.", documentsIndexed);
        } catch (Exception e) {
            return new IngestResponse(false, "Failed to ingest: " + e.getMessage(), 0);
        }
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return new ChatResponse(plannerAgent.handle(request.message()));
    }
}

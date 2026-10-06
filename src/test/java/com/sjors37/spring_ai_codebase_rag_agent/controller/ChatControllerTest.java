package com.sjors37.spring_ai_codebase_rag_agent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjors37.spring_ai_codebase_rag_agent.agents.PlannerAgent;
import com.sjors37.spring_ai_codebase_rag_agent.dto.ChatRequest;
import com.sjors37.spring_ai_codebase_rag_agent.dto.IngestRequest;
import com.sjors37.spring_ai_codebase_rag_agent.ingestion.CodebaseIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PlannerAgent plannerAgent;

    @MockitoBean
    private CodebaseIngestionService codebaseIngestionService;

    @Test
    void chat_returnsReply_fromPlannerAgent() throws Exception {
        when(plannerAgent.handle("How does X work?")).thenReturn("X works by...");

        mockMvc.perform(post("/chat")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new ChatRequest("How does X work?"))))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"reply": "X works by..."}
                        """));
    }

    @Test
    void ingest_returnsSuccessResponse_whenIngestionSucceeds() throws Exception {
        when(codebaseIngestionService.ingest("/some/path")).thenReturn(42);

        mockMvc.perform(post("/ingest")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new IngestRequest("/some/path"))))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"success": true, "message": "Indexed successfully.", "documentsIndexed": 42}
                        """));
    }

    @Test
    void ingest_returnsFailureResponse_whenIngestionThrows() throws Exception {
        when(codebaseIngestionService.ingest("/bad/path"))
                .thenThrow(new RuntimeException("Path does not exist"));

        mockMvc.perform(post("/ingest")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new IngestRequest("/bad/path"))))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"success": false, "documentsIndexed": 0}
                        """));
    }
}
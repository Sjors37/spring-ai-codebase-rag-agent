package com.sjors37.spring_ai_codebase_rag_agent.agents;

import com.sjors37.spring_ai_codebase_rag_agent.tools.RetrievalTool;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class RetrievalAgent {

    private static final String SYSTEM_PROMPT = """
            You are a retrieval specialist. Your only job is to search the indexed codebase
            using the search tool and return the most relevant passages for the given query.
            Do not answer questions yourself — only retrieve and relay relevant information.
            If nothing relevant is found, say so clearly.
            """;

    private final ChatClient chatClient;

    public RetrievalAgent(AnthropicChatModel chatModel, RetrievalTool retrievalTool) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(retrievalTool)
                .build();
    }

    public String retrieve(String query) {
        return chatClient.prompt().user(query).call().content();
    }
}
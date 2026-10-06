package com.sjors37.spring_ai_codebase_rag_agent.agents;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class AnswerAgent {

    private static final String SYSTEM_PROMPT = """
            You answer questions about a codebase using ONLY the provided context passages.
            Do not use outside knowledge or guess about code that isn't shown to you.
            If the context doesn't contain enough information to answer confidently, say so explicitly
            rather than filling gaps with assumptions.
            Cite which file(s) your answer is based on.
            """;

    private final ChatClient chatClient;

    public AnswerAgent(AnthropicChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public String answer(String question, String retrievedContext) {
        String prompt = "Question: %s\n\nContext:\n%s".formatted(question, retrievedContext);
        return chatClient.prompt().user(prompt).call().content();
    }
}
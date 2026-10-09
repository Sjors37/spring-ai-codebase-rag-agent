package com.sjors37.spring_ai_codebase_rag_agent.agents;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ValidationAgent {

    private static final String SYSTEM_PROMPT = """
            You validate whether an answer is genuinely supported by the given context passages.
            Respond with exactly one word first: GROUNDED or UNGROUNDED.
            Then, on a new line, briefly explain why.
            An answer is UNGROUNDED if it makes claims not present in the context,
            or if it answers confidently despite the context being insufficient.
            """;

    private final ChatClient chatClient;

    public ValidationAgent(AnthropicChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public boolean isGrounded(String answer, String retrievedContext) {
        String prompt = "Answer to validate:\n%s\n\nContext it should be based on:\n%s"
                .formatted(answer, retrievedContext);
        String result = chatClient.prompt().user(prompt).call().content();
        return result != null && result.strip().toUpperCase(Locale.ROOT).startsWith("GROUNDED");
    }
}
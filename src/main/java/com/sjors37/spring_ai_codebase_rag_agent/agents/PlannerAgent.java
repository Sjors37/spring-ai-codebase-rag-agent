package com.sjors37.spring_ai_codebase_rag_agent.agents;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlannerAgent {

    private static final int MAX_RETRIEVAL_ATTEMPTS = 2;
    private static final String UNVERIFIED_NOTE =
            "\n\n(Note: this answer could not be fully verified against the indexed codebase.)";
    private static final String RETRY_HINT =
            " (previous search was insufficient, try a different angle or broader terms)";

    private final RetrievalAgent retrievalAgent;
    private final AnswerAgent answerAgent;
    private final ValidationAgent validationAgent;

    public String handle(String question) {
        String query = question;
        String answer = "";

        for (int attempt = 1; attempt <= MAX_RETRIEVAL_ATTEMPTS; attempt++) {
            String context = retrievalAgent.retrieve(query);
            answer = answerAgent.answer(question, context);

            if (validationAgent.isGrounded(answer, context)) {
                return answer;
            }
            query = question + RETRY_HINT;
        }

        return answer + UNVERIFIED_NOTE;
    }
}

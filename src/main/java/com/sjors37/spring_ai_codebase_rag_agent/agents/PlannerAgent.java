package com.sjors37.spring_ai_codebase_rag_agent.agents;

import org.springframework.stereotype.Component;

@Component
public class PlannerAgent {

    private static final int MAX_RETRIEVAL_ATTEMPTS = 2;

    private final RetrievalAgent retrievalAgent;
    private final AnswerAgent answerAgent;
    private final ValidationAgent validationAgent;

    public PlannerAgent(RetrievalAgent retrievalAgent, AnswerAgent answerAgent, ValidationAgent validationAgent) {
        this.retrievalAgent = retrievalAgent;
        this.answerAgent = answerAgent;
        this.validationAgent = validationAgent;
    }

    public String handle(String question) {
        String query = question;

        for (int attempt = 1; attempt <= MAX_RETRIEVAL_ATTEMPTS; attempt++) {
            String context = retrievalAgent.retrieve(query);
            String answer = answerAgent.answer(question, context);

            boolean grounded = validationAgent.isGrounded(answer, context);

            if (grounded || attempt == MAX_RETRIEVAL_ATTEMPTS) {
                return grounded
                        ? answer
                        : answer + "\n\n(Note: this answer could not be fully verified against the indexed codebase.)";
            }

            query = question + " (previous search was insufficient, try a different angle or broader terms)";
        }

        return "Unable to produce a grounded answer.";
    }
}
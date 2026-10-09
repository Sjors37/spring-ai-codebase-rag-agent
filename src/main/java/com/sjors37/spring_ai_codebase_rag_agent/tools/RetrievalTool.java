package com.sjors37.spring_ai_codebase_rag_agent.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RetrievalTool {

    private static final String PASSAGE_SEPARATOR = "\n\n---\n\n";

    private final VectorStore vectorStore;

    @Tool(description = "Searches the indexed codebase for passages relevant to a query. " +
            "Returns matching code/doc snippets along with their file path. " +
            "Use this whenever you need information from the actual codebase rather than guessing.")
    public String search(
            @ToolParam(description = "The search query describing what information is needed") String query
    ) {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(5).build()
        );

        if (results.isEmpty()) {
            return "No relevant passages found in the indexed codebase for: " + query;
        }

        return results.stream()
                .map(doc -> "[%s]\n%s".formatted(doc.getMetadata().get("filePath"), doc.getText()))
                .collect(Collectors.joining(PASSAGE_SEPARATOR));
    }
}

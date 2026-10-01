package com.sjors37.spring_ai_codebase_rag_agent.ingestion;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextChunker {

    private static final int CHUNK_SIZE_CHARS = 1500;
    private static final int OVERLAP_CHARS = 200;

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();

        if (text.length() <= CHUNK_SIZE_CHARS) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE_CHARS, text.length());
            chunks.add(text.substring(start, end));

            if (end == text.length()) {
                break;
            }
            start = end - OVERLAP_CHARS;
        }

        return chunks;
    }
}
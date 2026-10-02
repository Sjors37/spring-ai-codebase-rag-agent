package com.sjors37.spring_ai_codebase_rag_agent.ingestion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextChunkerTest {

    private final TextChunker textChunker = new TextChunker();

    @Test
    void chunk_returnsSingleChunk_whenTextIsShort() {
        String text = "This is a short piece of text.";

        List<String> result = textChunker.chunk(text);

        assertThat(result).containsExactly(text);
    }

    @Test
    void chunk_splitsLongTextIntoMultipleChunks() {
        String text = "a".repeat(4000);

        List<String> result = textChunker.chunk(text);

        assertThat(result.size()).isGreaterThan(1);
    }

    @Test
    void chunk_consecutiveChunksOverlap() {
        String text = "a".repeat(1500) + "b".repeat(1500);

        List<String> result = textChunker.chunk(text);

        String firstChunkEnd = result.get(0).substring(result.get(0).length() - 200);
        String secondChunkStart = result.get(1).substring(0, 200);

        assertThat(firstChunkEnd).isEqualTo(secondChunkStart);
    }

    @Test
    void chunk_coversEntireOriginalText() {
        String text = "a".repeat(3000);

        List<String> result = textChunker.chunk(text);

        String lastChunk = result.getLast();
        assertThat(text.endsWith(lastChunk.substring(lastChunk.length() - 10))).isTrue();
    }
}
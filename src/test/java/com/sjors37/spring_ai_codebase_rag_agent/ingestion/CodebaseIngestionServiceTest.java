package com.sjors37.spring_ai_codebase_rag_agent.ingestion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodebaseIngestionServiceTest {

    @Mock
    private GitRepositoryCloner gitRepositoryCloner;
    @Mock
    private CodeFileReader codeFileReader;
    @Mock
    private TextChunker textChunker;
    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private CodebaseIngestionService codebaseIngestionService;

    @Test
    void ingest_createsDocumentsWithCorrectMetadata() throws Exception {
        Path repoRoot = Path.of("/fake/repo");
        CodeFileReader.CodeFile file = new CodeFileReader.CodeFile(
                repoRoot.resolve("Main.java"), "Main.java", "public class Main {}");

        when(gitRepositoryCloner.prepareLocalPath("some-source")).thenReturn(repoRoot);
        when(codeFileReader.readRelevantFiles(repoRoot)).thenReturn(List.of(file));
        when(textChunker.chunk("public class Main {}")).thenReturn(List.of("chunk1", "chunk2"));

        int result = codebaseIngestionService.ingest("some-source");

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());

        List<Document> documents = captor.getValue();
        assertThat(documents).hasSize(2);
        assertThat(documents.get(0).getMetadata()).containsEntry("filePath", "Main.java").containsEntry("chunkIndex", 0);
        assertThat(documents.get(1).getMetadata()).containsEntry("filePath", "Main.java").containsEntry("chunkIndex", 1);
        assertThat(result).isEqualTo(2);
    }

    @Test
    void ingest_handlesMultipleFiles() throws Exception {
        Path repoRoot = Path.of("/fake/repo");
        CodeFileReader.CodeFile file1 = new CodeFileReader.CodeFile(repoRoot.resolve("A.java"), "A.java", "content A");
        CodeFileReader.CodeFile file2 = new CodeFileReader.CodeFile(repoRoot.resolve("B.java"), "B.java", "content B");

        when(gitRepositoryCloner.prepareLocalPath("some-source")).thenReturn(repoRoot);
        when(codeFileReader.readRelevantFiles(repoRoot)).thenReturn(List.of(file1, file2));
        when(textChunker.chunk("content A")).thenReturn(List.of("chunk A"));
        when(textChunker.chunk("content B")).thenReturn(List.of("chunk B"));

        int result = codebaseIngestionService.ingest("some-source");

        assertThat(result).isEqualTo(2);
    }
}
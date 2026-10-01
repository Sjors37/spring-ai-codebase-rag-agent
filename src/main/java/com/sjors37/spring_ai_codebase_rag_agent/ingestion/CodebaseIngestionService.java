package com.sjors37.spring_ai_codebase_rag_agent.ingestion;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CodebaseIngestionService {

    private final GitRepositoryCloner gitRepositoryCloner;
    private final CodeFileReader codeFileReader;
    private final TextChunker textChunker;
    private final VectorStore vectorStore;

    public CodebaseIngestionService(GitRepositoryCloner gitRepositoryCloner,
                                    CodeFileReader codeFileReader,
                                    TextChunker textChunker,
                                    VectorStore vectorStore) {
        this.gitRepositoryCloner = gitRepositoryCloner;
        this.codeFileReader = codeFileReader;
        this.textChunker = textChunker;
        this.vectorStore = vectorStore;
    }

    public int ingest(String source) throws IOException, org.eclipse.jgit.api.errors.GitAPIException {
        Path repoRoot = gitRepositoryCloner.prepareLocalPath(source);
        List<CodeFileReader.CodeFile> files = codeFileReader.readRelevantFiles(repoRoot);

        List<Document> documents = new ArrayList<>();
        for (CodeFileReader.CodeFile file : files) {
            List<String> chunks = textChunker.chunk(file.content());
            for (int i = 0; i < chunks.size(); i++) {
                documents.add(new Document(
                        chunks.get(i),
                        Map.of("filePath", file.relativePath(), "chunkIndex", i)
                ));
            }
        }

        vectorStore.add(documents);
        return documents.size();
    }
}
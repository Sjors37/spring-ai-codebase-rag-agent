package com.sjors37.spring_ai_codebase_rag_agent.ingestion;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class GitRepositoryCloner {

    public Path prepareLocalPath(String source) throws IOException, GitAPIException {
        return isRemoteUrl(source) ? cloneToTempDir(source) : requireLocalDirectory(source);
    }

    private Path cloneToTempDir(String url) throws IOException, GitAPIException {
        Path tempDir = Files.createTempDirectory("codebase-rag-");
        try (Git ignored = Git.cloneRepository()
                .setURI(url)
                .setDirectory(tempDir.toFile())
                .call()) {
            return tempDir;
        }
    }

    private Path requireLocalDirectory(String source) throws IOException {
        Path localDir = Path.of(source);
        if (!Files.isDirectory(localDir)) {
            throw new IOException("Path does not exist or is not a directory: " + source);
        }
        return localDir;
    }

    private boolean isRemoteUrl(String source) {
        return source.startsWith("http://") || source.startsWith("https://") || source.endsWith(".git");
    }
}

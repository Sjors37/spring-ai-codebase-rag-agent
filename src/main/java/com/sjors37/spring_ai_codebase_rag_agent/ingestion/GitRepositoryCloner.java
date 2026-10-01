package com.sjors37.spring_ai_codebase_rag_agent.ingestion;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class GitRepositoryCloner {

    public Path prepareLocalPath(String source) throws IOException, org.eclipse.jgit.api.errors.GitAPIException {
        if (isRemoteUrl(source)) {
            Path tempDir = Files.createTempDirectory("codebase-rag-");
            try (Git ignored = Git.cloneRepository()
                    .setURI(source)
                    .setDirectory(tempDir.toFile())
                    .call()) {
                return tempDir;
            }
        }

        File localDir = new File(source);
        if (!localDir.exists() || !localDir.isDirectory()) {
            throw new IOException("Path does not exist or is not a directory: " + source);
        }
        return localDir.toPath();
    }

    private boolean isRemoteUrl(String source) {
        return source.startsWith("http://") || source.startsWith("https://") || source.endsWith(".git");
    }
}
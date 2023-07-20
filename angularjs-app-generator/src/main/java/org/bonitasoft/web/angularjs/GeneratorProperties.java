package org.bonitasoft.web.angularjs;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.createDirectories;


public class GeneratorProperties {
    public static final String EXTRACT_BACKEND_RESOURCES = "META-INF/resources";

    private String workspaceName;
    private final String i18NResourcesName;

    public GeneratorProperties(String workspaceName ,String i18nResourcesName){
        this.workspaceName = workspaceName;
        this.i18NResourcesName = i18nResourcesName;
    }

    public static final String FRAGMENTS = "fragments";
    public static final String TEMPLATES_RESOURCES = "templates";
    private static final String PAGES_DEFAULT_DIRECTORY = "pages";

    private boolean liveBuildEnabled = true;

    private Path path = Path.of(System.getProperty("java.io.tmpdir")).resolve(this.workspaceName);

    private Path extractPath = getPath().resolve("extract");

    public Path getTmpFragmentsRepositoryPath() {
        return getPath().resolve(FRAGMENTS);
    }

    public Path getTmpPagesRepositoryPath() {
        return getPath().resolve(PAGES_DEFAULT_DIRECTORY);
    }

    public Path getTmpI18nPath() throws IOException {
        return createDirectories(getPath().resolve(this.i18NResourcesName));
    }

    public Path getExportBackendResourcesPath() {
        return getExtractPath().resolve(EXTRACT_BACKEND_RESOURCES).resolve("runtime");
    }

    public Path getTemplateResourcesPath() {
        return getExtractPath().resolve(TEMPLATES_RESOURCES);
    }

    public Path getPath() {
        return path;
    }

    public Path getExtractPath() {
        return extractPath;
    }

    public boolean isLiveBuildEnabled() {
        return liveBuildEnabled;
    }
}


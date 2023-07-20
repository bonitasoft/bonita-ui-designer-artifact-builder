/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.angularjs.rendering;

import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.web.dao.JsonHandler;
import org.bonitasoft.web.dao.model.fragment.Fragment;
import org.bonitasoft.web.dao.model.page.Previewable;
import org.bonitasoft.web.dao.repository.FragmentRepository;
import org.bonitasoft.web.dao.visitor.ElementVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Benjamin Parisel
 */
public class DirectivesCollector {

    public static final String JS_FOLDER = "js";

    private static final Logger logger = LoggerFactory.getLogger(DirectivesCollector.class);
    private final JsonHandler jsonHandler;

    private final FragmentRepository fragmentRepository;
    private final ElementVisitor<Set<String>> fragmentIdVisitor;
    private final Path tmpFragmentsRepositoryPath;
    private final Path tmpPagesRepositoryPath;
    private final DirectiveFileGenerator directiveFileGenerator;

    public DirectivesCollector(JsonHandler jsonHandler,
            Path tmpPagesRepositoryPath,
            Path tmpFragmentsRepositoryPath,
            DirectiveFileGenerator directiveFileGenerator,
            ElementVisitor<Set<String>> fragmentIdVisitor,
            FragmentRepository fragmentRepository) {
        this.jsonHandler = jsonHandler;
        this.tmpPagesRepositoryPath = tmpPagesRepositoryPath;
        this.tmpFragmentsRepositoryPath = tmpFragmentsRepositoryPath;
        this.directiveFileGenerator = directiveFileGenerator;
        this.fragmentRepository = fragmentRepository;
        this.fragmentIdVisitor = fragmentIdVisitor;
    }

    public List<String> buildUniqueDirectivesFiles(Previewable previewable, String pageId) {
        if (previewable instanceof Fragment) {
            var filename = directiveFileGenerator.generateAllDirectivesFilesInOne(previewable,
                    getDestinationFolderPath(tmpFragmentsRepositoryPath.resolve(pageId)));
            return List.of(filename);
        } else {
            var filename = directiveFileGenerator.generateAllDirectivesFilesInOne(previewable,
                    getDestinationFolderPath(
                            tmpPagesRepositoryPath.resolve(pageId).resolve(JS_FOLDER)));
            var directives = new ArrayList<String>();
            directives.add(JS_FOLDER + "/" + filename);
            directives.addAll(collectFragment(previewable));
            return directives;
        }
    }

    protected Path getDestinationFolderPath(Path path) {
        if (exists(path)) {
            return path;
        }
        try {
            return createDirectories(path);
        } catch (IOException e) {
            throw new GenerationException("Error while create directories " + path, e);
        }
    }

    private List<String> collectFragment(Previewable previewable) {
        return fragmentRepository.getByIds(fragmentIdVisitor.visit(previewable)).stream()
                .map(fragment -> format("fragments/%s/%s.js?hash=%s", fragment.getId(), fragment.getId(),
                        getHash(fragment)))
                .collect(Collectors.toList());
    }

    private String getHash(Fragment fragment) {
        try {
            byte[] content = this.jsonHandler.toJson(fragment);
            return DigestUtils.sha1Hex(content);
        } catch (Exception e) {
            logger.warn("Failure to generate hash for fragment " + fragment.getName(), e);
            return UUID.randomUUID().toString();
        }
    }
}

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

import static java.util.stream.Collectors.toList;
import static org.bonitasoft.web.designer.model.asset.Asset.getComparatorByComponentId;
import static org.bonitasoft.web.designer.model.asset.Asset.getComparatorByOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.web.angularjs.visitor.HtmlBuilderVisitor;
import org.bonitasoft.web.angularjs.visitor.RequiredModulesVisitor;
import org.bonitasoft.web.designer.common.generator.rendering.GenerationException;
import org.bonitasoft.web.designer.common.generator.rendering.IHtmlGenerator;
import org.bonitasoft.web.designer.common.repository.AssetRepository;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.common.visitor.AssetVisitor;
import org.bonitasoft.web.designer.common.visitor.PageFactory;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.widgets.Widget;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlGenerator implements IHtmlGenerator {

    private static final Logger logger = LoggerFactory.getLogger(HtmlBuilderVisitor.class);
    private final DirectivesCollector directivesCollector;
    private final RequiredModulesVisitor requiredModulesVisitor;
    private final AssetVisitor assetVisitor;
    private final HtmlBuilderVisitor htmlBuilderVisitor;
    private final AssetRepository<Widget> widgetAssetRepository;
    private final AssetRepository<Page> pageAssetRepository;

    private final List<PageFactory> pageFactories;

    public HtmlGenerator(HtmlBuilderVisitor htmlBuilderVisitor,
            DirectivesCollector directivesCollector,
            RequiredModulesVisitor requiredModulesVisitor,
            AssetVisitor assetVisitor, AssetRepository<Widget> widgetAssetRepository,
            AssetRepository<Page> pageAssetRepository, List<PageFactory> pageFactories) {
        this.htmlBuilderVisitor = htmlBuilderVisitor;
        this.directivesCollector = directivesCollector;
        this.requiredModulesVisitor = requiredModulesVisitor;
        this.assetVisitor = assetVisitor;
        this.widgetAssetRepository = widgetAssetRepository;
        this.pageAssetRepository = pageAssetRepository;
        this.pageFactories = pageFactories;
    }

    public <P extends Previewable & Identifiable> String generateHtml(P previewable)
            throws GenerationException {
        return generateHtml(previewable, "");
    }

    public <P extends Previewable & Identifiable> String generateHtml(P previewable, String resourceContext)
            throws GenerationException {
        try {
            return format(this.build(previewable, resourceContext));
        } catch (RepositoryException e) {
            throw new GenerationException("Error while generating page", e);
        }
    }

    /**
     * Build a previewable HTML, based on the given list of widgets
     * TODO: once resourceContext remove we can merge this method with HtmlBuilderVisitor#visit(Previewable)
     *
     * @param previewable to build
     * @param resourceContext the URL context can change on export or preview...
     */
    public <P extends Previewable & Identifiable> String build(final P previewable, String resourceContext) {
        var sortedAssets = getSortedAssets(previewable);
        var template = new TemplateEngine("page.hbs.html")
                .with("resourceContext", resourceContext == null ? "" : resourceContext)
                .with("directives",
                        this.directivesCollector.buildUniqueDirectivesFiles(previewable, previewable.getId()))
                .with("rowsHtml", htmlBuilderVisitor.build(previewable.getRows()))
                .with("jsAsset", getAssetHtmlSrcList(previewable.getId(), AssetType.JAVASCRIPT, sortedAssets))
                .with("cssAsset", getAssetHtmlSrcList(previewable.getId(), AssetType.CSS, sortedAssets))
                .with("factories",
                        this.pageFactories.stream().map(factory -> factory.generate(previewable)).collect(toList()));

        var modules = this.requiredModulesVisitor.visit(previewable);
        if (!modules.isEmpty()) {
            template = template.with("modules", modules);
        }
        return template.build(previewable);
    }

    /**
     * Return the list of the previewable assets sorted with only active assets
     */
    protected <P extends Previewable & Identifiable> List<Asset> getSortedAssets(P previewable) {
        return this.assetVisitor.visit(previewable).stream().filter(Asset::isActive)
                .sorted(getComparatorByComponentId().thenComparing(getComparatorByOrder()))
                .collect(Collectors.toList());
    }

    private List<String> getAssetHtmlSrcList(String previewableId, AssetType assetType, List<Asset> sortedAssets) {
        var assetsSrc = new ArrayList<String>();
        sortedAssets.stream()
                .filter(asset -> assetType.equals(asset.getType()))
                .forEach(asset -> {
                    var widgetPrefix = "";
                    if (asset.isExternal()) {
                        assetsSrc.add(asset.getName());
                    } else {
                        String assetHash;
                        if (AssetScope.WIDGET.equals(asset.getScope())) {
                            widgetPrefix = String.format("widgets/%s/", asset.getComponentId());
                            assetHash = getHash(asset, this.widgetAssetRepository, previewableId);
                        } else {
                            assetHash = getHash(asset, this.pageAssetRepository, previewableId);
                        }
                        if (!assetsSrc.contains(asset.getName())) {
                            assetsSrc.add(String.format("%sassets/%s/%s?hash=%s", widgetPrefix,
                                    asset.getType().getPrefix(), asset.getName(), assetHash));
                        }
                    }
                });
        return assetsSrc;
    }

    private String getHash(Asset asset, AssetRepository<?> assetRepository, String previewableId) {
        try {
            var content = asset.getComponentId() == null ? assetRepository.readAllBytes(previewableId, asset)
                    : assetRepository.readAllBytes(asset);
            return DigestUtils.sha1Hex(content);
        } catch (Exception e) {
            logger.warn("Failure to generate hash for asset {}", asset.getName(), e);
            return UUID.randomUUID().toString();
        }
    }

    private String format(String html) {
        Parser parser = Parser.htmlParser();
        parser.settings(new ParseSettings(true, true)); // tag, attribute preserve case
        return parser.parseInput(html, "").toString();
    }
}

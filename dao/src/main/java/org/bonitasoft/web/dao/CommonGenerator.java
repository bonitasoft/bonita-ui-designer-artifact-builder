package org.bonitasoft.web.dao;


import org.bonitasoft.web.dao.export.ExportStep;

public class CommonGenerator {
    public ExportStep[] getPageExportStep() {

        return new ExportStep[] {
                //new AssetExportStep(pageAssetRepository),
        };
    }
}

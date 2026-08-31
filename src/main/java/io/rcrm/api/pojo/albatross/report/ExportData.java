package io.rcrm.api.pojo.albatross.report;

import java.util.List;

public class ExportData {

    private List<String> exportHeader;
    private List<String> exportColumns;

    public List<String> getExportHeader() {
        return exportHeader;
    }

    public void setExportHeader(List<String> exportHeader) {
        this.exportHeader = exportHeader;
    }

    public List<String> getExportColumns() {
        return exportColumns;
    }

    public void setExportColumns(List<String> exportColumns) {
        this.exportColumns = exportColumns;
    }

}

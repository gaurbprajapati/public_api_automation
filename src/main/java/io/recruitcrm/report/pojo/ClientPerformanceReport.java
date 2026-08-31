package io.recruitcrm.report.pojo;

public class ClientPerformanceReport {
    private String[] company_slugs;
    private KpiLists[] kpi_lists;
    private long from_date;
    private long to_date;

    // Getters
    public String[] getCompany_slugs() {
        return company_slugs;
    }

    public KpiLists[] getKpi_lists() {
        return kpi_lists;
    }

    public long getFrom_date() {
        return from_date;
    }

    public long getTo_date() {
        return to_date;
    }

    // Setters
    public void setCompany_slugs(String[] company_slugs) {
        this.company_slugs = company_slugs;
    }

    public void setKpi_lists(KpiLists[] kpi_lists) {
        this.kpi_lists = kpi_lists;
    }

    public void setFrom_date(long from_date) {
        this.from_date = from_date;
    }

    public void setTo_date(long to_date) {
        this.to_date = to_date;
    }
}
package io.rcrm.api.pojo.ostrich;

public class GlobalSaveCommonProperties {
    private boolean visible = true;
    private int listPageOrder;
    private int detailPageOrder = 0;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getListPageOrder() {
        return listPageOrder;
    }

    public void setListPageOrder(int listPageOrder) {
        this.listPageOrder = listPageOrder;
    }

    public int getDetailPageOrder() {
        return detailPageOrder;
    }

    public void setDetailPageOrder(int detailPageOrder) {
        this.detailPageOrder = detailPageOrder;
    }
}

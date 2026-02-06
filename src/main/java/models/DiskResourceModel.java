package models;


public class DiskResourceModel {
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String path;

    public DiskResourceModel(String path) {
        this.path = path;
    }
}

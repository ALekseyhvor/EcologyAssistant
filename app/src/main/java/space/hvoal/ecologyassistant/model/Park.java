package space.hvoal.ecologyassistant.model;

public class Park {
    public final String id;
    public final String title;
    public final String description;
    public final double lat;
    public final double lng;

    public Park(String id, String title, String description, double lat, double lng) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
    }
}

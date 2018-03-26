import java.util.Collection;

public class GeoJsonFile {
    public String type;
    public String name;
    class CRS{
        public String type;
        class properties{
            public String name;
        }
    }
    public CRS crs;
    public Collection<GeoJsonFeature> features;
}

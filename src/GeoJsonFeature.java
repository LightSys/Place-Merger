import java.util.Collection;

public class GeoJsonFeature {
    public String type;
    class Properties{
        public int OBJECTID;
        public int osm_id;
        public short code;
        public String fclass;
        public int population;
        public String name;
        public double Shape_Length;
        public double Shape_Area;
    }
    public Properties properties;
    class Geometry{
        public String type;
        public Collection<Collection<Collection<Collection<Double>>>> coordinates;
    }
    public Geometry geometry;
}
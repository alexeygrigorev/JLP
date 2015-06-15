package jlp.pojos;


public class Identifier {

    private ComplexType type;
    private String name;
    private Location location;

    public static enum Location {
        LOCAL, FIELD, METHOD_PARAM;
    }

    public Identifier() {
    }

    public Identifier(ComplexType type, String name, Location location) {
        this.type = type;
        this.name = name;
        this.location = location;
    }


    public ComplexType getType() {
        return type;
    }

    public void setType(ComplexType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return type + " " + name + " (" + location + ")";
    }

}

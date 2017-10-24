package ga.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysapy on 30/01/16.
 */
public class Route {

    List<Link> links;
    int origin;
    int destiny;

    public int getDestiny() {
        return destiny;
    }

    public void setDestiny(int destiny) {
        this.destiny = destiny;
    }

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public Route(List<Link> links, int origin, int destiny) {
        this.links = links;
        this.origin = origin;
        this.destiny = destiny;
    }

    public Route() {
        this.links = new ArrayList<Link>();
        this.origin = 0;
        this.destiny = 0;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }
}

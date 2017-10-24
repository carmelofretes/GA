package ga.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysapy on 30/01/16.
 */
public class Link {

    int origin, destiny;
    List<Boolean> usedSlots;
    List<Integer> firstSlot;

    public List<Integer> getFirstSlot() {
        return firstSlot;
    }

    public void setFirstSlot(List<Integer> firstSlot) {
        this.firstSlot = firstSlot;
    }

    public List<Boolean> getUsedSlots() {
        return usedSlots;
    }

    public void setUsedSlots(List<Boolean> usedSlots) {
        this.usedSlots = usedSlots;
    }

    public Link(int origin, int destiny, List<Boolean> slots, List<Integer> firstSlots) {
        this.origin = origin;
        this.destiny = destiny;
        this.usedSlots = slots;
        this.firstSlot = firstSlots;
    }

    public Link() {
        this.origin = -1;
        this.destiny = -1;
        this.usedSlots = new ArrayList<Boolean>();
        this.firstSlot = new ArrayList<Integer>();
    }

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public int getDestiny() {
        return destiny;
    }

    public void setDestiny(int destiny) {
        this.destiny = destiny;
    }

}

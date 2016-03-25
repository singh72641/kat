package com.punwire.kat.spark;

import java.util.LinkedList;

/**
 * Created by kanwal on 22/03/16.
 */
public class LimitedQueue<E> extends LinkedList<E> {

    private int limit;

    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > limit) { super.remove(); }
        return true;
    }

    public void dump() {
        System.out.println("In the Limited Queue");
        forEach( val -> {
            System.out.println(val);
        });
    }

    public boolean isFull()
    {
        return size() == limit;
    }
}

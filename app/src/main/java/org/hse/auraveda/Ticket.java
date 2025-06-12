package org.hse.auraveda;

public class Ticket {
    private final int id;
    private final String name;

    public Ticket(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

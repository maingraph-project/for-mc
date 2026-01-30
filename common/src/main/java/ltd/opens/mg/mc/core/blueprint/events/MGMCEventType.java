package ltd.opens.mg.mc.core.blueprint.events;

import java.util.Objects;

/**
 * Event type definition for MGMC blueprint events.
 * Converted from enum to class to allow extensibility.
 */
public class MGMCEventType {
    private final String id;

    public MGMCEventType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MGMCEventType that = (MGMCEventType) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Standard Events
    public static final MGMCEventType PLAYER_JOIN = new MGMCEventType("player_join");
    public static final MGMCEventType PLAYER_LEAVE = new MGMCEventType("player_leave");
    public static final MGMCEventType PLAYER_RESPAWN = new MGMCEventType("player_respawn");
    public static final MGMCEventType PLAYER_DEATH = new MGMCEventType("player_death");
    public static final MGMCEventType PLAYER_ATTACK = new MGMCEventType("player_attack");
    public static final MGMCEventType PLAYER_HURT = new MGMCEventType("player_hurt");
    public static final MGMCEventType PLAYER_TICK = new MGMCEventType("player_tick");
    public static final MGMCEventType PLAYER_MOVE = new MGMCEventType("player_move");

    public static final MGMCEventType BLOCK_BREAK = new MGMCEventType("block_break");
    public static final MGMCEventType BLOCK_PLACE = new MGMCEventType("block_place");
    public static final MGMCEventType BLOCK_INTERACT = new MGMCEventType("block_interact");
    public static final MGMCEventType BLOCK_LEFT_CLICK = new MGMCEventType("block_left_click");

    public static final MGMCEventType ENTITY_SPAWN = new MGMCEventType("entity_spawn");
    public static final MGMCEventType ENTITY_DEATH = new MGMCEventType("entity_death");
    public static final MGMCEventType ENTITY_HURT = new MGMCEventType("entity_hurt");

    public static final MGMCEventType ITEM_PICKUP = new MGMCEventType("item_pickup");
    public static final MGMCEventType ITEM_USE = new MGMCEventType("item_use");

    public static final MGMCEventType BLUEPRINT_CALLED = new MGMCEventType("blueprint_called");
    public static final MGMCEventType MGRUN = new MGMCEventType("mgrun");
}

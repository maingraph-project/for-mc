package ltd.opens.mg.mc.core.blueprint.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;

import org.jetbrains.annotations.Nullable;

public class MGMCEventContext {
    private final Level level;
    @Nullable private final Entity entity;
    @Nullable private final Player player;
    @Nullable private final Entity targetEntity; // For attacks/interactions
    @Nullable private final BlockPos pos;
    @Nullable private final net.minecraft.world.level.block.state.BlockState blockState;
    @Nullable private final ItemStack item;
    @Nullable private final DamageSource damageSource;
    private final float amount;
    
    // For MGRUN
    @Nullable private final String eventName;
    @Nullable private final Object[] args;
    
    public MGMCEventContext(Level level, @Nullable Entity entity, @Nullable Player player, @Nullable Entity targetEntity,
                            @Nullable BlockPos pos, @Nullable net.minecraft.world.level.block.state.BlockState blockState,
                            @Nullable ItemStack item, @Nullable DamageSource damageSource, float amount,
                            @Nullable String eventName, @Nullable Object[] args) {
        this.level = level;
        this.entity = entity;
        this.player = player;
        this.targetEntity = targetEntity;
        this.pos = pos;
        this.blockState = blockState;
        this.item = item;
        this.damageSource = damageSource;
        this.amount = amount;
        this.eventName = eventName;
        this.args = args;
    }
    
    public Level getLevel() { return level; }
    @Nullable public Entity getEntity() { return entity; }
    @Nullable public Player getPlayer() { return player; }
    @Nullable public Entity getTargetEntity() { return targetEntity; }
    @Nullable public BlockPos getPos() { return pos; }
    @Nullable public net.minecraft.world.level.block.state.BlockState getBlockState() { return blockState; }
    @Nullable public ItemStack getItem() { return item; }
    @Nullable public DamageSource getDamageSource() { return damageSource; }
    public float getAmount() { return amount; }
    @Nullable public String getEventName() { return eventName; }
    @Nullable public Object[] getArgs() { return args; }

    public static Builder builder(Level level) {
        return new Builder(level);
    }

    public static class Builder {
        private final Level level;
        private Entity entity;
        private Player player;
        private Entity targetEntity;
        private BlockPos pos;
        private net.minecraft.world.level.block.state.BlockState blockState;
        private ItemStack item;
        private DamageSource damageSource;
        private float amount;
        private String eventName;
        private Object[] args;

        public Builder(Level level) {
            this.level = level;
        }

        public Builder entity(Entity entity) { this.entity = entity; return this; }
        public Builder player(Player player) { this.player = player; return this; }
        public Builder targetEntity(Entity targetEntity) { this.targetEntity = targetEntity; return this; }
        public Builder pos(BlockPos pos) { this.pos = pos; return this; }
        public Builder blockState(net.minecraft.world.level.block.state.BlockState blockState) { this.blockState = blockState; return this; }
        public Builder item(ItemStack item) { this.item = item; return this; }
        public Builder damageSource(DamageSource damageSource) { this.damageSource = damageSource; return this; }
        public Builder amount(float amount) { this.amount = amount; return this; }
        public Builder eventName(String eventName) { this.eventName = eventName; return this; }
        public Builder args(Object[] args) { this.args = args; return this; }

        public MGMCEventContext build() {
            return new MGMCEventContext(level, entity, player, targetEntity, pos, blockState, item, damageSource, amount, eventName, args);
        }
    }
}

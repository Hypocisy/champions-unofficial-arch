package top.theillusivec4.champions.api.affix.handler.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;

public final class AttackEvent {
    private final LivingEntity target;
    private final DamageSource source;
    private final float amount;
    private final Runnable cancel;

    public AttackEvent(
            LivingEntity target,
            DamageSource source,
            float amount,
            Runnable cancel              // 写回通道
    ) {
        this.target = target;
        this.source = source;
        this.amount = amount;
        this.cancel = cancel;
    }

    public LivingEntity target() {
        return target;
    }

    public DamageSource source() {
        return source;
    }

    public float amount() {
        return amount;
    }

    public void cancel() {
        cancel.run();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AttackEvent) obj;
        return Objects.equals(this.target, that.target) &&
                Objects.equals(this.source, that.source) &&
                Float.floatToIntBits(this.amount) == Float.floatToIntBits(that.amount) &&
                Objects.equals(this.cancel, that.cancel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, source, amount, cancel);
    }

    @Override
    public String toString() {
        return "AttackEvent[" +
                "target=" + target + ", " +
                "source=" + source + ", " +
                "amount=" + amount + ", " +
                "cancel=" + cancel + ']';
    }

}
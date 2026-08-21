package top.theillusivec4.champions.common.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;


public class RankParticle extends TextureSheetParticle {

    private final SpriteSet spriteSet;

    public RankParticle(ClientLevel level, double x, double y, double z,
                        double xSpeed, double ySpeed, double zSpeed,
                        SpriteSet spriteSet) {
        super(level, x, y, z,
                0.5D - level.random.nextDouble(),
                ySpeed,
                0.5D - level.random.nextDouble());
        this.spriteSet = spriteSet;
        this.yd *= 0.2F;
        if (xSpeed == 0.0D && zSpeed == 0.0D) {
            this.xd *= 0.1F;
            this.zd *= 0.1F;
        }
        this.quadSize *= 0.75F;
        this.lifetime = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.spriteSet);
            this.yd += 0.004D;
            this.move(this.xd, this.yd, this.zd);
            if (this.y == this.yo) {
                this.xd *= 1.1D;
                this.zd *= 1.1D;
            }
            this.xd *= 0.96F;
            this.yd *= 0.96F;
            this.zd *= 0.96F;
            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }
        }
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record RankFactory(SpriteSet spriteSet)
            implements ParticleProvider<SimpleParticleType> {

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type,
                                       @NotNull ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            RankParticle p = new RankParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
            float f = level.random.nextFloat() * 0.5F + 0.35F;
            // color passed as velocity (r,g,b convention)
            p.setColor((float) xSpeed * f, (float) ySpeed * f, (float) zSpeed * f);
            return p;
        }
    }
}
package top.theillusivec4.champions.common.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.ShulkerBulletRenderState;

public class ColorizedBulletRenderState extends ShulkerBulletRenderState {
  public int color;
  public float partialTick;

  public ColorizedBulletRenderState(int color, float xRot, float yRot, float partialTick) {
    this.color = color;
    this.xRot = xRot;
    this.yRot = yRot;
    this.partialTick = partialTick;
  }
}
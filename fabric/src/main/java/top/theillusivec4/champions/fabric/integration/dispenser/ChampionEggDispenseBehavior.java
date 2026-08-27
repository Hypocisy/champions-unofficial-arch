package top.theillusivec4.champions.fabric.integration.dispenser;

import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.item.ChampionItems;

public final class ChampionEggDispenseBehavior extends DefaultDispenseItemBehavior {

    public static final ChampionEggDispenseBehavior INSTANCE = new ChampionEggDispenseBehavior();

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        Direction facing = source.state().getValue(DispenserBlock.FACING);
        ChampionEggItem.getEntityType(stack).ifPresent(type ->
                ChampionItems.egg()
                        .dispense(source.level(), source.pos(), facing, stack));
        return stack;
    }
}

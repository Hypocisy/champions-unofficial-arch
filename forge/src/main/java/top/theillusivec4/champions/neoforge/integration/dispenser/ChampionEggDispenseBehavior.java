package top.theillusivec4.champions.neoforge.integration.dispenser;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.item.ChampionItems;

public final class ChampionEggDispenseBehavior extends DefaultDispenseItemBehavior {

    public static final ChampionEggDispenseBehavior INSTANCE = new ChampionEggDispenseBehavior();

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
        ChampionEggItem.getEntityType(stack).ifPresent(type ->
                ((ChampionEggItem) ChampionItems.egg())
                        .dispense(source.getLevel(), source.getPos(), facing, stack));
        return stack;
    }
}

package nuclearscience.common.tile.fusionreactor;

import electrodynamics.api.capability.ElectrodynamicsCapabilities;
import electrodynamics.prefab.properties.Property;
import electrodynamics.prefab.properties.PropertyType;
import electrodynamics.prefab.tile.GenericTile;
import electrodynamics.prefab.tile.components.IComponentType;
import electrodynamics.prefab.tile.components.type.ComponentElectrodynamic;
import electrodynamics.prefab.tile.components.type.ComponentPacketHandler;
import electrodynamics.prefab.tile.components.type.ComponentTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import nuclearscience.common.settings.Constants;
import nuclearscience.registers.NuclearScienceBlockTypes;
import nuclearscience.registers.NuclearScienceBlocks;
import nuclearscience.registers.NuclearScienceItems;

public class TileFusionReactorCore extends GenericTile {

	public final Property<Integer> deuterium = property(new Property<>(PropertyType.Integer, "deuterium", 0));
	public final Property<Integer> tritium = property(new Property<>(PropertyType.Integer, "tritium", 0));
	public final Property<Integer> timeLeft = property(new Property<>(PropertyType.Integer, "timeleft", 0));

	public TileFusionReactorCore(BlockPos pos, BlockState state) {
		super(NuclearScienceBlockTypes.TILE_FUSIONREACTORCORE.get(), pos, state);

		addComponent(new ComponentTickable(this).tickServer(this::tickServer));
		addComponent(new ComponentPacketHandler(this));
		addComponent(new ComponentElectrodynamic(this, false, true).setInputDirections(Direction.DOWN, Direction.UP).maxJoules(Constants.FUSIONREACTOR_USAGE_PER_TICK * 20.0).voltage(ElectrodynamicsCapabilities.DEFAULT_VOLTAGE * 4));
	}

	public void tickServer(ComponentTickable tick) {
		ComponentElectrodynamic electro = getComponent(IComponentType.Electrodynamic);

		if (tritium.get() > 0 && deuterium.get() > 0 && timeLeft.get() <= 0 && electro.getJoulesStored() > Constants.FUSIONREACTOR_USAGE_PER_TICK) {
			deuterium.set(deuterium.get() - 1);
			tritium.set(tritium.get() - 1);
			timeLeft.set(15 * 20);
		}

		if (timeLeft.get() <= 0) {
			return;
		}

		timeLeft.set(timeLeft.get() - 1);

		if (electro.getJoulesStored() < Constants.FUSIONREACTOR_USAGE_PER_TICK) {
			return;
		}

		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos offset = worldPosition.relative(dir);
			BlockState state = level.getBlockState(offset);
			if (state.getBlock() == NuclearScienceBlocks.blockPlasma) {
				BlockEntity tile = level.getBlockEntity(offset);
				if (tile instanceof TilePlasma plasma && plasma.ticksExisted.get() > 30) {
					plasma.ticksExisted.set(0);
				}
			} else if (state.getBlock() == Blocks.AIR) {
				level.setBlockAndUpdate(offset, NuclearScienceBlocks.blockPlasma.defaultBlockState());
			}
		}
		electro.joules(electro.getJoulesStored() - Constants.FUSIONREACTOR_USAGE_PER_TICK);
	}

	@Override
	public InteractionResult use(Player player, InteractionHand hand, BlockHitResult result) {

		ItemStack inHand = player.getItemInHand(hand);

		Item itemInHand = inHand.getItem();

		if (itemInHand == NuclearScienceItems.ITEM_CELLDEUTERIUM.get() || itemInHand == NuclearScienceItems.ITEM_CELLTRITIUM.get()) {

			boolean isTritium = itemInHand == NuclearScienceItems.ITEM_CELLTRITIUM.get();

			int count = isTritium ? tritium.get() : deuterium.get();

			int added = Math.min(inHand.getCount(), Constants.FUSIONREACTOR_MAXSTORAGE - count);

			if (added > 0) {
				if (!level.isClientSide()) {
					inHand.setCount(inHand.getCount() - added);

					if (isTritium) {
						tritium.set(tritium.get() + added);
					} else {
						deuterium.set(deuterium.get() + added);
					}
				}

				return InteractionResult.CONSUME;
			}

		}

		return InteractionResult.FAIL;
	}

}
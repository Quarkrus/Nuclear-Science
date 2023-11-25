package nuclearscience.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nuclearscience.api.fusion.IElectromagnet;

public class BlockElectromagnet extends Block implements IElectromagnet {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	private final boolean isGlass;

	public BlockElectromagnet(boolean isGlass) {
		super(Properties.copy(isGlass ? Blocks.GLASS : Blocks.IRON_BLOCK).strength(3.5f, 20).requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((x, y, z) -> false));
		this.isGlass = isGlass;
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
		return isGlass ? Shapes.empty() : super.getVisualShape(state, reader, pos, context);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return !isGlass ? super.skipRendering(state, adjacentBlockState, side) : adjacentBlockState.is(this) || super.skipRendering(state, adjacentBlockState, side);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return isGlass ? 1.0F : super.getShadeBrightness(state, worldIn, pos);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return isGlass || super.propagatesSkylightDown(state, reader, pos);
	}
}

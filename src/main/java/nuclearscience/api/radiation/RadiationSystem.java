package nuclearscience.api.radiation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

public class RadiationSystem {
	public static double getRadiationModifier(World world, Vector3f source, Vector3f end, double strength) {
		double distance = 1 + Math.sqrt(Math.pow(source.getX() - end.getX(), 2) + Math.pow(source.getY() - end.getY(), 2) + Math.pow(source.getZ() - end.getZ(), 2));
		Vector3f clone = end.copy();
		double modifier = 1;
		Vector3f newSource = source.copy();
		clone.sub(source);
		clone.normalize();
		clone.mul(0.33f);
		int checks = (int) distance * 3;
		BlockPos curr = new BlockPos(newSource.getX(), newSource.getY(), newSource.getZ());
		double lastHard = 0;
		while (checks > 0) {
			newSource.add(clone);
			double hard = lastHard;
			BlockPos next = new BlockPos(newSource.getX(), newSource.getY(), newSource.getZ());
			if (!curr.equals(next)) {
				curr = next;
				lastHard = hard = world.getBlockState(curr).getBlockHardness(world, curr);
			}
			modifier += hard / 4.5f;
			checks--;
		}
		return modifier;
	}

	public static double getRadiation(World world, Vector3f source, Vector3f end, double strength) {
		double distance = 1 + Math.sqrt(Math.pow(source.getX() - end.getX(), 2) + Math.pow(source.getY() - end.getY(), 2) + Math.pow(source.getZ() - end.getZ(), 2));
		return strength / (getRadiationModifier(world, source, end, strength) * distance * distance);
	}

	public static void applyRadiation(LivingEntity entity, Vector3f source, double strength) {
		int protection = 1;
		Vector3f end = new Vector3f(entity.getPositionVec());
		if (!(entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) && protection < 6) {
			double radiation = getRadiation(entity.world, source, end, strength);
			double distance = 1 + Math.sqrt(Math.pow(source.getX() - end.getX(), 2) + Math.pow(source.getY() - end.getY(), 2) + Math.pow(source.getZ() - end.getZ(), 2));
			double modifier = strength / (radiation * distance * distance);
			strength /= modifier;
			int amplitude = (int) Math.max(0, Math.min(strength / (distance * 4000.0), 9));
			entity.addPotionEffect(new EffectInstance(EffectRadiation.INSTANCE, (int) (strength / ((amplitude + 1) * distance)), amplitude, false, true));
		}
	}
}
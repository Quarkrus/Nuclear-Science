package nuclearscience.common.world;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class QuantumCapacitorData extends WorldSavedData {
	public static final String DATANAME = "quantumcapacitordata";
	public HashMap<UUID, HashMap<Integer, Double>> powermapping = new HashMap<>();

	public QuantumCapacitorData() {
		super(DATANAME);
	}

	@Override
	public void load(CompoundNBT source) {
		powermapping.clear();
		ListNBT list = source.getList("list", 10);
		for (INBT en : list) {
			CompoundNBT compound = (CompoundNBT) en;
			UUID id = compound.getUUID("uuid");
			ListNBT entryList = compound.getList("entrylist", 10);
			HashMap<Integer, Double> info = new HashMap<>();
			for (INBT entryInside : entryList) {
				CompoundNBT inside = (CompoundNBT) entryInside;
				int frequency = inside.getInt("frequency");
				double joules = inside.getDouble("joules");
				info.put(frequency, joules);
			}
			powermapping.put(id, info);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT source) {
		ListNBT list = new ListNBT();
		source.put("list", list);
		for (Entry<UUID, HashMap<Integer, Double>> en : powermapping.entrySet()) {
			CompoundNBT compound = new CompoundNBT();
			compound.putUUID("uuid", en.getKey());
			ListNBT entrylist = new ListNBT();
			compound.put("entrylist", entrylist);
			for (Entry<Integer, Double> entryInside : en.getValue().entrySet()) {
				CompoundNBT inside = new CompoundNBT();
				inside.putInt("frequency", entryInside.getKey());
				inside.putDouble("joules", entryInside.getValue());
				entrylist.add(inside);
			}
			list.add(compound);
		}
		return source;
	}

	public static QuantumCapacitorData get(World world) {
		if (world instanceof ServerWorld) {
			DimensionSavedDataManager storage = ((ServerWorld) world).getDataStorage();
			QuantumCapacitorData instance = storage.computeIfAbsent(QuantumCapacitorData::new, DATANAME);
			if (instance == null) {
				instance = new QuantumCapacitorData();
				storage.set(instance);
			}
			return instance;
		}
		return null;
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	public double getJoules(UUID uuid, int frequency) {
		if (powermapping.containsKey(uuid)) {
			HashMap<Integer, Double> value = powermapping.get(uuid);
			if (value.containsKey(frequency)) {
				return value.get(frequency);
			}
			value.put(frequency, (double) 0);
			return 0;
		}
		powermapping.put(uuid, new HashMap<>());
		powermapping.get(uuid).put(frequency, 0.0);
		return 0;
	}

	public void setJoules(UUID uuid, int frequency, double value) {
		getJoules(uuid, frequency); // load
		powermapping.get(uuid).put(frequency, value);
	}

}

package powercrystals.minefactoryreloaded.block;

import powercrystals.minefactoryreloaded.entity.EntityPinkSlime;

public class BlockPinkSlimeFluid extends BlockFactoryFluid
{
	public BlockPinkSlimeFluid(int id, String liquidName)
	{
		super(id, liquidName);
	}
	
	@Override
	public void updateTick(net.minecraft.world.World world, int x, int y, int z, java.util.Random rand)
	{
		if (isSourceBlock(world, x, y, z))
		{
			if ((world.getTotalWorldTime() & 15) == rand.nextInt(16))
			{
				world.setBlockToAir(x, y, z);
				EntityPinkSlime s = new EntityPinkSlime(world);
				s.onSpawnWithEgg(null);
				s.setSlimeSize(1);
				s.setPosition(x + 0.5, y + 0.5, z + 0.5);
				world.spawnEntityInWorld(s);
				return;
			}
			world.scheduleBlockUpdate(x, y, z, blockID, tickRate);
		}
		super.updateTick(world, x, y, z, rand);
	}
}

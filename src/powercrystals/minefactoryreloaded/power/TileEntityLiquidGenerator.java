package powercrystals.minefactoryreloaded.power;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

public abstract class TileEntityLiquidGenerator extends TileEntityGenerator implements ITankContainer
{
	private int _liquidConsumedPerTick;
	private int _powerProducedPerConsumption;
	private int _ticksBetweenConsumption;
	private int _outputPulseSize;
	
	private int _ticksSinceLastConsumption = 0;
	private int _bufferMax = 1000;
	private int _buffer;
	
	private LiquidTank _tank;
	
	public TileEntityLiquidGenerator(int liquidConsumedPerTick, int powerProducedPerConsumption, int ticksBetweenConsumption)
	{
		_liquidConsumedPerTick = liquidConsumedPerTick;
		_powerProducedPerConsumption = powerProducedPerConsumption;
		_ticksBetweenConsumption = ticksBetweenConsumption;
		_outputPulseSize = 100;
		
		_tank = new LiquidTank(LiquidContainerRegistry.BUCKET_VOLUME * 4);
	}
	
	protected abstract LiquidStack getLiquidType();
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if(!worldObj.isRemote)
		{
			int mjPulse = Math.min(_buffer, _outputPulseSize);
			_buffer -= mjPulse;
			_buffer += producePower(mjPulse);
			
			if(_ticksSinceLastConsumption < _ticksBetweenConsumption)
			{
				_ticksSinceLastConsumption++;
				return;
			}
			_ticksSinceLastConsumption = 0;
			
			if(_tank.getLiquid() == null || _tank.getLiquid().amount < _liquidConsumedPerTick || _bufferMax - _buffer < _powerProducedPerConsumption)
			{
				return;
			}
			
			_tank.drain(_liquidConsumedPerTick, true);
			_buffer += _powerProducedPerConsumption;
		}
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill)
	{
		if(resource == null || resource.itemID != getLiquidType().itemID || resource.itemMeta != getLiquidType().itemMeta)
		{
			return 0;
		}
		return _tank.fill(resource, doFill);
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill)
	{
		return fill(ForgeDirection.UNKNOWN, resource, doFill);
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction)
	{
		return new ILiquidTank[] { _tank };
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type)
	{
		return _tank;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("ticksSinceLastConsumption", _ticksSinceLastConsumption);
		nbttagcompound.setInteger("buffer", _buffer);
		if(_tank.getLiquid() != null)
		{
			nbttagcompound.setInteger("tankAmount", _tank.getLiquid().amount);
			nbttagcompound.setInteger("tankItemId", _tank.getLiquid().itemID);
			nbttagcompound.setInteger("tankMeta", _tank.getLiquid().itemMeta);
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		
		_ticksSinceLastConsumption = nbttagcompound.getInteger("ticksSinceLastConsumption");
		_buffer = nbttagcompound.getInteger("buffer");
		
		_tank.setLiquid(new LiquidStack(nbttagcompound.getInteger("tankItemId"), nbttagcompound.getInteger("tankAmount"), nbttagcompound.getInteger("tankItemMeta")));

		if(_tank.getLiquid() != null && _tank.getLiquid().amount > _tank.getCapacity())
		{
			_tank.getLiquid().amount = _tank.getCapacity();
		}
	}
}

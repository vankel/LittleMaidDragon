package mod.ymt.lmd;

import info.ata4.minecraft.dragon.server.block.DragonEggBlock;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStage;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

public class DragonMaidEggBlock extends DragonEggBlock {

	// このクラスは reobfuscate_srg したものを実機に載せること
	// DragonEggBlock が reobfuscate_srg しているため

	public DragonMaidEggBlock(int par1) {
		super(par1);
		setHardness(3);
		setResistance(15);
		setStepSound(soundStoneFootstep);
		setLightValue(0.125F);
		setUnlocalizedName("dragonEgg");
		func_111022_d("dragon_egg");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		// this is the server's job
		if (world.isRemote) {
			return true;
		}

		// clear placed block
		if (world.getBlockId(x, y, z) == blockID) {
			world.setBlock(x, y, z, 0);
		}

		// create dragon egg
		EntityTameableDragon dragon = new EntityLmdDragon(world);
		dragon.setPosition(x + 0.5, y + 0.5, z + 0.5);
		dragon.getReproductionHelper().setBreederName(player.getEntityName()); // ユーザ名
		dragon.getLifeStageHelper().setLifeStage(DragonLifeStage.EGG);
		world.spawnEntityInWorld(dragon);

		LittleMaidDragonCore.getInstance().debugPrint("spawn EntityLmdDragon");
		return true;
	}
}

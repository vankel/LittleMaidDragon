package mod.ymt.lmd;

import info.ata4.minecraft.dragon.server.block.DragonEggBlock;
import info.ata4.minecraft.dragon.server.entity.LifeStage;
import mod.ymt.cmn.Utils;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

public class DragonMaidEggBlock extends DragonEggBlock {
	private final LittleMaidDragonCore core = LittleMaidDragonCore.getInstance();

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (Utils.isServerSide(world)) {
			if (isWarm(world, x, y, z)) {
				if (world.getBlockId(x, y, z) == blockID) {
					world.setBlockWithNotify(x, y, z, 0);
				}
				EntityLmdDragon dragon = new EntityLmdDragon(world);
				dragon.setPosition(x + 0.5, y + 0.5, z + 0.5);
				dragon.setBreederName(player.username);
				dragon.setLifeStage(LifeStage.EGG);
				world.spawnEntityInWorld(dragon);
			}
			else {
				player.addChatMessage("tile.dragonEgg.tooCold");
			}
		}
		return true;
	}

	private boolean isWarm(World world, int x, int y, int z) {
		int range = 2;
		for (int xn = -range; xn <= range; xn++) {
			for (int zn = -range; zn <= range; zn++) {
				for (int yn = -range; yn <= range; yn++) {
					int blockId = world.getBlockId(x + xn, y + yn, z + zn);
					if (blockId == Block.lavaMoving.blockID || blockId == Block.lavaStill.blockID || blockId == Block.fire.blockID
							|| blockId == Block.stoneOvenActive.blockID) {
						return true;
					}
				}
			}
		}
		return false;
	}
}

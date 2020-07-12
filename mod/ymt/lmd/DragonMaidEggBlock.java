package mod.ymt.lmd;

import info.ata4.minecraft.dragon.server.block.DragonEggBlock;
import info.ata4.minecraft.dragon.server.entity.DragonLifeStage;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

public class DragonMaidEggBlock extends DragonEggBlock {
	
	// Ç±ÇÃÉNÉâÉXÇÕ reobfuscate_srg ÇµÇΩÇ‡ÇÃÇé¿ã@Ç…ç⁄ÇπÇÈÇ±Ç∆
	// DragonEggBlock Ç™ reobfuscate_srg ÇµÇƒÇ¢ÇÈÇΩÇﬂ
	
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
        dragon.setBreederName(player.username);
        dragon.setLifeStage(DragonLifeStage.EGG);
        world.spawnEntityInWorld(dragon);

        LittleMaidDragonCore.getInstance().debugPrint("spawn EntityLmdDragon");
        return true;
	}
}

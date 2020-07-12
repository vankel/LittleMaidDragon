/**
 * Copyright 2015 Yamato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mod.ymt.lmd;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import mod.ymt.lmd.cmn.Utils;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.EntityRegistry;

@Mod(modid = "mod.ymt.lmd.LittleMaidDragon", name = "LittleMaidDragon", version = "17Av1", dependencies = "required-after:DragonMounts;required-after:lmmx")
public class LittleMaidDragon {
	private static final String MAIDSAN_STRING = "DragonMaidsan";
	private static final String DRAGON_STRING = "DragonMount";
	
	@EventHandler
	public void onInit(FMLInitializationEvent event) {
		// ドラゴン追加
		EntityList.stringToClassMapping.remove(DRAGON_STRING);
		int dragon_id = EntityRegistry.findGlobalUniqueEntityId();
		EntityRegistry.registerGlobalEntityID(EntityLmdDragon.class, DRAGON_STRING, dragon_id); // Egg は登録済みなので登録不要
		// STRING が被るように追加すると、ワールドロード時に差し替えが発生する
		
		// メイドさん追加
		EntityRegistry.registerModEntity(EntityLmdMaidsan.class, MAIDSAN_STRING, 0, this, 80, 3, true);
		
		// イベントバスに追加
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onSpawnEntity(EntityJoinWorldEvent event) {
		if (Utils.isServerSide(event.world)) {
			if (event.entity.getClass() == EntityTameableDragon.class) {
				// 差し替え
				NBTTagCompound tag = new NBTTagCompound();
				event.entity.writeToNBT(tag);
				EntityLmdDragon dragon = new EntityLmdDragon(event.world);
				dragon.readFromNBT(tag);
				event.setCanceled(true);
				event.world.spawnEntityInWorld(dragon);
			}
		}
	}
}

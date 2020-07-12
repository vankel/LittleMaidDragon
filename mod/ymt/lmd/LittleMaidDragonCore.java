/**
 * Copyright 2013 Yamato
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

import java.util.HashSet;
import mod.ymt.cmn.NekonoteCore;
import mod.ymt.cmn.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityEggInfo;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.src.BaseMod;
import net.minecraft.src.ModLoader;
import cpw.mods.fml.common.registry.GameRegistry;

@SuppressWarnings("deprecation")
public class LittleMaidDragonCore extends NekonoteCore {
	private static final LittleMaidDragonCore instance = new LittleMaidDragonCore();
	private boolean resque = false;
	private BaseMod baseMod = null;
	
	private LittleMaidDragonCore() {
		;
	}
	
	public BaseMod getBaseMod() {
		return baseMod;
	}
	
	@Override
	public void init() {
		// 消去
		EntityEggInfo eggInfo = (EntityEggInfo) EntityList.entityEggs.remove(getDragonMountEntityId());
		
		int egg1 = eggInfo != null ? eggInfo.primaryColor : 0;
		int egg2 = eggInfo != null ? eggInfo.secondaryColor : 0xcc00ff;
		int dragonId = Utils.getUnusedEntityID();
		ModLoader.registerEntityID(EntityLmdDragon.class, "DragonMount", dragonId, egg1, egg2);
		ModLoader.addEntityTracker(baseMod, EntityLmdDragon.class, dragonId, 80, 3, true);
		int maidsId = Utils.getUnusedEntityID();
		ModLoader.registerEntityID(EntityLmdMaidsan.class, "DragonMaidsan", maidsId);
		ModLoader.addEntityTracker(baseMod, EntityLmdMaidsan.class, maidsId, 80, 3, true);
		
		// 救済
		if (isResque()) {
			logFine("resque mode");
			Object obj = EntityList.entityEggs.get(dragonId);
			if (obj instanceof EntityEggInfo) {
				GameRegistry.addRecipe(new ItemStack(Item.monsterPlacer, 1, dragonId), new Object[]{
					"XXX", "XYX", "XXX", 'X', Block.obsidian, 'Y', Item.egg
				});
			}
		}
	}
	
	public boolean isResque() {
		return resque;
	}
	
	public void setBaseMod(BaseMod baseMod) {
		this.baseMod = baseMod;
	}
	
	public void setResque(boolean resque) {
		this.resque = resque;
	}
	
	private int getDragonMountEntityId() {
		for (Object obj: new HashSet<Object>(EntityList.entityEggs.keySet())) {
			if (obj instanceof Integer) {
				int entityId = (Integer) obj;
				if ("DragonMount".equals(EntityList.getStringFromID(entityId))) {
					return entityId;
				}
			}
		}
		return -1;
	}
	
	public static LittleMaidDragonCore getInstance() {
		return instance;
	}
}

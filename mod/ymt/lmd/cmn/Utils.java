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
package mod.ymt.lmd.cmn;

import java.util.Random;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

/**
 * @author Yamato
 *
 */
public class Utils {
	private Utils() {
		;
	}
	
	public static boolean hasString(String text) {
		return text != null && 0 < text.trim().length();
	}
	
	public static boolean isClientSide(World world) {
		return world.isRemote;
	}
	
	public static boolean isServerSide(World world) {
		return !world.isRemote;
	}
	
	public static void showMessage(EntityPlayer player, String msg) {
		if (player != null && msg != null) {
			msg = msg.trim();
			if (0 < msg.length()) {
				player.addChatMessage(new ChatComponentText(msg));
			}
		}
	}
	
	public static void showMessage(World world, String msg) {
		for (Object ent: world.loadedEntityList) {
			if (ent instanceof EntityPlayer) {
				showMessage((EntityPlayer) ent, msg);
			}
		}
	}
	
	public static void spawnExplodeParticle(World world, double x, double y, double z) {
		Random rand = world.rand;
		for (int j = 0; j < 20; j++) {
			double d = rand.nextGaussian() * 0.02D;
			double d1 = rand.nextGaussian() * 0.02D;
			double d2 = rand.nextGaussian() * 0.02D;
			world.spawnParticle("explode", x + rand.nextFloat() * 2.0 - 1.0, y + rand.nextFloat(), z + rand.nextFloat() * 2.0 - 1.0, d, d1, d2);
		}
	}
	
	public static void spawnPortalParticle(World world, double x, double y, double z) {
		Random rand = world.rand;
		for (int i = 0; i < 3; i++) {
			double px = x + (rand.nextDouble() - 0.5D);
			double py = y + rand.nextDouble();
			double pz = z + (rand.nextDouble() - 0.5D);
			double velx = (rand.nextDouble() - 0.5D) * 2.0D;
			double vely = -rand.nextDouble();
			double velz = (rand.nextDouble() - 0.5D) * 2.0D;
			world.spawnParticle("portal", px, py, pz, velx, vely, velz);
		}
	}
	
	public static boolean tryUseItems(EntityPlayer player, Item item, boolean consumed) {
		return tryUseItems(player, item, -1, consumed);
	}
	
	public static boolean tryUseItems(EntityPlayer player, Item item, int damage, boolean consumed) {
		ItemStack curItem = player.getCurrentEquippedItem();
		if (curItem != null && curItem.getItem() == item && (damage < 0 || curItem.getItemDamage() == damage)) {
			if (consumed && !player.capabilities.isCreativeMode) {
				curItem.stackSize--;
				if (curItem.stackSize <= 0) {
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				}
			}
			return true;
		}
		return false;
	}
}

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
import info.ata4.minecraft.dragon.server.entity.helper.DragonLifeStage;
import mod.ymt.lmd.cmn.Utils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityLmdDragon extends EntityTameableDragon {
	private NBTTagCompound maidsanData = null;

	public EntityLmdDragon(World world) {
		super(world);
		//		System.out.println("# LittleMaidDragon: EntityLmdDragon # world = " + world);
	}

	@Override
	public boolean interact(EntityPlayer player) {
		// isOwner -> func_152114_e
		if (isTamed() && func_152114_e(player) && Utils.tryUseItems(player, Items.sugar, true)) {
			if (switchToMaidsan(player, this)) { // メイドさんに変身
				setDead();
			}
			return true;
		}
		return super.interact(player);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		if (nbt.hasKey("maidsan")) {
			maidsanData = nbt.getCompoundTag("maidsan");
		}
	}

	public void setEntityMaid(EntityLiving maid) {
		if (maid == null) {
			maidsanData = null;
		}
		else {
			maidsanData = new NBTTagCompound();
			maid.writeEntityToNBT(maidsanData);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		if (maidsanData != null) {
			nbt.setTag("maidsan", maidsanData.copy());
		}
	}

	public static boolean switchDragon(EntityPlayer player, EntityLmdMaidsan maid) {
		NBTTagCompound tag = maid.getDragonData();
		if (Utils.isClientSide(player.worldObj))
			return true;
		EntityLmdDragon dragon = new EntityLmdDragon(player.worldObj);
		if (tag != null && !tag.hasNoTags()) {
			dragon.readEntityFromNBT(tag);
		}
		if (!dragon.isTamed()) {
			dragon.setTamed(true);
		}
		if (!dragon.isSaddled()) {
			dragon.setSaddled(true);
		}
		dragon.getLifeStageHelper().setLifeStage(DragonLifeStage.ADULT);
		dragon.setEntityMaid(maid); // ドラゴンをロードした後に改めて設定
		// 変身するたびにライフが全快する仕様に変更
		// dragon.setEntityHealth(MathHelper.ceiling_double_int(maid.getHealth() * (double) dragon.getMaxHealth() / maid.getMaxHealth()));
		dragon.setPathToEntity(null);
		dragon.setAttackTarget(null);
		dragon.setLocationAndAngles(maid.posX, maid.posY, maid.posZ, MathHelper.wrapAngleTo180_float(player.worldObj.rand.nextFloat() * 360.0F), 0);
		dragon.rotationYawHead = dragon.rotationYaw;
		dragon.renderYawOffset = dragon.rotationYaw;
		return player.worldObj.spawnEntityInWorld(dragon);
	}

	public static boolean switchToMaidsan(EntityPlayer player, EntityLmdDragon dragon) {
		if (Utils.isClientSide(player.worldObj))
			return false;
		NBTTagCompound tag = dragon.maidsanData;
		EntityLmdMaidsan maid = new EntityLmdMaidsan(player.worldObj);
		if (tag != null && !tag.hasNoTags()) {
			maid.readEntityFromNBT(tag);
		}
		else {
			maid.initNewMaidsan(player, dragon.isSaddled());
		}
		maid.setEntityDragon(dragon); // メイドさんをロードした後に改めて設定
		// 変身するたびにライフが全快する仕様に変更
		// maid.setEntityHealth(MathHelper.ceiling_double_int(this.getHealth() * (double) maid.getMaxHealth() / this.getMaxHealth()));
		maid.setPathToEntity(null);
		maid.setAttackTarget(null);
		maid.setLocationAndAngles(dragon.posX, dragon.posY, dragon.posZ, MathHelper.wrapAngleTo180_float(player.worldObj.rand.nextFloat() * 360.0F), 0);
		maid.rotationYawHead = maid.rotationYaw;
		maid.renderYawOffset = maid.rotationYaw;
		maid.setMaidMode("Escorter");
		maid.setMaidWait(true);
		maid.setFreedom(false);
		maid.changedFromDragon = true; // 竜へ変身した時の動作
		return player.worldObj.spawnEntityInWorld(maid);
	}
}

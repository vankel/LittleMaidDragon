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

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.LifeStage;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import mod.ymt.cmn.Utils;
import net.minecraft.src.EntityAIBase;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.MathHelper;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class EntityLmdDragon extends EntityTameableDragon {
	private final LittleMaidDragonCore core = LittleMaidDragonCore.getInstance();

	private NBTTagCompound maidsanData = null;

	public EntityLmdDragon(World world) {
		super(world);
		if (core.isEnableFallingCatcher()) {
			EntityAIBase fallingCatcher = new EntityAIFallingCatcher();
			tasks.addTask(0, fallingCatcher);
			airTasks.addTask(0, fallingCatcher);
		}
	}

	public void initNewDragon(EntityPlayer player) {
		if (player != null) {
			setTamed(true);
			setSaddled(true);
			setBreederName(player.username);
			setOwner(player.username);
		}
	}

	@Override
	public boolean interact(EntityPlayer player) {
		if (isTamed() && isOwner(player) && Utils.tryUseItems(player, Item.sugar, false)) { // いったん消費なしでアイテム判定して、飛行中判定後に再度消費させる
			if (isOnFlyingWithPlayer(player)) { // 飛行中なら何もしない
				return false;
			}
			if (Utils.tryUseItems(player, Item.sugar, true) && switchToMaidsan(player, maidsanData)) { // メイドさんに変身
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

	public void setLifeStageForce(LifeStage lifeStage) {
		setLifeStage(lifeStage);
		try {
			Field f_lifeStage = EntityTameableDragon.class.getDeclaredField("lifeStage");
			Field f_lifeStagePrev = EntityTameableDragon.class.getDeclaredField("lifeStagePrev");
			Method m_onNewLifeStage = EntityTameableDragon.class.getDeclaredMethod("onNewLifeStage");
			AccessibleObject.setAccessible(new AccessibleObject[]{
				f_lifeStage, f_lifeStagePrev, m_onNewLifeStage
			}, true);
			f_lifeStage.set(this, lifeStage);
			f_lifeStagePrev.set(this, lifeStage);
			m_onNewLifeStage.invoke(this);
		}
		catch (Exception e) {
			core.debugPrint(e, "set LifeStage failed");
		}
	}

	@Override
	public void setRidingPlayer(EntityPlayer player) {
		if (isOnFlyingWithPlayer(player)) { // 飛行中なら何もしない
			return;
		}
		super.setRidingPlayer(player);
	}

	public boolean switchToMaidsan(EntityPlayer player, NBTTagCompound tag) {
		if (Utils.isClientSide(player.worldObj))
			return false;
		EntityLmdMaidsan maid = new EntityLmdMaidsan(player.worldObj);
		if (tag != null && !tag.hasNoTags()) {
			maid.readEntityFromNBT(tag);
		}
		else {
			maid.initNewMaidsan(player, isSaddled());
		}
		maid.setEntityDragon(this); // メイドさんをロードした後に改めて設定
		maid.setEntityHealth(MathHelper.ceiling_double_int(this.getHealth() * (double) maid.getMaxHealth() / this.getMaxHealth()));
		maid.setPathToEntity(null);
		maid.setAttackTarget(null);
		maid.setLocationAndAngles(posX, posY, posZ, MathHelper.wrapAngleTo180_float(player.worldObj.rand.nextFloat() * 360.0F), 0);
		maid.rotationYawHead = maid.rotationYaw;
		maid.renderYawOffset = maid.rotationYaw;
		maid.setMaidMode("Escorter");
		maid.setMaidWait(true);
		maid.setFreedom(false);
		maid.changedFromDragon = true; // 竜へ変身した時の動作
		return player.worldObj.spawnEntityInWorld(maid);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		if (maidsanData != null) {
			nbt.setTag("maidsan", maidsanData.copy());
		}
	}

	private boolean isOnFlyingWithPlayer(EntityPlayer player) {
		if (onGround) // 地上なら false
			return false;
		if (riddenByEntity != player) // 乗っているのが player でないなら false
			return false;
		// 現在地から地面までの距離を調べる
		{
			final int DISTANCE = 5;
			int x = MathHelper.floor_double(posX);
			int y = MathHelper.floor_double(posY);
			int z = MathHelper.floor_double(posZ);
			for (int i = 0; i < DISTANCE && 0 < y - i; i++) {
				if (worldObj.getBlockId(x, y - i, z) != 0) {
					return false; // 一定距離内に空気ブロック以外のブロックがある
				}
			}
		}
		return true; // いずれでもない
	}

	private final class EntityAIFallingCatcher extends EntityAIBase {
		private EntityPlayer owner = null;

		public EntityAIFallingCatcher() {
			setMutexBits(-1);
		}

		@Override
		public boolean shouldExecute() {
			if (isChild() || !isSaddled())
				return false;
			if (owner == null)
				owner = (EntityPlayer) getOwner();
			if (owner == null || owner.isDead)
				return false;
			if (!core.isFalling(owner))
				return false;
			return true;
		}

		@Override
		public void updateTask() {
			if (owner != null) {
				if (teleportToOwnerBelow()) {
					owner.fallDistance = 0;
					setRidingPlayer(owner);
				}
			}
		}

		private boolean teleportToOwnerBelow() {
			int tx = MathHelper.floor_double(owner.posX);
			int ty = MathHelper.floor_double(owner.boundingBox.minY) - 2;
			int tz = MathHelper.floor_double(owner.posZ);
			for (int x = -2; x <= 2; x++) {
				for (int z = -2; z <= 2; z++) {
					if (worldObj.isBlockNormalCube(tx + x, ty, tz + z) || worldObj.isBlockNormalCube(tx + x, ty + 1, tz + z)) {
						return false;
					}
				}
			}
			aiSit.setSitting(false);
			setSitting(false);
			setLocationAndAngles(tx + 0.5f, ty, tz + 0.5f, rotationYaw, rotationPitch);
			getNavigator().clearPathEntity();
			worldObj.playSoundAtEntity(owner, "mob.endermen.portal", 1.0F, 1.0F);
			return true;
		}
	}
}

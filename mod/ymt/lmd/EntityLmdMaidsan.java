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

import littleMaidMobX.LMM_EntityLittleMaid;
import mmmlibx.lib.MMM_Helper;
import mod.ymt.lmd.cmn.Utils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import wrapper.W_Common;

public class EntityLmdMaidsan extends LMM_EntityLittleMaid {
	private NBTTagCompound dragonData = null;
	boolean changedFromDragon = false;
	
	public EntityLmdMaidsan(World world) {
		super(world);
		//		System.out.println("# LittleMaidDragon: EntityLmdMaidsan # world = " + world);
	}
	
	public NBTTagCompound getDragonData() {
		return dragonData;
	}
	
	@Override
	public void handleHealthUpdate(byte par1) {
		switch (par1) {
			case 15:
				spawnExplosionParticle();
				break;
			default:
				super.handleHealthUpdate(par1);
				break;
		}
	}
	
	public void initNewMaidsan(EntityPlayer player, boolean hasSaddle) {
		if (player != null) {
			setTamed(true);
			W_Common.setOwner(this, MMM_Helper.getPlayerName(player));
			maidContractLimit = (24000 * 7); // 初期契約期間
			maidAnniversary = player.worldObj.getTotalWorldTime() + 63072000000L; // 契約記念日
			// LittleMaidMob に合わせて getWorldTime にしてあるけど、doDaylightCycle を考慮するなら getTotalWorldTime が正解
			// ただ LMD 側だけ getTotalWorldTime にしてしまうと、getWorldTime を使う LMM と逆に重複してしまう可能性が僅かにあるので、
			// 重複する可能性の無いように 100 年を加算する
			// MEMO: 100 * 365 * 24 * 60 * 60 * 20 = 63072000000L
			if (hasSaddle && maidInventory.mainInventory[0] == null) {
				// サドルを持たせる
				maidInventory.mainInventory[0] = new ItemStack(Items.saddle);
			}
		}
	}
	
	@Override
	public boolean interact(EntityPlayer player) {
		// ほんとはEntityModelBaseを継承するのが正攻法なんだろうけど、これだけの処理で作るのも微妙なので
		if (isCommandWaiting(player)) {
			if (isMaidHanded(Items.saddle) && Utils.tryUseItems(player, Items.sugar, true)) {
				eatSugar(false, true);
				if (EntityLmdDragon.switchDragon(player, this)) {
					if (Utils.isClientSide(worldObj))
						worldObj.spawnParticle("hugeexplosion", posX, posY, posZ, 1.0D, 0.0D, 0.0D);
					else
						worldObj.playSoundAtEntity(this, "random.explode", 1.0F, 1.0F);
					setDead();
				}
				return true;
			}
		}
		return super.interact(player);
	}
	
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		super.onDeath(par1DamageSource);
		dropItem(Item.getItemFromBlock(Blocks.dragon_egg), 1);
	}
	
	@Override
	public void onLivingUpdate() {
		if (changedFromDragon) {
			worldObj.setEntityState(this, (byte) 15);
			changedFromDragon = false;
		}
		if (Utils.isClientSide(worldObj) && worldObj.rand.nextInt(10) == 0) {
			Utils.spawnPortalParticle(worldObj, posX, posY, posZ);
		}
		super.onLivingUpdate();
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		if (tag.hasKey("dragon")) {
			dragonData = tag.getCompoundTag("dragon");
		}
	}
	
	public void setEntityDragon(EntityLiving dragon) {
		if (dragon == null) {
			dragonData = null;
		}
		else {
			dragonData = new NBTTagCompound();
			dragon.writeEntityToNBT(dragonData);
		}
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		if (dragonData != null) {
			tag.setTag("dragon", dragonData.copy());
		}
	}
	
	private boolean isCommandWaiting(EntityPlayer player) {
		if (mstatgotcha != null || player.fishEntity != null) // 拉致中
			return false;
		if (getHealth() <= 0) // health 体力なし
			return false;
		if (!isMaidContractOwner(player) || !isRemainsContract()) // player が契約主ではないか、契約期間切れ
			return false;
		if (!isMaidWait()) // 待機状態ではない
			return false;
		return true;
	}
	
	private boolean isMaidHanded(Item item) {
		ItemStack handitem = maidInventory.getStackInSlot(0);
		if (handitem != null && handitem.getItem() == item) {
			return true;
		}
		return false;
	}
}

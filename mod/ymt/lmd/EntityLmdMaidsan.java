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

import mod.ymt.cmn.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.LMM_EntityLittleMaidAvatar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.LMM_EntityLittleMaid;
import net.minecraft.src.LMM_InventoryLittleMaid;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityLmdMaidsan extends LMM_EntityLittleMaid {
	private final LittleMaidDragonCore core = LittleMaidDragonCore.getInstance();
	private NBTTagCompound dragonData = null;
	boolean changedFromDragon = false;

	// PlayerFormLittleMaid さんからアクセスしてくる用変数
	private LMM_InventoryLittleMaid maidInventory = null;
	// PlayerFormLittleMaid さんからアクセスしてくる用変数
	private LMM_EntityLittleMaidAvatar maidAvatar = null;
	// PlayerFormLittleMaid さんからアクセスしてくる用変数
	private int maidDominantArm = 0;

	public EntityLmdMaidsan(World world) {
		super(world);
		core.logFine("EntityLmdMaidsan init on %s", world);
		try {
			this.maidInventory = (LMM_InventoryLittleMaid) LMM_EntityLittleMaid.class.getDeclaredField("maidInventory").get(this);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
			this.maidAvatar = (LMM_EntityLittleMaidAvatar) LMM_EntityLittleMaid.class.getDeclaredField("maidAvatar").get(this);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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
			setOwner(player.getEntityName());
			maidContractLimit = (24000 * 7); // 初期契約期間
			maidAnniversary = player.worldObj.getTotalWorldTime() + 63072000000L; // 契約記念日
			// LittleMaidMob に合わせて getWorldTime にしてあるけど、doDaylightCycle を考慮するなら getTotalWorldTime が正解
			// ただ LMD 側だけ getTotalWorldTime にしてしまうと、getWorldTime を使う LMM と逆に重複してしまう可能性が僅かにあるので、
			// 重複する可能性の無いように 100 年を加算する
			// MEMO: 100 * 365 * 24 * 60 * 60 * 20 = 63072000000L
			if (hasSaddle && maidInventory.mainInventory[0] == null) {
				// サドルを持たせる
				maidInventory.mainInventory[0] = new ItemStack(Item.saddle);
			}
		}
	}

	@Override
	public boolean interact(EntityPlayer player) {
		// ほんとはEntityModelBaseを継承するのが正攻法なんだろうけど、これだけの処理で作るのも微妙なので
		if (isCommandWaiting(player)) {
			if (isMaidHanded(Item.saddle) && Utils.tryUseItems(player, Item.sugar, true)) {
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
		dropItem(Block.dragonEgg.blockID, 1);
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

	@Override
	public void setDominantArm(int pindex) {
		super.setDominantArm(pindex);
		try {
			this.maidDominantArm = (Integer) LMM_EntityLittleMaid.class.getDeclaredField("maidDominantArm").get(this);
		}
		catch (Exception e) {
			e.printStackTrace();
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
		if (handitem != null && handitem.itemID == item.itemID) {
			return true;
		}
		return false;
	}
}

package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.entities.IRangedRecruit;
import com.talhanation.recruits.entities.IStrategicFire;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class RecruitUpkeepPosGoal extends Goal {
    public AbstractRecruitEntity recruit;
    public BlockPos chestPos;
    public Container container;
    public boolean message;
    public boolean messageNotChest;
    public boolean messageNeedNewChest;
    public boolean messageNotInRange;

    public RecruitUpkeepPosGoal(AbstractRecruitEntity recruit) {
        this.recruit = recruit;
    }

    @Override
    public boolean canUse() {
        return recruit.needsToGetFood() && recruit.getUpkeepPos() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }
    @Override
    public void start() {
        super.start();
        message = true;
        messageNotChest = true;
        messageNeedNewChest = true;
        messageNotInRange = true;
        this.chestPos = recruit.getUpkeepPos();
    }

    @Override
    public void tick() {
        super.tick();
        if(this.chestPos != recruit.getUpkeepPos()){
            this.chestPos = recruit.getUpkeepPos();
            this.stop();
            return;
        }

        if(recruit.getUpkeepTimer() == 0 && chestPos != null){
            BlockEntity entity = recruit.level.getBlockEntity(chestPos);
            if (entity instanceof Container containerEntity) {
                this.container = containerEntity;
            }
            else{
                if(recruit.getOwner() != null && messageNotChest){
                    recruit.getOwner().sendMessage(TEXT_CANT_INTERACT(recruit.getName().getString()),recruit.getOwner().getUUID());
                    messageNotChest = false;
                }
                this.chestPos = null;
            }

            if (chestPos != null){
                double distance = this.recruit.position().distanceToSqr(Vec3.atCenterOf(chestPos));
                if(distance > 10000){
                    if(recruit.getOwner() != null && messageNotInRange){
                        recruit.getOwner().sendMessage(TEXT_NOT_IN_RANGE(recruit.getName().getString()),recruit.getOwner().getUUID());
                        messageNotInRange = false;
                    }

                    recruit.clearUpkeepPos();
                    stop();
                    return;
                }
                this.recruit.getNavigation().moveTo(chestPos.getX(), chestPos.getY(), chestPos.getZ(), 1.15D);
                if (recruit.horizontalCollision || recruit.minorHorizontalCollision) {
                    this.recruit.getJumpControl().jump();
                }

                if (chestPos.closerThan(recruit.getOnPos(), 3) && container != null) {
                    this.recruit.getNavigation().stop();
                    this.recruit.getLookControl().setLookAt(chestPos.getX(), chestPos.getY() + 1, chestPos.getZ(), 10.0F, (float) this.recruit.getMaxHeadXRot());
                    if (isFoodInContainer(container)) {
                        for (int i = 0; i < 3; i++) {
                            ItemStack foodItem = this.getFoodFromInv(container);
                            ItemStack food;
                            if (foodItem != null && canAddFood()){
                                food = foodItem.copy();
                                food.setCount(1);
                                recruit.getInventory().addItem(food);
                                foodItem.shrink(1);
                            } else {
                                if(recruit.getOwner() != null && message){
                                    recruit.getOwner().sendMessage(TEXT_NO_PLACE(recruit.getName().getString()),recruit.getOwner().getUUID());
                                    message = false;
                                }
                                this.stop();
                            }
                        }
                    }
                    else {
                        if(recruit.getOwner() != null && message){
                            recruit.getOwner().sendMessage(TEXT_FOOD(recruit.getName().getString()), recruit.getOwner().getUUID());
                            message = false;
                        }
                        this.stop();
                    }

                    //Try to reequip
                    for(int i = 0; i < container.getContainerSize(); i++) {
                        ItemStack itemstack = container.getItem(i);
                        ItemStack equipment;
                        if(!itemstack.isEdible() && recruit.wantsToPickUp(itemstack)){
                            if (recruit.canEquipItem(itemstack)) {
                                equipment = itemstack.copy();
                                equipment.setCount(1);
                                recruit.equipItem(equipment);
                                itemstack.shrink(1);
                            }
                            else if (recruit instanceof IRangedRecruit && itemstack.is(ItemTags.ARROWS)){ //all that are ranged
                                if(recruit.canTakeArrows()){
                                    equipment = itemstack.copy();
                                    recruit.inventory.addItem(equipment);
                                    itemstack.shrink(equipment.getCount());
                                }
                            }
                        }
                    }
                    this.stop();
                }
            }
            else {
                this.chestPos = findInvPos();

                if(chestPos == null){
                    if(recruit.getOwner() != null && messageNeedNewChest){
                        recruit.getOwner().sendMessage(NEED_NEW_UPKEEP(recruit.getName().getString()),recruit.getOwner().getUUID());
                        messageNeedNewChest = false;
                    }

                    recruit.clearUpkeepPos();
                    stop();
                }
                else recruit.setUpkeepPos(chestPos);
                //Main.LOGGER.debug("Chest not found");
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        recruit.setUpkeepTimer(recruit.getUpkeepCooldown());
    }

    @Nullable
    private BlockPos findInvPos() {
        List<BlockPos> list = new ArrayList<>();
        BlockPos chestPos;
        if(this.recruit.getUpkeepPos() != null) {
            int range = 8;
            for (int x = -range; x < range; x++) {
                for (int y = -range; y < range; y++) {
                    for (int z = -range; z < range; z++) {
                        chestPos = recruit.getUpkeepPos().offset(x, y, z);
                        BlockEntity block = recruit.level.getBlockEntity(chestPos);
                        if (block instanceof Container blockContainer){
                            if(isFoodInContainer(blockContainer)) return chestPos;
                            else list.add(chestPos);
                        }
                    }
                }
            }
        }

        if(list.isEmpty()) return null;
        else return list.get(recruit.getRandom().nextInt(list.size()));
    }

    private boolean isFoodInContainer(Container container){
        for(int i = 0; i < container.getContainerSize(); i++) {
            ItemStack foodItem = container.getItem(i);
            if(foodItem.isEdible()){
                return true;
            }
        }
        return false;
    }
    @Nullable
    private ItemStack getFoodFromInv(Container inv){
        ItemStack itemStack = null;
        for(int i = 0; i < inv.getContainerSize(); i++){
            if(inv.getItem(i).isEdible()){
                itemStack = inv.getItem(i);
                break;
            }
        }
        return itemStack;
    }

    private boolean canAddFood(){
        for(int i = 6; i < 14; i++){
            if(recruit.getInventory().getItem(i).isEmpty())
                return true;
        }
        return false;
    }

    private MutableComponent TEXT_NO_PLACE(String name) {
        return new TranslatableComponent("chat.recruits.text.noPlaceInInv", name);
    }

    private MutableComponent TEXT_CANT_INTERACT(String name) {
        return new TranslatableComponent("chat.recruits.text.cantInteract", name);
    }

    private MutableComponent NEED_NEW_UPKEEP(String name) {
        return new TranslatableComponent("chat.recruits.text.findMeNewChest", name);
    }

    private MutableComponent TEXT_FOOD(String name) {
        return new TranslatableComponent("chat.recruits.text.noFoodInUpkeep", name);
    }

    private MutableComponent TEXT_NOT_IN_RANGE(String name) {
        return new TranslatableComponent("chat.recruits.text.upkeepNotInRange", name);
    }
}

package com.talhanation.recruits.network;

import com.talhanation.recruits.CommandEvents;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MessageEscortEntity implements Message<MessageEscortEntity> {

    private UUID uuid;
    private UUID target;
    private int group;

    public MessageEscortEntity(){

    }

    public MessageEscortEntity(UUID uuid, UUID target, int group) {
        this.uuid = uuid;
        this.target = target;
        this.group = 0;

    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        List<AbstractRecruitEntity> list = Objects.requireNonNull(context.getSender()).level.getEntitiesOfClass(AbstractRecruitEntity.class, context.getSender().getBoundingBox().inflate(64.0D));
        for (AbstractRecruitEntity recruits : list) {
            CommandEvents.onEscortButton(uuid, recruits, target, group);
            CommandEvents.onFollowCommand(uuid, recruits, 5, this.group, false);
        }
    }
    public MessageEscortEntity fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.target = buf.readUUID();
        this.group = buf.readInt();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeUUID(target);
        buf.writeInt(group);
    }

}
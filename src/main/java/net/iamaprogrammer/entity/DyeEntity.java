package net.iamaprogrammer.entity;

import net.iamaprogrammer.ThrowableDye;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DyeEntity extends ThrownItemEntity {

    public DyeEntity(EntityType<? extends ThrownItemEntity> entityEntityType, World world) {
        super(entityEntityType, world);
    }

    public DyeEntity(World world, LivingEntity owner) {
        super(ThrowableDye.DYE, owner, world);
    }

    public DyeEntity(World world, double x, double y, double z) {
        super(ThrowableDye.DYE, x, y, z, world);
    }

    protected Item getDefaultItem() {
        return Items.AIR;
    }

    private ParticleEffect getParticleParameters() {
        ItemStack itemStack = this.getItem();
        return (ParticleEffect)(itemStack.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack));
    }

    public void handleStatus(byte status) {
        if (status == 3) {
            ParticleEffect particleEffect = this.getParticleParameters();

            for(int i = 0; i < 8; ++i) {
                this.world.addParticle(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }

    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();

        if (entity instanceof SheepEntity sheep && sheep.getColor() != this.getColor()) {
            sheep.setColor(this.getColor());
        } else {
            this.dropStack(this.getItem());
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = this.world.getBlockState(pos);
        
        // Get block id and namespace
        Identifier id = Registries.BLOCK.getId(state.getBlock());
        String blockId = id.getPath();
        String blockNamespace = id.getNamespace();
        
        // Generate a new id.
        String newId = generateId(blockId);
        if (new Identifier(blockNamespace, newId).equals(id)) {
            this.dropStack(this.getItem());
            return;
        }

        Block newBlock = Registries.BLOCK.get(new Identifier(blockNamespace, newId));

        if (newBlock != Blocks.AIR) {
            this.addToWorld(pos, newBlock, state);
        } else {
            // Fallback to config to handle possible edge-cases.
            newId = ThrowableDye.CONFIG.getOutliers().get(id.toString());
            if (newId == null || !newId.contains(blockId)) {
                this.dropStack(this.getItem());
                return;
            }
            newId = newId.replace("{color}", this.getColor().getName());
            newBlock = Registries.BLOCK.get(new Identifier(blockNamespace, newId));

            if (newBlock == Blocks.AIR) {
                this.dropStack(this.getItem());
                return;
            }
            this.addToWorld(pos, newBlock, state);
        }
    }

    private void addToWorld(BlockPos pos, Block block, BlockState previousState) {
        if (previousState.contains(Properties.BED_PART)) {
            Direction facing = BedBlock.getOppositePartDirection(previousState);
            BlockPos otherEndPos = pos.offset(facing);

            this.world.setBlockState(pos, block.getStateWithProperties(previousState), Block.FORCE_STATE | Block.REDRAW_ON_MAIN_THREAD | Block.NOTIFY_ALL);
            this.world.setBlockState(otherEndPos, block.getStateWithProperties(this.world.getBlockState(otherEndPos)), Block.FORCE_STATE | Block.REDRAW_ON_MAIN_THREAD | Block.NOTIFY_ALL);

        } else {
            this.world.setBlockState(pos, block.getStateWithProperties(previousState));
        }
    }

    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (!this.world.isClient) {
            this.world.sendEntityStatus(this, (byte)3);
            this.discard();
        }
    }

    private DyeColor getColor() {
        return ((DyeItem) this.getItem().getItem()).getColor();
    }

    private String fromColoredBlock(String blockId) {
        for (DyeColor color : DyeColor.values()) {
            if (blockId.startsWith(color.getName()) || blockId.endsWith(color.getName())) {
                return blockId.replace(color.getName(), this.getColor().getName());
            }
        }
        return null;
    }

    private String fromPlainBlock(String blockId) {
        return this.getColor() + "_" + blockId;
    }

    private String generateId(String blockId) {
        String id = this.fromColoredBlock(blockId);
        id = id == null ? this.fromPlainBlock(blockId) : id;
        return id;
    }
}

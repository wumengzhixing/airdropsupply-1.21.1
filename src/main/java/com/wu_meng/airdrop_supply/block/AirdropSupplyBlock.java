package com.wu_meng.airdrop_supply.block;

import com.mojang.serialization.MapCodec;
import com.wu_meng.airdrop_supply.blockentity.AirdropSupplyBlockEntity;
import com.wu_meng.airdrop_supply.entry.ModBlockEntities;
import com.wu_meng.airdrop_supply.misc.Configuration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;

@SuppressWarnings("null")
public class AirdropSupplyBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final MapCodec<AirdropSupplyBlock> CODEC = BlockBehaviour.simpleCodec(AirdropSupplyBlock::new);
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);
    public static final EnumProperty<Type> TYPE = EnumProperty.create("type",Type.class);
    public static final EnumProperty<CaseLevel> LEVEL = EnumProperty.create("level",CaseLevel.class);

    public AirdropSupplyBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(
                Objects.requireNonNull(this.stateDefinition.any(), "default state")
                        .setValue(FACING, Direction.SOUTH)
                        .setValue(TYPE, Type.MEDIC)
                        .setValue(LEVEL, CaseLevel.BASIC)
        );
    }

    public static BlockBehaviour.Properties createProperties() {
        return Objects.requireNonNull(BlockBehaviour.Properties.of(), "BlockBehaviour.Properties")
                .strength(20F)
                .sound(Objects.requireNonNull(SoundType.METAL, "SoundType.METAL"))
                .explosionResistance(20)
                .noOcclusion()
                .lightLevel(state -> 1);
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pPos, @Nonnull BlockState pState) {
        return ModBlockEntities.AIRDROP_SUPPLY.get().create(pPos,pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level pLevel, @Nonnull BlockState pState, @Nonnull BlockEntityType<T> pBlockEntityType) {
        return pBlockEntityType == ModBlockEntities.AIRDROP_SUPPLY.get() ? AirdropSupplyBlockEntity::ticker : null;
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING).add(TYPE).add(LEVEL);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof AirdropSupplyBlockEntity airdropSupplyBlockEntity) {
                if (airdropSupplyBlockEntity.isOpened()) {
                    pPlayer.openMenu(airdropSupplyBlockEntity);
                } else {
                    airdropSupplyBlockEntity.markOpened(pPlayer);
                }
                return InteractionResult.CONSUME;
            }
            if (blockentity instanceof MenuProvider menuProvider) {
                pPlayer.openMenu(menuProvider);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public void onRemove(@Nonnull BlockState pState, @Nonnull Level pLevel, @Nonnull BlockPos pPos, @Nonnull BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof Container) {
                Containers.dropContents(pLevel, pPos, (Container)blockentity);
                pLevel.updateNeighbourForOutputSignal(pPos, this);
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    @Override
    public VoxelShape getCollisionShape(@Nonnull BlockState pState, @Nonnull BlockGetter pLevel, @Nonnull BlockPos pPos, @Nonnull CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public VoxelShape getShape(@Nonnull BlockState pState, @Nonnull BlockGetter pLevel, @Nonnull BlockPos pPos, @Nonnull CollisionContext pContext) {
        return SHAPE;
    }

    public static void setDespawnTime(@Nonnull Level overworld, @Nonnull BlockPos pPos){
        if(overworld.getBlockEntity(pPos) instanceof AirdropSupplyBlockEntity airdropSupplyBlockEntity){
            airdropSupplyBlockEntity.setDespawnTime(overworld.getGameTime() + Configuration.AIRDROP_DESPAWN_TIME.get());
        }
    }

    public enum Type implements StringRepresentable {
        NORMAL,
        MEDIC;
        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

    public enum CaseLevel implements StringRepresentable {
        BASIC,
        MEDIUM,
        ADVANCED;
        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }
}

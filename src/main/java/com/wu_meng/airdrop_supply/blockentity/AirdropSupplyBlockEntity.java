package com.wu_meng.airdrop_supply.blockentity;

import com.wu_meng.airdrop_supply.block.AirdropSupplyBlock;
import com.wu_meng.airdrop_supply.entry.ModBlockEntities;
import com.wu_meng.airdrop_supply.entry.ModSoundEvents;
import com.wu_meng.airdrop_supply.misc.AmbushManager;
import com.wu_meng.airdrop_supply.misc.Configuration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("null")
public class AirdropSupplyBlockEntity extends RandomizableContainerBlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation OPEN_ANIMATION = RawAnimation.begin().thenPlayAndHold("animation.airdrop.open");

    public static final DustColorTransitionOptions AIRDROP_SIGNAL = new DustColorTransitionOptions(
            new Vector3f(Objects.requireNonNull(Vec3.fromRGB24(14761505).toVector3f(), "signal color")),
            DustParticleOptions.REDSTONE_PARTICLE_COLOR, 4.0F
    );

    private NonNullList<ItemStack> items = NonNullList.withSize(27, Objects.requireNonNull(ItemStack.EMPTY, "ItemStack.EMPTY"));
    private long despawnTime = Long.MAX_VALUE;
    public long ticksExisted = 0;
    private boolean isOpen = false; // 客户端核心状态
    private boolean openAnimationStarted = false;
    private int openAnimationId = 0;
    @Nullable
    private UUID pendingOpenPlayerId = null;
    private long pendingOpenAtGameTime = -1L;

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(@Nonnull Level pLevel, @Nonnull BlockPos blockPos, @Nonnull BlockState pState) {
            playSound(pState, ModSoundEvents.OPEN_AIRDROP.get());
        }

        @Override
        protected void onClose(@Nonnull Level pLevel, @Nonnull BlockPos pPos, @Nonnull BlockState pState) {}

        @Override
        protected void openerCountChanged(@Nonnull Level pLevel, @Nonnull BlockPos pPos, @Nonnull BlockState pState, int pCount, int pOpenCount) {
            if (!pLevel.isClientSide && pOpenCount <= 0) {
                closeCrate(pLevel, pPos, pState);
            }
        }

        @Override
        protected boolean isOwnContainer(@Nonnull Player player) {
            if (player.containerMenu instanceof ChestMenu) {
                Container container = ((ChestMenu)player.containerMenu).getContainer();
                return container == AirdropSupplyBlockEntity.this;
            }
            return false;
        }
    };

    public AirdropSupplyBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.AIRDROP_SUPPLY.get(), blockPos, blockState);
    }

    public static <T extends BlockEntity> void ticker(Level level, BlockPos blockPos, BlockState blockState, T t) {
        tick(level, blockPos, blockState, (AirdropSupplyBlockEntity) t);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, AirdropSupplyBlockEntity pBlockEntity) {
        if (pLevel.isClientSide) {
            if ((pLevel.getGameTime() & 1L) == 0L) {
                for (int i = 0; i < 3; i++) {
                    pLevel.addAlwaysVisibleParticle(AIRDROP_SIGNAL,
                            pPos.getX() + pLevel.random.nextGaussian() * 1 / 3,
                            pPos.getY() + 10 + Math.abs(pLevel.random.nextGaussian() * 30),
                            pPos.getZ() + pLevel.random.nextGaussian() * 1 / 3,
                            0, 3, 0);
                }
                for (int i = 0; i < 3; i++) {
                    pLevel.addAlwaysVisibleParticle(AIRDROP_SIGNAL,
                            pPos.getX() + pLevel.random.nextGaussian(),
                            pPos.getY() + 20 + Math.abs(pLevel.random.nextGaussian() * 20),
                            pPos.getZ() + pLevel.random.nextGaussian(),
                            0, 3, 0);
                }
                for (int i = 0; i < 3; i++) {
                    pLevel.addAlwaysVisibleParticle(AIRDROP_SIGNAL,
                            pPos.getX() + pLevel.random.nextGaussian() * 2.5,
                            pPos.getY() + 30 + Math.abs(pLevel.random.nextGaussian() * 10),
                            pPos.getZ() + pLevel.random.nextGaussian() * 2.5,
                            0, 3, 0);
                }
            }
        } else {
            pBlockEntity.ticksExisted++;

            if (!pBlockEntity.remove) {
                pBlockEntity.openersCounter.recheckOpeners(pLevel, pPos, pState);
            }

            if (pBlockEntity.pendingOpenPlayerId != null && pLevel.getGameTime() >= pBlockEntity.pendingOpenAtGameTime) {
                var server = pLevel.getServer();
                if (server != null) {
                    ServerPlayer player = server.getPlayerList().getPlayer(pBlockEntity.pendingOpenPlayerId);
                    if (player != null && player.level() == pLevel && player.blockPosition().closerThan(pPos, 8.0)) {
                        player.openMenu(pBlockEntity);
                    }
                }
                pBlockEntity.pendingOpenPlayerId = null;
                pBlockEntity.pendingOpenAtGameTime = -1L;
                pBlockEntity.setChanged();
            }

            if (pLevel.getGameTime() >= pBlockEntity.despawnTime) {
                pBlockEntity.items.clear();
                pLevel.setBlockAndUpdate(pPos, Blocks.AIR.defaultBlockState());
            }

            if (pBlockEntity.lootTable == null && pBlockEntity.isEmpty()) {
                pLevel.setBlockAndUpdate(pPos, Blocks.AIR.defaultBlockState());
            }

            if (pBlockEntity.lootTable != null) {
                int startAfterTicks = Math.max(0, Configuration.AMBUSH_START_AFTER_TICKS.get());
                int intervalTicks = Math.max(1, Configuration.AMBUSH_ATTEMPT_INTERVAL_TICKS.get());
                if (pBlockEntity.ticksExisted > startAfterTicks && pBlockEntity.ticksExisted % intervalTicks == 0) {
                    int day = (int) (pLevel.getDayTime() / 24000) + 1;
                    AmbushManager.trySpawnAmbush(pLevel, pPos, pState.getValue(AirdropSupplyBlock.TYPE), pState.getValue(AirdropSupplyBlock.LEVEL), day);
                }
            }
        }
    }

    // ============================================
    // GeckoLib 动画控制器配置
    // ============================================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<AirdropSupplyBlockEntity> state) {
        AnimationController<?> controller = state.getController();
        if (this.isOpen) {
            if (!this.openAnimationStarted) {
                controller.forceAnimationReset();
                controller.setAnimation(OPEN_ANIMATION);
                this.openAnimationStarted = true;
            }
            return PlayState.CONTINUE;
        } else {
            return PlayState.STOP;
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ============================================
    // 数据持久化与网络同步（修复了致命Bug）
    // ============================================
    public void setDespawnTime(long despawnTime) {
        this.despawnTime = despawnTime;
        this.setChanged();
    }

    public boolean isOpened() {
        return this.isOpen;
    }

    public void markOpened(@Nonnull Player opener) {
        Level currentLevel = this.getLevel();
        if (currentLevel == null || currentLevel.isClientSide) {
            return;
        }
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;
        this.openAnimationStarted = false;
        this.openAnimationId++;

        if (opener instanceof ServerPlayer serverPlayer) {
            int delayTicks = Math.max(0, Configuration.OPEN_ANIMATION_DELAY_TICKS.get());
            if (delayTicks == 0) {
                serverPlayer.openMenu(this);
            } else {
                this.pendingOpenPlayerId = serverPlayer.getUUID();
                this.pendingOpenAtGameTime = currentLevel.getGameTime() + delayTicks;
            }
        }

        this.setChanged();
        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();
        currentLevel.sendBlockUpdated(pos, state, state, 3);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider provider) {
        super.saveAdditional(pTag, provider);
        pTag.putLong("DespawnTime", this.despawnTime);
        pTag.putLong("TicksExisted", this.ticksExisted);
        pTag.putBoolean("IsOpen", this.isOpen);
        pTag.putInt("OpenAnimationId", this.openAnimationId);
        if (!this.trySaveLootTable(pTag)) {
            ContainerHelper.saveAllItems(pTag, this.items, provider);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider provider) {
        super.loadAdditional(pTag, provider);
        if (pTag.contains("DespawnTime")) this.despawnTime = pTag.getLong("DespawnTime");
        if (pTag.contains("TicksExisted")) this.ticksExisted = pTag.getLong("TicksExisted");

        int oldOpenAnimationId = this.openAnimationId;
        if (pTag.contains("OpenAnimationId")) this.openAnimationId = pTag.getInt("OpenAnimationId");

        if (pTag.contains("IsOpen")) this.isOpen = pTag.getBoolean("IsOpen");
        this.openAnimationStarted = !this.isOpen;
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(pTag)) {
            ContainerHelper.loadAllItems(pTag, this.items, provider);
        }

        // Camera animation is temporarily disabled.
        if (this.isOpen && this.openAnimationId > oldOpenAnimationId) {
            this.openAnimationStarted = false;
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider pRegistries) {
        // 【关键修复】：必须调用 super.getUpdateTag 获取 xyz 等基础坐标数据，否则客户端会直接把包丢弃！
        CompoundTag tag = super.getUpdateTag(pRegistries);
        tag.putBoolean("IsOpen", this.isOpen);
        tag.putInt("OpenAnimationId", this.openAnimationId);
        return tag;
    }

    // ============================================
    // 原版容器基础方法
    // ============================================
    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> pItems) {
        this.items = pItems;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.airdrop_supply.airdrop_supply");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int pId, @NotNull Inventory pPlayer) {
        return ChestMenu.threeRows(pId, pPlayer, this);
    }

    @Override
    public void startOpen(@NotNull Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.incrementOpeners(pPlayer, Objects.requireNonNull(this.getLevel()), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(@NotNull Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.decrementOpeners(pPlayer, Objects.requireNonNull(this.getLevel(), "level"), this.getBlockPos(), this.getBlockState());
        }
    }

    private void closeCrate(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        if (!this.isOpen) {
            return;
        }
        this.isOpen = false;
        this.openAnimationStarted = false;
        this.pendingOpenPlayerId = null;
        this.pendingOpenAtGameTime = -1L;
        this.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    private void playSound(@Nonnull BlockState pState, @Nonnull SoundEvent pSound) {
        Vec3i vec3i = pState.getValue(AirdropSupplyBlock.FACING).getNormal();
        double d0 = (double)this.worldPosition.getX() + 0.5D + (double)vec3i.getX() / 2.0D;
        double d1 = (double)this.worldPosition.getY() + 0.5D + (double)vec3i.getY() / 2.0D;
        double d2 = (double)this.worldPosition.getZ() + 0.5D + (double)vec3i.getZ() / 2.0D;
        Level level = Objects.requireNonNull(this.level, "level");
        level.playSound(null, d0, d1, d2, pSound, SoundSource.BLOCKS, 3F, level.random.nextFloat() * 0.1F + 0.9F);
    }
}

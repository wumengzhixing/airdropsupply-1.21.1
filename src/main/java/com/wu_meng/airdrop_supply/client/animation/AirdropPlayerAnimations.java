package com.wu_meng.airdrop_supply.client.animation;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractModifier;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

public final class AirdropPlayerAnimations {
    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(
            AirdropSupply.CONTENT_ID, "crate_opening");
    private static final ResourceLocation OPEN_CRATE = ResourceLocation.fromNamespaceAndPath(
            AirdropSupply.CONTENT_ID, "open_crate");

    private AirdropPlayerAnimations() {}

    public static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                LAYER_ID,
                2000,
                AirdropPlayerAnimations::createController
        );
    }

    public static void playOpenCrate(AbstractClientPlayer player) {
        if (PlayerAnimationAccess.getPlayerAnimationLayer(player, LAYER_ID)
                instanceof PlayerAnimationController controller) {
            controller.triggerAnimation(OPEN_CRATE);
        }
    }

    private static PlayerAnimationController createController(AbstractClientPlayer player) {
        PlayerAnimationController controller = new PlayerAnimationController(
                player,
                (animationController, state, animationSetter) -> PlayState.STOP
        );
        controller.addModifierBefore(new FirstPersonBodyAnchorModifier());
        controller.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
        controller.setFirstPersonFollowsCamera(true);
        controller.setFirstPersonTransitionLength(4);
        controller.setFirstPersonConfiguration(new FirstPersonConfiguration()
                .setShowRightArm(true)
                .setShowLeftArm(true)
                .setShowRightItem(false)
                .setShowLeftItem(false)
                .setShowArmor(true));
        return controller;
    }

    private static final class FirstPersonBodyAnchorModifier extends AbstractModifier {
        @Override
        public PlayerAnimBone get3DTransform(PlayerAnimBone bone) {
            PlayerAnimBone transformed = super.get3DTransform(bone);
            if (FirstPersonMode.isFirstPersonPass() && "body".equals(transformed.getName())) {
                transformed.updatePosition(0.0F, 0.0F, 0.0F);
            }
            return transformed;
        }
    }
}

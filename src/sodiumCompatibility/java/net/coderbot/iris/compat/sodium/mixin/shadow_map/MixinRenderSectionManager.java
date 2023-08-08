package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.minecraft.client.Camera;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager {
    @Shadow private @NotNull SortedRenderLists renderLists;
    @Unique
    private @NotNull SortedRenderLists shadowRenderLists = SortedRenderLists.empty();

    @Redirect(method = "createTerrainRenderList", at = @At(value = "FIELD", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSectionManager;renderLists:Lme/jellysquid/mods/sodium/client/render/chunk/lists/SortedRenderLists;"))
    private void useShadowRenderList(RenderSectionManager instance, SortedRenderLists value) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            shadowRenderLists = value;
        } else {
            renderLists = value;
        }
    }

    @Inject(method = "updateRenderLists", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSectionManager;createTerrainRenderList(Lnet/minecraft/client/Camera;Lme/jellysquid/mods/sodium/client/render/viewport/Viewport;IZ)V", shift = At.Shift.AFTER), cancellable = true)
    private void cancelIfShadow(Camera camera, Viewport viewport, int frame, boolean spectator, CallbackInfo ci) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) ci.cancel();
    }

    @Redirect(method = {
            "getRenderLists",
            "renderLayer"
    }, at = @At(value = "FIELD", target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSectionManager;renderLists:Lme/jellysquid/mods/sodium/client/render/chunk/lists/SortedRenderLists;"))
    private SortedRenderLists useShadowRenderList2(RenderSectionManager instance) {
        return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? shadowRenderLists : renderLists;
    }
}

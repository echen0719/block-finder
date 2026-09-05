package echen0719.blockfinder.client.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class BlockUpdaterMixin {
    @Inject(method = "setBlock", at = @At("RETURN"))
    private void onSetBlock(BlockPos position, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            echen0719.blockfinder.client.BlockScanner.onBlockUpdate(position, state); // annoying VS Code undefined error
        }
    }
}
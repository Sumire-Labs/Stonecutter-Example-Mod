package com.sumirelabs;

//? if fabric {
import net.fabricmc.api.ModInitializer;

public final class ModEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        StonecutterExampleMod.initialize();
    }
}
//?} elif neoforge {
/*import net.neoforged.fml.common.Mod;

@Mod(StonecutterExampleMod.MOD_ID)
public final class ModEntrypoint {
    public ModEntrypoint() {
        StonecutterExampleMod.initialize();
    }
}
*///?} else {
/*import net.minecraftforge.fml.common.Mod;

@Mod(StonecutterExampleMod.MOD_ID)
public final class ModEntrypoint {
    public ModEntrypoint() {
        StonecutterExampleMod.initialize();
    }
}
*///?}

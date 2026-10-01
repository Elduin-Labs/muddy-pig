package com.elduin.muddy_pig.platform.fabric;

//? fabric {

import com.elduin.muddy_pig.MuddyPig;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		MuddyPig.onInitialize();
		FabricEventSubscriber.registerEvents();
	}
}
//?}

package com.elduin.muddy_pig.platform.fabric;

//? fabric {

import com.elduin.muddy_pig.MuddyPig;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		MuddyPig.onInitializeClient();
	}

}
//?}

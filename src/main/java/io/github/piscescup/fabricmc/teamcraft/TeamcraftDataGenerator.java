package io.github.piscescup.fabricmc.teamcraft;

import io.github.piscescup.fabricmc.teamcraft.datagen.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.jspecify.annotations.NonNull;


/**
 * Data generation entry.
 */
public class TeamcraftDataGenerator
    implements DataGeneratorEntrypoint
{

    @Override
    public void onInitializeDataGenerator(@NonNull FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ENUSLanguageProvider::new);
        pack.addProvider(ZHCNLanguageProvider::new);
        pack.addProvider(ZHHKLanguageProvider::new);
        pack.addProvider(ZHTWLanguageProvider::new);
        pack.addProvider(ENGBLanguageProvider::new);
    }
}

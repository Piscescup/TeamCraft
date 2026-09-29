package io.github.piscescup.fabricmc.teamcraft;

import io.github.piscescup.fabricmc.teamcraft.datagen.EnglishLanguageProvider;
import io.github.piscescup.fabricmc.teamcraft.datagen.SimplifiedChineseLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;


/**
 * Data generation entry.
 */
public class TeamcraftDataGenerator
    implements DataGeneratorEntrypoint
{

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(EnglishLanguageProvider::new);
        pack.addProvider(SimplifiedChineseLanguageProvider::new);
    }
}

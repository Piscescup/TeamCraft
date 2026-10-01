package io.github.piscescup.fabricmc.teamcraft.datagen;

import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class ENGBLanguageProvider
    extends FabricLanguageProvider
{
    public ENGBLanguageProvider(
        FabricPackOutput packOutput,
        CompletableFuture<HolderLookup.Provider> registryLookup
    )
    {
        super(packOutput, "en_gb", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
        for (TeamcraftTranslations translation : TeamcraftTranslations.values()) {
            translationBuilder.add(translation.key(), translation.enUsTranslation());
        }
    }
}

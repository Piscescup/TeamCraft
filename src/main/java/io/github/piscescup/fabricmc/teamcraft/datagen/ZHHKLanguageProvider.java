package io.github.piscescup.fabricmc.teamcraft.datagen;

import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

/** Generates the {@code zh_hk} language file. */
public final class ZHHKLanguageProvider extends FabricLanguageProvider {
    public ZHHKLanguageProvider(
        FabricPackOutput output,
        CompletableFuture<HolderLookup.Provider> registries
    ) {
        super(output, "zh_hk", registries);
    }

    @Override
    public void generateTranslations(
        HolderLookup.Provider registries,
        TranslationBuilder builder
    ) {
        for (TeamcraftTranslations translation : TeamcraftTranslations.values()) {
            builder.add(
                translation.key(),
                TraditionalChineseTranslations.hongKong(translation.zhCnTranslation())
            );
        }
    }
}

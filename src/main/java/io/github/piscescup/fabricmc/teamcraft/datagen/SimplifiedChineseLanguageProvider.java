package io.github.piscescup.fabricmc.teamcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * Generates the {@code zh_cn} language file.
 */
public final class SimplifiedChineseLanguageProvider extends FabricLanguageProvider
{
    public SimplifiedChineseLanguageProvider(
        FabricPackOutput output,
        CompletableFuture<HolderLookup.Provider> registries
    ) {
        super(output, "zh_cn", registries);
    }

    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider registries, @NonNull TranslationBuilder builder) {
        TeamcraftTranslations.addSimplifiedChinese(builder);
    }
}

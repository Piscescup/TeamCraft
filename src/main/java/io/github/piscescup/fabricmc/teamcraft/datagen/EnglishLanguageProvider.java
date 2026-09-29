package io.github.piscescup.fabricmc.teamcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * Generates the {@code en_us} language file.
 */
public final class EnglishLanguageProvider extends FabricLanguageProvider
{
    public EnglishLanguageProvider(
        FabricPackOutput output,
        CompletableFuture<HolderLookup.Provider> registries
    ) {
        super(output, registries);
    }

    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider registries, @NonNull TranslationBuilder builder) {
        TeamcraftTranslations.addEnglish(builder);
    }
}

package net.bteuk.uk121.mixin;

import com.google.common.base.MoreObjects;
import net.bteuk.uk121.world.gen.EarthGenerator;
import net.bteuk.uk121.world.gen.biome.EarthBiomeSource;
import net.bteuk.uk121.world.gen.biome.EarthPopulationSource;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

@Mixin(GeneratorOptions.class)
public class MixinGeneratorOptions {
    @Inject(method = "fromProperties", at = @At("HEAD"), cancellable = true)
    private static void injectWorldTypes(DynamicRegistryManager registryManager, ServerPropertiesHandler.WorldGenProperties worldGenProperties, CallbackInfoReturnable<GeneratorOptions> cir) {

        // no server.properties file generated
        if (worldGenProperties.generatorSettings().get("level-type") == null) {
            return;
        }

        // check for our world type and return if so
        if (worldGenProperties.generatorSettings().get("level-type").toString().trim().toLowerCase().equals("earth")) {
            // get or generate seed
            String seed = (String) MoreObjects.firstNonNull(worldGenProperties.generatorSettings().get("level-seed"), "");
            long lSeed = new Random().nextLong();
            if (!seed.isEmpty()) {
                try {
                    long m = Long.parseLong(seed);
                    if (m != 0L) {
                        lSeed = m;
                    }
                } catch (NumberFormatException var14) {
                    lSeed = seed.hashCode();
                }
            }
            Registry<DimensionType> dimensionTypes = registryManager.get(Registry.DIMENSION_TYPE_KEY);
            Registry<Biome> biomes = registryManager.get(Registry.BIOME_KEY);
            Registry<ChunkGeneratorSettings> chunkGeneratorSettings = registryManager.get(Registry.CHUNK_GENERATOR_SETTINGS_KEY);
            Registry<DimensionOptions> dimensionOptions = DimensionType.createDefaultDimensionOptions(registryManager,lSeed);
            List<RegistryEntry<Biome>> biomeList = new ArrayList<>();
            biomeList.add(registryManager.get(Registry.BIOME_KEY).entryOf(BiomeKeys.PLAINS));
            FixedBiomeSource fixedBiomeSource = new FixedBiomeSource(registryManager.get(Registry.BIOME_KEY).entryOf(BiomeKeys.PLAINS));
            // return our chunk generator

            cir.setReturnValue(new GeneratorOptions(lSeed, false, false, GeneratorOptions.getRegistryWithReplacedOverworldGenerator(dimensionTypes, dimensionOptions, new EarthGenerator(fixedBiomeSource, fixedBiomeSource))));
        }

    }
}

package net.bteuk.uk121;

import net.bteuk.uk121.commands.Tpll;
import net.bteuk.uk121.mixin.GeneratorTypeAccessor;
import net.bteuk.uk121.world.gen.EarthGenerator;
import net.bteuk.uk121.world.gen.biome.EarthBiomeSource;
import net.bteuk.uk121.world.gen.biome.EarthPopulationSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UK121 implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LogManager.getLogger("uk121");

    public static final String directory = System.getProperty("user.dir") + "/uk121/";

    public static final String MOD_ID = "uk121";

    //Title Screen image.
    public static final Identifier TITLE_SCREEN = new Identifier("uk121:textures/gui/title/background/1.png");

    //Setup empty biome
    public static final RegistryKey<Biome> EMPTY_KEY = RegistryKey.of(Registry.BIOME_KEY, new Identifier(MOD_ID, "empty"));

    private Config config;
    public static Config CONFIG;


    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.



        //Register Biome Source for biome and population
        //Registry.register(Registry.BIOME_SOURCE, id("earth_population_source"), EarthPopulationSource.CODEC);
        //Registry.register(Registry.BIOME_SOURCE, id("earth_biome_source"), EarthBiomeSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, id("earth"), EarthGenerator.CODEC);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            GeneratorType EARTH = new GeneratorType("earth") {
                @Override
                protected ChunkGenerator getChunkGenerator(DynamicRegistryManager registryManager, long seed) {

                 List<RegistryEntry<Biome>> biomeList = new ArrayList<>();
                    biomeList.add(registryManager.get(Registry.BIOME_KEY).entryOf(BiomeKeys.PLAINS));

                    //EarthBiomeSource earthBiomeSource = new EarthBiomeSource(biomeList,registryManager.get(Registry.BIOME_KEY), 0, 0);
                    //Initiates a new biome population object. Fields: biomes, biome size, seed
                    //EarthPopulationSource earthPopulationSource = new EarthPopulationSource(biomeList,registryManager.get(Registry.BIOME_KEY), 0, 0);

                    FixedBiomeSource fixedBiomeSource = new FixedBiomeSource(registryManager.get(Registry.BIOME_KEY).entryOf(BiomeKeys.PLAINS));
                    //Returns a new EarthGenerator object, parsing the biome source in
                    return new EarthGenerator(fixedBiomeSource, fixedBiomeSource);
                }

                //Returns a new EarthGenerator class

            };
            GeneratorTypeAccessor.getValues().add(EARTH);
        }


        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            Tpll.register(dispatcher);
        }));



    }

    public Identifier id(String... path){
        return new Identifier(MOD_ID, String.join(".", path));
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_ID+"] " + message);
    }
}

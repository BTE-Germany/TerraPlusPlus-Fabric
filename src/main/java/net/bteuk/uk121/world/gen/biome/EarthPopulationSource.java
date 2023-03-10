package net.bteuk.uk121.world.gen.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bteuk.uk121.UK121;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.registry.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.bteuk.uk121.UK121.MOD_ID;

public class EarthPopulationSource extends BiomeSource {

    //Creates the CODEC for EarthBiomeSource.
    public static final Codec<EarthPopulationSource> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(RegistryOps.createRegistryCodec(
                                    Registry.BIOME_KEY).forGetter((biomeSource) -> biomeSource.BIOME_REGISTRY),
                            Codec.intRange(1, 20).fieldOf("biome_size").orElse(2).forGetter((biomeSource) -> biomeSource.biomeSize),
                            Codec.LONG.fieldOf("seed").stable().forGetter((biomeSource) -> biomeSource.seed))
                    .apply(instance, instance.stable(EarthPopulationSource::new)));


    private final Registry<Biome> BIOME_REGISTRY;
    //private final List<RegistryEntry<Biome>> BIOME_REGISTRY_ENTRY_LIST;
    public static Registry<Biome> LAYERS_BIOME_REGISTRY;
    private final long seed;
    private final int biomeSize;


    /*
    Constructor.
    Sets Registry<Biome> biomeRegistry with all existing biomes in the game.
    int BiomeSize and long seed are not applicable for this world generation type, however they are parameters nontheless.
     */
    public EarthPopulationSource(List<RegistryEntry<Biome>> biomeRegistryEntryList,Registry<Biome> biomeRegistry, int biomeSize, long seed) {
        super(biomeRegistryEntryList);
        /*super(biomeRegistry.streamEntries()
                .filter(entry -> entry.getKey().stream().equals(UK121.MOD_ID))
                .collect(Collectors.toList()));

        super(biomeRegistry.getEntrySet().stream()
                .filter(entry -> entry.getKey().getValue().getNamespace().equals(UK121.MOD_ID))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList()));
 */
        this.BIOME_REGISTRY = biomeRegistry;
        //this.BIOME_REGISTRY_ENTRY_LIST = biomeRegistryEntryList;
        EarthPopulationSource.LAYERS_BIOME_REGISTRY = biomeRegistry;
        this.biomeSize = biomeSize;
        this.seed = seed;
    }

    public EarthPopulationSource(Registry<Biome> biomes, int biomeSize, Long seed) {
        super(Stream.<RegistryEntry<Biome>>builder().build());
        this.BIOME_REGISTRY = biomes;
        EarthBiomeSource.LAYERS_BIOME_REGISTRY = biomes;
        this.biomeSize = biomeSize;
        this.seed = seed;
    }

    @Override
    protected Codec<? extends BiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long seed) {
        return new EarthPopulationSource(this.BIOME_REGISTRY, this.biomeSize, seed);
    }

    @Override
    public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
        return null;
    }

    /*
    Selects the biome for a specific x,z coordinate, y is not taken into account.
    To add y as a parameter simply edit the method parameters.
    Any methods that call this method will also need to be adjusted then.
     */
    public Biome sample(Registry<Biome> dynamicBiomeRegistry, int x, int z) {
        //No biome selection process has been added currently, defaults to plains.
        return dynamicBiomeRegistry.get(UK121.EMPTY_KEY);
    }


}

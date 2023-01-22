package net.bteuk.uk121.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.generator.ChunkDataLoader;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class EarthGenerator extends ChunkGenerator {

    EarthGeneratorSettings settings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);
    ChunkDataLoader loader = new ChunkDataLoader(settings);

    private RegistryEntry<Biome> biome;
    private static final Registry<StructureSet> EMPTY_STRUCTURE_REGISTRY = new SimpleRegistry<>(Registry.STRUCTURE_SET_KEY, Lifecycle.stable(), (x) -> null).freeze();

    public static final Codec<EarthGenerator> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                   // StructureSet.CODEC.fieldOf("structure_set_key").forGetter((EarthGenerator) -> EarthGenerator.field_37053),
                   // BiomeSource.CODEC.fieldOf("earth_population_source").forGetter((EarthGenerator) -> EarthGenerator.populationSource),
                    BiomeSource.CODEC.fieldOf("earth_biome_source").forGetter((EarthGenerator) -> EarthGenerator.biomeSource))
            .apply(instance, instance.stable(EarthGenerator::new)));


/*

        public static final Codec<EarthGenerator> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Biome.REGISTRY_CODEC.stable().fieldOf("biome").forGetter(g -> g.biome)
            ).apply(instance, instance.stable(EarthGenerator::new));
        });
    */
    public EarthGenerator(BiomeSource populationSource, BiomeSource biomeSource) {
       super(EMPTY_STRUCTURE_REGISTRY, Optional.empty(),populationSource, biomeSource, 0);
    }

    public EarthGenerator(BiomeSource biomeSource) {
       super(EMPTY_STRUCTURE_REGISTRY, Optional.empty(), biomeSource);
    }

     /*

       public EarthGenerator(RegistryEntry<Biome> biomeRegistryEntry) {
           super(EMPTY_STRUCTURE_REGISTRY,Optional.empty(),new FixedBiomeSource(biomeRegistryEntry));
       }

*/

    @Override
    protected Codec<? extends net.minecraft.world.gen.chunk.ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public net.minecraft.world.gen.chunk.ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public MultiNoiseUtil.MultiNoiseSampler getMultiNoiseSampler() {
        return MultiNoiseUtil.method_40443();
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver generationStep) {

    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, Chunk chunk) {

        final int minY = chunk.getBottomY();
        final int maxY = chunk.getTopY();

        try {
            int move = 0;
            CachedChunkData terraData = this.loader.load(new ChunkPos(chunk.getPos().x,chunk.getPos().z)).get();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {

                    int groundY = terraData.groundHeight(x, z);
                    int waterY = terraData.waterHeight(x, z);
                    net.buildtheearth.terraminusminus.substitutes.BlockState state = terraData.surfaceBlock(x, z);


                    //Generates stone under all surfaces

                    for (int y = minY; y < Math.min(maxY, groundY+move); y++) {
                        chunk.setBlockState(new BlockPos(x,y,z), Blocks.STONE.getDefaultState(), false);
                    }

                    //Genrates terrain with block states
                    if (groundY+move < maxY) {
                        if(state != null){
                            switch (state.getBlock().toString()) {
                                case "minecraft:dirt_path":
                                    chunk.setBlockState(new BlockPos(x,groundY+move,z), Blocks.MOSS_BLOCK.getDefaultState(), false);
                                    break;
                                case "minecraft:gray_concrete":
                                    chunk.setBlockState(new BlockPos(x,groundY+move,z), Blocks.GRAY_CONCRETE_POWDER.getDefaultState(), false);
                                    break;
                                case "minecraft:bricks":
                                    chunk.setBlockState(new BlockPos(x,groundY+move,z),Blocks.BRICKS.getDefaultState(), false);
                                    break;
                                default:
                                    chunk.setBlockState(new BlockPos(x,groundY+move,z), Blocks.GRASS_BLOCK.getDefaultState(), false);
                                    break;
                            }

                        } else {
                            chunk.setBlockState(new BlockPos(x,groundY+move,z), Blocks.GRASS_BLOCK.getDefaultState(), false);
                        }
                    }

                    for (int y = groundY+move + 1; y < Math.min(maxY, waterY+move); y++) chunk.setBlockState(new BlockPos(x,groundY+move,z), Blocks.WATER.getDefaultState(), false);

                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void populateEntities(ChunkRegion region) {

    }

    @Override
    public int getWorldHeight() {
        return 320;
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
        CompletableFuture<Chunk> cfc = new CompletableFuture<>();
        cfc.complete(chunk);
        return cfc;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinimumY() {
        return -64;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world) {
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world) {
        return null;
    }

    @Override
    public void getDebugHudText(List<String> text, BlockPos pos) {

    }

}

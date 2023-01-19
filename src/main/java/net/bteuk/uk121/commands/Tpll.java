package net.bteuk.uk121.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.buildtheearth.terraminusminus.dataset.IScalarDataset;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.generator.GeneratorDatasets;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class Tpll {

    private static final EarthGeneratorSettings bteGeneratorSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("tpll")
                .then(argument("coordinates", greedyString())
                        .executes(ctx -> run(ctx.getSource(), getString(ctx, "coordinates"))))
                .executes(ctx -> run(ctx.getSource(), null)));
    }

    public static int run(ServerCommandSource source, String arg) {

        final PlayerEntity player;
        try {
            player = source.getPlayer();
        } catch (CommandSyntaxException e) {
            source.sendError(Text.of("This command can only be executed by a player!"));
            return 1;
        }

        String[] args = arg.split(" ");

        double[] coordinates = new double[2];
        coordinates[1] = Double.parseDouble(args[0].replace(",", ""));
        coordinates[0] = Double.parseDouble(args[1]);

        double[] mcCoordinates = new double[0];
        try {
            mcCoordinates = bteGeneratorSettings.projection().fromGeo(coordinates[0], coordinates[1]);
        } catch (OutOfProjectionBoundsException e) {
            e.printStackTrace();
        }

        double altitude;
        if(player.getWorld().isChunkLoaded(player.getChunkPos().x,player.getChunkPos().z)){
            altitude = player.getWorld().getChunk(new BlockPos(mcCoordinates[0], 0, mcCoordinates[1])).sampleHeightmap(Heightmap.Type.WORLD_SURFACE,(int) mcCoordinates[0], (int) mcCoordinates[1]);
        }else{
            altitude = getHeight(coordinates[0], coordinates[1]).join();
        }

        player.teleport(mcCoordinates[0], altitude, mcCoordinates[1]);

        player.sendMessage( Text.of( "§2§lT++ (Fabric) §8» §7Teleported to " + coordinates[1] + ", " + coordinates[0] + "."),false);

        return 1;

    }

    public static CompletableFuture<Double> getHeight(double adjustedLon, double adjustedLat) {
        CompletableFuture<Double> altFuture;
        try {
            GeneratorDatasets datasets = new GeneratorDatasets(bteGeneratorSettings);


            altFuture = datasets.<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_HEIGHTS)
                    .getAsync(adjustedLon, adjustedLat)
                    .thenApply(a -> a + 1.0d);
        } catch (OutOfProjectionBoundsException e) {
            altFuture = CompletableFuture.completedFuture(0.0);
        }
        return altFuture;
    }

}

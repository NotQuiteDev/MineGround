package org.battle.mineground;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;

public class SchematicLoader {

    public void loadSchematic(String schematicName, Location location) {
        try {
            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
            if (!(plugin instanceof WorldEditPlugin)) {
                throw new IllegalStateException("WorldEdit plugin not found!");
            }

            WorldEditPlugin worldEditPlugin = (WorldEditPlugin) plugin;
            File schematicFile = new File(worldEditPlugin.getDataFolder(), "schematics/" + schematicName + ".schem");

            Clipboard clipboard;
            try (ClipboardReader reader = ClipboardFormats.findByFile(schematicFile).getReader(new FileInputStream(schematicFile))) {
                clipboard = reader.read();
            }

            World adaptedWorld = BukkitAdapter.adapt(location.getWorld());
            ClipboardHolder holder = new ClipboardHolder(clipboard);

            BlockVector3 to = BlockVector3.at(location.getX(), location.getY(), location.getZ());
            Operation operation = new ForwardExtentCopy(holder.getClipboard(), holder.getClipboard().getRegion(), holder.getClipboard().getOrigin(), adaptedWorld, to);

            Operations.complete(operation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

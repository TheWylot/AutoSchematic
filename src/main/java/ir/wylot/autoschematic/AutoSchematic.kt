package ir.wylot.autoschematic

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.WorldEditException
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.World
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.logging.Level

class AutoSchematic : JavaPlugin() {
    private var worldEditPlugin: WorldEditPlugin? = null
    private var schematicFile: String? = null
    private var x = 0
    private var y = 0
    private var z = 0

    override fun onEnable() {
        worldEditPlugin = server.pluginManager.getPlugin("WorldEdit") as WorldEditPlugin?
        if (worldEditPlugin == null) {
            logger.severe("WorldEdit plugin is not present")
            return
        }
        logger.severe( "Plugin has been initialized successfully.")

        loadConfig()
        loadSchematic()
    }

    private fun loadConfig() {
        config.addDefault("schematic-file", "schematic.schematic")
        config.addDefault("spawn-x", 0)
        config.addDefault("spawn-y", 0)
        config.addDefault("spawn-z", 0)
        config.options().copyDefaults(true)
        saveConfig()
        schematicFile = config.getString("schematic-file")
        x = config.getInt("spawn-x")
        y = config.getInt("spawn-y")
        z = config.getInt("spawn-z")
    }

    private fun loadSchematic() {
        val world = Bukkit.getWorlds()[0]
        if (world == null) {
            logger.severe("No worlds available")
            return
        }
        val format = ClipboardFormats.findByFile(File(schematicFile))
        if (format == null) {
            logger.severe("Invalid schematic file specified.")
            return
        }
        try {
            format!!.getReader(FileInputStream(schematicFile)).use { reader ->
                val clipboard = reader.read()
                if (clipboard == null) {
                    logger.severe("Failed to read schematic file")
                    return
                }
                try {
                    WorldEdit.getInstance().editSessionFactory.getEditSession(world as World, -1).use { editSession ->
                        val operation = ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(x, y, z))
                            .ignoreAirBlocks(false)
                            .build()
                        Operations.complete(operation)
                    }
                } catch (e: WorldEditException) {
                    logger.severe("Failed to load schematic: " + e.message)
                }
            }
        } catch (e: IOException) {
            logger.severe("Failed to read schematic file: " + e.message)
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
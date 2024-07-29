package com.jiangbai.prefabworldmod;


import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

@Mod.EventBusSubscriber(modid = Prefabworldmod.MODID)
public class WorldEventHandler {
    // 定义预制存档文件夹和预制世界的名称
    private static final String FOLDER_NAME = "prefab_saves";
    private static final String PRESET_WORLD_NAME = "preset_world";

    // 创建一个Logger实例
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldEventHandler.class);

    // 世界创建生成点时调用此方法
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.CreateSpawnPosition event) {
        // 确保只在服务器世界加载时执行
        if (!(event.getLevel() instanceof ServerLevel)) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) event.getLevel();
        MinecraftServer server = serverLevel.getServer();
        // 获取Minecraft根目录路径
        Path minecraftRootFolder = server.getServerDirectory().toPath();
        Path prefabFolder = minecraftRootFolder.resolve(FOLDER_NAME);

        LOGGER.info("Minecraft根目录路径: " + minecraftRootFolder.toAbsolutePath().toString());
        LOGGER.info("预制存档文件夹路径: " + prefabFolder.toAbsolutePath().toString());

        // 检查预制存档文件夹是否存在且为目录
        if (!Files.exists(prefabFolder) || !Files.isDirectory(prefabFolder)) {
            LOGGER.warn("预制存档文件夹不存在或不是目录: " + prefabFolder.toAbsolutePath().toString());
            return;
        }

        // 获取预制存档文件夹中的所有子文件夹
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(prefabFolder)) {
            boolean renamed = false;
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    // 重命名子文件夹为 preset_world
                    Path presetWorldFolder = prefabFolder.resolve(PRESET_WORLD_NAME);
                    try {
                        Files.move(path, presetWorldFolder, StandardCopyOption.REPLACE_EXISTING);
                        LOGGER.info("已将文件夹重命名为: " + presetWorldFolder.toAbsolutePath().toString());
                        renamed = true;
                        break; // 只重命名第一个找到的文件夹
                    } catch (IOException e) {
                        LOGGER.error("重命名文件夹时出错", e);
                    }
                }
            }

            if (!renamed) {
                LOGGER.warn("在预制存档文件夹中未找到任何目录: " + prefabFolder.toAbsolutePath().toString());
                return;
            }
        } catch (IOException e) {
            LOGGER.error("读取预制存档文件夹时出错", e);
            return;
        }

        // 获取预制世界文件夹路径
        Path presetWorldFolder = prefabFolder.resolve(PRESET_WORLD_NAME);

        // 检查预制世界文件夹是否存在且为目录
        if (Files.exists(presetWorldFolder) && Files.isDirectory(presetWorldFolder)) {
            try {
                // 将预制世界文件夹中的数据复制到当前世界文件夹中
                Path currentWorldFolder = serverLevel.getServer().getWorldPath(LevelResource.ROOT);
                Files.walk(presetWorldFolder).forEach(source -> {
                    Path destination = currentWorldFolder.resolve(presetWorldFolder.relativize(source).toString());
                    try {
                        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        LOGGER.error("复制预制存档时出错", e);
                    }
                });
                LOGGER.info("已加载预制存档: " + presetWorldFolder.toAbsolutePath().toString());
            } catch (IOException e) {
                // 捕捉可能的异常并记录错误信息
                LOGGER.error("加载预制存档时出错", e);
            }
        } else {
            // 如果预制世界文件夹不存在或不是目录，记录警告信息
            LOGGER.warn("预制存档文件夹不存在或不是目录: " + presetWorldFolder.toAbsolutePath().toString());
        }
    }
}

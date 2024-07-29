package com.jiangbai.prefabworldmod;

import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Mod.EventBusSubscriber(modid = Prefabworldmod.MODID)
public class FileManager {
    // 定义预制存档文件夹名称
    private static final String FOLDER_NAME = "prefab_saves";

    // 使用 SLF4J LoggerFactory 获取 Logger 实例
    private static final Logger LOGGER = LoggerFactory.getLogger(Prefabworldmod.MODID);

    // 服务器启动时调用此方法
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // 获取服务器目录并创建预制存档文件夹
        File prefabFolder = new File(event.getServer().getServerDirectory(), FOLDER_NAME);
        if (!prefabFolder.exists()) {
            // 如果文件夹不存在，则创建
            prefabFolder.mkdirs();
            LOGGER.info("预制存档文件夹已创建: " + prefabFolder.getAbsolutePath());
        } else {
            // 如果文件夹已存在，则输出日志信息
            LOGGER.info("预制存档文件夹已存在: " + prefabFolder.getAbsolutePath());
        }
    }
}

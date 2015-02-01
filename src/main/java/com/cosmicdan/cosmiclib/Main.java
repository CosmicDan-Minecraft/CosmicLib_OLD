package com.cosmicdan.cosmiclib;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Main.MODID, version = Main.VERSION)
public class Main
{
    public static final String MODID = "cosmiclib";
    public static final String VERSION = "0.0.1";
    
    @Mod.Instance(value = MODID)
    public static Main instance;
    
    //@SidedProxy(clientSide="com.cosmicdan.craftingoverhaul.client.ClientProxy", serverSide="com.cosmicdan.craftingoverhaul.server.CommonProxy")
    //public static CommonProxy proxy;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // initialize the proxy
        //proxy.preInit(event);
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        //proxy.init(event);
    }
    
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        //proxy.postInit(event);
    }
}

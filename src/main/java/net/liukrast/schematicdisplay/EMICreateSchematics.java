package net.liukrast.schematicdisplay;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(EMICreateSchematics.MOD_ID)
public class EMICreateSchematics {
    public static final String MOD_ID = "emi_create_schematics";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public EMICreateSchematics(IEventBus modBus) {
    }
}

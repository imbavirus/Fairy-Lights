package me.paulf.fairylights;

import me.paulf.fairylights.client.ClientProxy;
import me.paulf.fairylights.server.ServerProxy;
import me.paulf.fairylights.server.block.FLBlocks;
import me.paulf.fairylights.server.block.entity.FLBlockEntities;
import me.paulf.fairylights.server.connection.ConnectionType;
import me.paulf.fairylights.server.connection.ConnectionTypes;
import me.paulf.fairylights.server.creativetabs.FairyLightsItemGroup;
import me.paulf.fairylights.server.entity.FLEntities;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.server.item.crafting.FLCraftingRecipes;
import me.paulf.fairylights.server.net.NetBuilder;
import me.paulf.fairylights.server.net.clientbound.JingleMessage;
import me.paulf.fairylights.server.net.clientbound.OpenEditLetteredConnectionScreenMessage;
import me.paulf.fairylights.server.net.clientbound.UpdateEntityFastenerMessage;
import me.paulf.fairylights.server.net.serverbound.EditLetteredConnectionMessage;
import me.paulf.fairylights.server.net.serverbound.InteractionConnectionMessage;
import me.paulf.fairylights.server.sound.FLSounds;
import me.paulf.fairylights.server.string.StringType;
import me.paulf.fairylights.server.string.StringTypes;
import me.paulf.fairylights.util.CalendarEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.fml.common.Mod;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
// Network channel - will be fixed separately

import java.time.Month;
import java.util.function.Supplier;

@Mod(FairyLights.ID)
public final class FairyLights {
    public static final String ID = "fairylights";

    public static final ResourceLocation STRING_TYPE = ResourceLocation.fromNamespaceAndPath(ID, "string_type");

    public static final ResourceLocation CONNECTION_TYPE = ResourceLocation.fromNamespaceAndPath(ID, "connection_type");

    @SuppressWarnings("Convert2MethodRef")
    public static final Object NETWORK = new NetBuilder(ResourceLocation.fromNamespaceAndPath(ID, "net"))
        .version(1).optionalServer().requiredClient()
        .clientbound(JingleMessage::new).consumer(() -> new JingleMessage.Handler())
        .clientbound(() -> new UpdateEntityFastenerMessage(0, new net.minecraft.nbt.CompoundTag())).consumer(() -> new UpdateEntityFastenerMessage.Handler())
        .clientbound(OpenEditLetteredConnectionScreenMessage::new).consumer(() -> new OpenEditLetteredConnectionScreenMessage.Handler())
        .serverbound(InteractionConnectionMessage::new).consumer(() -> new InteractionConnectionMessage.Handler())
        .serverbound(EditLetteredConnectionMessage::new).consumer(() -> new EditLetteredConnectionMessage.Handler())
        .build();

    public static final CalendarEvent CHRISTMAS = new CalendarEvent(Month.DECEMBER, 24, 26);

    public static final CalendarEvent HALLOWEEN = new CalendarEvent(Month.OCTOBER, 31, 31);

    public static Supplier<Registry<ConnectionType<?>>> CONNECTION_TYPES;
    public static Supplier<Registry<StringType>> STRING_TYPES;

    public FairyLights(final IEventBus modEventBus) {
        final IEventBus bus = modEventBus;
        
        FLSounds.REG.register(bus);
        FLBlocks.REG.register(bus);
        FLEntities.REG.register(bus);
        FLItems.REG.register(bus);
        FLBlockEntities.REG.register(bus);
        FLCraftingRecipes.REG.register(bus);
        ConnectionTypes.REG.register(bus);
        StringTypes.REG.register(bus);
        
        // Create custom registries - ConnectionTypes and StringTypes already create the DeferredRegisters
        // Set up registry access - will be available after registration
        // DeferredRegister may not have getRegistry() - try accessing through registry manager
        // For now, use a supplier that accesses the registry when needed
        // registryOrThrow() needs a RegistryKey, not ResourceLocation
        CONNECTION_TYPES = () -> net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY).registryOrThrow(net.minecraft.resources.ResourceKey.createRegistryKey(FairyLights.CONNECTION_TYPE));
        STRING_TYPES = () -> net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY).registryOrThrow(net.minecraft.resources.ResourceKey.createRegistryKey(FairyLights.STRING_TYPE));
        final ServerProxy proxy = EffectiveSide.get().isClient() ? new ClientProxy() : new ServerProxy();
        proxy.init(bus);
        FairyLightsItemGroup.TAB_REG.register(modEventBus);
        
        // Register networking payloads
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {
            final PayloadRegistrar registrar = event.registrar(FairyLights.ID);
            final NetBuilder network = (NetBuilder) NETWORK;
            network.setRegistrar(registrar);
            
            // Register UpdateEntityFastenerMessage as clientbound payload
            registrar.playToClient(
                UpdateEntityFastenerMessage.TYPE,
                UpdateEntityFastenerMessage.STREAM_CODEC,
                UpdateEntityFastenerMessage::handle
            );
        });

    }


}

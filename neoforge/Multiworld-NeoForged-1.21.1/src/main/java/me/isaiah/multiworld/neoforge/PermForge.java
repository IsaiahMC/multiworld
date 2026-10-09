package me.isaiah.multiworld.neoforge;

import java.util.HashMap;
import java.util.Map;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.server.network.ServerPlayerEntity;
import me.isaiah.multiworld.perm.Perm;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.exceptions.UnregisteredPermissionException;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

public class PermForge extends Perm {

    private static final Map<String, PermissionNode<Boolean>> NODES = new HashMap<>();

    static {
        addNode("cmd");
        addNode("admin");
        addNode("setspawn");
        addNode("spawn");
        addNode("gamerule");
        addNode("difficulty");
        addNode("tp");
        addNode("create");
        addNode("portal");
    }

    private static void addNode(String node) {
        String permission = MultiworldMod.MOD_ID + "." + node;
        NODES.put(permission, new PermissionNode<>(MultiworldMod.MOD_ID, node, PermissionTypes.BOOLEAN,
                (player, uuid, context) -> player != null && player.hasPermissionLevel(2)));
    }

    public static void init() {
        Perm.setPerm(new PermForge());
        NeoForge.EVENT_BUS.addListener(PermForge::registerPermissionNodes);
    }

    private static void registerPermissionNodes(PermissionGatherEvent.Nodes event) {
        for (PermissionNode<Boolean> node : NODES.values()) {
            event.addNodes(node);
        }
    }

    @Override
    public boolean has_impl(ServerPlayerEntity plr, String perm) {
        boolean cyber = ModList.get().getModContainerById("cyberpermissions").isPresent();
        
        boolean res = plr.hasPermissionLevel(2);

        if (cyber) {
            if (CyberHandler.hasPermission(plr, perm)) res = true;
        }
        PermissionNode<Boolean> node = NODES.get(perm);
        if (node != null && PermissionAPI.getActivePermissionHandler() != null) {
            try {
                if (PermissionAPI.getPermission(plr, node)) res = true;
            } catch (UnregisteredPermissionException ignored) {
                // PermissionAPI may be queried before NeoForge gathers nodes; keep OP/Cyber fallback.
            }
        }
        return res;
    }
}

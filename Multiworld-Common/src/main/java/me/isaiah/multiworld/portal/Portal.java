package me.isaiah.multiworld.portal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.PortalCommand;
import me.isaiah.multiworld.command.SpawnCommand;
import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

/**
 * Representation of a Multiworld Portal
 * 
 * @see {@link https://mvplugins.org/portals/fundamentals/basic-usage/}
 */
public class Portal {

	// public ArrayList<BlockPos> blocks;
	// public ArrayList<Long> po

	private String name;
	private String owner;
	private Identifier worldIn;
	
	// private BlockPos fromPos;
	// private BlockPos toPos;
	
	private BlockPos minEdge;
	private BlockPos maxEdge;
	
	private String destination;
	
	private BlockPos destPos;
	
	private Portal(String name, String owner, Identifier worldId, String destination) {
		// this.blocks = new ArrayList<>();
		this.name = name;
		this.owner = owner;
		this.worldIn = worldId;
		this.destination = destination;
	}
	
	public Portal(String name, String owner, Identifier worldId, String destination, BlockPos a, BlockPos b) {
		this(name, owner, worldId, destination);
		
		this.minEdge = PortalUtil.getMinPos(a, b);
		this.maxEdge = PortalUtil.getMaxPos(a, b);
		
		//this.fromPos = from;
		//this.toPos = to;
	}
	
	public Portal(String name, String owner, Identifier worldId, String destination, String location) {
		this(name, owner, worldId, destination);

		try {
			this.locationFromString(location);
		} catch (ArrayIndexOutOfBoundsException e) {
			MultiworldMod.LOGGER.info("Oops!");
			e.printStackTrace();
		}
		
	}

	public BlockPos getMinPos() {
		return minEdge;
	}
	
	public BlockPos getMaxPos() {
		return maxEdge;
	}
	
	public int getMaxY(int offset) {
		if (minEdge.getY() > maxEdge.getY()) {
			return minEdge.getY();
		}
		return maxEdge.getY() + offset;
	}
	
	public BlockPos getCenterExit() {
		return PortalUtil.getCenterWithLowestY(minEdge, maxEdge, 0);
	}
	
	
	private void locationFromString(String s) {
		String[] spl = s.split(Pattern.quote(":"));
		String[] from = spl[0].split(Pattern.quote(","));
		String[] to = spl[1].split(Pattern.quote(","));
		
		double x1 = Double.valueOf(from[0]);
		double y1 = Double.valueOf(from[1]);
		double z1 = Double.valueOf(from[2]);
		
		double x2 = Double.valueOf(to[0]);
		double y2 = Double.valueOf(to[1]);
		double z2 = Double.valueOf(to[2]);
		
		BlockPos a = MultiworldMod.get_world_creator().get_pos(x1, y1, z1);
		BlockPos b = MultiworldMod.get_world_creator().get_pos(x2, y2, z2);

		this.minEdge = PortalUtil.getMinPos(a, b);
		this.maxEdge = PortalUtil.getMaxPos(a, b);
		
		/*
		this.fromPos = MultiworldMod.get_world_creator().get_pos(x1, y1, z1);
		this.toPos = MultiworldMod.get_world_creator().get_pos(x2, y2, z2);
		*/
	}

	public void addToMap() {
		// TODO Auto-generated method stub
	}
	
	/**
     * Load an existing saved portals from config (YAML) 
     */
	public static int reinit_portals_from_config(MinecraftServer mc) {
		File config_dir = new File("config");
        config_dir.mkdirs();
        
        File cf = new File(config_dir, "multiworld"); 
        cf.mkdirs();

        File wc = new File(cf, "portals.yml");
        FileConfiguration config;
        try {
			if (!wc.exists()) {
				wc.createNewFile();
				return 0;
			}
            config = new FileConfiguration(wc);
            
            if (!config.hasSection("portals")) {
            	MultiworldMod.LOGGER.info("No save portals to load");
            	return 0;
            }

            LinkedHashMap <String, Object> sect = config.getSection("portals");
            Set<String> keys = sect.keySet();
            
            int loaded = 0;
            for (String name : keys) {
            	
        		String prefix = "portals." + name;
            	
        		String owner = config.getString(prefix + ".owner");
        		String location = config.getString(prefix + ".location");
        		String world = config.getString(prefix + ".world");
        		String dest = config.getString(prefix + ".destination");
        		
        		Identifier worldIn = MultiworldMod.new_id(world);
        		
        		Portal p = new Portal(name, owner, worldIn, dest, location);
        		
        		// Refresh Portal Frame
        		p.buildPortalArea(p.getMinPos(), p.getMaxPos(), p.getOriginWorld());
        		
            	PortalCommand.addKnownPortal(name, p);
            	loaded += 1;
            }
            return loaded;
        } catch (Exception e) {
            e.printStackTrace();
            // throw e;
        }
        return 0;
	}

	public void save() throws IOException {
		File config_dir = new File("config");
        config_dir.mkdirs();
        
        File cf = new File(config_dir, "multiworld"); 
        cf.mkdirs();

        String name = this.getName();
		String prefix = "portals." + name;

        File wc = new File(cf, "portals.yml");
        FileConfiguration config;
        try {
			if (!wc.exists()) {
				wc.createNewFile();
			}
            config = new FileConfiguration(wc);

            // Copied from Multiverse-Portals 5.0.3
			config.set(prefix + ".entryfee.amount", 0.0);
			config.set(prefix + ".safeteleport", true);
			config.set(prefix + ".teleportnonplayers", false);
			config.set(prefix + ".handlerscript", "''");

			config.set(prefix + ".owner", this.getOwner()); // player
			config.set(prefix + ".location", this.getLocationConfigString()); // x1,y1,z1:x2,y2,z2
			config.set(prefix + ".world", this.getOriginWorldId());
			config.set(prefix + ".destination", this.getDestination());

			config.save();
        } catch (Exception e) {
            // e.printStackTrace();
            throw e;
        }
	}
	
	/**
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 */
	public Identifier getOriginWorldId() {
		return this.worldIn;
	}

	/**
	 */
	/*
	public BlockPos getFromLocation() {
		return this.fromPos;
	} 
	*/
	
	/**
	 */
	public BlockPos getDestLocation() {
		if (null == this.destPos) {
			// this.destPos = SpawnCommand.getSpawn(this.getDestWorld());
			return this.findDestPos();
		}
		
		return this.destPos;
	}
	
	/**
	 * Retrieve the Portal's Bounds location in the Multiverse config form
	 */
	public String getLocationConfigString() {
		return this.minEdge.getX() + "," + this.minEdge.getY() + "," + this.minEdge.getZ() + ":" +
				this.maxEdge.getX() + "," + this.maxEdge.getY() + "," + this.maxEdge.getZ();
	}
	
	/**
	 */
	public String getOwner() {
		return this.owner;
	}
	
	/**
	 * The destination of the Portal, in Multiverse format.
	 * 
	 * @see {@link https://mvplugins.org/core/reference/destinations/}
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * @return 
	 */
	public BlockPos findDestPos() {
		String name = this.getDestination();

		String[] spl = name.split(Pattern.quote(":"));
		
		String start = spl[0];
		if (start.length() > 1) {
			return SpawnCommand.getSpawn(this.getDestWorld());
		}
		
		if (start.equalsIgnoreCase("w")) {
			return SpawnCommand.getSpawn(this.getDestWorld());
		}
		
		if (start.equalsIgnoreCase("p")) {
			Portal pp = PortalCommand.getKnownPortal( name.split(Pattern.quote("p:"))[1] );
			if (null != pp) {
				
				Direction.Axis axis = (Math.abs(pp.getMinPos().getX() - getMaxPos().getX()) > Math.abs(getMinPos().getZ() - getMaxPos().getZ()))
		        	    ? Direction.Axis.X
		        	    : Direction.Axis.Z;
			
				BlockPos cen = PortalUtil.findSafeExit(pp.getDestWorld(), pp.getCenterExit(), 2, getMaxY(4));
				if (axis == Axis.X) {
					cen = cen.add(0, 0, 2);
				} else {
					cen = cen.add(3, 0, 0);
				}
				
				return cen;
			}
		}
		
		if (start.equalsIgnoreCase("e")) {
			// World+Location
			for (int i = 0; i < spl.length; i++) {
				String vall = spl[i];
				if (vall.indexOf(',') != -1) {
					// Location details
					if (vall.split(Pattern.quote(",")).length != 3) {
						// Don't Have x,y,z
						return SpawnCommand.getSpawn(this.getDestWorld());
					}
					BlockPos pos = PortalUtil.blockPosFrom(vall);
					return pos;
				}
				
			}
			
		}
		if (start.equalsIgnoreCase("a")) {
			// Anchor
			// note: multiverse-portals docs don't mention this.
		}
		return SpawnCommand.getSpawn(this.getDestWorld());
	}

	public String getDestWorldName() {
		String name = this.getDestination();
		if (name.startsWith("p:")) {
			// Portal Dest
			Portal pp = PortalCommand.getKnownPortal( name.split(Pattern.quote("p:"))[1] );
			if (null != pp) return pp.getOriginWorldId().toString();
		}
		
		if (name.startsWith("e:")) {
			name = PortalUtil.extractIdentifier(name);
		}

		if (name.indexOf(':') == -1) name = "multiworld:" + name;
		return name;
	}
	
	/**
	 */
	public ServerWorld getDestWorld() {
		
		String name = this.getDestination();
		
		if (name.startsWith("p:")) {
			// Portal Dest
			Portal pp = PortalCommand.getKnownPortal( name.split(Pattern.quote("p:"))[1] );
			if (null != pp) {
				return pp.getOriginWorld();
			}
		}
		
		if (name.startsWith("e:")) {
			name = PortalUtil.extractIdentifier(name);
		}
		
		if (name.indexOf(':') == -1) name = "multiworld:" + name;
		
		HashMap<String,ServerWorld> worlds = new HashMap<>();
        MultiworldMod.mc.getWorldRegistryKeys().forEach(r -> {
            ServerWorld world = MultiworldMod.mc.getWorld(r);
            worlds.put(r.getValue().toString(), world);
        });
        
        ServerWorld w = worlds.get(name);
        // BlockPos sp = SpawnCommand.getSpawn(w);
		return w;
	}
	
	/**
	 */
	public ServerWorld getOriginWorld() {
		String name = this.getOriginWorldId().toString();
		HashMap<String,ServerWorld> worlds = new HashMap<>();
        MultiworldMod.mc.getWorldRegistryKeys().forEach(r -> {
            ServerWorld world = MultiworldMod.mc.getWorld(r);
            worlds.put(r.getValue().toString(), world);
        });
        ServerWorld w = worlds.get(name);
		return w;
	}
	
	@Deprecated
	public void fillBlocks(BlockPos pos, ServerWorld w) {
		// fill outside frame
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 5; y++) {
				if (x == 0 || x == 3 || y == 0 || y == 4) {
					BlockPos pos2 = pos.add(x, y, 0);
					w.setBlockState(pos2, Blocks.STONE.getDefaultState());
				}
			}
		}

		// Fill the inner section with portal
		for (int x = 1; x < 3; x++) {
			for (int y = 1; y < 4; y++) {
				BlockPos pos2 = pos.add(x, y, 0);
				//this.blocks.add(pos2);
				w.setBlockState(pos2, Blocks.NETHER_PORTAL.getDefaultState());
			}
		}

	}
	
	public void refreshPortalArea() {
		this.buildPortalArea(this.getMinPos(), this.getMaxPos(), this.getOriginWorld());
	}
	
	public void buildPortalArea(BlockPos pos1, BlockPos pos2, ServerWorld world) {
	    int minX = Math.min(pos1.getX(), pos2.getX());
	    int minY = Math.min(pos1.getY(), pos2.getY());
	    int minZ = Math.min(pos1.getZ(), pos2.getZ());

	    int maxX = Math.max(pos1.getX(), pos2.getX());
	    int maxY = Math.max(pos1.getY(), pos2.getY());
	    int maxZ = Math.max(pos1.getZ(), pos2.getZ());

	    ArrayList<BlockPos> innerBlocks = new ArrayList<>();
	    
	    for (int x = minX; x <= maxX; x++) {
	        for (int y = minY; y <= maxY; y++) {
	            for (int z = minZ; z <= maxZ; z++) {
	                BlockPos currentPos = new BlockPos(x, y, z);

	                int edgeCount = 0;
	                if (x == minX || x == maxX) edgeCount++;
	                if (y == minY || y == maxY) edgeCount++;
	                if (z == minZ || z == maxZ) edgeCount++;

	                boolean isOnEdge = edgeCount >= 2;

	                if (isOnEdge) {
	                    // Frame block
	                	if (world.isAir(currentPos)) {
	                		world.setBlockState(currentPos, Blocks.OBSIDIAN.getDefaultState());
	                	}
	                } else {
	                    // Inner portal
	                	innerBlocks.add(currentPos);
	                }
	            }
	        }
	    }
	    
	    Direction.Axis axis = (Math.abs(pos1.getX() - pos2.getX()) > Math.abs(pos1.getZ() - pos2.getZ()))
        	    ? Direction.Axis.X
        	    : Direction.Axis.Z;
	    
	    // Set the portal blocks after we have a complete frame
	    for (BlockPos currentPos : innerBlocks) {
	    	BlockState portalState = Blocks.NETHER_PORTAL.getDefaultState().with(NetherPortalBlock.AXIS, axis);
            world.setBlockState(currentPos, portalState);
	    }
	}


}

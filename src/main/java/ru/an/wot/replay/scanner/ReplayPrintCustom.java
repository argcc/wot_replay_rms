package ru.an.wot.replay.scanner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import ru.an.wot.replay.ByteArraySlice;
import ru.an.wot.replay.Main;
import ru.an.wot.replay.PacketIteratorCallback;
import ru.an.wot.replay.Replay;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class ReplayPrintCustom implements PacketIteratorCallback {

	protected Replay replay;
	protected int counter = 0;
	protected FileWriter out;
	protected String sep = System.lineSeparator();
	

	public ReplayPrintCustom(Replay replay, FileWriter out) throws Throwable {
		super();
		this.replay = replay;
		this.out = out;
		
		this.replay.iteratePackets(this);
	}
	
	@Override
	public boolean doPacket(ByteArraySlice packet) {
		int type = packet.getInt(4);
		float time = packet.getFloat(8);
		
		if(type != 8 && type != 26) return true;
		
		if(type != 8) return true;
		if(packet.getInt(16) != 45) return true;
		
		
		try {
			
			if(counter % 20 == 0){
				out.write("                ");
				for(int i=0; i <= 80; i+=4) {
					if(i == 12 || i == 24 ) out.write("  ");
					out.write(String.format("%9s", String.format("%02d", i)));
				}
				out.write(sep);
			}
			++counter;
			
			if(type == 8 && packet.getInt(16) == 0)
				out.write(String.format("%16s", type + " SHOT " + time));
			else
				out.write(String.format("%16s", type + " ---- " + time));
			
			if(type == 26) {
				Vector3d tpos = new Vector3d(packet.getFloat(12), packet.getFloat(16), packet.getFloat(20));
				Vector3d cpos = new Vector3d(packet.getFloat(28), packet.getFloat(32), packet.getFloat(36));
				Vector3d trot = new Vector3d(packet.getFloat(40), packet.getFloat(44), packet.getFloat(48));
				
				double trad = packet.getFloat(24);
				double crad = packet.getFloat(52);
		
				out.write(String.format("  RECTICLE: tpos:%s cpos:%s trot:%s trad:%f crad:%f", 
						tpos.toString(), cpos.toString(),
						trot.toString(), trad, crad));
			}
			else if(type == 8 && packet.getInt(16) == 26) {
				
				int vehicleId = packet.getInt(24);
				int projectileId = packet.getInt(28);
				
				Vector3d pos = new Vector3d(packet.getFloat(34), packet.getFloat(38), packet.getFloat(42));
				Vector3d rot = new Vector3d(packet.getFloat(46), packet.getFloat(50), packet.getFloat(54));
				
				out.write(String.format("  PROJECTILE: vehicle: %08X projectile id: %08X pos:%s rot:%s length:%f HEX: %08X %08X %02X",
						Main.byteReverse(vehicleId), Main.byteReverse(projectileId),
						pos.toString(), rot.toString(), rot.length(),
						Main.byteReverse(packet.getInt(58)), Main.byteReverse(packet.getInt(62)), packet.getByte(66)));
			}
			else if(type == 8 && packet.getInt(16) == 45) {
				int projectileId = packet.getInt(24);
				out.write(String.format("  PROJECTILE_HIT: projectile id: %08X pos:%s dir:%s",
						Main.byteReverse(projectileId),
						new Vector3d(packet.getFloat(30), packet.getFloat(34), packet.getFloat(38)).toString(),
						new Vector3d(packet.getFloat(42), packet.getFloat(46), packet.getFloat(50)).toString()));
			}
			else{
				out.write("  HEX:");
				for(int i = 0; i < packet.getLength(); ++i) {
					if(i == 12) out.write(" >");
					if(i == 24) out.write(" >");
					out.write(String.format("%s%02X", (i%4==0)?" ":"", packet.getByte(i)));
				}
			}
			out.write(sep);
		}
		catch(IOException e) {throw new RuntimeException(e);}
		return true;
	}

}

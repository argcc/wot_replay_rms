package ru.an.wot.replay.scanner;

import java.util.LinkedList;
import javax.vecmath.Vector3d;

import ru.an.wot.replay.ByteArraySlice;
import ru.an.wot.replay.PacketIteratorCallback;
import ru.an.wot.replay.Replay;
import ru.an.wot.replay.ReplayException;

public class ReplayShots implements PacketIteratorCallback {
	
	public static interface ShotCallback{
		public void shot(float time, double displacePercentage) throws Throwable;
	}

	protected LinkedList<ByteArraySlice> recticle = new LinkedList<ByteArraySlice>();
	protected Replay replay;
	protected ShotCallback shotCB;
	
	protected int recticleSubType = -1;
	protected int projectileSubType = -1;
	protected int projectileDisp = -1;
	protected int prjSTypeByte = -1;
	protected int prjSTypeShort = -1;
	
	protected float battleStartTime = 0.0f;
	
	public ReplayShots(Replay replay, ShotCallback shotCB) throws Throwable {
		super();
		this.replay = replay;
		this.shotCB = shotCB;
		this.battleStartTime = 0.0f;
		initTypes();
		this.replay.iteratePackets(this);
	}
	
	protected void initTypes() {
		/*
		if(replay.clientVersionExe.equals("0, 9, 0, 0") 
				|| replay.clientVersionExe.endsWith("0, 9, 1, 0")
				|| replay.clientVersionExe.endsWith("0, 9, 2, 0")) {
			projectileSubType = 17;
			recticleSubType = 14;
			projectileDisp = 1;
			prjSTypeByte = 0x44;
		}
		
		if(replay.clientVersionExe.endsWith("0, 9, 3, 0")
				|| replay.clientVersionExe.endsWith("0, 9, 4, 0")) {
			projectileSubType = 17;
			recticleSubType = 14;
			projectileDisp = 2;
			prjSTypeShort = 0x4434;
		}
		*/
		
		
		if(replay.clientVersionExe.endsWith("0.9.23.0")) {
			projectileSubType = 27;
			recticleSubType = 22;
			projectileDisp = 2;
			prjSTypeShort = 0x4434;
		}
		
		if(replay.clientVersionExe.endsWith("1.0.1.0")) {
			projectileSubType = 0x29;
			recticleSubType = 0x23;
			projectileDisp = 2;
			prjSTypeShort = 0x4434;
		}
		
		if(projectileSubType == -1 || projectileDisp == -1 || recticleSubType == -1)
			throw new ReplayException("Не получилось определить идентификаторы типов");
	}
	
	@Override
	public boolean doPacket(ByteArraySlice packet) throws Throwable {
		int type = packet.getInt(4);
		float time = packet.getFloat(8);
		
		if(type == 22 && packet.getInt(12) == 3)
			battleStartTime = packet.getFloat(8);
		
		if(type != 8) return true;

		if(packet.getInt(16) == recticleSubType)
			recticle.add(packet.copy());
		
		while(recticle.size() > 2) recticle.removeFirst();
		
		if(packet.getInt(16) == projectileSubType && 
			packet.getInt(24) == replay.vehicleId &&
			((prjSTypeByte!=-1 && packet.getByte(64) == prjSTypeByte) || 
			(prjSTypeShort!=-1 && packet.getShort(64) == prjSTypeShort))
			) 
		{
			if(recticle.size() != 2) throw new ReplayException("Не обнаружен серверный прицел");
			
			ByteArraySlice rcl = recticle.getLast();
			ByteArraySlice rc = rcl;
			
			if(rcl.getInt(8) == packet.getInt(8) || rcl.getFloat(8) > packet.getFloat(8))
				rc = recticle.getFirst();
			
			if(Math.abs(rc.getFloat(8) - time) > 0.4f)
				return true;
			
			processShot(rc, packet);
		}

		return true;
	}
	
	protected void processShot(ByteArraySlice recticle, ByteArraySlice projectile) throws Throwable {
		
		Vector3d projectileDir = new Vector3d(
				projectile.getFloat(44 + projectileDisp), 
				projectile.getFloat(48 + projectileDisp), 
				projectile.getFloat(52 + projectileDisp));
		
		Vector3d cdir = new Vector3d(
				recticle.getFloat(40),
				recticle.getFloat(44),
				recticle.getFloat(48));
		
		double crad = recticle.getFloat(52);
		double angle = projectileDir.angle(cdir);
		double maxAngle = Math.atan(crad)*2.0f;
		double displacePercentage = 100*angle/maxAngle;
		shotCB.shot(projectile.getFloat(8) - battleStartTime, displacePercentage);

	}
	
}

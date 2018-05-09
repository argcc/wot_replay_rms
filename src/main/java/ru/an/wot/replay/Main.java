package ru.an.wot.replay;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.Inflater;

import org.json.*;
import ru.an.wot.replay.scanner.ReplayNearShotEvetns;
import ru.an.wot.replay.scanner.ReplayPacket8;
import ru.an.wot.replay.scanner.ReplayPrintAll;
import ru.an.wot.replay.scanner.ReplayPrintCustom;
import ru.an.wot.replay.scanner.ReplayShots;
import ru.an.wot.replay.scanner.ReplayStates;
import ru.an.wot.replay.scanner.ReplaySummary;
import ru.an.wot.replay.ui.MainWindow;
import ru.an.wot.replay.ui.ProgressBar;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.print.attribute.SupportedValuesAttribute;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;


public class Main {
	
	public static int byteReverse(int v) {
		return  ((v << 24) & 0xFF000000) |
				((v << 8) & 0x00FF0000) |
				((v >> 8) & 0x0000FF00) |
				((v >> 24) & 0x000000FF);
	}
	
	/*public static void PrintHeader(Replay replay) {
		String sep = System.lineSeparator();
		StringBuffer sb = new StringBuffer(sep);

		sb.append("     Version EXE: " + replay.clientVersionExe + sep);
		sb.append("     Version XML: " + replay.clientVersionXml + sep);
		sb.append("     Battle Type: " + replay.battleType + sep);
		sb.append("        Gameplay: " + replay.gameplayId + sep);
		sb.append("Info Block Count: " + replay.infoBlockCount + sep);
		sb.append("             Map: " + replay.mapName + sep);
		sb.append("          Player: " + replay.playerName + sep);
		sb.append(String.format(
				  "       Player ID: %d [%08X][rvs: %08X]"+sep, replay.playerId, replay.playerId, byteReverse(replay.playerId)));
		sb.append("         Vehicle: " + replay.vehicleName + sep);
		sb.append(String.format(
				  "      Vehicle ID: %d [%08X][rvs: %08X]"+sep, replay.vehicleId, replay.vehicleId, byteReverse(replay.vehicleId)));
	}*/
	
	
	public static void main(String[] args) throws Throwable{
		
//		File file = new File("E:/Games/World_of_Tanks_RU/replays/20180508_2304_italy-It13_Progetto_M35_mod_46_208_bf_epic_normandy.wotreplay");
//		CheckHeader headerChecker = new CheckHeader();
//		ByteArraySlice r = new ByteArraySlice(Files.readAllBytes(Paths.get(file.getPath())));
//		Replay replay = new Replay(r, headerChecker);
		
//		replay.iteratePackets(new ReplayStates());
		
//		System.out.println(replay.header.toString(2));
		
//		new ReplayPrintAll(replay, new FileWriter(new File("replay_all.txt")));
		
//		System.out.println(new ReplayNearShotEvetns(replay, 0.0f).getResult());
		
//		replay.iteratePackets(new ReplayStates());
		
		
		MainWindow mw = new MainWindow();
	}
}



package ru.an.wot.replay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONException;

import ru.an.wot.replay.scanner.ReplayShots;

public class Storage implements ReplayShots.ShotCallback {
	
	protected Connection conn = null;
	
	protected String patch, replay, vehicle;
	
	protected String replayDate;
	
	protected PreparedStatement putShot;
	
	protected Statement stmt;
	
	protected StringBuffer errorBuffer = new StringBuffer();
	
	protected static final SimpleDateFormat DATETIME= new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	protected static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy-MM-dd");
	protected static final long HOUR = 1000*60*60;
	public static long hourDisplace = 5;
	
 	public Storage() throws SQLException {
		conn = DriverManager.getConnection("jdbc:sqlite:");

		stmt = null;
		stmt = conn.createStatement();
		stmt.execute("create table shot(patch text, replay text, replay_date text, vehicle text, shot_time real, shot_displace real)");
		stmt.execute("CREATE INDEX idx_shot_patch ON shot (patch)");
		stmt.execute("CREATE INDEX idx_shot_replay ON shot (replay)");
		stmt.execute("CREATE INDEX idx_shot_vehicle ON shot (vehicle)");
		stmt.execute("begin");
			
		putShot = conn.prepareStatement("insert into shot values(?,?,?,?,?,?)");

	}
	
	public void close() throws SQLException {
		if(stmt != null) {
			stmt.execute("end");
			stmt.close();
		}
		if(putShot != null)
			putShot.close();
		if(conn != null)
			conn.close();
	}
	
	public void setCurrentReplay(String replayName, Replay replay) throws JSONException, ParseException {
		this.replay = replayName;
		patch = replay.clientVersionExe + ":" + replay.clientVersionXml;
		vehicle = replay.vehicleName;
		//"24.03.2018 09:21:43"
		replayDate = DATE.format(new Date(DATETIME.parse(replay.header.getString("dateTime")).getTime() - hourDisplace*HOUR));
	}
	
	private void writeToFile(String filename, String data) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter out = new PrintWriter(filename, "utf-8");
		out.print(data);
		out.close();
	}
	
	public void save(File dir, boolean detailed) throws SQLException, FileNotFoundException, UnsupportedEncodingException {
		
		String LS = System.lineSeparator();
		
		if(errorBuffer.length() > 0)
			writeToFile(dir.getPath()+File.separator+"errors.txt", errorBuffer.toString());
		
		//вывод детальной информации
		if(detailed) {
			StringBuffer sb = new StringBuffer();
			sb.append("Replay;Time;Displace percentage").append(LS);
			ResultSet rs = stmt.executeQuery("select replay, shot_time, shot_displace from shot order by 1, 2");
			String prevReplayName="";
			try {
				while(rs.next()) {
					String replayName = rs.getString(1);
					double time = rs.getDouble(2);
					double displace = rs.getDouble(3);
					
					if(!replayName.equals(prevReplayName))
						sb.append(replayName).append(";");
					else
						sb.append(";");
					prevReplayName = replayName;
					
					sb.append(Double.toString(time).replace(".", ","))
					.append(";")
					.append(Double.toString(displace).replace(".", ","))
					.append(LS);
				}
			}
			finally {
				rs.close();
			}
			writeToFile(dir.getPath()+File.separator+"details.csv", sb.toString());
		}
		

		{
			StringBuffer sb = new StringBuffer();

			//группировка по технике
			sb.append("By vehicle:").append(LS);
			ResultSet rs = stmt.executeQuery("select count(1), sum(shot_displace), sum(shot_displace*shot_displace), vehicle from shot group by vehicle order by 1 desc");
			sb.append(";Vehicle;Shots;Average displace;Root mean square displace").append(LS);
			while(rs.next()) {
				int count = rs.getInt(1);
				double average = rs.getDouble(2);
				double rms = rs.getDouble(3);
				String vehicle = rs.getString(4);
				if(count > 0) {
					rms = Math.sqrt(rms/count);
					average = average/count;
					sb.append(";").append(vehicle).append(";")
					.append(count).append(";")
					.append(Double.toString(average).replace(".", ",")).append(";")
					.append(Double.toString(rms).replace(".", ",")).append(";")
					.append(LS);
				}
			}
			rs.close();
			
			//группировка по дате реплея
			sb.append(LS).append("By date (displace " + hourDisplace + " hours):").append(LS);
			rs = stmt.executeQuery("select count(1), sum(shot_displace), sum(shot_displace*shot_displace), replay_date from shot group by replay_date order by replay_date");
			sb.append(";Replay date;Shots;Average displace;Root mean square displace").append(LS);
			while(rs.next()) {
				int count = rs.getInt(1);
				double average = rs.getDouble(2);
				double rms = rs.getDouble(3);
				String rDate = rs.getString(4);
				if(count > 0) {
					rms = Math.sqrt(rms/count);
					average = average/count;
					sb.append(";").append(rDate).append(";")
					.append(count).append(";")
					.append(Double.toString(average).replace(".", ",")).append(";")
					.append(Double.toString(rms).replace(".", ",")).append(";")
					.append(LS);
				}
			}
			rs.close();
			
			//группировка по минуте от начала боя
			sb.append(LS).append("By battle minute:").append(LS);
			rs = stmt.executeQuery("select count(1), sum(shot_displace), sum(shot_displace*shot_displace), CAST(shot_time/60.0 AS INTEGER) from shot group by CAST(shot_time/60.0 AS INTEGER) order by 4");
			sb.append(";Battle minute;Shots;Average displace;Root mean square displace").append(LS);
			while(rs.next()) {
				int count = rs.getInt(1);
				double average = rs.getDouble(2);
				double rms = rs.getDouble(3);
				int minute = rs.getInt(4);
				if(count > 0) {
					rms = Math.sqrt(rms/count);
					average = average/count;
					sb.append(";").append(minute).append(";")
					.append(count).append(";")
					.append(Double.toString(average).replace(".", ",")).append(";")
					.append(Double.toString(rms).replace(".", ",")).append(";")
					.append(LS);
				}
			}
			rs.close();
			
			
			//суммарный результат
			sb.append(LS).append("Total:").append(LS);
			rs = stmt.executeQuery("select count(1), sum(shot_displace), sum(shot_displace*shot_displace) from shot");
			sb.append(";;Shots;Average displace;Root mean square displace").append(LS);
			while(rs.next()) {
				int count = rs.getInt(1);
				double average = rs.getDouble(2);
				double rms = rs.getDouble(3);
				if(count > 0) {
					rms = Math.sqrt(rms/count);
					average = average/count;
					sb.append(";;").append(count).append(";")
					.append(Double.toString(average).replace(".", ",")).append(";")
					.append(Double.toString(rms).replace(".", ",")).append(";")
					.append(LS);
				}
			}
			rs.close();
			
			writeToFile(dir.getPath()+File.separator+"stats.csv", sb.toString());
		}
		
	}

	@Override
	public void shot(float time, double displacePercentage) throws Throwable {
		putShot.setString(1, patch);
		putShot.setString(2, replay);
		putShot.setString(3, replayDate);
		putShot.setString(4, vehicle);
		putShot.setDouble(5, time);
		putShot.setDouble(6, displacePercentage);
		putShot.executeUpdate();
	}
	
	public void error(String replayName, String errorMsg) throws SQLException {
		errorBuffer.append(replayName).append(": ").append(errorMsg).append(System.lineSeparator());
	}
}

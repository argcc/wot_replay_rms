package ru.an.wot.replay.ui;

import java.awt.Frame;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;


import ru.an.wot.replay.ByteArraySlice;
import ru.an.wot.replay.CheckHeader;
import ru.an.wot.replay.Replay;
import ru.an.wot.replay.ReplayException;
import ru.an.wot.replay.Storage;
import ru.an.wot.replay.scanner.ReplayShots;

public class ProgressBar extends JDialog implements PropertyChangeListener {
	
	protected JLabel label;
	protected JProgressBar pb;
	protected Task task;
	public Throwable error;
	
	class Task extends SwingWorker<Void, Void> {
		
		protected File folder;
		protected boolean details;
		protected String currentFile = "---";
		
		public Task(File folder, boolean details) {
			this.folder = folder;
			this.details = details;
		}

		@Override
		protected Void doInBackground() throws Exception {
			setProgress(0);
			
			CheckHeader headerChecker = new CheckHeader();
			Storage storage;
			try {
				storage = new Storage();
			} catch (SQLException e2) {
				error = e2;
				return null;
			}
			
			int total = folder.listFiles().length;
			int i = 0;
			
			for(File file : folder.listFiles()) {//TODO сделать многопоточность?
				try {
					label.setText(file.getName());
					setProgress((int)(100*((float)i/total)));
					++i;
					
					if(!file.exists() || file.isDirectory() || !(file.length() >= 8) || !file.getName().endsWith(".wotreplay"))
						continue;
					
					ByteArraySlice r = new ByteArraySlice(Files.readAllBytes(Paths.get(file.getPath())));
					Replay replay = new Replay(r, headerChecker);
					storage.setCurrentReplay(file.getName(), replay);
					new ReplayShots(replay, storage);
				}
				catch (Throwable e) {
					if(e instanceof ReplayException) {
						if(!e.getMessage().equals("Реплеи на артиллерии не поддерживаются") && 
								!e.getMessage().equals("Не обнаружен серверный прицел"))
							try {
								storage.error(file.getName(), e.getMessage());
							} catch (SQLException e1) {
								error=e1;
								return null;
							}
					}
					else {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						PrintStream ps;
						try {
							ps = new PrintStream(baos, true, "utf-8");
						} catch (UnsupportedEncodingException e2) {
							error=e2;
							return null;
						}
						e.printStackTrace(ps);
						String errMsg = new String(baos.toByteArray(), StandardCharsets.UTF_8);
						ps.close();
						try {
							storage.error(file.getName(), errMsg);
						} catch (SQLException e1) {
							error=e1;
							return null;
						}
					}
				}
			}
			
			storage.save(folder, details);
			storage.close();
			return null;
		}
		
		@Override
		protected void done() {
			ProgressBar.this.dispose();
		}
		
	}
	
	public ProgressBar(Frame parent, File folder, boolean details) throws Throwable {
		super(parent, "Подожите", true);
		
		this.setUndecorated(true);
		
		this.setLocationRelativeTo(parent);

		this.setLayout(new GridLayout(2, 1));
		
		label = new JLabel(String.format("%060d", 0));
		this.getContentPane().add(label);
		
		pb = new JProgressBar();
		pb.setMaximum(100);
		this.getContentPane().add(pb);
		this.pack();

		label.setText(" ");
		task = new Task(folder, details);
		task.addPropertyChangeListener(this);
		task.execute();
		
		this.setVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            pb.setValue(progress);
        }
	}
}

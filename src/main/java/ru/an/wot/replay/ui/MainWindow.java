package ru.an.wot.replay.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import ru.an.wot.replay.ByteArraySlice;
import ru.an.wot.replay.CheckHeader;
import ru.an.wot.replay.Replay;
import ru.an.wot.replay.ReplayException;
import ru.an.wot.replay.Storage;
import ru.an.wot.replay.scanner.ReplayShots;

public class MainWindow extends JFrame implements ActionListener{
	
	protected JButton replayFolderChoose, start;
	protected JLabel replayFolderText;
	protected JCheckBox detailCheckBox;
	
	public File replayDir;
	public boolean detailed;
	
	protected abstract class MWActionListener implements ActionListener{}
	
	public MainWindow() {
		super("Replay RMS");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		
		
		this.setLayout(new GridLayout(2, 2));
		replayFolderText = new JLabel("");
		getContentPane().add(replayFolderText);
		
		replayFolderChoose = new JButton("Указать папку с реплеями");
		replayFolderChoose.addActionListener(new MWActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser(); 
			    chooser.setCurrentDirectory(new File("."));
			    chooser.setDialogTitle("Укажите папку с реплеями");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);
			    if (chooser.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
			    	MainWindow.this.replayFolderText.setText(chooser.getSelectedFile().getPath());
			    }
			}
		});
		getContentPane().add(replayFolderChoose);
		
		detailCheckBox = new JCheckBox("Детальная информация");
		getContentPane().add(detailCheckBox);
		
		start = new JButton("Пек-пек!");
		start.addActionListener(this);
		this.getContentPane().add(start);
		this.pack();
		this.setVisible(true);
	}

	//событие запуска обработки реплеев
	@Override
	public void actionPerformed(ActionEvent e) {
		replayDir = new File(replayFolderText.getText());
		if(!replayDir.exists() || replayDir.isFile()) {
			JOptionPane.showMessageDialog(this, "Папка с реплеями указана некорректно");
			return;
		}
		
		detailed = detailCheckBox.isSelected();
		
		String endMsg = "Готово";
		try {
			ProgressBar pb = new ProgressBar(this, replayDir, detailed);
			if(pb.error != null)
				throw pb.error;
		} catch (Throwable e1) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps;
			try {
				ps = new PrintStream(baos, true, "utf-8");
			} catch (UnsupportedEncodingException e2) {
				throw new RuntimeException(e2);
			}
			e1.printStackTrace(ps);
			endMsg = new String(baos.toByteArray(), StandardCharsets.UTF_8);
			ps.close();
			
		}
		
		JOptionPane.showMessageDialog(this, endMsg);
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
}


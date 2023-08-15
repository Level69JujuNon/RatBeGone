package me.mindlessly.antirat.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Console extends JTextPane {

	private static final long serialVersionUID = 1L;
	DocOutputStream out;
	PrintStream pout;
	DocInputStream in;
	JFrame frame;
	StyledDocument doc;

	public Console() {
		super();
		setPreferredSize(new Dimension(800, 500));
		doc = this.getStyledDocument();
		out = new DocOutputStream(doc, this);
		pout = new PrintStream(out);
		in = new DocInputStream();
		this.addKeyListener(in);
		setFGColor(Color.black);
		setBGColor(Color.white);
		frame = new JFrame("RatBeGone");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new JScrollPane(this));
		frame.pack();
		frame.setVisible(true);
	}

	public InputStream getIn() {
		return in;
	}

	public PrintStream getOut() {
		return pout;
	}

	public void setFGColor(Color c) {
		StyleConstants.setForeground(out.cur, c);
	}

	public void setBGColor(Color c) {
		StyleConstants.setBackground(out.cur, c);
	}

	private static class DocOutputStream extends OutputStream {

		StyledDocument doc;
		MutableAttributeSet cur;
		JTextPane pane;

		public DocOutputStream(StyledDocument doc, JTextPane pane) {
			this.doc = doc;
			this.pane = pane;
			cur = new SimpleAttributeSet();
		}

		@Override
		public void write(int b) throws IOException {
			try {
				doc.insertString(doc.getLength(), (char) b + "", cur);
				pane.setCaretPosition(doc.getLength());
			} catch (BadLocationException ex) {
				Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
	}

	private static class DocInputStream extends InputStream implements KeyListener {

		ArrayBlockingQueue<Integer> queue;

		public DocInputStream() {
			queue = new ArrayBlockingQueue<Integer>(1024);
		}

		@Override
		public int read() throws IOException {
			Integer i = null;
			try {
				i = queue.take();
			} catch (InterruptedException ex) {
				Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
			}
			if (i != null)
				return i;
			return -1;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (b == null) {
				throw new NullPointerException();
			} else if (off < 0 || len < 0 || len > b.length - off) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return 0;
			}
			int c = read();
			if (c == -1) {
				return -1;
			}
			b[off] = (byte) c;

			int i = 1;
			try {
				for (; i < len && available() > 0; i++) {
					c = read();
					if (c == -1) {
						break;
					}
					b[off + i] = (byte) c;
				}
			} catch (IOException ee) {
			}
			return i;

		}

		@Override
		public int available() {
			return queue.size();
		}

		@Override
		public void keyTyped(KeyEvent e) {
			int c = e.getKeyChar();
			try {
				queue.put(c);
			} catch (InterruptedException ex) {
				Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {

		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

	}
}
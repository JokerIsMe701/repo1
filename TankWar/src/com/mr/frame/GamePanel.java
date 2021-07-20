package com.mr.frame;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.JPanel;

import com.mr.model.Base;
import com.mr.model.Boom;
import com.mr.model.Bot;
import com.mr.model.Bullet;
import com.mr.model.Level;
import com.mr.model.Map;
import com.mr.model.Tank;
import com.mr.model.wall.Wall;
import com.mr.type.GameType;
import com.mr.type.TankType;
import com.mr.util.ImageUtil;

/**
 * ��Ϸ��� ʵ�ʴ�С[794,572]
 * 
 * @author www.mingrisoft.com
 *
 */
public class GamePanel extends JPanel implements KeyListener {
	/**
	 * ��Ϸ����ˢ��ʱ�䣺20����
	 */
	public static final int FRESH = 20;
	private BufferedImage image;// ���������ʾ����ͼƬ
	private Graphics2D g2;// ͼƬ�Ļ�ͼ����
	private MainFrame frame;// ������
	private GameType gameType;// ��Ϸģʽ
	private Tank play1, play2;// ���1�����2
	private boolean y_key, s_key, w_key, a_key, d_key, up_key, down_key, left_key, right_key, num1_key;// �����Ƿ��±�־����൥���ǰ�����
	private int level;// �ؿ�ֵ
	private List<Bullet> bullets;// �����ӵ�����
	private volatile List<Tank> allTanks;// ����̹�˼���
	private List<Tank> botTanks;// ����̹�˼���
	private final int botCount = 20;// ����̹������
	private int botReadyCount = botCount;// ׼�������ĵ���̹������
	private int botSurplusCount = botCount;// ����̹��ʣ����
	private int botMaxInMap = 6;// ����������̹����
	private int botX[] = { 10, 367, 754 };// ����̹�˳�����3��������λ��
	private List<Tank> playerTanks;// ���̹�˼���
	private volatile boolean finish = false;// ��Ϸ�Ƿ����
	private Base base;// ����
	private List<Wall> walls;// ����ǽ��
	private List<Boom> boomImage;// ̹��������ı�ըЧ������
	private Random r = new Random();// ���������
	private int createBotTimer = 0;// �������Լ�ʱ��
	private Tank survivor;// ����ң��Ҵ���,���ڻ������һ����ըЧ��

	/**
	 * ��Ϸ��幹�췽��
	 * 
	 * @param frame
	 *            - ������
	 * @param level
	 *            - �ؿ�
	 * @param gameType
	 *            - ��Ϸģʽ
	 */
	public GamePanel(MainFrame frame, int level, GameType gameType) {
		this.frame = frame;
		this.level = level;
		this.gameType = gameType;
		setBackground(Color.WHITE);// ���ʹ�ð�ɫ����
		init();// ��ʼ�����
		Thread t = new FreshThead();// ������Ϸ֡ˢ���߳�
		t.start();// �����߳�
		addListener();// ��������
	}

	/**
	 * �����ʼ��
	 */
	private void init() {
		bullets = new ArrayList<Bullet>();// ʵ�����ӵ�����
		allTanks = new ArrayList<>();// ʵ��������̹�˼���
		walls = new ArrayList<>();// ʵ��������ǽ�鼯��
		boomImage = new ArrayList<>();// ʵ������ըЧ������

		image = new BufferedImage(794, 572, BufferedImage.TYPE_INT_BGR);// ʵ������ͼƬ���������ʵ�ʴ�С
		g2 = image.createGraphics();// ��ȡ��ͼƬ��ͼ����

		playerTanks = new ArrayList<>();// ʵ�������̹�˼���
		play1 = new Tank(278, 537, ImageUtil.PLAYER1_UP_IMAGE_URL, this, TankType.player1);// ʵ�������1
		if (gameType == GameType.TWO_PLAYER) {// �����˫��ģʽ
			play2 = new Tank(448, 537, ImageUtil.PLAYER2_UP_IMAGE_URL, this, TankType.player2);// ʵ�������2
			playerTanks.add(play2);// ���̹�˼����������2
		}
		playerTanks.add(play1);// ���̹�˼����������1

		botTanks = new Vector<>();// ʵ��������̹�˼���
		botTanks.add(new Bot(botX[0], 1, this, TankType.bot));// �ڵ�һ��λ�����ӵ���
		botTanks.add(new Bot(botX[1], 1, this, TankType.bot));// �ڵڶ���λ�����ӵ���
		botTanks.add(new Bot(botX[2], 1, this, TankType.bot));// �ڵ�����λ�����ӵ���
		botReadyCount -= 3;// ׼��������̹��������ȥ��ʼ������
		allTanks.addAll(playerTanks);// ����̹�˼����������̹�˼���
		allTanks.addAll(botTanks);// ����̹�˼������ӵ���̹�˼���
		base = new Base(367, 532);// ʵ��������
		initWalls();// ��ʼ����ͼ�е�ǽ��
	}

	/**
	 * �������
	 */
	private void addListener() {
		frame.addKeyListener(this);// ������������̼�����������ʵ��KeyListener�ӿ�
	}

	/**
	 * ��ʼ����ͼ�е�ǽ��
	 */
	private void initWalls() {
		Map map = Map.getMap(level);// ��ȡ��ǰ�ؿ��ĵ�ͼ����
		walls.addAll(map.getWalls());// ǽ�鼯�����ӵ�ǰ��ͼ������ǽ��
		walls.add(base);// ǽ�鼯�����ӻ���
	}

	/**
	 * ��д�����������
	 */
	public void paint(Graphics g) {
		paintTankActoin();// ִ��̹�˶���
		CreateBot();// ѭ����������̹��
		paintImage();// ������ͼƬ
		g.drawImage(image, 0, 0, this); // ����ͼƬ���Ƶ������
	}

	/**
	 * ������ͼƬ
	 */
	private void paintImage() {
		g2.setColor(Color.WHITE);// ʹ�ð�ɫ
		g2.fillRect(0, 0, image.getWidth(), image.getHeight());// ���һ����������ͼƬ�İ�ɫ����
		panitBoom();// ���Ʊ�ըЧ��
		paintBotCount();// ����Ļ��������ʣ��̹������
		panitBotTanks();// ���Ƶ���̹��
		panitPlayerTanks();// �������̹��
		allTanks.addAll(playerTanks);// ̹�˼����������̹�˼���
		allTanks.addAll(botTanks);// ̹�˼������ӵ���̹�˼���
		panitWalls();// ����ǽ��
		panitBullets();// �����ӵ�

		if (botSurplusCount == 0) {// ������е��Զ�������
			stopThread();// ������Ϸ֡ˢ���߳�
			paintBotCount();// ����Ļ��������ʣ��̹������
			g2.setFont(new Font("����", Font.BOLD, 50));// ���û�ͼ����
			g2.setColor(Color.green);// ʹ����ɫ
			g2.drawString("ʤ   �� !", 250, 400);// ��ָ�������������
			gotoNextLevel();// ������һ�ؿ�
		}

		if (gameType == GameType.ONE_PLAYER) {// ����ǵ���ģʽ
			if (!play1.isAlive()) {// ����������
				stopThread();// ������Ϸ֡ˢ���߳�
				boomImage.add(new Boom(play1.x, play1.y));// �������1��ըЧ��
				panitBoom();// ���Ʊ�ըЧ��
				paintGameOver();// ����Ļ�������game over
				gotoPrevisousLevel();// ���½��뱾�ؿ�
			}
		} else {// �����˫��ģʽ
			if (play1.isAlive() && !play2.isAlive()) {// ������1�� �Ҵ���
				survivor = play1;// �Ҵ��������1
			} else if (!play1.isAlive() && play2.isAlive()) {
				survivor = play2;// �Ҵ��������2
			} else if (!(play1.isAlive() || play2.isAlive())) {// ����������ȫ������
				stopThread();// ������Ϸ֡ˢ���߳�
				boomImage.add(new Boom(survivor.x, survivor.y));// �����Ҵ��߱�ըЧ��
				panitBoom();// ���Ʊ�ըЧ��
				paintGameOver();// ����Ļ�������game over
				gotoPrevisousLevel();// ���½��뱾�ؿ�
			}
		}

		if (!base.isAlive()) {// ������ر�����
			stopThread();// ������Ϸ֡ˢ���߳�
			paintGameOver();// ����Ļ�������game over
			base.setImage(ImageUtil.BREAK_BASE_IMAGE_URL);// ����ʹ������ͼƬ
			gotoPrevisousLevel();// ���½��뱾�ؿ�
		}
		g2.drawImage(base.getImage(), base.x, base.y, this);// ���ƻ���
	}

	/**
	 * ����Ļ��������ʣ��̹������
	 */
	private void paintBotCount() {
		g2.setColor(Color.BLUE);// ʹ����ɫ
		g2.drawString("�з�̹��ʣ�ࣺ" + botSurplusCount, 337, 15);// ��ָ����������ַ���
	}

	/**
	 * ����Ļ�������game over
	 */
	private void paintGameOver() {
		g2.setFont(new Font("����", Font.BOLD, 50));// ���û�ͼ����
		g2.setColor(Color.RED);// ���û�ͼ��ɫ
		g2.drawString("Game Over !", 250, 400);// ��ָ�������������

	}

	/**
	 * ���Ʊ�ըЧ��
	 */
	private void panitBoom() {
		for (int i = 0; i < boomImage.size(); i++) {// ѭ��������ըЧ������
			Boom boom = boomImage.get(i);// ��ȡ��ը����
			if (boom.isAlive()) {// �����ըЧ����Ч
				boom.show(g2);// չʾ��ըЧ��
			} else {// �����ըЧ����Ч
				boomImage.remove(i);// �ڼ����Єh���˱�ը����
				i--;// ѭ������-1����֤�´�ѭ��i��ֵ������i+1���Ա���Ч�������ϣ��ҷ�ֹ�±�Խ��
			}
		}
	}

	/**
	 * ����ǽ��
	 */
	private void panitWalls() {
		for (int i = 0; i < walls.size(); i++) {// ѭ������ǽ�鼯��
			Wall w = walls.get(i);// ��ȡǽ�����
			if (w.isAlive()) {// ���ǽ����Ч
				g2.drawImage(w.getImage(), w.x, w.y, this);// ����ǽ��
			} else {// ���ǽ����Ч
				walls.remove(i);// �ڼ����Єh����ǽ��
				i--;// ѭ������-1����֤�´�ѭ��i��ֵ������i+1���Ա���Ч�������ϣ��ҷ�ֹ�±�Խ��
			}
		}
	}

	/**
	 * �����ӵ�
	 */
	private void panitBullets() {
		for (int i = 0; i < bullets.size(); i++) {// ѭ�������ӵ�����
			Bullet b = bullets.get(i);// ��ȡ�ӵ�����
			if (b.isAlive()) {// ����ӵ���Ч
				b.move();// �ӵ�ִ���ƶ�����
				b.hitBase();// �ӵ�ִ�л��л����ж�
				b.hitWall();// �ӵ�ִ�л���ǽ���ж�
				b.hitTank();// �ӵ�ִ�л���̹���ж�
				g2.drawImage(b.getImage(), b.x, b.y, this);// �����ӵ�
			} else {// ����ӵ���Ч
				bullets.remove(i);// �ڼ����Єh�����ӵ�
				i--;// ѭ������-1����֤�´�ѭ��i��ֵ������i+1���Ա���Ч�������ϣ��ҷ�ֹ�±�Խ��
			}
		}
	}

	/**
	 * ���Ƶ���̹��
	 */
	private void panitBotTanks() {
		for (int i = 0; i < botTanks.size(); i++) {// ѭ����������̹�˼���
			Bot t = (Bot) botTanks.get(i);// ��ȡ����̹�˶���
			if (t.isAlive()) {// ���̹�˴��
				t.go();// ����̹��չ���ж�
				g2.drawImage(t.getImage(), t.x, t.y, this);// ����̹��
			} else {// ���̹������
				botTanks.remove(i);// ������ɾ����̹��
				i--;// ѭ������-1����֤�´�ѭ��i��ֵ������i+1���Ա���Ч�������ϣ��ҷ�ֹ�±�Խ��
				boomImage.add(new Boom(t.x, t.y));// ��̹��λ�ô�����ըЧ��
				decreaseBot();// ʣ��̹������-1
			}
		}
	}

	/**
	 * �������̹��
	 */
	private void panitPlayerTanks() {
		for (int i = 0; i < playerTanks.size(); i++) {// ѭ���������̹��
			Tank t = playerTanks.get(i);// ��ȡ���̹�˶���
			if (t.isAlive()) {// ���̹�˴��
				g2.drawImage(t.getImage(), t.x, t.y, this);// ����̹��
			} else {// ���̹������
				playerTanks.remove(i);// ������ɾ����̹��
				i--;// ѭ������-1����֤�´�ѭ��i��ֵ������i+1���Ա���Ч�������ϣ��ҷ�ֹ�±�Խ��
				boomImage.add(new Boom(t.x, t.y));// ��̹��λ�ô�����ըЧ��
			}
		}
	}

	/**
	 * ������Ϸ֡ˢ��
	 */
	private synchronized void stopThread() {
		frame.removeKeyListener(this);// ������ɾ����������¼���������
		finish = true;// ��Ϸֹͣ��־Ϊtrue
	}

	/**
	 * ��Ϸ֡ˢ���߳�
	 */
	private class FreshThead extends Thread {
		public void run() {// �߳�������
			while (!finish) {// �����Ϸδֹͣ
				repaint();// ִ�б����ػ淽��
				try {
					Thread.sleep(FRESH);// ָ��ʱ������»��ƽ���
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * ���ӵ���̹�ˣ��������̹��δ�������ֵ��ÿ4����֮������������λ�����ѡ����һ����������̹�ˡ�
	 */
	private void CreateBot() {
		createBotTimer += FRESH;// ��ʱ������ˢ��ʱ�����
		// �������ϵ���С�ڳ��������ʱ�� ���� ��׼���ϳ���̹����������0�� ���� ����ʱ����¼�ѹ�ȥ4���ӡ�
		if (botTanks.size() < botMaxInMap && botReadyCount > 0 && createBotTimer >= 4000) {
			int index = r.nextInt(3);// �����ȡ0��1��2����һ��ֵ
			Rectangle bornRect = new Rectangle(botX[index], 1, 35, 35);// ����̹�������������
			for (int i = 0, lengh = allTanks.size(); i < lengh; i++) {// ѭ����������̹�˼���
				Tank t = allTanks.get(i);// ��ȡ̹�˶���
				if (t.isAlive() && t.hit(bornRect)) {// ������ϴ��������λ���غϲ�����̹��
					return;// ��������
				}
			}
			botTanks.add(new Bot(botX[index], 1, GamePanel.this, TankType.bot));// �����λ�ô������̹��
			botReadyCount--;// ׼���ϳ���������-1
			createBotTimer = 0;// �������Լ�ʱ�����¼�ʱ
		}
	}

	/**
	 * ������һ�ؿ�
	 */
	private void gotoNextLevel() {
		Thread jump = new JumpPageThead(Level.nextLevel());// ������ת����һ�ؿ����߳�
		jump.start();// �����߳�
	}

	/**
	 * ���½��뱾�ؿ�
	 */
	private void gotoPrevisousLevel() {
		Thread jump = new JumpPageThead(Level.previsousLevel());// �������½��뱾�ؿ����߳�
		jump.start();// �����߳�
	}

	/**
	 * ʣ��̹����������1
	 */
	public void decreaseBot() {
		botSurplusCount--;// ����ʣ������-1
	}

	/**
	 * ��������ʱ
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {// �жϰ��µİ���ֵ
		case KeyEvent.VK_Y:// ������µ��ǡ�Y��
			y_key = true;// ��Y�����±�־Ϊtrue
			break;
		case KeyEvent.VK_W:// ������µ��ǡ�W��
			w_key = true;// ��W�����±�־Ϊtrue
			a_key = false;// ��A�����±�־Ϊfalse
			s_key = false;// ��S�����±�־Ϊfalse
			d_key = false;// ��D�����±�־Ϊfalse
			break;
		case KeyEvent.VK_A:// ������µ��ǡ�A��
			w_key = false;// ��W�����±�־Ϊfalse
			a_key = true;// ��A�����±�־Ϊtrue
			s_key = false;// ��S�����±�־Ϊfalse
			d_key = false;// ��D�����±�־Ϊfalse
			break;
		case KeyEvent.VK_S:// ������µ��ǡ�S��
			w_key = false;// ��W�����±�־Ϊfalse
			a_key = false;// ��A�����±�־Ϊfalse
			s_key = true;// ��S�����±�־Ϊtrue
			d_key = false;// ��D�����±�־Ϊfalse
			break;
		case KeyEvent.VK_D:// ������µ��ǡ�D��
			w_key = false;// ��W�����±�־Ϊfalse
			a_key = false;// ��A�����±�־Ϊfalse
			s_key = false;// ��S�����±�־Ϊfalse
			d_key = true;// ��D�����±�־Ϊtrue
			break;
		case KeyEvent.VK_HOME:// ������µ��ǡ�HOME����Ч��ͬ��
		case KeyEvent.VK_NUMPAD1:// ������µ���С��������1
			num1_key = true;// С��������1���±�־Ϊtrue
			break;
		case KeyEvent.VK_UP:// ������µ��ǡ�����
			up_key = true;// ���������±�־Ϊtrue
			down_key = false;// ���������±�־Ϊfalse
			right_key = false;// ���������±�־Ϊfalse
			left_key = false;// ���������±�־Ϊfalse
			break;
		case KeyEvent.VK_DOWN:// ������µ��ǡ�����
			up_key = false;// ���������±�־Ϊfalse
			down_key = true;// ���������±�־Ϊtrue
			right_key = false;// ���������±�־Ϊfalse
			left_key = false;// ���������±�־Ϊfalse
			break;
		case KeyEvent.VK_LEFT:// ������µ��ǡ�����
			up_key = false;// ���������±�־Ϊfalse
			down_key = false;// ���������±�־Ϊfalse
			right_key = false;// ���������±�־Ϊfalse
			left_key = true;// ���������±�־Ϊtrue
			break;
		case KeyEvent.VK_RIGHT:// ������µ��ǡ�����
			up_key = false;// ���������±�־Ϊfalse
			down_key = false;// ���������±�־Ϊfalse
			right_key = true;// ���������±�־Ϊtrue
			left_key = false;// ���������±�־Ϊfalse
			break;
		}
	}

	/**
	 * ���ݰ�������״̬����̹��ִ����Ӧ����
	 */
	private void paintTankActoin() {
		if (y_key) {// �����Y�����ǰ���״̬
			play1.attack();// ���1̹�˹���
		}
		if (w_key) {// �����W�����ǰ���״̬
			play1.upward();// ���1̹�������ƶ�
		}
		if (d_key) {// �����D�����ǰ���״̬
			play1.rightward();// ���1̹�������ƶ�
		}
		if (a_key) {// �����A�����ǰ���״̬
			play1.leftward();// ���1̹�����ƶ�
		}
		if (s_key) {// �����S�����ǰ���״̬
			play1.downward();// ���1̹�������ƶ�
		}
		if (gameType == GameType.TWO_PLAYER) {
			if (num1_key) {// �����M�����ǰ���״̬
				play2.attack();// ���2̹�˹���
			}
			if (up_key) {// ������������ǰ���״̬
				play2.upward();// ���2̹�������ƶ�
			}
			if (right_key) {// ������������ǰ���״̬
				play2.rightward();// ���2̹�������ƶ�
			}
			if (left_key) {// ������������ǰ���״̬
				play2.leftward();// ���2̹�����ƶ�
			}
			if (down_key) {// ������������ǰ���״̬
				play2.downward();// ���2̹�˺��ƶ�
			}
		}
	}

	/**
	 * ����̧��ʱ
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_Y:// ���̧����ǡ�Y��
			y_key = false;// ��Y�����±�־Ϊfalse
			break;
		case KeyEvent.VK_W:// ���̧����ǡ�W��
			w_key = false;// ��W�����±�־Ϊfalse
			break;
		case KeyEvent.VK_A:// ���̧����ǡ�A��
			a_key = false;// ��A�����±�־Ϊfalse
			break;
		case KeyEvent.VK_S:// ���̧����ǡ�S��
			s_key = false;// ��S�����±�־Ϊfalse
			break;
		case KeyEvent.VK_D:// ���̧����ǡ�D��
			d_key = false;// ��D�����±�־Ϊfalse
			break;
		case KeyEvent.VK_HOME:// ���̧����ǡ�HOME����Ч��ͬ��
		case KeyEvent.VK_NUMPAD1:// ���̧�����С����1
			num1_key = false;// С����1���±�־Ϊfalse
			break;
		case KeyEvent.VK_UP:// ���̧����ǡ�����
			up_key = false;// ���������±�־Ϊfalse
			break;
		case KeyEvent.VK_DOWN:// ���̧����ǡ�����
			down_key = false;// ���������±�־Ϊfalse
			break;
		case KeyEvent.VK_LEFT:// ���̧����ǡ�����
			left_key = false;// ���������±�־Ϊfalse
			break;
		case KeyEvent.VK_RIGHT:// ���̧����ǡ�����
			right_key = false;// ���������±�־Ϊfalse
			break;
		}
	}

	/**
	 * ���ӵ������������ӵ�
	 * 
	 * @param b
	 *            - ���ӵ��ӵ�
	 */
	public void addBullet(Bullet b) {
		bullets.add(b);// �ӵ������������ӵ�
	}

	/**
	 * ��ȡ����ǽ�鼯��
	 * 
	 * @return ����ǽ��
	 */
	public List<Wall> getWalls() {
		return walls;
	}

	/**
	 * ��ȡ���ض���
	 * 
	 * @return ����
	 */
	public Base getBase() {
		return base;
	}

	/**
	 * ��ȡ����̹�˼���
	 * 
	 * @return ����̹��
	 */
	public List<Tank> getTanks() {
		return allTanks;
	}

	/**
	 * ��Ϸ������ת�߳�
	 */
	private class JumpPageThead extends Thread {
		int level;// ��ת�Ĺؿ�

		/**
		 * ��ת�̹߳��췽��
		 * 
		 * @param level
		 *            - ��ת�Ĺؿ�
		 */
		public JumpPageThead(int level) {
			this.level = level;
		}

		/**
		 * �߳�������
		 */
		public void run() {
			try {
				Thread.sleep(1000);// 1���Ӻ�
				frame.setPanel(new LevelPanel(level, frame, gameType));// ��������ת��ָ���ؿ�
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ����ĳ�����¼�
	 */
	public void keyTyped(KeyEvent e) {
		// ��ʵ�ִ˷�����������ɾ��
	}
}
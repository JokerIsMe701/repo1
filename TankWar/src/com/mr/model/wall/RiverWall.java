package com.mr.model.wall;

import com.mr.util.ImageUtil;

/**
 * ����
 * 
 * @author www.mingrisoft.com
 *
 */
public class RiverWall extends Wall {
	/**
	 * 
	 * �������췽��
	 * 
	 * @param x
	 *            - ��ʼ��������
	 * @param y
	 *            - ��ʼ��������
	 */
	public RiverWall(int x, int y) {
		super(x, y, ImageUtil.RIVERWALL_IMAGE_URL);// ���ø��๹�췽����ʹ��Ĭ�Ϻ���ͼƬ
	}

}
package bsu.rfe.java.group10.lab4.Slavinsky.varC;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

//Почему-то не находился интерфейс MouseAdapter
//Пришлось сделать свой с урезанным функционалом изза наследования только от MouseListener
//Создан с целью не захламлять GraphicsDisplay
public interface CustomMouseAdapter extends MouseMotionListener, MouseListener {

	@Override
	public default void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public default  void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public default void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public default void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public default void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}

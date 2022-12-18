package bsu.rfe.java.group10.lab4.Slavinsky.varC;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class GornerTableCellRenderer implements TableCellRenderer {
	private JPanel panel = new JPanel();
	private JLabel label = new JLabel();
	// Ищем ячейки, строковое представление которых совпадает с needle
	// (иголкой). Применяется аналогия поиска иголки в стоге сена, в роли
	// стога сена - таблица
	private String needle = null;
	private Double min = null;
	private Double max = null;
	private DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance();
	
	public GornerTableCellRenderer() {
		// Показывать только 5 знаков после запятой
		formatter.setMaximumFractionDigits(5);
		// Не использовать группировку (т.е. не отделять тысячи
		// ни запятыми, ни пробелами), т.е. показывать число как "1000",
		// а не "1 000" или "1,000"
		formatter.setGroupingUsed(false);
		// Установить в качестве разделителя дробной части точку, а не
		// запятую. По умолчанию, в региональных настройках
		// Россия/Беларусь дробная часть отделяется запятой
		DecimalFormatSymbols dottedDouble = formatter.getDecimalFormatSymbols();
		dottedDouble.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(dottedDouble);
		// Разместить надпись внутри панели
		panel.add(label);
		// Установить выравнивание надписи по левому краю панели
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
	}
	
	public String formate(Object value) {
		return formatter.format(value);
	}
	
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		// Преобразовать double в строку с помощью форматировщика
		String formattedDouble = formatter.format(value);
		// Установить текст надписи равным строковому представлению числа
		label.setText(formattedDouble);
		
		Double dValue = (double)value;
		
		if(col == 1 &&min != null && max != null && dValue >= min && dValue <= max) {
			panel.setBackground(Color.RED);
		}
		else if((row + col) % 2 == 0) {			
			panel.setBackground(Color.WHITE);
			label.setForeground(Color.BLACK);
		}
		else {
			panel.setBackground(Color.DARK_GRAY);
			label.setForeground(Color.WHITE);
		}
		
		if (col == 1 && (needle != null)  && needle.equals(formattedDouble)) {
			// Номер столбца = 1 (т.е. второй столбец) + иголка не null
			// (значит что-то ищем) +
			// значение иголки совпадает со значением ячейки таблицы -
			// окрасить задний фон панели в красный цвет
			panel.setBackground(Color.RED);
		} 
		return panel;
	}
	
	public void setNeedle(String needle) {
		this.needle = needle;
	}
	
	public void setRange(Double min, Double max) {
		this.max = max;
		this.min = min;
	}
}
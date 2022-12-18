package bsu.rfe.java.group10.lab4.Slavinsky.varC;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

@SuppressWarnings("serial")

public class GraphicsDisplay extends JPanel {
	// Список координат точек для построения графика
	private Double[][] graphicsData;
	private Double[][] originalGraphicsData;
	private Double[][] rotatedGraphicsData;
	// Флаговые переменные, задающие правила отображения графика
	private boolean showAxis = true;
	private boolean showMarkers = true;
	private boolean showSpecialMarkers = true;
	private boolean showClosedAreas = false;
	private boolean rotate = false;
	// Границы диапазона пространства, подлежащего отображению
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	// Используемый масштаб отображения
	private double scale;
	// Различные стили черчения линий
	private BasicStroke graphicsStroke;
	private BasicStroke axisStroke;
	private BasicStroke markerStroke;
	
	private GornerTableCellRenderer formatter = new GornerTableCellRenderer();
	
	
	// Различные шрифты отображения надписей
	private Font axisFont;
	
	public GraphicsDisplay() {
		// Цвет заднего фона области отображения - белый
		setBackground(Color.WHITE);
		// Сконструировать необходимые объекты, используемые в рисовании
		// Перо для рисования графика
		graphicsStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_ROUND, 10.0f, new float[] {8, 2, 2, 2, 2, 2, 4, 2, 4}, 0.0f);
		// Перо для рисования осей координат
		axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
		// Перо для рисования контуров маркеров
		markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
		// Шрифт для подписей осей координат
		axisFont = new Font("Serif", Font.BOLD, 36);
	}
	// Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
	// главного окна приложения в случае успешной загрузки данных
	public void showGraphics(Double[][] graphicsData) {
		// Сохранить массив точек во внутреннем поле класса
		this.originalGraphicsData = graphicsData;
		this.graphicsData = graphicsData;
		this.rotatedGraphicsData = new Double[graphicsData.length][2];
		
		for(int i = 0; i < graphicsData.length; ++i) {
			double rotX = graphicsData[i][1];
			rotatedGraphicsData[i][1] = graphicsData[i][0];
			rotatedGraphicsData[i][0] = rotX * (-1);
		}
		// Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
		repaint();
	}
	// Методы-модификаторы для изменения параметров отображения графика
	// Изменение любого параметра приводит к перерисовке области
	public void setShowAxis(boolean showAxis) {
		this.showAxis = showAxis;
		repaint();
	}
	public void setShowMarkers(boolean showMarkers) {
		this.showMarkers = showMarkers;
		repaint();
	}
	public void setShowClosedAreas(boolean showClosedAreas) {
		this.showClosedAreas = showClosedAreas;
		repaint();
	}
	public void setRotate(boolean rotate) {
		this.rotate = rotate;
		
		if(rotate)
			this.graphicsData = this.rotatedGraphicsData;
		else
			this.graphicsData = this.originalGraphicsData;
		
		repaint();
	}
	// Метод отображения всего компонента, содержащего график
	public void paintComponent(Graphics g) {
		/* Шаг 1 - Вызвать метод предка для заливки области цветом заднего фона
		* Эта функциональность - единственное, что осталось в наследство от
		* paintComponent класса JPanel
		*/
			super.paintComponent(g);
		// Шаг 2 - Если данные графика не загружены (при показе компонента при запуске программы) - ничего не делать
			if (originalGraphicsData == null || originalGraphicsData.length == 0) return;
		// Шаг 3 - Определить минимальное и максимальное значения для координат X и Y
		// Это необходимо для определения области пространства, подлежащей отображению
		// Еѐ верхний левый угол это (minX, maxY) - правый нижний это (maxX, minY)
		minX = originalGraphicsData[0][0];
		maxX = originalGraphicsData[graphicsData.length-1][0];
		minY = originalGraphicsData[0][1];
		maxY = minY;
		// Найти минимальное и максимальное значение функции
		for (int i = 1; i < originalGraphicsData.length; i++) {
			if (originalGraphicsData[i][1] < minY) {
				minY = originalGraphicsData[i][1];
			}
			if (originalGraphicsData[i][1] > maxY) {
				maxY = originalGraphicsData[i][1];
			}
		}
		/* Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X
		и Y - сколько пикселов
		* приходится на единицу длины по X и по Y
		*/
		if(rotate) {
			double tempMinX = minX;
			double tempMaxX = maxX;
			
			minX = -1 * maxY;
			maxX = -1 * minY;
			minY = tempMinX;
			maxY = tempMaxX;
		}
		
		double scaleX = getSize().getWidth() / (maxX - minX);
		double scaleY = getSize().getHeight() / (maxY - minY);
		// Шаг 5 - Чтобы изображение было неискажѐнным - масштаб должен быть одинаков
		// Выбираем за основу минимальный
		
		scale = Math.min(scaleX, scaleY);
		
		// Шаг 6 - корректировка границ отображаемой области согласно выбранному масштабу
		if (scale == scaleX) {
			/* Если за основу был взят масштаб по оси X, значит по оси Y
			делений меньше,
			* т.е. подлежащий визуализации диапазон по Y будет меньше
			высоты окна.
			* Значит необходимо добавить делений, сделаем это так:
			* 1) Вычислим, сколько делений влезет по Y при выбранном
			масштабе - getSize().getHeight()/scale
			* 2) Вычтем из этого сколько делений требовалось изначально
			* 3) Набросим по половине недостающего расстояния на maxY и
			minY
			*/
			double height = getSize().getHeight();
			double yIncrement = (height/scale - (maxY - minY))/2;
			maxY += yIncrement;
			minY -= yIncrement;
		}
		if (scale == scaleY) {
			// Если за основу был взят масштаб по оси Y, действовать по аналогии
			double width = getSize().getWidth();
			double xIncrement = (width/scale - (maxX - minX))/2;
			maxX += xIncrement;
			minX -= xIncrement;
		}
		
		// Шаг 7 - Сохранить текущие настройки холста
		Graphics2D canvas = (Graphics2D) g;
		Stroke oldStroke = canvas.getStroke();
		Color oldColor = canvas.getColor();
		Paint oldPaint = canvas.getPaint();
		Font oldFont = canvas.getFont();
		// Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
		// Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет затираться последующим
		// Первыми (если нужно) отрисовываются оси координат.
		if (showAxis) 
			paintAxis(canvas);
		// Затем отображается сам график
			paintGraphics(canvas);
		// Затем (если нужно) отображаются маркеры точек, по которым строился график.
		if (showMarkers) paintMarkers(canvas);
		
		if(showClosedAreas)
			paintClosedAreas(canvas);
			// Шаг 9 - Восстановить старые настройки холста
			canvas.setFont(oldFont);
			canvas.setPaint(oldPaint);
			canvas.setColor(oldColor);
			canvas.setStroke(oldStroke);
		}
		// Отрисовка графика по прочитанным координатам
		protected void paintGraphics(Graphics2D canvas) {
		// Выбрать линию для рисования графика
		canvas.setStroke(graphicsStroke);
		// Выбрать цвет линии
		canvas.setColor(Color.RED);
		/* Будем рисовать линию графика как путь, состоящий из множества
		сегментов (GeneralPath)
		* Начало пути устанавливается в первую точку графика, после чего
		прямой соединяется со
		* следующими точками
		*/
		GeneralPath graphics = new GeneralPath();
		for (int i=0; i<graphicsData.length; i++) {
		// Преобразовать значения (x,y) в точку на экране point
			Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
			if (i > 0) {
			// Не первая итерация цикла - вести линию в точку point
				graphics.lineTo(point.getX(), point.getY());
			} else {
			// Первая итерация цикла - установить начало пути в точку point
				graphics.moveTo(point.getX(), point.getY());
			}
		}
		// Отобразить график
		canvas.draw(graphics);
	}
	// Отображение маркеров точек, по которым рисовался график
	protected void paintMarkers(Graphics2D canvas) {
	// Шаг 1 - Установить специальное перо для черчения контуров маркеров
	canvas.setStroke(markerStroke);
	// Шаг 2 - Организовать цикл по всем точкам графика
	for (Double[] point: graphicsData) {
		if(showSpecialMarkers && isSpecial(point[1])) {
			// Выбрать красный цвета для контуров маркеров
			canvas.setColor(Color.BLUE);
			// Выбрать красный цвет для закрашивания маркеров внутри
			canvas.setPaint(Color.BLUE);
		}
		else {
			canvas.setColor(Color.RED);
			canvas.setPaint(Color.RED);
		}
		// точки "звёздочками"
		Point2D.Double center = xyToPoint(point[0], point[1]);
				
		canvas.drawLine((int)center.x + 5, (int)center.y, (int)center.x - 5, (int)center.y);
		canvas.drawLine((int)center.x, (int)center.y + 5, (int)center.x, (int)center.y - 5);
		canvas.drawLine((int)center.x + 5, (int)center.y + 5, (int)center.x - 5, (int)center.y - 5);
		canvas.drawLine((int)center.x - 5, (int)center.y + 5, (int)center.x + 5, (int)center.y - 5);		
		}
	} 
	
	protected void paintClosedAreas(Graphics2D canvas) {
			FontRenderContext context = canvas.getFontRenderContext();
			
			int from = 0;
			int to = 0;
			double fromSign;
			
			var data = originalGraphicsData;
			
			for(int i = 1; i < data.length; ++i) {
				
				if(Math.signum(data[i][1]) != Math.signum(data[i - 1][1])) {
					from = i;
					fromSign = Math.signum(data[i][1]);
				
					for(int k = from; k < data.length; ++k) {
						if(Math.signum(data[k][1]) != fromSign) {
							to = k;
							fillClosedArea(canvas, from, to);
							double square = calcSquare(data, from, to);
							String sqrText = formatter.formate(square);
							Rectangle2D bounds = axisFont.getStringBounds(sqrText, context);
							Point2D.Double labelPos = xyToPoint((graphicsData[from][0] + graphicsData[to][0])/2, 0);
							canvas.setPaint(Color.DARK_GRAY);
							if(rotate) {
								labelPos = xyToPoint(0, (graphicsData[from][1] + graphicsData[to][1])/2);
								if(fromSign < 0)
									canvas.drawString(sqrText, (float)(labelPos.getX() + bounds.getWidth()/2), (float)(labelPos.getY()));
								else
									canvas.drawString(sqrText, (float)(labelPos.getX() - bounds.getWidth()/2), (float)(labelPos.getY()));
							}
							else {
							// Вывести надпись в точке с вычисленными координатами
							if(fromSign < 0)
								canvas.drawString(sqrText, (float)(labelPos.getX() - bounds.getWidth()/2), (float)(labelPos.getY() + bounds.getHeight()));
							else
								canvas.drawString(sqrText, (float)(labelPos.getX() - bounds.getWidth()/2), (float)(labelPos.getY()));
							}
							
							from = k;
							fromSign = Math.signum(data[k][1]);
							i = k + 1;
						}
					}
				}
			}
			
			
			
			
	}
	// Метод, обеспечивающий отображение осей координат
	protected void paintAxis(Graphics2D canvas) {
		// Установить особое начертание для осей
		canvas.setStroke(axisStroke);
		// Оси рисуются чѐрным цветом
		canvas.setColor(Color.BLACK);
		// Стрелки заливаются чѐрным цветом
		canvas.setPaint(Color.BLACK);
		// Подписи к координатным осям делаются специальным шрифтом
		canvas.setFont(axisFont);
		// Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
		FontRenderContext context = canvas.getFontRenderContext();
		// Определить, должна ли быть видна ось Y на графике
		if (minX<=0.0 && maxX>=0.0) {
			// Она должна быть видна, если левая граница показываемой области (minX) <= 0.0,
			// а правая (maxX) >= 0.0
			// Сама ось - это линия между точками (0, maxY) и (0, minY)
			canvas.draw(new Line2D.Double(xyToPoint(0, maxY),
			xyToPoint(0, minY)));
			// Стрелка оси Y
			GeneralPath arrow = new GeneralPath();
			// Установить начальную точку ломаной точно на верхний конец оси Y
			Point2D.Double lineEnd = xyToPoint(0, maxY);
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
			// Вести левый "скат" стрелки в точку с относительными координатами (5,20)
			arrow.lineTo(arrow.getCurrentPoint().getX()+5,
			arrow.getCurrentPoint().getY()+20);
			// Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
			arrow.lineTo(arrow.getCurrentPoint().getX()-10,
			arrow.getCurrentPoint().getY());
			// Замкнуть треугольник стрелки
			arrow.closePath();
			canvas.draw(arrow); // Нарисовать стрелку
			canvas.fill(arrow); // Закрасить стрелку
			// Нарисовать подпись к оси Y
			// Определить, сколько места понадобится для надписи "y"
			if(rotate) {
				Rectangle2D bounds = axisFont.getStringBounds("x", context);
				Point2D.Double labelPos = xyToPoint(0, maxY);
				// Вывести надпись в точке с вычисленными координатами
				canvas.drawString("x", (float)labelPos.getX() + 10,
				(float)(labelPos.getY() - bounds.getY()));
			}
			else {
				Rectangle2D bounds = axisFont.getStringBounds("y", context);
				Point2D.Double labelPos = xyToPoint(0, maxY);
				// Вывести надпись в точке с вычисленными координатами
				canvas.drawString("y", (float)labelPos.getX() + 10,
				(float)(labelPos.getY() - bounds.getY()));
			}
		}
		// Определить, должна ли быть видна ось X на графике
		if (minY<=0.0 && maxY>=0.0) {
			// Она должна быть видна, если верхняя граница показываемой области (maxX) >= 0.0,
			// а нижняя (minY) <= 0.0
			canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
			xyToPoint(maxX, 0)));
			// Стрелка оси X
			GeneralPath arrow = new GeneralPath();
			// Установить начальную точку ломаной точно на правый конец оси X
			Point2D.Double lineEnd = xyToPoint(maxX, 0);
			if(rotate)
				lineEnd = xyToPoint(minX, 0);
			
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
			if(rotate) {
				arrow.lineTo(arrow.getCurrentPoint().getX()+20,
						arrow.getCurrentPoint().getY()+5);
				// Вести левую часть стрелки в точку с относительными координатами (0, 10)
				arrow.lineTo(arrow.getCurrentPoint().getX(),
						arrow.getCurrentPoint().getY() - 10);
			}
			else {
				// Вести верхний "скат" стрелки в точку с относительными координатами (-20,-5)
				arrow.lineTo(arrow.getCurrentPoint().getX()-20,
						arrow.getCurrentPoint().getY()-5);
				// Вести левую часть стрелки в точку с относительными координатами (0, 10)
				arrow.lineTo(arrow.getCurrentPoint().getX(),
						arrow.getCurrentPoint().getY()+10);
			}
			
			// Замкнуть треугольник стрелки
			arrow.closePath();
			canvas.draw(arrow); // Нарисовать стрелку
			canvas.fill(arrow); // Закрасить стрелку
			// Нарисовать подпись к оси X
			// Определить, сколько места понадобится для надписи "x"
			if(rotate) {
				Rectangle2D bounds = axisFont.getStringBounds("y", context);
				Point2D.Double labelPos = xyToPoint(minX, 0);
				// Вывести надпись в точке с вычисленными координатами
				canvas.drawString("y", (float)(labelPos.getX() +
				bounds.getWidth() + 10), (float)(labelPos.getY() + bounds.getY()));
			}
			else {
				Rectangle2D bounds = axisFont.getStringBounds("x", context);
				Point2D.Double labelPos = xyToPoint(maxX, 0);
				// Вывести надпись в точке с вычисленными координатами
				canvas.drawString("x", (float)(labelPos.getX() -
				bounds.getWidth() - 10), (float)(labelPos.getY() + bounds.getY()));
			}
		}
	}
	/* Метод-помощник, осуществляющий преобразование координат.
	* Оно необходимо, т.к. верхнему левому углу холста с координатами
	* (0.0, 0.0) соответствует точка графика с координатами (minX, maxY),
	где
	* minX - это самое "левое" значение X, а
	* maxY - самое "верхнее" значение Y.
	*/
	protected Point2D.Double xyToPoint(double x, double y) {
		// Вычисляем смещение X от самой левой точки (minX)
		double deltaX = x - minX;
		// Вычисляем смещение Y от точки верхней точки (maxY)
		double deltaY = maxY - y;
		return new Point2D.Double(deltaX*scale, deltaY*scale);
	}
	/* Метод-помощник, возвращающий экземпляр класса Point2D.Double
	* смещѐнный по отношению к исходному на deltaX, deltaY
	* К сожалению, стандартного метода, выполняющего такую задачу, нет.
	*/
	protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
		// Инициализировать новый экземпляр точки
		Point2D.Double dest = new Point2D.Double();
		// Задать еѐ координаты как координаты существующей точки + заданные смещения
		dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
	return dest;
	}
	
	//проверка на удовлетворение условию
	protected boolean isSpecial(double value) {

		String strVal = formatter.formate(value);
		//цифры располагаются по порядку, так что нет необходимости конвертировать их в числа
		char[] figures = strVal.toCharArray();
		
		char prev = figures[0];
		for(int i = 1; i < figures.length; ++i) {
			if(figures[i] == '.')
				continue;
			
			if(figures[i] < prev)
				return false;
			
			prev = figures[i];
		}
		
		return true;
	}
	
	//площадь методом трапеций
	protected double calcSquare(Double[][] points, int from, int to) {
		if(to - from < 1)
			return 0;
		
		double answer = 0;
		for(int i = from; i <= to; ++i) {
			answer += (points[i][1] + points[i - 1][1]) / 2 * (points[i][0] - points[i - 1][0]);
		}
		return Math.abs(answer);
	}

	protected void fillClosedArea(Graphics2D canvas, int from, int to) {
		canvas.setStroke(graphicsStroke);
		// Выбрать цвет линии
		canvas.setColor(Color.GRAY);
		/* Будем рисовать линию графика как путь, состоящий из множества
		сегментов (GeneralPath)*/
		GeneralPath area = new GeneralPath();
		if(!rotate)
			area.moveTo(xyToPoint(graphicsData[from][0], graphicsData[from][1]).getX(), xyToPoint(graphicsData[from][0], 0).getY());
		else
			area.moveTo(xyToPoint(graphicsData[from][0], graphicsData[from][1]).getX(), xyToPoint(graphicsData[from][0], graphicsData[from][1]).getY());
		for (int i = from; i < to; i++) {
		// Преобразовать значения (x,y) в точку на экране point
			Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
			area.lineTo(point.getX(), point.getY());
		}
		if(!rotate)
			area.lineTo(xyToPoint(graphicsData[to][0], graphicsData[to][1]).getX(), xyToPoint(graphicsData[to][0], 0).getY());
		
		
		area.closePath();
		canvas.fill(area);
		// Отобразить 
		canvas.draw(area);
	}
}


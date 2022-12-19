package bsu.rfe.java.group10.lab4.Slavinsky.varC;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import javax.swing.JPanel;


@SuppressWarnings("serial")

public class GraphicsDisplay extends JPanel implements CustomMouseAdapter{
	// Список координат точек для построения графика
	private Double[][] graphicsData;
	private Double[][] originalGraphicsData;
	// Флаговые переменные, задающие правила отображения графика
	private boolean showAxis = true;
	private boolean showMarkers = true;
	private boolean showSpecialMarkers = true;
	private boolean showClosedAreas = false;
	private boolean rotate = false;
	// Границы диапазона пространства, подлежащего отображению
	private Borders border;
	private LinkedList<Borders> borderValues = new LinkedList<Borders>();
	// Используемый масштаб отображения
	private double scale;
	// Различные стили черчения линий
	private BasicStroke graphicsStroke;
	private BasicStroke scalingStroke;
	private BasicStroke axisStroke;
	private BasicStroke markerStroke;
	
	private GornerTableCellRenderer formatter = new GornerTableCellRenderer();
	
	// Различные шрифты отображения надписей
	private Font axisFont;
	private Font coordFont;
		
	//
	Double[] interedPoint = null;
	
	//переменные для мыши
	private int mouseX = 0;
	private int mouseY = 0;
	private int previousMouseX = 0;
	private int previousMouseY = 0;
	private boolean isScaling = false;
	
  	public GraphicsDisplay() {
  		border = new Borders();
  		borderValues.add(border);
  		
		// Цвет заднего фона области отображения - белый
		setBackground(Color.WHITE);
		// Сконструировать необходимые объекты, используемые в рисовании
		// Перо для рисования графика
		graphicsStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_ROUND, 10.0f, new float[] {8, 2, 2, 2, 2, 2, 4, 2, 4}, 0.0f);
		//перо для рисования области увеличения
		scalingStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_ROUND, 10.0f, new float[] {10, 10}, 0.0f);
		// Перо для рисования осей координат
		axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
		// Перо для рисования контуров маркеров
		markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
		// Шрифт для подписей осей координат
		axisFont = new Font("Serif", Font.BOLD, 36);
		coordFont = new Font("Serif", Font.BOLD, 14);
		//добавим возможность перехватывания событий мыши
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
	}
	// Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
	// главного окна приложения в случае успешной загрузки данных
	public void showGraphics(Double[][] graphicsData) {
		// Сохранить массив точек во внутреннем поле класса
		this.originalGraphicsData = graphicsData;
		this.graphicsData = new Double[graphicsData.length][2];

		for(int i = 0; i < graphicsData.length; ++i) {
			this.graphicsData[i][1] = graphicsData[i][1];
			this.graphicsData[i][0] = graphicsData[i][0];
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
		repaint();
	}
	
	public void restoreData() {
		this.graphicsData = new Double[graphicsData.length][2];
		
		for(int i = 0; i < graphicsData.length; ++i) {
			graphicsData[i][1] = originalGraphicsData[i][1];
			graphicsData[i][0] = originalGraphicsData[i][0];
		}
		// Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
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
			if (graphicsData == null || graphicsData.length == 0) return;
			
			//borderValues.size() == 1 отсуствует масштабирование
			if(borderValues.size() == 1) {
				// Определить минимальное и максимальное значения для координат X и Y
				// Это необходимо для определения области пространства, подлежащей отображению
				// Еѐ верхний левый угол это (minX, maxY) - правый нижний это (maxX, minY)
				border.minX = graphicsData[0][0];
				border.maxX = graphicsData[graphicsData.length-1][0];
				border.minY = graphicsData[0][1];
				border.maxY = border.minY;
				// Найти минимальное и максимальное значение функции
				for (int i = 1; i < graphicsData.length; i++) {
					if (graphicsData[i][1] < border.minY) {
						border.minY = originalGraphicsData[i][1];
					}
					if (graphicsData[i][1] > border.maxY) {
						border.maxY = graphicsData[i][1];
					}
				}
				
				
				if(rotate) {
					double tempMinX = border.minX;
					double tempMaxX = border.maxX;
					border.minX = -1 * border.maxY;
					border.maxX = -1 * border.minY;
					border.minY = tempMinX;
					border.maxY = tempMaxX;
				}
			}
		/* Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X
		и Y - сколько пикселов
		* приходится на единицу длины по X и по Y
		*/
		
		double scaleX = getSize().getWidth() / (border.maxX - border.minX);
		double scaleY = getSize().getHeight() / (border.maxY - border.minY);
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
			double yIncrement = (height/scale - (border.maxY - border.minY))/2;
			border.maxY += yIncrement;
			border.minY -= yIncrement;
		}
		if (scale == scaleY) {
			// Если за основу был взят масштаб по оси Y, действовать по аналогии
			double width = getSize().getWidth();
			double xIncrement = (width/scale - (border.maxX - border.minX))/2;
			border.maxX += xIncrement;
			border.minX -= xIncrement;
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
		if (showMarkers) {
			paintMarkers(canvas);
		}
		
		if(showClosedAreas)
			paintClosedAreas(canvas);
		
		if (isScaling) {
			canvas.setStroke(scalingStroke);
			canvas.drawRect(Math.min(previousMouseX, mouseX), Math.min(previousMouseY, mouseY), 
					Math.abs(mouseX - previousMouseX), Math.abs(mouseY - previousMouseY));
		}
			// Шаг 9 - Восстановить старые настройки холста
			canvas.setFont(oldFont);
			canvas.setPaint(oldPaint);
			canvas.setColor(oldColor);
			canvas.setStroke(oldStroke);
	}
		// Отрисовка графика по прочитанным координатам
	protected void paintGraphics(Graphics2D canvas) {
		
		Stroke oldStroke = canvas.getStroke();
		Color oldColor = canvas.getColor();
		Paint oldPaint = canvas.getPaint();
		Font oldFont = canvas.getFont();
		
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
			Point2D.Double point;
			if(!rotate)
				point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
			else {
				point = xyToPoint(-1 * graphicsData[i][1], graphicsData[i][0]);
			}
			
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
		
		canvas.setFont(oldFont);
		canvas.setPaint(oldPaint);
		canvas.setColor(oldColor);
		canvas.setStroke(oldStroke);
	}
	// Отображение маркеров точек, по которым рисовался график
	protected void paintMarkers(Graphics2D canvas) {
		Stroke oldStroke = canvas.getStroke();
		Color oldColor = canvas.getColor();
		Paint oldPaint = canvas.getPaint();
		// Шаг 1 - Установить специальное перо для черчения контуров маркеров
		canvas.setStroke(markerStroke);
		
		//Точка с координатами не выбрана
		interedPoint = null;
		
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
			Point2D.Double center;
			if(!rotate)
				center = xyToPoint(point[0], point[1]);
			else {
				center = xyToPoint(-1 * point[1], point[0]);
			}
					
			canvas.drawLine((int)center.x + 5, (int)center.y, (int)center.x - 5, (int)center.y);
			canvas.drawLine((int)center.x, (int)center.y + 5, (int)center.x, (int)center.y - 5);
			canvas.drawLine((int)center.x + 5, (int)center.y + 5, (int)center.x - 5, (int)center.y - 5);
			canvas.drawLine((int)center.x - 5, (int)center.y + 5, (int)center.x + 5, (int)center.y - 5);
			
			if(Math.abs((int)center.x - mouseX) <= 5 && Math.abs((int)center.y - mouseY) <= 5)
				interedPoint = point;
		}
		
		if(interedPoint != null) {
			String coordText = "X= " + formatter.formate(interedPoint[0]) + ", Y= " + formatter.formate(interedPoint[1]);
			canvas.setPaint(Color.BLACK);
			canvas.setFont(coordFont);
			Rectangle2D bounds = coordFont.getStringBounds(coordText, canvas.getFontRenderContext());
			canvas.drawString(coordText, mouseX, (int) (mouseY - 3 - bounds.getHeight()));
		}
		
		canvas.setColor(oldColor);
		canvas.setPaint(oldPaint);
		canvas.setStroke(oldStroke);
	} 
	
	protected void paintClosedAreas(Graphics2D canvas) {
			FontRenderContext context = canvas.getFontRenderContext();
			
			Stroke oldStroke = canvas.getStroke();
			Color oldColor = canvas.getColor();
			Paint oldPaint = canvas.getPaint();
			
			int from = 0;
			int to = 0;
			double fromSign;
			
			var data = this.graphicsData;
			
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
							canvas.setFont(axisFont);
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
			
			canvas.setColor(oldColor);
			canvas.setPaint(oldPaint);
			canvas.setStroke(oldStroke);	
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
		if (border.minX<=0.0 && border.maxX>=0.0) {
			// Она должна быть видна, если левая граница показываемой области (minX) <= 0.0,
			// а правая (maxX) >= 0.0
			// Сама ось - это линия между точками (0, maxY) и (0, minY)
			canvas.draw(new Line2D.Double(xyToPoint(0, border.maxY),
			xyToPoint(0, border.minY)));
			// Стрелка оси Y
			GeneralPath arrow = new GeneralPath();
			// Установить начальную точку ломаной точно на верхний конец оси Y
			Point2D.Double lineEnd = xyToPoint(0, border.maxY);
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
				Point2D.Double labelPos = xyToPoint(0, border.maxY);
				// Вывести надпись в точке с вычисленными координатами
				canvas.drawString("x", (float)labelPos.getX() + 10,
				(float)(labelPos.getY() - bounds.getY()));
			}
			else {
				Rectangle2D bounds = axisFont.getStringBounds("y", context);
				Point2D.Double labelPos = xyToPoint(0, border.maxY);
				// Вывести надпись в точке с вычисленными координатами
				canvas.drawString("y", (float)labelPos.getX() + 10,
				(float)(labelPos.getY() - bounds.getY()));
			}
		}
		// Определить, должна ли быть видна ось X на графике
		if (border.minY<=0.0 && border.maxY>=0.0) {
			// Она должна быть видна, если верхняя граница показываемой области (maxX) >= 0.0,
			// а нижняя (minY) <= 0.0
			canvas.draw(new Line2D.Double(xyToPoint(border.minX, 0),
			xyToPoint(border.maxX, 0)));
			// Стрелка оси X
			GeneralPath arrow = new GeneralPath();
			// Установить начальную точку ломаной точно на правый конец оси X
			Point2D.Double lineEnd = xyToPoint(border.maxX, 0);
			if(rotate)
				lineEnd = xyToPoint(border.minX, 0);
			
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
				Point2D.Double labelPos = xyToPoint(border.minX, 0);
				// Вывести надпись в точке с вычисленными координатами
				canvas.drawString("y", (float)(labelPos.getX() +
				bounds.getWidth() + 10), (float)(labelPos.getY() + bounds.getY()));
			}
			else {
				Rectangle2D bounds = axisFont.getStringBounds("x", context);
				Point2D.Double labelPos = xyToPoint(border.maxX, 0);
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
		double deltaX = x - border.minX;
		// Вычисляем смещение Y от точки верхней точки (maxY)
		double deltaY = border.maxY - y;
		return new Point2D.Double(deltaX * scale, deltaY * scale);
	}
	
	protected double[] pointToXY(Point2D.Double point) {
		double[] answer = new double[2];
		answer[0] = point.x / scale + border.minX;
		answer[1] = border.maxY - point.y / scale;
		return answer;
	}
	
	//конвертация перемещения мыши в изменение для графика
 	protected double screnDeltaToGraph(int screenDelta) {
		return (double)screenDelta / scale;
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
	
	public Double[][] getCurrentData(){
		return graphicsData.clone();
	}
	
	private void scaleGraph() {
		double[] leftHigh = pointToXY(new Point2D.Double(previousMouseX, previousMouseY));
		double[] rightLow = pointToXY(new Point2D.Double(mouseX, mouseY));
		
		if(rightLow[0] < leftHigh[0]) {
			double temp = rightLow[0];
			rightLow[0] = leftHigh[0];
			leftHigh[0] = temp;
		}
		
		if(rightLow[1] > leftHigh[1]) {
			double temp = rightLow[1];
			rightLow[1] = leftHigh[1];
			leftHigh[1] = temp;
		}
		
		borderValues.add(new Borders());
		if(leftHigh[0] > border.minX)
			borderValues.getLast().minX = leftHigh[0];
		if(leftHigh[1] < border.maxY)
			borderValues.getLast().maxY = leftHigh[1];
		if(rightLow[0] < border.maxX)
			borderValues.getLast().maxX = rightLow[0];
		if(rightLow[1] > border.minY)
		borderValues.getLast().minY = rightLow[1];
		border = borderValues.getLast();
		
		repaint();
	}
	
	private void cancelScalling() {
		if(borderValues.size() > 1) {
			borderValues.removeLast();
			border = borderValues.getLast();
		}
		repaint();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {

		if(interedPoint != null && !isScaling) {
			previousMouseX = mouseX;
			previousMouseY = mouseY;
			double deltaY = 0;
			if(!rotate) {
				mouseY = e.getY();
				deltaY = screnDeltaToGraph(mouseY - previousMouseY);
			}
			else {
				mouseX = e.getX();
				deltaY = screnDeltaToGraph(mouseX - previousMouseX);
			}
			interedPoint[1] -= deltaY;
		}
		else if(isScaling){
			mouseX = e.getX();
			mouseY = e.getY();
		}
		 
		
		repaint();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		previousMouseX = mouseX;
		previousMouseY = mouseY;
		mouseX = e.getX();
		mouseY = e.getY();
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		if(e.getButton() == 1 && interedPoint == null)
			isScaling = true;
	}

	public void mouseClicked(MouseEvent e) {
		 if(e.getButton() == 3)
				cancelScalling();
	}
	
	public void mouseReleased(MouseEvent e) {
			if(isScaling)
				scaleGraph();
			isScaling = false;
	}
}
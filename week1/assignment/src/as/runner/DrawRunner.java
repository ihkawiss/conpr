package as.runner;

import as.CancelSupport;
import as.Mandelbrot;
import as.PixelPainter;
import as.Plane;
import javafx.application.Platform;
import javafx.beans.value.WritableStringValue;

public class DrawRunner implements Runnable {

	private final PixelPainter painter;
	private final Plane plane;
	private final CancelSupport cancelSupport;
	private final WritableStringValue millis;

	public DrawRunner(PixelPainter painter, Plane plane, CancelSupport cancelSupport, WritableStringValue millis) {
		this.painter = painter;
		this.plane = plane;
		this.cancelSupport = cancelSupport;
		this.millis = millis;
	}

	@Override
	public void run() {
		double start = System.currentTimeMillis();
		Mandelbrot.computeSequential(painter, plane, cancelSupport);
		double end = System.currentTimeMillis();
		Platform.runLater(() -> {
			millis.set((end - start) + "ms");
			System.out.println(millis.toString());
		});
	}

}
